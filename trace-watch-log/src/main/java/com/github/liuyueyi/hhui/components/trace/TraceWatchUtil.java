package com.github.liuyueyi.hhui.components.trace;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.github.liuyueyi.hhui.components.trace.mdc.MdcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * 执行链路观察工具类
 *
 * @author YiHui
 * @date 2024/8/11
 */
public class TraceWatchUtil {
    private static final Logger log = LoggerFactory.getLogger(TraceWatchUtil.class);

    private static final TransmittableThreadLocal<TraceBridge> THREAD_LOCAL = new TransmittableThreadLocal<>();

    public static class TraceBridge implements Closeable {
        /**
         * trace记录名
         */
        private final String traceName;

        /**
         * 异步任务执行的结果
         */
        private List<CompletableFuture> list;
        /**
         * 一个子任务的执行耗时
         */
        private Map<String, Long> cost;

        /**
         * 异步调度的线程池
         */
        private final ExecutorService executorService;

        /**
         * 用于标记是否所有的任务执行完毕
         * 执行完毕之后，不在支持继续添加记录
         */
        private volatile boolean markExecuteOver;


        public TraceBridge() {
            this(AsyncUtil.executorService, "TraceBridge");
        }

        public TraceBridge(ExecutorService executorService, String task) {
            this.traceName = task;
            list = new CopyOnWriteArrayList<>();
            // 支持排序的耗时记录
            cost = new ConcurrentSkipListMap<>();
            startRecord(task);
            this.executorService = TtlExecutors.getTtlExecutorService(executorService);
            this.markExecuteOver = false;
        }

        /**
         * 异步执行，带返回结果
         *
         * @param supplier 执行任务
         * @param name     耗时标识
         * @return
         */
        public <T> Future<T> async(Supplier<T> supplier, String name) {
            CompletableFuture<T> ans = CompletableFuture.supplyAsync(supplyWithTime(supplier, name + "(异步)", MdcUtil.getOrInitGlobalMsgId()), this.executorService);
            list.add(ans);
            return ans;
        }

        /**
         * 同步执行，待返回结果
         *
         * @param supplier 执行任务
         * @param name     耗时标识
         * @param <T>      返回类型
         * @return 任务的执行返回结果
         */
        public <T> T sync(Supplier<T> supplier, String name) {
            return supplyWithTime(supplier, name, MdcUtil.getOrInitGlobalMsgId()).get();
        }

        /**
         * 异步执行，无返回结果
         *
         * @param run  执行任务
         * @param name 耗时标识
         * @return
         */
        public CompletableFuture<Void> async(Runnable run, String name) {
            // 添加一个标识，区分同步执行与异步执行
            // 异步任务的执行，在整体的耗时占比只能作为参考
            CompletableFuture<Void> future = CompletableFuture.runAsync(runWithTime(run, name + "(异步)", MdcUtil.getOrInitGlobalMsgId()), this.executorService);
            list.add(future);
            return future;
        }

        /**
         * 同步执行，无返回结果
         *
         * @param run  执行任务
         * @param name 耗时标识
         * @return
         */
        public void sync(Runnable run, String name) {
            runWithTime(run, name, MdcUtil.getOrInitGlobalMsgId()).run();
        }

        /**
         * 封装一下执行业务逻辑，记录耗时时间
         *
         * @param run  执行的具体业务逻辑
         * @param name 任务名
         * @return
         */
        private Runnable runWithTime(Runnable run, String name, String msgId) {
            return () -> {
                // 将父线程的msgId设置到当前这个执行线程
                MdcUtil.setGlobalMsgId(msgId);
                startRecord(name);
                try {
                    run.run();
                } finally {
                    endRecord(name);
                }
            };
        }

        /**
         * 封装一下执行业务逻辑，记录耗时时间
         *
         * @param call 执行的具体业务逻辑
         * @param name 任务名
         * @return 返回结果
         */
        private <T> Supplier<T> supplyWithTime(Supplier<T> call, String name, String msgId) {
            return () -> {
                // 将父线程的msgId设置到当前这个执行线程
                MdcUtil.setGlobalMsgId(msgId);
                startRecord(name);
                try {
                    return call.get();
                } finally {
                    endRecord(name);
                }
            };
        }

        /**
         * 等待所有的任务执行完毕
         *
         * @return
         */
        public TraceBridge allExecuted() {
            if (!list.isEmpty()) {
                CompletableFuture.allOf(list.toArray(new CompletableFuture[]{})).join();
            }
            // 记录整体结束
            endRecord(this.traceName);
            this.markExecuteOver = true;
            return this;
        }

        private void startRecord(String name) {
            cost.put(name, System.currentTimeMillis());
        }

        private void endRecord(String name) {
            long now = System.currentTimeMillis();
            long last = cost.getOrDefault(name, now);
            if (last >= now / 1000) {
                // 之前存储的是时间戳，因此我们需要更新成执行耗时 ms单位
                cost.put(name, now - last);
            }
        }

        public void prettyPrint() {
            // 在格式化输出时，要求所有任务执行完毕
            if (!this.markExecuteOver) {
                this.allExecuted();
            }

            StringBuilder sb = new StringBuilder();
            sb.append('\n');
            long totalCost = cost.remove(traceName);
            sb.append("TraceWatch '").append(traceName).append("': running time = ").append(totalCost).append(" ms");
            sb.append('\n');
            if (cost.size() <= 1) {
                sb.append("No task info kept");
            } else {
                sb.append("---------------------------------------------\n");
                sb.append("ms         %     Task name\n");
                sb.append("---------------------------------------------\n");
                NumberFormat pf = NumberFormat.getPercentInstance();
                pf.setMinimumIntegerDigits(2);
                pf.setMinimumFractionDigits(2);
                pf.setGroupingUsed(false);
                for (Map.Entry<String, Long> entry : cost.entrySet()) {
                    sb.append(entry.getValue()).append("\t\t");
                    sb.append(pf.format(entry.getValue() / (double) totalCost)).append("\t\t");
                    sb.append(entry.getKey()).append("\n");
                }
            }

            log.info("\n---------------------\n{}\n--------------------\n", sb);
        }

        @Override
        public void close() {
            try {
                // 做一个兜底，避免业务侧没有手动结束，导致异步任务没有执行完就提前返回结果
                this.allExecuted().prettyPrint();
            } catch (Exception e) {
                log.error("释放耗时上下文异常! {}", traceName, e);
            } finally {
                endTrace();
            }
        }
    }

    public static TraceBridge startTrace(String name) {
        return startTrace(AsyncUtil.executorService, name);
    }

    /**
     * 开始trace记录
     *
     * @param executorService 线程池
     * @param name            任务名
     * @return
     */
    public static TraceBridge startTrace(ExecutorService executorService, String name) {
        TraceBridge bridge = new TraceBridge(executorService, name);
        THREAD_LOCAL.set(bridge);
        return bridge;
    }

    public static TraceBridge getBridge() {
        return THREAD_LOCAL.get();
    }

    public static void endTrace() {
        THREAD_LOCAL.remove();
    }
}

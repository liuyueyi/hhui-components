package com.github.liuyueyi.hhui.trace.test.step;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;

import java.io.Closeable;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author YiHui
 * @date 2024/8/22
 */
public class Step4 {
    public static class TraceWatch implements Closeable {
        private static final Logger log = LoggerFactory.getLogger(TraceWatch.class);
        /**
         * 任务名
         */
        private String taskName;
        /**
         * 子任务耗时时间
         */
        private Map<String, Long> taskCost;

        /**
         * 用于标记是否所有的任务执行完毕
         * 执行完毕之后，不在支持继续添加记录
         */
        private volatile boolean markExecuteOver;

        public TraceWatch(String taskName) {
            this.taskName = taskName;
            this.taskCost = new ConcurrentHashMap<>();
            markExecuteOver = false;
            start(taskName);
        }

        public TraceWatch() {
            this("");
        }

        public void cost(Runnable run, String task) {
            try {
                start(task);
                run.run();
            } finally {
                stop(task);
            }
        }

        public <T> T cost(Supplier<T> sup, String task) {
            try {
                start(task);
                return sup.get();
            } finally {
                stop(task);
            }
        }

        public void start(String task) {
            if (markExecuteOver) {
                System.out.println("所有耗时记录已结束，忽略 " + task);
                return;
            }
            taskCost.put(task, System.currentTimeMillis());
        }

        public void stop(String task) {
            Long last = taskCost.get(task);
            if (last == null || last < 946656000L) {
                // last = null -> 兼容没有使用开始，直接调用了结束的场景
                // last 存的是耗时而非时间戳 -> 兼容重复调用stop的场景
                return;
            }
            taskCost.put(task, System.currentTimeMillis() - last);
        }

        public void prettyPrint() {
            if (markExecuteOver) {
                // 未执行完毕，则等待所有的任务执行完毕
                stop(this.taskName);
                allExecuted();
            }

            StringBuilder sb = new StringBuilder();
            sb.append('\n');
            long totalCost = taskCost.remove(this.taskName);
            sb.append("TraceWatch '").append(taskName).append("': running time = ").append(totalCost).append(" ms");
            sb.append('\n');
            if (taskCost.isEmpty()) {
                sb.append("No task info kept");
            } else {
                sb.append("---------------------------------------------\n");
                sb.append("ms         %     Task name\n");
                sb.append("---------------------------------------------\n");
                NumberFormat pf = NumberFormat.getPercentInstance();
                pf.setMinimumIntegerDigits(2);
                pf.setMinimumFractionDigits(2);
                pf.setGroupingUsed(false);
                for (Map.Entry<String, Long> entry : taskCost.entrySet()) {
                    sb.append(entry.getValue()).append("\t\t");
                    sb.append(pf.format(entry.getValue() / (double) totalCost)).append("\t\t");
                    sb.append(entry.getKey()).append("\n");
                }
            }

            if (LoggerFactory.getILoggerFactory() instanceof NOPLoggerFactory) {
                // 若项目中没有Slfj4的实现，则直接使用标准输出
                System.out.printf("\n---------------------\n%s\n--------------------\n%n", sb);
            } else if (log.isInfoEnabled()) {
                log.info("\n---------------------\n{}\n--------------------\n", sb);
            }
        }

        /**
         * 等待所有的任务执行完毕
         */
        public void allExecuted() {
            while (true) {
                boolean hasTaskRun = false;
                for (Long val : taskCost.values()) {
                    if (val > 946656000L) {
                        // 表示还有任务没有执行完毕，自旋等一会
                        hasTaskRun = true;
                        break;
                    }
                }

                if (hasTaskRun) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    break;
                }
            }
            markExecuteOver = true;
        }

        @Override
        public void close() {
            stop(this.taskName);
            // 等待所有任务执行完毕
            allExecuted();
            prettyPrint();
        }
    }

    private static Random random = new Random();

    /**
     * 随机睡眠一段时间
     *
     * @param max
     */
    private static void randSleep(String task, int max) {
        int sleepMillSecond = random.nextInt(max);
        try {
            System.out.println(task + "==> 随机休眠 " + sleepMillSecond + "ms");
            Thread.sleep(sleepMillSecond);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCost() throws InterruptedException {
        TraceWatch traceWatch = new TraceWatch();

        traceWatch.start("task1");
        randSleep("task1", 200);
        traceWatch.stop("task1");

        new Thread(() -> {
            traceWatch.start("task2");
            randSleep("task2", 100);
            traceWatch.stop("task2");
        }).start();

        traceWatch.start("task3");
        randSleep("task3", 40);
        traceWatch.stop("task3");


        new Thread(() -> {
            traceWatch.start("task4");
            randSleep("task4", 100);
            traceWatch.stop("task4");
        }).start();

        Thread.sleep(100);
        traceWatch.prettyPrint();
    }


    @Test
    public void testCost2() {
        try (TraceWatch traceWatch = new TraceWatch()) {
            traceWatch.cost(() -> randSleep("task1", 200), "task1");

            new Thread(() -> {
                traceWatch.cost(() -> randSleep("task2", 100), "task2");
            }).start();

            traceWatch.cost(() -> randSleep("task3", 40), "task3");

            new Thread(() -> {
                String ans = traceWatch.cost(() -> {
                    randSleep("task4", 100);
                    return "ok";
                }, "task4");
                System.out.println("task4 返回" + ans);
            }).start();
        }
    }

    @Test
    public void testCost3() {
        long start = System.currentTimeMillis();
        try (TraceWatch traceWatch = new TraceWatch()) {
            randSleep("前置",20);
            traceWatch.cost(() -> randSleep("task1", 200), "task1");

            new Thread(() -> {
                traceWatch.cost(() -> randSleep("task2", 100), "task2");
            }).start();

            traceWatch.cost(() -> randSleep("task3", 40), "task3");

            new Thread(() -> {
                String ans = traceWatch.cost(() -> {
                    randSleep("task4", 100);
                    return "ok";
                }, "task4");
                System.out.println("task4 返回" + ans);
            }).start();
        }
        long end = System.currentTimeMillis();
        System.out.println("整体耗时 = " + (end - start));
    }

    @Test
    public void testCost4() {
        for (int i = 0; i < 10; i++) {
            testCost3();
        }
    }
}

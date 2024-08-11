package com.github.liuyueyi.hhui.components.trace;

import com.alibaba.ttl.threadpool.TtlExecutors;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步工具类
 *
 * @author YiHui
 * @date 2024/8/11
 */
public class AsyncUtil {
    static ExecutorService executorService;

    static {
        initExecutorService(Runtime.getRuntime().availableProcessors() * 2, 50);
    }

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            Thread thread = this.defaultFactory.newThread(r);
            if (!thread.isDaemon()) {
                thread.setDaemon(true);
            }

            thread.setName("trace-watch-dog-" + this.threadNumber.getAndIncrement());
            return thread;
        }
    };

    public static void initExecutorService(int core, int max) {
        // 异步工具类的默认线程池构建
        max = Math.max(core, max);
        executorService = new ThreadPoolExecutor(core, max, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), THREAD_FACTORY, new ThreadPoolExecutor.CallerRunsPolicy());
        // 包装一下线程池，避免出现上下文复用场景
        executorService = TtlExecutors.getTtlExecutorService(executorService);
    }

}

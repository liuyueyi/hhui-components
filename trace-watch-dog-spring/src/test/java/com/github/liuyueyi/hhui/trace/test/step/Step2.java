package com.github.liuyueyi.hhui.trace.test.step;

import org.junit.Test;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.util.Random;
import java.util.function.Supplier;

/**
 * 第一阶段的使用工具类
 *
 * @author YiHui
 * @date 2024/8/21
 */
public class Step2 {
    private static Random random = new Random();

    /**
     * 随机睡眠一段时间
     *
     * @param max
     */
    private static void randSleep(int max) {
        int sleepMillSecond = random.nextInt(max);
        try {
            System.out.println("随机休眠 " + sleepMillSecond + "ms");
            Thread.sleep(sleepMillSecond);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCost() {
        StopWatch stopWatch = new StopWatch("测试耗时");
        // 注意这里的第一个randSleep的耗时，不会被记录再StopWatch中
        randSleep(300);
        stopWatch.start("task1");
        randSleep(100);
        stopWatch.stop();

        stopWatch.start("task2");
        randSleep(30);
        stopWatch.stop();

        stopWatch.start("task3");
        randSleep(50);
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());
    }

    @Test
    public void testCost2() {
        try (StopWatchWrapper wrapper = StopWatchWrapper.instance("耗时统计")) {
            randSleep(300);

            wrapper.cost(() -> randSleep(100), "task1");

            String ans = wrapper.cost(() -> {
                randSleep(30);
                return "ok";
            }, "task2");
            System.out.println("task2 返回:" + ans);

            wrapper.cost(() -> randSleep(50), "task3");

            randSleep(300);
        }
    }

    @Test
    public void testCost3() {
        StopWatch stopWatch = new StopWatch("测试耗时");
        // 注意这里的第一个randSleep的耗时，不会被记录再StopWatch中
        randSleep(300);

        new Thread(() -> {
            stopWatch.start("task1");
            randSleep(100);
            stopWatch.stop();
        }).start();


        new Thread(() -> {
            stopWatch.start("task2");
            randSleep(30);
            stopWatch.stop();
        }).start();


        stopWatch.start("task3");
        randSleep(50);
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());
    }

    public static class StopWatchWrapper implements Closeable {
        private StopWatch stopWatch;

        public static StopWatchWrapper instance(String task) {
            StopWatchWrapper wrapper = new StopWatchWrapper();
            wrapper.stopWatch = new StopWatch(task);
            return wrapper;
        }

        public void cost(Runnable run, String task) {
            stopWatch.start(task);
            try {
                run.run();
            } finally {
                stopWatch.stop();
            }
        }

        public <T> T cost(Supplier<T> sup, String task) {
            stopWatch.start(task);
            try {
                return sup.get();
            } finally {
                stopWatch.stop();
            }
        }

        @Override
        public void close() {
            System.out.println(stopWatch.prettyPrint());
        }
    }
}

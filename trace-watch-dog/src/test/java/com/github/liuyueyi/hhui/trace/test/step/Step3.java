package com.github.liuyueyi.hhui.trace.test.step;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YiHui
 * @date 2024/8/22
 */
public class Step3 {
    public static class TraceWatch {
        private static final Logger log = LoggerFactory.getLogger(TraceWatch.class);
        /**
         * 任务名
         */
        private String taskName;
        /**
         * 子任务耗时时间
         */
        private Map<String, Long> taskCost;

        public TraceWatch(String taskName) {
            this.taskName = taskName;
            this.taskCost = new ConcurrentHashMap<>();
        }

        public TraceWatch() {
            this("");
        }

        public void start(String task) {
            taskCost.put(task, System.currentTimeMillis());
        }

        public void stop(String task) {
            Long last = taskCost.get(task);
            if (last == null) {
                // 兼容没有使用开始，直接调用了结束的场景
                return;
            }
            taskCost.put(task, System.currentTimeMillis() - last);
        }

        public void prettyPrint() {
            StringBuilder sb = new StringBuilder();
            sb.append('\n');
            long totalCost = taskCost.values().stream().reduce(0L, Long::sum);
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

}

package com.github.liuyueyi.hhui.trace.test;

import com.github.liuyueyi.hhui.components.trace.TraceWatch;
import com.github.liuyueyi.hhui.components.trace.mdc.MdcUtil;
import com.github.liuyueyi.hhui.components.trace.output.CostOutput;
import com.github.liuyueyi.hhui.components.trace.recoder.ITraceRecoder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * 自定义输出重定向使用案例
 *
 * @author YiHui
 * @date 2024/8/12
 */
public class SelfOutputDemo {
    private static final Logger log = LoggerFactory.getLogger(SelfOutputDemo.class);
    private static Random random = new Random();

    /**
     * 随机睡眠一段时间
     *
     * @param max
     */
    private static void randSleep(int max) {
        int sleepMillSecond = random.nextInt(max);
        try {
            Thread.sleep(sleepMillSecond);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String fun1WithReturn() {
        long start = System.currentTimeMillis();
        randSleep(50);
        log.info("fun1WithReturn执行完毕 -> " + (System.currentTimeMillis() - start));
        return "fun1";
    }

    private void fun2NoReturn(String txt) {
        long start = System.currentTimeMillis();
        randSleep(50);
        log.info("fun2 -->" + txt + " --> " + (System.currentTimeMillis() - start));
    }


    /**
     * 异步执行，不关心返回结果
     *
     * @param ans
     */
    private void runAsyncNoReturn(String ans) {
        long start = System.currentTimeMillis();
        randSleep(15);
        log.info("runAsyncNoReturn ->" + ans + " --> " + (System.currentTimeMillis() - start));
    }

    private String runAsyncWithReturn(String ans) {
        long start = System.currentTimeMillis();
        randSleep(15);
        log.info("runAsyncWithReturn ->" + ans + " --> " + (System.currentTimeMillis() - start));
        return ans + "_over";
    }

    private String runAsyncWithReturn2(String ans) {
        long start = System.currentTimeMillis();
        randSleep(25);
        log.info("runAsyncWithReturn2 ->" + ans + " --> " + (System.currentTimeMillis() - start));
        return ans + "_over2";
    }

    /**
     * 预热逻辑
     */
    private void preLoad() {
        String uuid = MdcUtil.newGlobalTraceId();
        System.out.println("uuid -> " + uuid);
        // 这里的执行，主要是为了解决 TraceWatch等相关类的初始化耗时对整体结果的影响
        try (ITraceRecoder recoder = TraceWatch.startTrace("预热", false)) {
            recoder.async(() -> {
                randSleep(30);
            }, "preLoad0");
            recoder.sync(() -> {
                randSleep(20);
            }, "preLoad1");
        } finally {
            log.info("========================= 预热 ========================= \n\n");
        }
    }


    /**
     * 根据实际的场景，对一些逻辑调整为异步执行
     */
    @Test
    public void testWithAsyncTrace() {
        //  注册自定义的输出重定向
        TraceWatch.registerOutput(new CostOutput() {
            @Override
            public void output(Map<String, Long> cost, String traceName) {
                Long totalCost = cost.get(traceName);
                log.info("总耗时:{} 明细：{}", totalCost, cost);
            }
        });

        preLoad();
        MdcUtil.initTraceIdAutoGen(true);
        long start = System.currentTimeMillis();
        // 通过设置创建 TraceWatch 时，制定false，关闭默认的日志输出
        try (ITraceRecoder recoder = TraceWatch.startTrace("traceLog", false)) {
            String ans = recoder.sync(this::fun1WithReturn, "1.fun1WithReturn");
            recoder.sync(() -> fun2NoReturn(ans), "2.fun2NoReturn");
            recoder.async(() -> runAsyncNoReturn(ans), "3.runAsyncNoReturn");
            log.info("异步结果阻塞获取前耗时：" + (System.currentTimeMillis() - start));

            // 下面两个演示的是异步返回的场景，在合适的地方，将异步返回结果进行拼接
            CompletableFuture<String> a2 = recoder.async(() -> runAsyncWithReturn(ans), "4.runAsyncWithReturn");
            CompletableFuture<String> a3 = recoder.async(() -> runAsyncWithReturn2(ans), "5.runAsyncWithReturn");
            CompletableFuture.allOf(a2, a3).whenComplete((unused, throwable) -> log.info("最终的结果是: -> " + (a2.join() + a3.join())));
        }
        long end = System.currentTimeMillis();
        log.info("总耗时： " + (end - start));
    }
}
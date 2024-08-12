package com.github.liuyueyi.hhui.trace.test;

import com.github.liuyueyi.hhui.components.trace.TraceWatch;
import com.github.liuyueyi.hhui.components.trace.recoder.DefaultTraceRecoder;
import org.junit.Test;

import java.net.ServerSocket;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * 基本使用示例
 *
 * @author YiHui
 * @date 2024/8/12
 */
public class BasicDemo {
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
        System.out.println("fun1WithReturn执行完毕 -> " + (System.currentTimeMillis() - start));
        return "fun1";
    }

    private void fun2NoReturn(String txt) {
        long start = System.currentTimeMillis();
        randSleep(50);
        System.out.println("fun2 -->" + txt + " --> " +  (System.currentTimeMillis() - start));
    }


    /**
     * 异步执行，不关心返回结果
     *
     * @param ans
     */
    private void runAsyncNoReturn(String ans) {
        long start = System.currentTimeMillis();
        randSleep(15);
        System.out.println("runAsyncNoReturn ->" + ans + " --> " +  (System.currentTimeMillis() - start) );
    }

    private String runAsyncWithReturn(String ans) {
        long start = System.currentTimeMillis();
        randSleep(15);
        System.out.println("runAsyncWithReturn ->" + ans + " --> " +  (System.currentTimeMillis() - start));
        return ans + "_over";
    }

    private String runAsyncWithReturn2(String ans) {
        long start = System.currentTimeMillis();
        randSleep(25);
        System.out.println("runAsyncWithReturn2 ->" + ans + " --> " +  (System.currentTimeMillis() - start));
        return ans + "_over2";
    }


    @Test
    public void testWithNoTrace() {
        long start = System.currentTimeMillis();
        // 默认不添加trace记录的执行顺序
        String ans = fun1WithReturn();
        fun2NoReturn(ans);
        runAsyncNoReturn(ans);
        String a2 = runAsyncWithReturn(ans);
        String a3 = runAsyncWithReturn2(ans);
        System.out.println("最终的结果是: -> " + (a2 + a3));
        long end = System.currentTimeMillis();
        System.out.println("总耗时： " + (end - start));
    }

    /**
     * 预热逻辑
     */
    private void preLoad() {
        // 这里的执行，主要是为了解决 TraceWatch等相关类的初始化耗时对整体结果的影响
        try(DefaultTraceRecoder recoder = TraceWatch.startTrace("预热", () -> false)) {
            recoder.sync(() -> { randSleep(20); }, "2");
        } finally {
            System.out.println("========================= 预热 ========================= \n\n");
        }
    }


    /**
     * 集成TraceWatch，全部都使用同步执行的方式
     */
    @Test
    public void testWithSyncTrace() {
        preLoad();
        long start = System.currentTimeMillis();
        try (DefaultTraceRecoder recoder = TraceWatch.startTrace("traceLog-全同步")) {
            System.out.println("到这里耗时： " + (System.currentTimeMillis() - start));
            String ans = recoder.sync(() -> fun1WithReturn(), "1.fun1WithReturn");
            recoder.sync(() -> fun2NoReturn(ans), "2.fun2NoReturn");
            recoder.sync(() -> runAsyncNoReturn(ans), "3.runAsyncNoReturn");
            String a2 = recoder.sync(() -> runAsyncWithReturn(ans), "4.runAsyncWithReturn");
            String a3 = recoder.sync(() -> runAsyncWithReturn2(ans), "5.runAsyncWithReturn");
            System.out.println("最终的结果是: -> " + (a2 + a3));
            System.out.println("结果结束耗时：" + (System.currentTimeMillis() - start));
        }
        long end = System.currentTimeMillis();
        System.out.println("总耗时： " + (end - start));
    }


    /**
     * 根据实际的场景，对一些逻辑调整为异步执行
     */
    @Test
    public void testWithAsyncTrace() {
        preLoad();
        long start = System.currentTimeMillis();
        try (DefaultTraceRecoder recoder = TraceWatch.startTrace("traceLog")) {
            String ans = recoder.sync(() -> fun1WithReturn(), "1.fun1WithReturn");
            recoder.sync(() -> fun2NoReturn(ans), "2.fun2NoReturn");
            recoder.async(() -> runAsyncNoReturn(ans), "3.runAsyncNoReturn");

            // 下面两个演示的是异步返回的场景，在合适的地方，将异步返回结果进行拼接
            CompletableFuture<String> a2 = recoder.async(() -> runAsyncWithReturn(ans), "4.runAsyncWithReturn");
            CompletableFuture<String> a3 = recoder.async(() -> runAsyncWithReturn2(ans), "5.runAsyncWithReturn");
            System.out.println("异步结果阻塞获取前耗时：" + (System.currentTimeMillis() - start));
            CompletableFuture.allOf(a2, a3).whenComplete((unused, throwable) -> System.out.println("最终的结果是: -> " + (a2.join() + a3.join())));
        }
        long end = System.currentTimeMillis();
        System.out.println("总耗时： " + (end - start));
    }
}

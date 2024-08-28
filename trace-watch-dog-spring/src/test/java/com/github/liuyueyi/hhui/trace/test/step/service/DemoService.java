package com.github.liuyueyi.hhui.trace.test.step.service;

import com.github.liuyueyi.hhui.components.trace.TraceWatch;
import com.github.liuyueyi.hui.components.trace.SpringTraceDogWrapper;
import com.github.liuyueyi.hui.components.trace.aop.TraceDog;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Component
public class DemoService {
    private Random random = new Random();

    /**
     * 随机睡眠一段时间
     *
     * @param max
     */
    private void randSleep(String task, int max) {
        randSleepAndRes(task, max);
    }

    private int randSleepAndRes(String task, int max) {
        int sleepMillSecond = random.nextInt(max);
        try {
            System.out.println(task + "==> 随机休眠 " + sleepMillSecond + "ms");
            Thread.sleep(sleepMillSecond);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return sleepMillSecond;
    }

    @TraceDog
    public void sync() {
        randSleep("A-同步执行sync", 20);
    }

    // 方法的耗时不要，但是记录方法内部的代码块执行耗时
    public int ignoreCost() {
        // 代码块的耗时统计
        TraceWatch.getRecoderOrElseSync().sync(() -> randSleep("B-代码块", 30), "B-代码块");
        return randSleepAndRes("B-ignoreCost", 50);
    }

    @TraceDog(async = true, value = "C-标记异步，实际同步执行")
    public int c() {
        return randSleepAndRes("C-标记异步，实际同步执行", 50);
    }

    @TraceDog(async = true, value = "D-异步返回")
    public CompletableFuture<Integer> d() {
        return SpringTraceDogWrapper.rsp(randSleepAndRes("异步返回d", 50));
    }

    @TraceDog(value = "E-异步代码块", async = true)
    public void e() {
        randSleep("异步代码块e", 50);
    }
}
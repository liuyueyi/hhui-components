package com.github.liuyueyi.hhui.components.trace.recoder;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 兜底的同步记录器，主要是为了避免在没有手动开启 trace 记录的场景下，也能正常的运行
 *
 * @author YiHui
 * @date 2024/8/11
 */
public class SyncTraceRecoder implements ITraceRecoder {
    public static SyncTraceRecoder SYNC_RECODER = new SyncTraceRecoder();

    /**
     * 待返回结果的同步执行
     *
     * @param supplier 执行内容
     * @param name     耗时标记
     * @param <T>      返回类型
     * @return 返回结果
     */
    @Override
    public <T> T sync(Supplier<T> supplier, String name) {
        return supplier.get();
    }

    /**
     * 无返回结果的同步执行
     *
     * @param run  执行内容
     * @param name 耗时标记
     */
    @Override
    public void sync(Runnable run, String name) {
        run.run();
    }

    /**
     * 依然是同步执行，会直接返回结果
     *
     * @param supplier 异步任务
     * @param name     耗时标记
     * @param <T>      返回类型
     * @return 返回结果
     */
    @Override
    public <T> CompletableFuture<T> async(Supplier<T> supplier, String name) {
        return CompletableFuture.completedFuture(supplier.get());
    }


    /**
     * 依然是同步执行，会直接返回结果
     *
     * @param run  异步任务
     * @param name 耗时标记
     * @return 返回结果
     */
    @Override
    public CompletableFuture<Void> async(Runnable run, String name) {
        run.run();
        return CompletableFuture.completedFuture(null);
    }
}

package com.github.liuyueyi.hhui.components.trace.recoder;

import java.io.Closeable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 执行链路的核心记录器
 *
 * @author YiHui
 * @date 2024/8/11
 */
public interface ITraceRecoder extends Closeable {
    /**
     * 待返回结果的同步执行
     *
     * @param supplier 执行内容
     * @param name     耗时标记
     * @param <T>      返回类型
     * @return 返回结果
     */
    <T> T sync(Supplier<T> supplier, String name);

    /**
     * 无返回结果的同步执行
     *
     * @param run  执行内容
     * @param name 耗时标记
     */
    void sync(Runnable run, String name);

    /**
     * 异步执行
     *
     * @param supplier 异步任务
     * @param name     耗时标记
     * @param <T>      返回类型
     * @return 返回结果
     */
    <T> CompletableFuture<T> async(Supplier<T> supplier, String name);


    /**
     * 异步执行
     *
     * @param run  异步任务
     * @param name 耗时标记
     * @return 返回结果
     */
    CompletableFuture<Void> async(Runnable run, String name);

    /**
     * 等待全部任务执行完毕
     *
     * @return 返回结果
     */
    default ITraceRecoder allExecuted() {
        return this;
    }

    /**
     * 日志打印
     *
     * @return 各任务耗时情况
     */
    default Map<String, Long> prettyPrint() {
        return Collections.emptyMap();
    }

    @Override
    default void close() {
    }
}

package com.github.liuyueyi.hhui.components.trace;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.github.liuyueyi.hhui.components.trace.async.AsyncUtil;
import com.github.liuyueyi.hhui.components.trace.output.CostOutput;
import com.github.liuyueyi.hhui.components.trace.output.LogOutput;
import com.github.liuyueyi.hhui.components.trace.recoder.DefaultTraceRecoder;
import com.github.liuyueyi.hhui.components.trace.recoder.ITraceRecoder;
import com.github.liuyueyi.hhui.components.trace.recoder.SyncTraceRecoder;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * 执行链路观察工具类
 *
 * @author YiHui
 * @date 2024/8/11
 */
public class TraceWatch {

    private static final TransmittableThreadLocal<ITraceRecoder> THREAD_LOCAL = new TransmittableThreadLocal<>();

    /**
     * 全局的重定向策略
     */
    private static Set<CostOutput> globalOutputStrategy;

    static {
        globalOutputStrategy = new HashSet<>();
        globalOutputStrategy.add(LogOutput.defaultLogOutput);
    }

    /**
     * 注册全局的输出重定向规则
     *
     * @param costOutput
     */
    public static void registerOutput(CostOutput costOutput) {
        globalOutputStrategy.add(costOutput);
    }

    /**
     * 获取默认的全局输出重定向策略
     *
     * @return
     */
    public static Set<CostOutput> getGlobalOutputStrategy() {
        return globalOutputStrategy;
    }

    public static ITraceRecoder startTrace(String name) {
        return startTrace(name, true);
    }

    public static ITraceRecoder startTrace(String name, boolean logEnable) {
        return startTrace(AsyncUtil.executorService, name, logEnable);
    }

    /**
     * 开始trace记录
     *
     * @param executorService 线程池
     * @param name            任务名
     * @return
     */
    public static ITraceRecoder startTrace(ExecutorService executorService, String name, boolean logEnable) {
        DefaultTraceRecoder bridge = new DefaultTraceRecoder(executorService, name, logEnable).setEndHook(TraceWatch::endTrace);
        THREAD_LOCAL.set(bridge);
        return bridge;
    }

    /**
     * 在使用时，请确保先调用了 startTrace， 一定可以拿到 TraceRecoder，否则请使用 getRecoderOrElseSync() 方法
     *
     * @return
     */
    public static ITraceRecoder getRecoder() {
        return THREAD_LOCAL.get();
    }

    /**
     * 获取记录器
     * - 如果在请求链路中，有调用过 startTrace，则返回 DefaultTraceRecoder，可以实现链路的耗时统计；
     * - 若之前没有调用过 startTrace, 则返回 SyncTraceRecoder, 被记录的函数代码块和直接调用没有区别，不会执行异步、也不会记录耗时
     * <p>
     * 主要的应用场景就是，同一个方法，会被多个入口调用，当只想记录某几个入口的耗时情况时，使用下面这个方法，就可以保证不会影响其他的调用方
     *
     * @return
     */
    public static ITraceRecoder getRecoderOrElseSync() {
        ITraceRecoder recoder = getRecoder();
        if (recoder != null) {
            return recoder;
        }
        return SyncTraceRecoder.SYNC_RECODER;
    }

    public static void endTrace() {
        THREAD_LOCAL.remove();
    }
}

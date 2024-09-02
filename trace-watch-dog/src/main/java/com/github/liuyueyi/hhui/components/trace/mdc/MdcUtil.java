package com.github.liuyueyi.hhui.components.trace.mdc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 日志工具类
 *
 * @author YiHui
 * @date 2024/8/11
 */
public class MdcUtil {
    private static final Logger log = LoggerFactory.getLogger(MdcUtil.class);
    private static final String DEFAULT_TRACE_ID_TAG_KEY = "globalMsgId";
    /**
     * 生成msgId的方法
     */
    private static Supplier<String> genIdFunc = null;

    /**
     * 获取MDC上下文中持有msgId的tagKey
     */
    private static String traceIdTagKey = DEFAULT_TRACE_ID_TAG_KEY;


    /**
     * true 表示上下文中没有全链路traceId时，使用默认的生成策略来记录全链路id； 有则使用上下文中的全链路id
     * false 表示只有上下文中存在traceId时，才进行子线程的透传，不会额外生成
     */
    private static volatile Boolean traceIdAutoGen = false;


    /**
     * 注册traceId生成规则
     * 说明: 对于已经有自己的一套全链路的监控的场景，需要在这里进行替换
     *
     * @param gen
     */
    public static void registerIdGenFunc(Supplier<String> gen) {
        genIdFunc = gen;
    }

    /**
     * 注册全链路traceId存储的key
     *
     * @param tagKey
     */
    public static void registerTraceTagKeyGetFunc(String tagKey) {
        traceIdTagKey = tagKey;
    }

    /**
     * 控制是否使用上下文的traceId
     *
     * @param traceIdAutoGen true 表示上下文中没有全链路traceId时，使用默认的生成策略来记录全链路id； 有则使用上下文中的全链路id
     *                       false 表示只有上下文中存在traceId时，才进行子线程的透传，不会额外生成
     */
    public static void initTraceIdAutoGen(Boolean traceIdAutoGen) {
        MdcUtil.traceIdAutoGen = traceIdAutoGen;
    }

    private static void autoInit() {
        if (genIdFunc == null) {
            registerIdGenFunc(MdcUtil::defaultGenGlobalTraceId);
        }
    }

    /**
     * 根据配置，来判断没有traceId时，是直接返回还是新创建一个
     *
     * @return traceId
     */
    public static String fetchGlobalMsgIdForTraceRecoder() {
        if (Objects.equals(Boolean.TRUE, traceIdAutoGen)) {
            return getOrInitGlobalTraceId();
        } else {
            return getGlobalTraceId();
        }
    }

    public static String getGlobalTraceId() {
        return MDC.get(traceIdTagKey);
    }

    /**
     * 获取全局的traceId，若不存在，则进行初始化
     *
     * @return traceId
     */
    public static String getOrInitGlobalTraceId() {
        String traceId = getGlobalTraceId();
        if (traceId == null || traceId.isEmpty()) {
            return newGlobalTraceId();
        }
        return traceId;
    }

    public static void setGlobalTraceId(String msgId) {
        if (msgId == null) {
            return;
        }

        try {
            MDC.put(traceIdTagKey, msgId);
        } catch (Exception e) {
            log.error("failed to init MDC globalMsgId! msgId:{}", msgId, e);
        }
    }

    public static String newGlobalTraceId() {
        autoInit();
        String id = genIdFunc.get();
        MDC.put(traceIdTagKey, id);
        return id;
    }

    public static void clear() {
        MDC.clear();
    }

    /**
     * 默认的全链路id生成规则
     *
     * @return traceId
     */
    public static String defaultGenGlobalTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

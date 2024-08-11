package com.github.liuyueyi.hhui.components.trace.mdc;

import com.sun.deploy.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * 日志工具类
 *
 * @author YiHui
 * @date 2024/8/11
 */
public class MdcUtil {
    private static final Logger log = LoggerFactory.getLogger(MdcUtil.class);
    public static Supplier<String> genIdFunc = null;
    public static Supplier<String> idTagGet = null;

    public static void registerFunc(Supplier<String> genIdFunc, Supplier<String> idTagGet) {
        MdcUtil.genIdFunc = genIdFunc;
        MdcUtil.idTagGet = idTagGet;
    }

    private static void autoInit() {
        if (genIdFunc == null) {
            registerFunc(MdcUtil::defaultGenGlobalMsgId, MdcUtil::defaultIdGet);
        }
    }

    public static String getGlobalMsgId() {
        autoInit();
        return MDC.get(idTagGet.get());
    }

    /**
     * 获取全局的msgId，若不存在，则进行初始化
     *
     * @return
     */
    public static String getOrInitGlobalMsgId() {
        String traceId = getGlobalMsgId();
        if (traceId == null || traceId.isEmpty()) {
            return newGlobalMsgId();
        }
        return traceId;
    }

    public static void setGlobalMsgId(String msgId) {
        try {
            autoInit();
            MDC.put(idTagGet.get(), msgId);
        } catch (Exception e) {
            log.error("failed to init MDC globalMsgId! msgId:{}", msgId, e);
        }
    }

    public static String newGlobalMsgId() {
        autoInit();
        String id = genIdFunc.get();
        MDC.put(idTagGet.get(), id);
        return id;
    }

    /**
     * 默认的全链路id生成规则
     *
     * @return traceId
     */
    public static String defaultGenGlobalMsgId() {
        // fixme: 对于已经有自己的一套全链路的监控的场景，需要在这里进行替换
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 默认的记录全链路traceId对应的参数名
     *
     * @return traceIdName
     */
    public static String defaultIdGet() {
        return "globalMsgId";
    }
}

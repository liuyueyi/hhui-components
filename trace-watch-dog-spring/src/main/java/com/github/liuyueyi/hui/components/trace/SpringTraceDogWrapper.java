package com.github.liuyueyi.hui.components.trace;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.concurrent.CompletableFuture;

/**
 * 辅助类，用于包装返回结果，服务内的方法调用
 *
 * @author YiHui
 * @date 2024/8/12
 */
public class SpringTraceDogWrapper implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringTraceDogWrapper.context = applicationContext;
    }

    public static <T> T proxy(T t) {
        Object target = t;
        while (AopUtils.isCglibProxy(target)) {
            target = AopProxyUtils.getSingletonTarget(target);
        }
        return (T) context.getBean(target.getClass());
    }

    /**
     * 返回结果封装为Future对象，支持异步使用
     *
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> CompletableFuture<T> rsp(T obj) {
        return CompletableFuture.completedFuture(obj);
    }
}

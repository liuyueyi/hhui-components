package com.github.liuyueyi.hui.components.trace.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author YiHui
 * @date 2024/8/11
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TraceDog {

    /**
     * traceName，为空时，默认使用serviceName#methodName
     *
     * @return
     */
    String value() default "";

    /**
     * 传播属性，默认是当前开了traceWatch，则记录；没有开启，则同步的方式执行
     * 因此，在记录链路的开始，请将这个属性设置为 REQUIRED
     *
     * @return
     */
    Propagation propagation() default Propagation.SUPPORTS;

    /**
     * 同步还是异步, 默认都是同步执行这段方法
     *
     * @return
     */
    boolean async() default false;

    /**
     * 是否打印日志 与 logSpeEL 配合使用
     *
     * @return true 可打印日志
     */
    boolean logEnable() default true;


    String logSpEL() default "";
}

package com.github.liuyueyi.hui.components.trace;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 对于非SpringBoot环境，可以通过引入这个注解来开启WatchDog注解模式
 *
 * @author YiHui
 * @date 2024/8/11
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SpringTraceDogConfiguration.class)
@Documented
public @interface EnableTraceWatchDog {
}

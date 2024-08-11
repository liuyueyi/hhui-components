package com.github.liuyueyi.hui.components.trace;

import com.github.liuyueyi.hui.components.trace.aop.TraceAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author YiHui
 * @date 2024/8/11
 */
@Configuration
public class SpringTraceDogConfiguration {

    @Bean
    public TraceAspect traceAspect() {
        return new TraceAspect();
    }

}

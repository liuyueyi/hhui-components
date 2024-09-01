package com.github.liuyueyi.hui.components.trace.aop;

import com.github.liuyueyi.hhui.components.trace.TraceWatch;
import com.github.liuyueyi.hhui.components.trace.recoder.ITraceRecoder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * @author YiHui
 * @date 2024/8/11
 */
@Aspect
public class TraceAspect implements ApplicationContextAware {

    private ExpressionParser parser;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.parser = new SpelExpressionParser();
        this.applicationContext = applicationContext;
    }

    @Around("@annotation(traceDog)")
    public Object handle(ProceedingJoinPoint joinPoint, TraceDog traceDog) throws Throwable {
        if (traceDog.propagation() == Propagation.NEVER) {
            return executed(joinPoint);
        }

        MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());
        if (traceDog.propagation() == Propagation.REQUIRED && TraceWatch.getRecoder() == null) {
            // 入口点： 开启trace耗时记录
            try (ITraceRecoder traceRecoder = TraceWatch.startTrace(genTraceName(methodSignature, traceDog), buildLogCondition(joinPoint, traceDog))) {
                return executed(joinPoint);
            }
        }

        // trace链路的阶段过程
        ITraceRecoder recoder = TraceWatch.getRecoderOrElseSync();
        if (traceDog.async()) {
            if (CompletableFuture.class.isAssignableFrom(methodSignature.getReturnType())) {
                // 有返回结果的场景，因为watchDog本身就包装了返回结果；因此我们需要将实际业务执行的返回结果拿出来使用，否则对于调用方而言，就出现了两层Future
                return recoder.async(() -> executeWithFuture(joinPoint), genTraceName(methodSignature, traceDog));
            } else if (methodSignature.getReturnType() == Void.class || methodSignature.getReturnType() == void.class) {
                // 无返回结果的场景
                return recoder.async(() -> executed(joinPoint), genTraceName(methodSignature, traceDog));
            } else {
                // 都不满足，则采用同步执行
                // --> 这种通常是方法上声明了异步，但是返回结果没有做适配，直接返回了对象。这种场景即便放在线程池中执行，因为也是直接获取方法的返回，相比较同步还多了线程切换的开销
                // --> 使用异步的方法，返回结果需要时 CompletureFuture 进行封装，在最后需要的地方进行获取结果
                return recoder.sync(() -> executed(joinPoint), genTraceName(methodSignature, traceDog));
            }
        } else {
            return recoder.sync(() -> executed(joinPoint), genTraceName(methodSignature, traceDog));
        }
    }


    private Boolean buildLogCondition(ProceedingJoinPoint joinPoint, TraceDog dog) {
        if (!dog.logEnable()) {
            return false;
        }

        if (dog.logSpEL() == null || dog.logSpEL().isEmpty()) {
            return true;
        }

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanFactoryResolver(applicationContext));

        // 将请求参数也作为上下文参数
        MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        return (Boolean) parser.parseExpression(dog.logSpEL()).getValue(context);
    }

    private String genTraceName(MethodSignature methodSignature, TraceDog traceDog) {
        if (traceDog.value() == null || traceDog.value().isEmpty()) {
            return traceDog.value();
        }

        return methodSignature.getDeclaringTypeName() + "#" + methodSignature.getMethod().getName();
    }

    private Object executed(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private Object executeWithFuture(ProceedingJoinPoint joinPoint) {
        try {
            return ((CompletableFuture) joinPoint.proceed()).join();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}

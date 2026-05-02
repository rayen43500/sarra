package org.example.backend.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceExecutionAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceExecutionAspect.class);

    @Around("execution(* org.example.backend.service..*(..))")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long tookMs = System.currentTimeMillis() - start;
            log.info("[AOP] {} executed in {} ms", joinPoint.getSignature().toShortString(), tookMs);
        }
    }
}

package br.com.hyperativa.service.application.config.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final String BEFORE_POINTCUT = "within(@org.springframework.web.bind.annotation.RestController *)";
    private static final String AFTER_POINTCUT = "within(@org.springframework.web.bind.annotation.RestController *)";

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LoggingAspect.class);

    @Before(BEFORE_POINTCUT)
    public void logBefore(final JoinPoint joinPoint) {
        LOGGER.info("Requesting: {}", joinPoint.getSignature());
    }

    @AfterReturning(pointcut = AFTER_POINTCUT, returning = "result")
    public void logAfter(final JoinPoint joinPoint, Object result) {
        LOGGER.info("Returning: {}", result);
    }
}

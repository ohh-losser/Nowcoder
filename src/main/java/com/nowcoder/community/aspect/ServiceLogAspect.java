package com.nowcoder.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    //定义切点
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){

    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        // 用户[1.2.3.4] , 在 [xxx]， 访问了[com.nowcoder.community.service.*.*(..)]
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //得到类名 + 方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        logger.info("用户[{}], 在 [{}], 访问了[{}]", ip, now, target);
    }

    @After("pointcut()")
    public void after(){
        logger.info("after");
    }

    @AfterReturning("pointcut()")
    public void afterReturning(){
        logger.info("afterReturning");
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        logger.info("afterThrowing");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("around");
        Object result = joinPoint.proceed();
        logger.info("around end");
        return result;
    }
}

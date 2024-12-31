package com.td.common.aspect;

import com.td.common.annotations.RedisSynchronized;
import com.td.common.base.LoginUser;
import com.td.common.lock.CustomRedisLock;
import com.td.common.utils.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RedisSynchronizedAspect {
    @Autowired
    private CustomRedisLock customRedisLock;

    @Around("@annotation(redisSynchronized)")
    public Object around(ProceedingJoinPoint joinPoint, RedisSynchronized redisSynchronized) throws Throwable {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String key = redisSynchronized.key() + ":" + loginUser.getId();
        boolean locked = customRedisLock.tryLock(key);
        if (locked) {
            try {
                return joinPoint.proceed();
            } finally {
                customRedisLock.unlock(key);
            }
        } else {
            throw new RuntimeException("请勿重复提交");
        }
    }
}
package com.td.common.lock;

import com.td.common.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CustomRedisLock {
    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_EXPIRE_TIME = 15; // 默认锁的过期时间，单位为秒

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisUtils redisUtils;

    public boolean tryLock(String key) {
        String lockKey = LOCK_PREFIX + key;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "true");
        if (success != null && success) {
            redisTemplate.expire(lockKey, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    public void unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        redisTemplate.delete(lockKey);
    }

    /**
     * 自旋锁
     *
     * @param key
     */
    public void spinLock(String key) {
        String lockKey = LOCK_PREFIX + key;

        while (!redisUtils.tryLock(lockKey, "1", DEFAULT_EXPIRE_TIME)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void spinUnlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        redisUtils.unlock(lockKey);
    }
}
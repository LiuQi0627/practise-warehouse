package com.messi.system.lock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * redis分布式锁
 */
@Slf4j
public class DistributedLock {

    //  构造器注入
    public RedissonClient redissonClient;

    public DistributedLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 自动续期互斥锁
     * <p>
     * rLock.lock：获取不到锁时，会一直等待
     * rLock.tryLock()是获取不到锁直接返回false的可重入锁，
     * 默认key的ttl是30s,同一个key可多次重入，每重入一次，次数+1
     */
    public Boolean tryLock(String redisKey) {
        RLock rLock = redissonClient.getLock(redisKey);
        log.info("tryLock,Key:{},Lock:{}", redisKey, rLock);
        boolean tryResult = rLock.tryLock();//  获取不到锁，返回false
        return tryResult;
    }

    /**
     * 放锁
     */
    public void unLock(String redisKey) {
        RLock rLock = redissonClient.getLock(redisKey);
        if (rLock.isLocked()) {
            rLock.unlock();
            log.info("unLock,Key:{}", redisKey);
        }
    }
}

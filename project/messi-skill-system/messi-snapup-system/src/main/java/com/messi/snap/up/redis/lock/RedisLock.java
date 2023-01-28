package com.messi.snap.up.redis.lock;

import java.util.concurrent.TimeUnit;

public interface RedisLock {

    String tryLock(String lockKey, long expiration, TimeUnit timeUnit);

    boolean release(String lockKey, String lockToken);

}

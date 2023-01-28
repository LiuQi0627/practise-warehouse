package com.messi.snap.up.redis.lock;

import com.messi.snap.up.config.jedis.JedisManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RedisLockImpl implements RedisLock {

    private final JedisManager jedisManager;

    public RedisLockImpl(JedisManager jedisManager) {
        this.jedisManager = jedisManager;
    }

    /**
     * 锁库存
     * 根据key在集群中做路由，找到指定的实例后再做执行
     */
    @Override
    public String tryLock(String lockKey, long expiration, TimeUnit timeUnit) {
        int hashKey = lockKey.hashCode();
        try (Jedis jedis = jedisManager.getJedisByHashKey(hashKey)) {
            String lockToken = UUID.randomUUID().toString();
            String result = jedis.set(lockKey, lockToken,
                    SetParams.setParams().nx().px(timeUnit.toMillis(expiration))
            );
            if ("OK".equals(result)) {
                return lockToken;
            }
        }
        return null;
    }

    @Override
    public boolean release(String lockKey, String lockToken) {
        int hashKey = lockKey.hashCode();
        try (Jedis jedis = jedisManager.getJedisByHashKey(hashKey)) {
            String script = String.format(DEL_KEY_BY_VALUE, lockKey, lockToken, lockKey);
            String result = (String) jedis.eval(script);
            if ("1".equals(result)) {
                return true;
            }
        }
        return false;
    }

    private static final String DEL_KEY_BY_VALUE =
            "if redis.call('get', '%s') == '%s'\n" +
                    "then\n" +
                    "   redis.call('del','%s');" +
                    "   return '1';\n" +
                    "else\n" +
                    "    return '0'\n" +
                    "end";
}

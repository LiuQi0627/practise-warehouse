package com.messi.snap.up.redis;

import com.messi.snap.up.config.jedis.JedisManager;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 这个里面，针对key去进行有一个操作，redis集群，需要根据key hash，
 * hash取模，路由对应的redis节点里去
 * 针对那个redis节点进行操作的，是基于jedis pool连接池来进行操作的
 * 他主要封装的是对redis常规的操作，我们在这里都可以基于key进行hash，取模路由，支持redis集群
 */
public class RedisCommandImpl implements RedisCommand {

    private final JedisManager jedisManager;

    public RedisCommandImpl(JedisManager jedisManager) {
        this.jedisManager = jedisManager;
    }

    @Override
    public int getRedisCount() {
        return jedisManager.getRedisCount();
    }

    @Override
    public Boolean exists(String key) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(key.hashCode())) {
            return jedis.exists(key);
        }
    }

    @Override
    public Long expire(String key, int seconds) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(key.hashCode())) {
            return jedis.expire(key, seconds);
        }
    }

    @Override
    public Long del(String key) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(key.hashCode())) {
            return jedis.del(key);
        }
    }

    @Override
    public Long delOnAllRedis(String key) {
        for (int i = 0; i < jedisManager.getRedisCount(); i++) {
            try (Jedis jedis = jedisManager.getJedisByHashKey(i)) {
                jedis.del(key);
            }
        }
        return 1L;
    }

    @Override
    public String set(String key, String value) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(key.hashCode())) {
            return jedis.set(key, value);
        }
    }

    @Override
    public String get(String key) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(key.hashCode())) {
            return jedis.get(key);
        }
    }

    @Override
    public Long incr(String key) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(key.hashCode())) {
            return jedis.incr(key);
        }
    }

    @Override
    public Object eval(Long hashKey, String script) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(hashKey)) {
            return jedis.eval(script);
        }
    }

    // 一个商品库存数据分散再各个 redis节点上
    // 需要从redis节点来查询我们的库存数据，合并起来，才算是一份总的数据
    @Override
    public List<Map<String, String>> hgetAllOnAllRedis(String key) {
        List<Map<String, String>> list = new ArrayList<>();
        for (int i = 0; i < jedisManager.getRedisCount(); i++) {
            try (Jedis jedis = jedisManager.getJedisByHashKey(i)) {
                list.add(jedis.hgetAll(key));
            }
        }
        return list;
    }

    /**
     * hset命令
     * 对redis的每个节点都执行hset
     */
    @Override
    public void hsetOnAllRedis(String key, List<Map<String, String>> hashList) {
        for (int i = 0; i < jedisManager.getRedisCount(); i++) {
            try (Jedis jedis = jedisManager.getJedisByHashKey(i)) {
                jedis.hset(key, hashList.get(i));
            }
        }
    }
}

package com.messi.snap.up.config.jedis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JedisManager implements DisposableBean {
    private final List<JedisPool> jedisPools = new ArrayList<>();

    public JedisManager(JedisConfig jedisConfig) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(jedisConfig.getMaxTotal());
        jedisPoolConfig.setMaxIdle(jedisConfig.getMaxIdle());
        jedisPoolConfig.setMinIdle(jedisConfig.getMinIdle());

        //  加载集群地址
        for (String address : jedisConfig.getRedisAddress()) {
            String[] ipAndPort = address.split(":");
            String redisIp = ipAndPort[0];
            int redisPort = Integer.parseInt(ipAndPort[1]);
            //  分别为每个redis实例建立一个连接池
            JedisPool jedisPool = new JedisPool(jedisPoolConfig, redisIp, redisPort);
            log.info("创建ip:{} 的JedisPool", redisIp);
            jedisPools.add(jedisPool);
        }
    }

    public int getRedisCount() {
        return jedisPools.size();
    }

    public Jedis getJedisByIndex(int index) {
        return jedisPools.get(index).getResource();
    }

    public Jedis getJedisByHashKey(long hashKey) {
        hashKey = Math.abs(hashKey);
        int index = (int) (hashKey % getRedisCount());
        return getJedisByIndex(index);
    }

    public Jedis getJedisByHashKey(int hashKey) {
        hashKey = Math.abs(hashKey);
        int index = hashKey % getRedisCount();
        return getJedisByIndex(index);
    }

    @Override
    public void destroy() {
        for (JedisPool jedisPool : jedisPools) {
            log.info("关闭JedisPool:{}", jedisPool);
            jedisPool.close();
        }
    }
}
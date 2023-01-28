package com.messi.snap.up.config.jedis;

import com.messi.snap.up.redis.RedisCommand;
import com.messi.snap.up.redis.RedisCommandImpl;
import com.messi.snap.up.redis.lock.RedisLock;
import com.messi.snap.up.redis.lock.RedisLockImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
@EnableConfigurationProperties(JedisConfig.class)
public class JedisAutoConfiguration {

    private final JedisConfig jedisConfig;

    public JedisAutoConfiguration(JedisConfig jedisConfig) {
        if (Objects.isNull(jedisConfig.getMaxTotal())) {
            jedisConfig.setMaxTotal(8);
        }
        if (Objects.isNull(jedisConfig.getMaxIdle())) {
            jedisConfig.setMaxIdle(8);
        }
        if (Objects.isNull(jedisConfig.getMinIdle())) {
            jedisConfig.setMinIdle(0);
        }
        this.jedisConfig = jedisConfig;
    }

    /**
     * 封装jedis连接池,并指定连接池销毁方法
     */
    @Bean(destroyMethod = "destroy")
    @ConditionalOnMissingBean
    public JedisManager jedisManager() {
        return new JedisManager(jedisConfig);
    }

    /**
     * 建立操作redis命令的bean
     */
    @Bean(name = "redisCacheService")
    @ConditionalOnMissingBean
    public RedisCommand redisCacheService(JedisManager jedisManager) {
        return new RedisCommandImpl(jedisManager);
    }

    /**
     * 建立操作redis锁库存和释放库存的bean
     */
    @Bean(name = "redisLockService")
    @ConditionalOnMissingBean
    public RedisLock redisLockService(JedisManager jedisManager) {
        return new RedisLockImpl(jedisManager);
    }
}
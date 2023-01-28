package com.messi.snap.up.config.jedis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "messi.snap.up.jedis")
public class JedisConfig {

    private Integer maxTotal;

    private Integer maxIdle;

    private Integer minIdle;

    private List<String> redisAddress;

    public Integer getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal;
    }

    public Integer getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(Integer maxIdle) {
        this.maxIdle = maxIdle;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public List<String> getRedisAddress() {
        return redisAddress;
    }

    public void setRedisAddress(List<String> redisAddress) {
        this.redisAddress = redisAddress;
    }
}
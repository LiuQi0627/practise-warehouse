package com.messi.snap.up.redis;

import java.util.List;
import java.util.Map;

/**
 * redis命令操作类
 */
public interface RedisCommand {

    int getRedisCount();

    Boolean exists(String key);

    Long expire(String key, int seconds);

    Long del(String key);

    Long delOnAllRedis(String key);

    String set(String key, String value);

    String get(String key);

    Long incr(String key);

    void hsetOnAllRedis(String key, List<Map<String, String>> hashList);

    List<Map<String, String>> hgetAllOnAllRedis(String key);

    Object eval(Long hashKey, String script);

}
package com.messi.snap.up.service;

import com.alibaba.fastjson.JSONObject;
import com.messi.sanp.up.service.SnapUpInventoryService;
import com.messi.snap.up.redis.RedisCommand;
import com.messi.snap.up.redis.SnapupKey;
import com.messi.system.core.ResResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抢购库存service 实现类
 */
@Slf4j
@Service
public class SnapUpInventoryServiceImpl implements SnapUpInventoryService {

    @Autowired
    private RedisCommand redisCommand;

    /**
     * 抢购库存分片
     * 将库存均匀的分配给redis cluster集群
     *
     * @param promotionId        促销活动id
     * @param skuId              skuid
     * @param totalPurchaseStock 抢购库存总量
     * @return 分片结果
     */
    @Override
    public ResResult<Boolean> snapUpInventoryShards(Long promotionId, Long skuId, Integer totalPurchaseStock) {
        //  1、获取redis cluster的master集群数量
        int redisCount = redisCommand.getRedisCount();

        //  2、均匀划分库存分片
        Map<Integer, Integer> redisShardMap = new HashMap<>();
        for (int i = 0; i < totalPurchaseStock; i++) {
            //  对redis集群数量取模，平均分配到指定的一个集群实例中
            int index = i % redisCount;
            //  当前index如果不存在，先配置一个初始值是0，避免在put时空指针异常
            redisShardMap.putIfAbsent(index, 0);
            redisShardMap.put(index, redisShardMap.get(index) + 1);
        }

        //  3、分配库存
        List<Map<String, String>> stockList = new ArrayList<>();
        for (int i = 0; i < redisCount; i++) {
            Map<String, String> stockMap = new HashMap<>();
            //  可售库存
            stockMap.put(SnapupKey.SALABLE_STOCK, redisShardMap.get(i) + "");
            //  已锁定库存
            stockMap.put(SnapupKey.LOCKED_STOCK, "0");
            //  已售库存
            stockMap.put(SnapupKey.SOLD_STOCK, "0");

            stockList.add(stockMap);
            log.info("redis分配实例：{},库存分片stockMap:{}",
                    JSONObject.toJSONString(redisShardMap), JSONObject.toJSONString(stockList));
        }

        //  3、更新库存
        String snapUpRedisKey = SnapupKey.buildStockKey(promotionId, skuId + "");
        redisCommand.hsetOnAllRedis(snapUpRedisKey, stockList);

        return ResResult.buildSuccess();
    }
}

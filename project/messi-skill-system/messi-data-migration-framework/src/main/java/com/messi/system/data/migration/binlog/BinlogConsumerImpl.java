package com.messi.system.data.migration.binlog;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.messi.system.data.migration.service.targetdb.OrderInfoTargetService;
import com.messi.system.data.migration.service.targetdb.OrderItemInfoTargetService;
import com.messi.system.data.migration.service.targetdb.OrderPriceDetailsTargetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BinlogConsumerImpl implements BinlogConsumer {

    @Autowired
    private OrderInfoTargetService orderInfoTargetService;

    @Autowired
    private OrderItemInfoTargetService orderItemInfoTargetService;

    @Autowired
    private OrderPriceDetailsTargetService orderPriceDetailsTargetService;

    /**
     * 消费binlog数据
     *
     * @param tableName  表名
     * @param eventType  监听的binlog事件类型
     * @param beforeData 旧数据
     * @param afterData  新数据
     */
    @Override
    public void consumer(String tableName, CanalEntry.EventType eventType, JSONObject beforeData, JSONObject afterData) {
        if ("order_info".equalsIgnoreCase(tableName)) {
            orderInfoTargetService.consumeBinlog(eventType, beforeData, afterData);
        }

        if ("order_item_info".equalsIgnoreCase(tableName)) {
            orderItemInfoTargetService.consumeBinlog(eventType, beforeData, afterData);
        }

        if ("order_price_details".equalsIgnoreCase(tableName)) {
            orderPriceDetailsTargetService.consumeBinlog(eventType, beforeData, afterData);
        }

        log.info("完成监听Binlog处理,tableName:{},eventType:{},beforeData:{},afterData:{}",
                tableName, eventType, beforeData, afterData);
    }
}

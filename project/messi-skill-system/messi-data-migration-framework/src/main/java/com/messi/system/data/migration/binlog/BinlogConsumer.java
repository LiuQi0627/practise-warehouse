package com.messi.system.data.migration.binlog;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;

/**
 * binlog消费组件接口
 */
public interface BinlogConsumer {

    /**
     * 消费binlog数据
     *
     * @param tableName  表名
     * @param eventType  监听的binlog事件类型
     * @param beforeData 起始时间
     * @param afterData  结束时间
     */
    void consumer(String tableName, CanalEntry.EventType eventType, JSONObject beforeData, JSONObject afterData);

}

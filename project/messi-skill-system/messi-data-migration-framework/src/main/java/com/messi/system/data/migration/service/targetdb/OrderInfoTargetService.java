package com.messi.system.data.migration.service.targetdb;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.messi.system.data.migration.entity.OrderInfoDO;

import java.util.List;

/**
 * 迁移order_info表的service
 */
public interface OrderInfoTargetService {

    /**
     * 获取最大ID
     */
    Long getMaxId();

    /**
     * 同步迁移数据
     *
     * @param orderInfoDOs 待迁移的批量数据
     * @param maxId        当前批次的最大id
     */
    void syncMigration(List<OrderInfoDO> orderInfoDOs, Long maxId);

    /**
     * 消费binlog
     *
     * @param eventType  binlog的事件类型
     * @param beforeData 旧数据
     * @param afterData  新数据
     */
    void consumeBinlog(CanalEntry.EventType eventType, JSONObject beforeData, JSONObject afterData);
}

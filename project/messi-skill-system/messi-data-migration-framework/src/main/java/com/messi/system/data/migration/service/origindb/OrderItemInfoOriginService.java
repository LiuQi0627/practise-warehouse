package com.messi.system.data.migration.service.origindb;

import com.messi.system.data.migration.entity.OrderItemInfoDO;

import java.util.Date;
import java.util.List;

/**
 * 迁移order_item_info表的service
 */
public interface OrderItemInfoOriginService {

    /**
     * 批量查询全量数据
     */
    List<OrderItemInfoDO> queryByBatch(Long minId, Date fullDataDeadline, Integer batchDataSize);

}

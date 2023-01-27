package com.messi.system.data.migration.service.origindb;

import com.messi.system.data.migration.entity.OrderInfoDO;

import java.util.Date;
import java.util.List;

/**
 * 迁移order_info表的service
 */
public interface OrderInfoOriginService {

    /**
     * 批量查询全量数据
     */
    List<OrderInfoDO> queryByBatch(Long minId, Date fullDataDeadline, Integer batchDataSize);

}

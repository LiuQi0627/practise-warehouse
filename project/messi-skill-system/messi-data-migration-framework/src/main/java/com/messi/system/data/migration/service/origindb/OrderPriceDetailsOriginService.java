package com.messi.system.data.migration.service.origindb;

import com.messi.system.data.migration.entity.OrderPriceDetailsDO;

import java.util.Date;
import java.util.List;

/**
 * 迁移order_price_details表的service
 */
public interface OrderPriceDetailsOriginService {

    /**
     * 批量查询全量数据
     */
    List<OrderPriceDetailsDO> queryByBatch(Long minId, Date fullDataDeadline, Integer batchDataSize);

}

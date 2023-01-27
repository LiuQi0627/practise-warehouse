package com.messi.system.data.migration.service.origindb;

import com.messi.system.data.migration.entity.OrderItemInfoDO;
import com.messi.system.data.migration.mapper.origindb.OriginOrderItemInfoTableMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 迁移order_item_info表的service
 */
@Slf4j
@Service
public class OrderItemInfoOriginServiceImpl implements OrderItemInfoOriginService {

    @Autowired
    private OriginOrderItemInfoTableMapper originOrderItemInfoTableMapper;

    /**
     * 批量查询全量数据
     *
     * @param minId            数据库最小id
     * @param fullDataDeadline 迁移全量数据的截止时间
     * @param batchDataSize    批量数据量
     */
    @Override
    public List<OrderItemInfoDO> queryByBatch(Long minId, Date fullDataDeadline, Integer batchDataSize) {
        //  基于当前的最小id查询出指定量级的数据
        return originOrderItemInfoTableMapper.queryByBatch(minId, fullDataDeadline, batchDataSize);
    }

}

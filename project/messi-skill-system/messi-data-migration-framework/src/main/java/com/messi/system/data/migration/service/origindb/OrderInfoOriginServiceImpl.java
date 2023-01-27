package com.messi.system.data.migration.service.origindb;

import com.messi.system.data.migration.entity.OrderInfoDO;
import com.messi.system.data.migration.mapper.origindb.OriginOrderInfoTableMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 迁移order_info表的service
 */
@Slf4j
@Service
public class OrderInfoOriginServiceImpl implements OrderInfoOriginService {

    @Autowired
    private OriginOrderInfoTableMapper originOrderInfoTableMapper;

    /**
     * 批量查询全量数据
     *
     * @param minId            数据库最小id
     * @param fullDataDeadline 迁移全量数据的截止时间
     * @param batchDataSize    批量数据量
     */
    @Override
    public List<OrderInfoDO> queryByBatch(Long minId, Date fullDataDeadline, Integer batchDataSize) {
        //  基于当前的最小id查询出指定量级的数据
        return originOrderInfoTableMapper.queryByBatch(minId, fullDataDeadline, batchDataSize);
    }

}

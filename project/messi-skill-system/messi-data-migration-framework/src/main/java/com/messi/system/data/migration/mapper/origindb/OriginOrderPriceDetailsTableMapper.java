package com.messi.system.data.migration.mapper.origindb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.messi.system.data.migration.entity.OrderPriceDetailsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * order_price_details mapper
 */
@Mapper
public interface OriginOrderPriceDetailsTableMapper extends BaseMapper<OrderPriceDetailsDO> {

    /**
     * 从指定的最小id开始,查询出指定量级的数据量
     *
     * @param minId            数据库最小id
     * @param fullDataDeadline 迁移全量数据的截止时间
     * @param batchDataSize    批量数据量
     */
    List<OrderPriceDetailsDO> queryByBatch(@Param("minId") Long minId, @Param("fullDataDeadline") Date fullDataDeadline,
                                       @Param("batchDataSize") Integer batchDataSize);

}

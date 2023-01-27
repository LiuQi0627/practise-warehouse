package com.messi.system.data.migration.mapper.targetdb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.messi.system.data.migration.entity.OrderInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * order_info mapper
 */
@Mapper
public interface TargetOrderInfoTableMapper extends BaseMapper<OrderInfoDO> {

    /**
     * 获取数据表最大IO
     */
    Long getMaxId();

    /**
     * 批量插入
     */
    void batchInsert(@Param("orderInfoDOs") List<OrderInfoDO> orderInfoDOs);

    /**
     * 查询modified字段
     *
     * @param id      主键id
     * @param orderId 业务id，这里的业务id是订单id
     */
    Date getModified(@Param("id") Long id, @Param("orderId") String orderId);
}

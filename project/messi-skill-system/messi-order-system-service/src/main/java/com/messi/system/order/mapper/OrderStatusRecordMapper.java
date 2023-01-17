package com.messi.system.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.messi.system.order.domain.entity.OrderStatusRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单状态变更记录表 Mapper 接口
 */
@Mapper
public interface OrderStatusRecordMapper extends BaseMapper<OrderStatusRecordDO> {

}

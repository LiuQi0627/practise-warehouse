package com.messi.system.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.messi.system.order.domain.entity.OrderPriceDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单价格表 Mapper 接口
 */
@Mapper
public interface OrderPriceMapper extends BaseMapper<OrderPriceDO> {

}

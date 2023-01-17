package com.messi.system.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.messi.system.order.domain.entity.OrderPriceDetailsDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单价格明细表 Mapper 接口
 */
@Mapper
public interface OrderPriceDetailsMapper extends BaseMapper<OrderPriceDetailsDO> {

}

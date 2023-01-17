package com.messi.system.order.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.messi.system.mybatis.BaseDAO;
import com.messi.system.order.domain.entity.OrderPriceDO;
import com.messi.system.order.mapper.OrderPriceMapper;
import org.springframework.stereotype.Repository;

/**
 * 操作order_price DAO
 */
@Repository
public class OrderPriceDAO extends BaseDAO<OrderPriceMapper, OrderPriceDO> {

    public OrderPriceDO getOrderPrice(String orderId) {
        LambdaQueryWrapper<OrderPriceDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderPriceDO::getOrderId, orderId);

        return getOne(queryWrapper);
    }
}

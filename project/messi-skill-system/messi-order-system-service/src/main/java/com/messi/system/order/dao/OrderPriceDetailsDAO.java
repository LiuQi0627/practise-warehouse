package com.messi.system.order.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.messi.system.mybatis.BaseDAO;
import com.messi.system.order.domain.entity.OrderPriceDetailsDO;
import com.messi.system.order.mapper.OrderPriceDetailsMapper;
import org.springframework.stereotype.Repository;

/**
 * 操作order_price_details DAO
 */
@Repository
public class OrderPriceDetailsDAO extends BaseDAO<OrderPriceDetailsMapper, OrderPriceDetailsDO> {

    public OrderPriceDetailsDO getOrderPrice(String orderId) {
        LambdaQueryWrapper<OrderPriceDetailsDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderPriceDetailsDO::getOrderId, orderId);

        return getOne(queryWrapper);
    }
}

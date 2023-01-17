package com.messi.system.order.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.messi.system.mybatis.BaseDAO;
import com.messi.system.order.domain.entity.OrderInfoDO;
import com.messi.system.order.mapper.OrderInfoMapper;
import org.springframework.stereotype.Repository;

/**
 * 操作order_info DAO
 */
@Repository
public class OrderDAO extends BaseDAO<OrderInfoMapper, OrderInfoDO> {

    /**
     * 根据 orderId 查询 order
     *
     * @param orderId 订单号
     */
    public OrderInfoDO getOrder(String orderId) {
        LambdaQueryWrapper<OrderInfoDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfoDO::getOrderId, orderId);

        return getOne(queryWrapper);
    }
}

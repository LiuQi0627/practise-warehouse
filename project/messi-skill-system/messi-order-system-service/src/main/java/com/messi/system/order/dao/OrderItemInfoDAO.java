package com.messi.system.order.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.messi.system.mybatis.BaseDAO;
import com.messi.system.order.domain.entity.OrderItemInfoDO;
import com.messi.system.order.mapper.OrderItemInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 操作order_item_info DAO
 */
@Repository
public class OrderItemInfoDAO extends BaseDAO<OrderItemInfoMapper, OrderItemInfoDO> {

    @Autowired
    private OrderItemInfoMapper orderItemInfoMapper;

    /**
     * 根据订单号查询条目
     */
    public List<OrderItemInfoDO> getItemList(String orderId) {
        QueryWrapper<OrderItemInfoDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        return orderItemInfoMapper.selectList(queryWrapper);
    }
}

package com.messi.system.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.messi.system.order.dao.OrderDAO;
import com.messi.system.order.domain.entity.OrderInfoDO;
import com.messi.system.order.domain.request.CancelOrderReq;
import com.messi.system.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 订单service实现类
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDAO orderDAO;

    /**
     * 支付前取消订单的操作
     */
    @Override
    public void notPaidCancelOrder(CancelOrderReq cancelOrderReq) {
        log.info("做支付前取消订单的业务逻辑操作,取消订单参数:{}", JSONObject.toJSONString(cancelOrderReq));
    }

    /**
     * 查询订单
     *
     * @param orderId orderId
     * @return OrderInfoDO
     */
    @Override
    public OrderInfoDO getOrder(String orderId) {
        return orderDAO.getOrder(orderId);
    }

}

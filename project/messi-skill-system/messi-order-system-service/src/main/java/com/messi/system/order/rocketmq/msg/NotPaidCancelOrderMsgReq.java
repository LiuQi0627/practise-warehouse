package com.messi.system.order.rocketmq.msg;

import com.messi.system.order.domain.entity.*;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 解析 未支付取消订单 消息后的参数
 */
@Data
public class NotPaidCancelOrderMsgReq implements Serializable {
    private static final long serialVersionUID = -7084484249640031485L;

    /**
     * 订单信息
     */
    private OrderInfoDO orderInfoDO;

    /**
     * 订单条目信息
     */
    private List<OrderItemInfoDO> orderItemList;

    /**
     * 订单价格
     */
    private OrderPriceDO orderPriceDO;

    /**
     * 订单条目价格
     */
    private List<OrderPriceDetailsDO> orderPriceItemList;

    /**
     * 订单状态变更记录
     */
    private OrderStatusRecordDO orderStatusRecordDO;

}

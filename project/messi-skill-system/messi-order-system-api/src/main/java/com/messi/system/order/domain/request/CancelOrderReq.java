package com.messi.system.order.domain.request;

import com.messi.system.order.domain.entity.*;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 取消订单入参
 */
@Data
public class CancelOrderReq implements Serializable {
    private static final long serialVersionUID = 5434422327294314099L;

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

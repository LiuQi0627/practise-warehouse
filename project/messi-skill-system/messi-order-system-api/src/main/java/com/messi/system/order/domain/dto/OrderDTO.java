package com.messi.system.order.domain.dto;

import com.messi.system.order.domain.entity.*;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 完整的订单数据DTO
 */
@Data
public class OrderDTO implements Serializable {
    private static final long serialVersionUID = 9182680867845225775L;

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

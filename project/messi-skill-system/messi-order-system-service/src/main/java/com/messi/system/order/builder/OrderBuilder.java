package com.messi.system.order.builder;

import com.messi.system.order.domain.builder.Order;
import com.messi.system.order.domain.entity.*;
import com.messi.system.order.domain.request.SubmitOrderReq;
import com.messi.system.order.enums.OrderAppraiseEnums;
import com.messi.system.order.enums.OrderPayTypeEnums;
import com.messi.system.order.enums.OrderStatusEnums;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 订单建造者
 */
public class OrderBuilder {

    public SubmitOrderReq submitOrderReq;

    public Integer finalPrice;

    public OrderInfoDO orderInfoDO;

    public List<OrderItemInfoDO> orderItemList;

    public OrderPriceDO orderPriceDO;

    public List<OrderPriceDetailsDO> orderPriceItemList;

    public OrderStatusRecordDO orderStatusRecordDO;

    public Order order;

    public OrderBuilder(SubmitOrderReq submitOrderReq, Integer finalPrice) {
        this.submitOrderReq = submitOrderReq;
        this.finalPrice = finalPrice;
        this.orderInfoDO = new OrderInfoDO();
        this.orderItemList = new LinkedList<>();
        this.orderPriceDO = new OrderPriceDO();
        this.orderPriceItemList = new LinkedList<>();
        this.orderStatusRecordDO = new OrderStatusRecordDO();
        this.order = new Order();
    }

    //  订单信息
    public OrderBuilder buildOrder() {

        this.orderInfoDO.setChannel(submitOrderReq.getChannel());

        this.orderInfoDO.setOrderId(submitOrderReq.getOrderId());

        this.orderInfoDO.setOrderType(submitOrderReq.getOrderType());

        this.orderInfoDO.setOrderStatus(OrderStatusEnums.CREATED.getCode());

        this.orderInfoDO.setOrderCancelTime(null);

        this.orderInfoDO.setSellerId(submitOrderReq.getSellerId());

        this.orderInfoDO.setUserId(submitOrderReq.getUserId());

        this.orderInfoDO.setTotalAmount(submitOrderReq.getTotalAmount());

        this.orderInfoDO.setActualAmount(finalPrice);

        this.orderInfoDO.setOrderPayType(OrderPayTypeEnums.WECHAT.getCode());

        this.orderInfoDO.setPayTime(new Date());

        this.orderInfoDO.setCouponId(submitOrderReq.getCouponId());

        this.orderInfoDO.setAppraiseStatus(OrderAppraiseEnums.NO_RATED.getCode());

        this.order.setOrderInfoDO(orderInfoDO);
        return this;
    }

    //  订单明细
    public OrderBuilder buildOrderItem() {
        List<SubmitOrderReq.OrderItemReq> orderItemReqList = submitOrderReq.getOrderItemReqList();

        for (SubmitOrderReq.OrderItemReq orderItemReq : orderItemReqList) {
            OrderItemInfoDO orderItem = OrderItemInfoDO.builder()
                    .orderId(submitOrderReq.getOrderId())
                    .orderItemId(orderItemReq.getOrderItemId())
                    .productId(orderItemReq.getProductId())
                    .skuId(orderItemReq.getSkuId())
                    .saleNum(orderItemReq.getSaleNum())
                    .salePrice(orderItemReq.getSkuPrice())
                    .sellerId(submitOrderReq.getSellerId())
                    .userId(submitOrderReq.getUserId())
                    .build();

            this.orderItemList.add(orderItem);
        }

        this.order.setOrderItemList(orderItemList);
        return this;
    }

    //  订单价格
    public OrderBuilder buildOrderPrice() {
        this.orderPriceDO.setOrderId(submitOrderReq.getOrderId());
        this.orderPriceDO.setOrderTotalPrice(finalPrice);

        this.order.setOrderPriceDO(orderPriceDO);
        return this;
    }

    //  订单价格明细
    public OrderBuilder buildOrderPriceItem() {
        for (OrderItemInfoDO orderItemInfoDO : this.orderItemList) {
            OrderPriceDetailsDO orderPriceDetailsDO = OrderPriceDetailsDO.builder()
                    .orderId(orderItemInfoDO.getOrderId())
                    .orderItemId(orderItemInfoDO.getOrderItemId())
                    .skuId(orderItemInfoDO.getSkuId())
                    .saleNum(orderItemInfoDO.getSaleNum())
                    .salePrice(orderItemInfoDO.getSalePrice())
                    .orderItemPrice(averageConcessionAmount(orderItemInfoDO.getSalePrice(), orderItemInfoDO.getSaleNum()))
                    .build();

            orderPriceItemList.add(orderPriceDetailsDO);
        }

        this.order.setOrderPriceItemList(orderPriceItemList);
        return this;
    }

    //  sku最终价格 = sku均摊的优惠金额
    //  在除法中使用四舍五入，其实对于金额计算并不严谨
    //  这里这样做的目的，仅仅是为了快速过掉业务
    private Integer averageConcessionAmount(Integer salePrice, Integer saleNum) {
        BigDecimal decimal = new BigDecimal(salePrice).divide(new BigDecimal(saleNum), RoundingMode.HALF_UP);
        return decimal.intValue();
    }

    //  订单状态变更记录
    public OrderBuilder buildNewOrderStatusRecord() {
        OrderStatusRecordDO record = new OrderStatusRecordDO();
        record.setOrderId(submitOrderReq.getOrderId());
        record.setPrevStatus(OrderStatusEnums.NEW.getCode());
        record.setCurStatus(OrderStatusEnums.CREATED.getCode());

        this.order.setOrderStatusRecordDO(record);
        return this;
    }

    public Order build() {
        return this.order;
    }
}

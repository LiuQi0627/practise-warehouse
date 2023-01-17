package com.messi.system.order.service.impl.submit;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.messi.system.constant.RocketMqConstants;
import com.messi.system.core.ResResult;
import com.messi.system.market.domain.request.MarketPriceReq;
import com.messi.system.order.domain.builder.Order;
import com.messi.system.order.builder.OrderBuilder;
import com.messi.system.order.converter.OrderConverter;
import com.messi.system.order.dao.*;
import com.messi.system.order.domain.dto.CheckCouponDTO;
import com.messi.system.order.domain.dto.CheckOrderPriceDTO;
import com.messi.system.order.domain.dto.SubmitOrderDTO;
import com.messi.system.order.domain.entity.*;
import com.messi.system.order.domain.request.OrderPriceDTO;
import com.messi.system.order.domain.request.SubmitOrderReq;
import com.messi.system.order.remote.CouponRemote;
import com.messi.system.order.remote.MarketRemote;
import com.messi.system.order.remote.ProductRemote;
import com.messi.system.order.remote.UserRemote;
import com.messi.system.order.service.submit.SubmitOrder;
import com.messi.system.rocketmq.DelayTimeLevel;
import com.messi.system.rocketmq.producer.MqProducer;
import com.messi.system.utils.CheckParamUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.messi.system.order.constants.OrderSymbolConstants.*;

/**
 * 提交订单业务的抽象模板
 */
@Slf4j
public abstract class AbstractSubmitOrder implements SubmitOrder {

    @Autowired
    protected OrderConverter orderConverter;

    @Autowired
    protected UserRemote userRemote;

    @Autowired
    protected MarketRemote marketRemote;

    @Autowired
    protected CouponRemote couponRemote;

    @Autowired
    protected ProductRemote productRemote;

    @Autowired
    protected OrderDAO orderDAO;

    @Autowired
    protected OrderItemInfoDAO orderItemInfoDAO;

    @Autowired
    protected OrderPriceDAO orderPriceDAO;

    @Autowired
    protected OrderPriceDetailsDAO orderPriceDetailsDAO;

    @Autowired
    protected OrderStatusRecordDAO orderStatusRecordDAO;

    @Autowired
    protected MqProducer mqProducer;

    //  初始的抵扣金额
    private static Integer DEDUCTION_PRICE = 0;

    /**
     * 默认订单总金额，用于金额统计
     */
    protected Integer orderTotalPrice = 0;

    public abstract ResResult<SubmitOrderDTO> submitOrder(SubmitOrderReq submitOrderReq);

    //  业务上定义未支付取消订单延迟30m
    //  这里为了方便测试，只延迟10s
    protected void sendDelayCancelOrder(Order order) {
        mqProducer.sendMessage(RocketMqConstants.NOT_PAID_CANCEL_ORDER_TOPIC,
                JSONObject.toJSONString(order), DelayTimeLevel.DELAYED_10s);
    }

    protected void generateOrderAndOrderItemId(SubmitOrderReq submitOrderReq) {
        //  订单id
        //  分布式id
        String orderId = generateDistributedId(submitOrderReq.getUserId());
        submitOrderReq.setOrderId(orderId);

        //  订单条目id
        int tempNo = 0;
        for (SubmitOrderReq.OrderItemReq orderItemReq : submitOrderReq.getOrderItemReqList()) {
            tempNo++;
            orderItemReq.setOrderItemId(orderId + tempNo);
        }

    }

    /**
     * 生成业务分布式id
     * 业务分布式id：订单id基于时间+业务id做组合，订单条目id基于订单id的基础上，做编号的升序
     * 优点：实现简单，不用考虑另外封装工具类，不考虑时钟回拨，没有额外成本
     * 缺点：有固定的业务场景，在某些高并发场景下需要不断下探时间的精准度，否则可能会有重复的问题，需要另外的并发控制。
     *
     * @param userId 用户id 符号
     * @return distributeId 组装的分布式订单id
     */
    private String generateDistributedId(String userId) {
        //  基于单位是毫秒的时间戳生成时间编号
        String dateFormat = DatePattern.NORM_DATETIME_MS_FORMAT.format(new Date());

        //  拼接业务用户id,格式：年+月+日+时+分+秒+毫秒+业务id
        return dateFormat
                .replace(SYMBOL_MIDLINE, SYMBOL_NONE)
                .replace(SYMBOL_SPOT, SYMBOL_NONE)
                .replace(SYMBOL_BLANK, SYMBOL_NONE)
                .replace(SYMBOL_COLON, SYMBOL_NONE) + userId;
    }

    protected ResResult<SubmitOrderDTO> submitResponse(SubmitOrderReq submitOrderReq) {
        SubmitOrderDTO submitOrderDTO = new SubmitOrderDTO();
        submitOrderDTO.setOrderId(submitOrderReq.getOrderId());
        return ResResult.buildSuccess(submitOrderDTO);
    }

    protected Order buildOrder(SubmitOrderReq submitOrderReq, Integer finalPrice) {
        OrderBuilder orderBuilder = new OrderBuilder(submitOrderReq, finalPrice);
        return orderBuilder
                .buildOrder()
                .buildOrderItem()
                .buildOrderPrice()
                .buildOrderPriceItem()
                .buildNewOrderStatusRecord()
                .build();
    }

    protected void saveOrder(Order order) {

        OrderInfoDO orderInfoDO = order.getOrderInfoDO();

        List<OrderItemInfoDO> orderItemList = order.getOrderItemList();

        OrderPriceDO orderPriceDO = order.getOrderPriceDO();

        List<OrderPriceDetailsDO> orderOrderPriceItemList = order.getOrderPriceItemList();

        OrderStatusRecordDO orderStatusRecordDO = order.getOrderStatusRecordDO();

        orderDAO.save(orderInfoDO);

        orderItemInfoDAO.saveBatch(orderItemList);

        orderPriceDAO.save(orderPriceDO);

        orderPriceDetailsDAO.saveBatch(orderOrderPriceItemList);

        orderStatusRecordDAO.save(orderStatusRecordDO);

    }

    protected CheckCouponDTO checkCoupon(SubmitOrderReq submitOrderReq) {
        String couponId = submitOrderReq.getCouponId();
        String userId = submitOrderReq.getUserId();

        return couponRemote.getCoupon(couponId, userId);
    }

    protected OrderPriceDTO statisticsOrderPrice(List<CheckOrderPriceDTO> calculatedOrderPrices) {
        OrderPriceDTO orderPriceDTO = new OrderPriceDTO();

        for (CheckOrderPriceDTO calculatedOrderPrice : calculatedOrderPrices) {
            this.orderTotalPrice += calculatedOrderPrice.getCalculatePrice();
        }

        orderPriceDTO.setOrderTotalPrice(this.orderTotalPrice);
        return orderPriceDTO;
    }

    protected Integer deductionOrderPrice(CheckCouponDTO checkCouponDTO, List<CheckOrderPriceDTO> calculatedOrderPrices) {

        //  计算订单原总价
        OrderPriceDTO orderPriceDTO = statisticsOrderPrice(calculatedOrderPrices);
        Integer orderTotalPrice = orderPriceDTO.getOrderTotalPrice();

        //  优惠券抵扣金额
        if (checkCouponDTO != null) {
            DEDUCTION_PRICE = preferentialOrderPrice(checkCouponDTO);
        }

        //  最终价格 = 订单原总价 - 优惠券抵扣金额
        BigDecimal finalPriceBigDecimal = new BigDecimal(orderTotalPrice).subtract(new BigDecimal(DEDUCTION_PRICE));
        return finalPriceBigDecimal.intValue();
    }

    /**
     * 优惠券抵扣的金额
     *
     * @param checkCouponDTO checkCouponDTO
     * @return 优惠的金额
     */
    protected Integer preferentialOrderPrice(CheckCouponDTO checkCouponDTO) {
        return couponRemote.getCouponDeductionPrice(checkCouponDTO);
    }

    /**
     * 前台传递的订单价格 和 营销中心计算的订单价格 做比对
     *
     * @param orderItemPriceList    前台传递的订单价格
     * @param calculatedOrderPrices 营销中心计算的订单价格
     */
    protected void checkOrderPrice(List<SubmitOrderReq.OrderItemPrice> orderItemPriceList,
                                   List<CheckOrderPriceDTO> calculatedOrderPrices) {

        //  List -> Map
        Map<String, Integer> frontPriceMap = orderItemPriceList.stream().collect(
                Collectors.toMap(
                        SubmitOrderReq.OrderItemPrice::getSkuId,
                        SubmitOrderReq.OrderItemPrice::getSalePrice)
        );

        Map<String, Integer> calculatedPriceMap = calculatedOrderPrices.stream().collect(
                Collectors.toMap(
                        CheckOrderPriceDTO::getSkuId,
                        CheckOrderPriceDTO::getCalculatePrice
                )
        );

        for (Map.Entry<String, Integer> frontPriceEntry : frontPriceMap.entrySet()) {
            Integer frontSkuPrice = frontPriceEntry.getValue();
            Integer calculateSkuPrice = calculatedPriceMap.get(frontPriceEntry.getKey());
            if (!(frontSkuPrice.equals(calculateSkuPrice))) {
                log.error("订单价格计算错误,sku:{},前台价格:{},营销中心计算的价格:{}",
                        frontPriceEntry.getKey(), frontSkuPrice, calculateSkuPrice);
                throw new RuntimeException("订单价格计算错误");
            }
        }
    }

    /**
     * 调用营销中心，计算订单价格
     *
     * @param submitOrderReq 订单入参
     * @return 营销中心计算的订单价格
     */
    protected List<CheckOrderPriceDTO> calculateOrderPrice(SubmitOrderReq submitOrderReq) {

        List<SubmitOrderReq.OrderProduct> orderProductList = submitOrderReq.getOrderProductList();

        List<MarketPriceReq> marketPriceReqList = orderConverter.orderPriceToMarketPrice(orderProductList);

        return marketRemote.calculateOrderPrice(marketPriceReqList);

    }

    /**
     * 检查商品
     *
     * @param submitOrderReq 入参
     */
    protected void checkProduct(SubmitOrderReq submitOrderReq) {
        List<SubmitOrderReq.OrderProduct> orderProductList = submitOrderReq.getOrderProductList();
        if (!productRemote.checkProduct(orderProductList)) {
            throw new RuntimeException("商品信息核验未通过");
        }
    }

    /**
     * 检查用户
     *
     * @param submitOrderReq 入参
     */
    protected void checkUser(SubmitOrderReq submitOrderReq) {
        if (!userRemote.checkUserValidity(submitOrderReq.getUserId())) {
            throw new RuntimeException("用户核验未通过");
        }
    }

    /**
     * 检查入参参数
     *
     * @param submitOrderReq 入参
     */
    protected void checkReqParam(SubmitOrderReq submitOrderReq) {

        CheckParamUtil.checkParamNotEmpty(submitOrderReq.getChannel(), "渠道标识不能是空");

        CheckParamUtil.checkParamNotEmpty(submitOrderReq.getOrderType(), "订单类型不能是空");

        CheckParamUtil.checkStringNotEmpty(submitOrderReq.getSellerId(), "卖家id不能是空");

        CheckParamUtil.checkStringNotEmpty(submitOrderReq.getUserId(), "买家id不能是空");

        CheckParamUtil.checkParamNotEmpty(submitOrderReq.getTotalAmount(), "订单总金额不能是空");

        CheckParamUtil.checkStringNotEmpty(submitOrderReq.getProvinceNo(), "省编号不能是空");

        CheckParamUtil.checkStringNotEmpty(submitOrderReq.getCityNo(), "市编号不能是空");

        CheckParamUtil.checkStringNotEmpty(submitOrderReq.getAreaNo(), "区编号不能是空");

        CheckParamUtil.checkStringNotEmpty(submitOrderReq.getDetailAddress(), "详细地址不能是空");

        CheckParamUtil.checkStringNotEmpty(submitOrderReq.getRecipientName(), "收货人姓名不能是空");

        CheckParamUtil.checkStringNotEmpty(submitOrderReq.getRecipientPhone(), "收货人联系电话不能是空");

        for (SubmitOrderReq.OrderItemReq orderItemReq : submitOrderReq.getOrderItemReqList()) {

            CheckParamUtil.checkStringNotEmpty(orderItemReq.getProductId(), "商品id不能是空");

            CheckParamUtil.checkStringNotEmpty(orderItemReq.getSkuId(), "sku id不能是空");

            CheckParamUtil.checkParamNotEmpty(orderItemReq.getSaleNum(), "销售数量不能是空");
        }

        for (SubmitOrderReq.OrderItemPrice orderItemPrice : submitOrderReq.getOrderItemPriceList()) {

            CheckParamUtil.checkParamNotEmpty(orderItemPrice.getSalePrice(), "销售单价不能是空");
        }

        for (SubmitOrderReq.OrderProduct orderProduct : submitOrderReq.getOrderProductList()) {

            CheckParamUtil.checkStringNotEmpty(orderProduct.getProductId(), "订单商品信息里的商品id不能是空");

            CheckParamUtil.checkStringNotEmpty(orderProduct.getSkuId(), "订单商品信息里的skuId不能是空");

            CheckParamUtil.checkParamNotEmpty(orderProduct.getSaleNum(), "订单商品信息里的销售数量不能是空");

            CheckParamUtil.checkParamNotEmpty(orderProduct.getSkuPrice(), "订单商品信息里的sku销售单价不能是空");

        }
    }

}

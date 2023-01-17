package com.messi.system.order.service.impl.submit;

import com.alibaba.fastjson.JSONObject;
import com.messi.system.constant.RocketMqConstants;
import com.messi.system.core.ResResult;
import com.messi.system.market.domain.dto.CouponDTO;
import com.messi.system.order.domain.builder.Order;
import com.messi.system.order.domain.dto.CheckCouponDTO;
import com.messi.system.order.domain.dto.CheckOrderPriceDTO;
import com.messi.system.order.domain.dto.SubmitOrderDTO;
import com.messi.system.order.domain.request.SubmitOrderReq;
import com.messi.system.order.rocketmq.msg.SubmitOrderTransactionMsgDeductionReq;
import com.messi.system.order.service.submit.SubmitOrderV2;
import com.messi.system.product.domain.request.ProductReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提交订单 v2
 * 使用rocketmq事务消息做分布式微服务系统之间的解耦
 * 优点：
 * 弊端：业务链路太长，事务消息要做事务回查，如果最后一组事务消息回滚就会影响到前面的全部链路
 */
@Slf4j
@Service
public class SubmitOrderServiceV2Impl extends AbstractSubmitOrder implements SubmitOrderV2 {

    @Override
    public ResResult<SubmitOrderDTO> submitOrder(SubmitOrderReq submitOrderReq) {

        //  1、入参检查
        super.checkReqParam(submitOrderReq);

        //  2、调用用户中心，验证用户
        super.checkUser(submitOrderReq);

        //  3、调用商品中心，验证商品
        super.checkProduct(submitOrderReq);

        //  4、调用营销中心，验证优惠券
        CheckCouponDTO checkCouponDTO = super.checkCoupon(submitOrderReq);

        //  5、调用营销中心，计算订单价格
        List<CheckOrderPriceDTO> calculatedOrderPrices = super.calculateOrderPrice(submitOrderReq);

        //  6、生成订单和条目id
        super.generateOrderAndOrderItemId(submitOrderReq);

        //  7、验证订单价格
        super.checkOrderPrice(submitOrderReq.getOrderItemPriceList(), calculatedOrderPrices);

        //  8、做订单价格优惠扣减
        Integer finalPrice = super.deductionOrderPrice(checkCouponDTO, calculatedOrderPrices);

        //  9、发送事务消息，做扣减操作
        this.sendTransactionMsgDeduction(checkCouponDTO, submitOrderReq.getOrderProductList());

        //  10、构造订单
        Order order = super.buildOrder(submitOrderReq, finalPrice);

        //  11、保存订单
        super.saveOrder(order);

        //  12、发送延迟关单MQ
        super.sendDelayCancelOrder(order);

        //  13、返回响应信息
        log.info("v2创建订单完成,orderId:{}", order.getOrderInfoDO().getOrderId());
        return super.submitResponse(submitOrderReq);

    }

    /**
     * 发送事务消息，一条消息，多组消费
     * 消费到消息后，微服务各自执行自己的业务
     *
     * @param checkCouponDTO   优惠券信息
     * @param orderProductList 商品信息
     */
    public void sendTransactionMsgDeduction(CheckCouponDTO checkCouponDTO, List<SubmitOrderReq.OrderProduct> orderProductList) {
        SubmitOrderTransactionMsgDeductionReq msgDeductionReq = new SubmitOrderTransactionMsgDeductionReq();

        //  扣优惠券
        CouponDTO couponDTO = orderConverter.checkCouponDTO2CouponDTO(checkCouponDTO);
        msgDeductionReq.setCouponDTO(couponDTO);

        //  扣库存
        List<ProductReq> productReqList = orderConverter.orderProductList2ProductList(orderProductList);
        msgDeductionReq.setProductReqList(productReqList);

        //  发送mq消息
        mqProducer.sendMessage(
                RocketMqConstants.NOT_PAID_CANCEL_ORDER_DEDUCTION_TRANSACTION_TOPIC, JSONObject.toJSONString(msgDeductionReq)
        );

    }

}

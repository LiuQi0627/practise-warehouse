package com.messi.system.market.rocketmq.listener;

import com.alibaba.fastjson.JSONObject;
import com.messi.system.constant.RocketMqConstants;
import com.messi.system.market.domain.dto.CouponDTO;
import com.messi.system.market.enums.MarketCouponEnums;
import com.messi.system.market.rocketmq.msg.ReceiveDeductionCouponMsgReq;
import com.messi.system.market.service.CouponService;
import com.messi.system.rocketmq.listener.AbstractMessageListenerConcurrently;
import com.messi.system.rocketmq.producer.MqProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 提交订单扣减优惠券的消息监听器
 */
@Slf4j
@Component
public class DeductionCouponListener extends AbstractMessageListenerConcurrently {

    private final CouponService couponService;

    public DeductionCouponListener(CouponService couponService) {
        this.couponService = couponService;
    }

    @Autowired
    private MqProducer mqProducer;

    @Override
    public ConsumeConcurrentlyStatus onMessage(String message) {
        ReceiveDeductionCouponMsgReq msgReq = JSONObject.parseObject(message, ReceiveDeductionCouponMsgReq.class);
        CouponDTO couponDTO = msgReq.getCouponDTO();
        log.info("取得消息参数：{}", couponDTO);

        try {
            //  设置事务消息
            TransactionMQProducer transactionProducer = mqProducer.getTransactionProducer();
            transactionProducer.setTransactionListener(new TransactionListener() {
                //  提交本地事务
                @Override
                public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                    couponService.deductionCoupon(couponDTO);
                    return LocalTransactionState.COMMIT_MESSAGE;
                }

                //  回滚
                @Override
                public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                    String couponId = couponDTO.getCouponId();
                    String userId = couponDTO.getUserId();
                    CouponDTO coupon = couponService.getCoupon(couponId, userId);
                    if (coupon == null
                            || coupon.getUsageTime() == null
                            || coupon.getUseStatus().equals(MarketCouponEnums.COUPON_IS_NOT_USED.getCode())) {
                        return LocalTransactionState.COMMIT_MESSAGE;
                    }
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            });

            //  发送事务消息
            Message msg = new Message(RocketMqConstants.DEDUCTION_COUPON_TOPIC, JSONObject.toJSONString(couponDTO).getBytes(StandardCharsets.UTF_8));
            transactionProducer.sendMessageInTransaction(msg, null);

        } catch (MQClientException e) {
            log.error("发送事务消息失败,失败原因:{}", e.getErrorMessage());
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }

        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}

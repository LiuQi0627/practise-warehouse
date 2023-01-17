package com.messi.system.product.rocketmq.listener;

import com.alibaba.fastjson.JSONObject;
import com.messi.system.constant.RocketMqConstants;
import com.messi.system.product.domain.request.ProductReq;
import com.messi.system.product.rocketmq.msg.ReceiveDeductionStockMsgReq;
import com.messi.system.product.service.ProductService;
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
import java.util.List;

/**
 * 提交订单扣减库存的消息监听器
 */
@Slf4j
@Component
public class DeductionStockListener extends AbstractMessageListenerConcurrently {

    private final ProductService productService;

    public DeductionStockListener(ProductService productService) {
        this.productService = productService;
    }

    @Autowired
    private MqProducer mqProducer;

    @Override
    public ConsumeConcurrentlyStatus onMessage(String message) {
        ReceiveDeductionStockMsgReq msgReq = JSONObject.parseObject(message, ReceiveDeductionStockMsgReq.class);
        List<ProductReq> productReqList = msgReq.getProductReqList();
        log.info("取得消息参数：{}", productReqList);

        try {
            //  设置事务消息
            TransactionMQProducer transactionProducer = mqProducer.getTransactionProducer();
            transactionProducer.setTransactionListener(new TransactionListener() {
                //  提交本地事务
                @Override
                public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                    productService.deductionStock(productReqList);
                    return LocalTransactionState.COMMIT_MESSAGE;
                }

                //  回滚
                @Override
                public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                    if (productService.compensationStock(productReqList)) {
                        return LocalTransactionState.COMMIT_MESSAGE;
                    }
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            });

            //  发送事务消息
            Message msg = new Message(RocketMqConstants.DEDUCTION_STOCK_TOPIC, JSONObject.toJSONString(productReqList).getBytes(StandardCharsets.UTF_8));
            transactionProducer.sendMessageInTransaction(msg, null);

        } catch (MQClientException e) {
            log.error("发送事务消息失败,失败原因:{}", e.getErrorMessage());
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }

        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}

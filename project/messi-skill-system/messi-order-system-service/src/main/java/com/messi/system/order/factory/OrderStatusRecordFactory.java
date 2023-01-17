package com.messi.system.order.factory;

import com.messi.system.order.domain.entity.OrderStatusRecordDO;
import org.springframework.stereotype.Component;

/**
 * 订单状态变更记录工厂
 */
@Component
public class OrderStatusRecordFactory {

    /**
     * 创建变更记录
     */
    public OrderStatusRecordDO create(String orderId, Integer prevStatus, Integer curStatus) {
        OrderStatusRecordDO record = new OrderStatusRecordDO();
        record.setOrderId(orderId);
        record.setPrevStatus(prevStatus);
        record.setCurStatus(curStatus);

        return record;
    }
}

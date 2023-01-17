package com.messi.system.product.rocketmq.msg;

import com.messi.system.product.domain.request.ProductReq;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 接收扣减库存的消息
 */
@Data
public class ReceiveDeductionStockMsgReq implements Serializable {
    private static final long serialVersionUID = 8230030079766539657L;

    /**
     * 商品信息
     */
    List<ProductReq> productReqList;
}

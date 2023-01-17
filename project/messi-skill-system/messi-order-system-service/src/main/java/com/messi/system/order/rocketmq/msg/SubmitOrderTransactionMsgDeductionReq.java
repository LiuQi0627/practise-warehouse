package com.messi.system.order.rocketmq.msg;

import com.messi.system.market.domain.dto.CouponDTO;
import com.messi.system.product.domain.request.ProductReq;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 提交订单 封装需要扣减数据的事务消息入参
 */
@Data
public class SubmitOrderTransactionMsgDeductionReq implements Serializable {
    private static final long serialVersionUID = 5754190101492186498L;

    /**
     * 提交订单 优惠券信息
     */
    private CouponDTO couponDTO;

    /**
     * 提交订单 商品信息
     */
    List<ProductReq> productReqList;
}

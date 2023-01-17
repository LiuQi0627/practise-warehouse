package com.messi.system.order.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 提交订单结果DTO
 */
@Data
public class SubmitOrderDTO implements Serializable {
    private static final long serialVersionUID = 3647920404472387539L;

    /**
     * 订单号
     */
    private String orderId;
}

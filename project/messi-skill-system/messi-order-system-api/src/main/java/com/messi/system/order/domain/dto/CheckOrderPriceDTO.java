package com.messi.system.order.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单系统检查订单价格DTO
 */
@Data
public class CheckOrderPriceDTO implements Serializable {
    private static final long serialVersionUID = -2586292287916130347L;

    /**
     * sku id
     */
    private String skuId;

    /**
     * 计算后的价格 = 单价 * 数量
     */
    private Integer calculatePrice;

}

package com.messi.sanp.up.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 抢购入参
 */
@Data
public class SnapupRequest implements Serializable {
    private static final long serialVersionUID = -3871327130177873835L;

    /**
     * 已审核通过后的促销活动id
     */
    public Long promotionId;

    /**
     * sku id
     */
    public String skuId;

    /**
     * 用户id
     */
    public String userId;
}

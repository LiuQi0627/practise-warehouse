package com.messi.system.order.domain.query;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 订单分页查询条件
 */
@Data
public class OrderQueryCondition implements Serializable {
    private static final long serialVersionUID = 3527823566454521482L;

    /**
     * 查询订单状态，可多选
     */
    private Set<Integer> orderStatuses;

    /**
     * 卖家id，可多选
     */
    private Set<String> sellerIds;

    /**
     * 买家id，可多选
     */
    private Set<String> userIds;

    /**
     * 订单支付类型，可多选
     */
    private Set<Integer> orderPayTypes;

    /**
     * 订单评价状态，可多选
     */
    private Set<Integer> appraiseStatuses;

    /**
     * 查询页码,默认第一页
     */
    private Integer pageNo = 1;

    /**
     * 每页查询的数量
     */
    private Integer pageSize = 20;
}

package com.messi.snap.up.reactor.context;

import com.messi.snap.up.reactor.async.AsyncContext;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 抢购事件上下文
 */
@Getter
@Setter
public class SnapupContext implements Serializable, AsyncContext {
    private static final long serialVersionUID = -5517967182360175428L;

    /**
     * 已审核通过后的促销活动id
     */
    private Long promotionId;

    /**
     * sku id
     */
    private String skuId;

    /**
     * 抢购数量限制
     */
    private int snapupNum = 1;

    /**
     * 用户id
     */
    public String userId;

    /**
     * 异步上下文
     */
    private javax.servlet.AsyncContext asyncContext;

    /**
     * 保存的是执行redis实例的序列
     */
    private Long sequence;
}

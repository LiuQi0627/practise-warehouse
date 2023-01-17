package com.messi.system.order.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;

import com.messi.system.mybatis.BaseEntity;
import lombok.Data;

/**
 * 订单状态变更记录表
 */
@Data
@TableName("order_status_record")
public class OrderStatusRecordDO extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单号
     */
    @TableField(value = "order_id", fill = FieldFill.DEFAULT)
    private String orderId;

    /**
     * 订单的前一个状态
     */
    @TableField(value = "prev_status", fill = FieldFill.DEFAULT)
    private Integer prevStatus;

    /**
     * 订单的当前状态
     */
    @TableField(value = "cur_status", fill = FieldFill.DEFAULT)
    private Integer curStatus;


}

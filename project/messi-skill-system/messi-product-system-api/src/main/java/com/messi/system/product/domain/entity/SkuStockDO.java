package com.messi.system.product.domain.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.messi.system.mybatis.BaseEntity;
import lombok.*;

/**
 * sku库存表
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sku_stock")
public class SkuStockDO extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * skuid
     */
    @TableField(value = "sku_id", fill = FieldFill.DEFAULT)
    private String skuId;

    /**
     * 总库存
     */
    @TableField(value = "total_stock", fill = FieldFill.DEFAULT)
    private Integer totalStock;

    /**
     * 已售库存
     */
    @TableField(value = "saled_stock", fill = FieldFill.DEFAULT)
    private Integer saledStock;

}
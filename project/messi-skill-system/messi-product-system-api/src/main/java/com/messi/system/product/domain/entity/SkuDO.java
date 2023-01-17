package com.messi.system.product.domain.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.messi.system.mybatis.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 商品表
 */
@Getter
@Setter
@TableName("sku_info")
public class SkuDO extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 商品id
     */
    @TableField(value = "product_id", fill = FieldFill.DEFAULT)
    private String productId;

    /**
     * skuid
     */
    @TableField(value = "sku_id", fill = FieldFill.DEFAULT)
    private String skuId;

    /**
     * 商品名称
     */
    @TableField(value = "sku_name", fill = FieldFill.DEFAULT)
    private String skuName;

    /**
     * sku单价 单位：分
     */
    @TableField(value = "sku_price", fill = FieldFill.DEFAULT)
    private Integer skuPrice;

}

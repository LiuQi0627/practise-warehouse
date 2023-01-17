package com.messi.system.product.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.messi.system.mybatis.BaseDAO;
import com.messi.system.product.domain.entity.SkuDO;
import com.messi.system.product.mapper.SkuMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * 操作product DAO
 */
@Repository
public class SkuDAO extends BaseDAO<SkuMapper, SkuDO> {

    /**
     * 根据商品id和sku查询商品信息
     */
    public SkuDO getSku(String productId, String skuId) {
        LambdaQueryWrapper<SkuDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuDO::getProductId, productId);
        queryWrapper.eq(SkuDO::getSkuId, skuId);
        return getOne(queryWrapper);
    }
}

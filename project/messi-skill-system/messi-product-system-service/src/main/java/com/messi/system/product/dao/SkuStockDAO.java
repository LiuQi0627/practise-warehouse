package com.messi.system.product.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.messi.system.mybatis.BaseDAO;
import com.messi.system.product.domain.entity.SkuStockDO;
import com.messi.system.product.mapper.SkuStockMapper;
import org.springframework.stereotype.Repository;

/**
 * 操作sku DAO
 */
@Repository
public class SkuStockDAO extends BaseDAO<SkuStockMapper, SkuStockDO> {

    /**
     * 根据sku id 查询 sku stock
     *
     * @param skuId sku id
     * @return SkuStockDO
     */
    public SkuStockDO getSkuStock(String skuId) {
        LambdaQueryWrapper<SkuStockDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuStockDO::getSkuId, skuId);
        return getOne(queryWrapper);
    }
}

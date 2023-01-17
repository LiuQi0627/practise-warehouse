package com.messi.system.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.messi.system.product.domain.entity.SkuStockDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * sku库存表 Mapper 接口
 */
@Mapper
public interface SkuStockMapper extends BaseMapper<SkuStockDO> {

}

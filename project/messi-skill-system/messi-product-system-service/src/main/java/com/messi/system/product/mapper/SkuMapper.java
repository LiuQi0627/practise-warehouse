package com.messi.system.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.messi.system.product.domain.entity.SkuDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品表 Mapper 接口
 */
@Mapper
public interface SkuMapper extends BaseMapper<SkuDO> {

}

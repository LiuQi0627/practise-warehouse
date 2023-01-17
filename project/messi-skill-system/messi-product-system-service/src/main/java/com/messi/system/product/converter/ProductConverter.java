package com.messi.system.product.converter;

import com.messi.system.product.domain.dto.SkuDTO;
import com.messi.system.product.domain.entity.SkuDO;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * 商品对象转换器
 */
@Component
@Mapper(componentModel = "spring")
public interface ProductConverter {

    /**
     * SkuDTO 转换 SkuDO
     */
    SkuDTO skuDTO2SkuDO(SkuDO skuDO);
}

package com.messi.system.market.service.impl;

import com.messi.system.market.domain.dto.MarketCheckOrderPriceDTO;
import com.messi.system.market.domain.request.MarketPriceReq;
import com.messi.system.market.remote.ProductRemote;
import com.messi.system.market.service.MarketService;
import com.messi.system.product.domain.dto.SkuDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * 营销service实现类
 */
@Slf4j
@Service
public class MarketServiceImpl implements MarketService {

    @Autowired
    private ProductRemote productRemote;

    /**
     * 营销中心计算订单价格
     *
     * @return marketPriceReqList 接收订单系统传递的计算价格入参
     */
    @Override
    public List<MarketCheckOrderPriceDTO> calculateOrderPrice(List<MarketPriceReq> marketPriceReqList) {
        List<MarketCheckOrderPriceDTO> marketCheckOrderPriceDTOList = new LinkedList<>();
        for (MarketPriceReq marketPriceReq : marketPriceReqList) {
            String skuId = marketPriceReq.getSkuId();
            String productId = marketPriceReq.getProductId();

            SkuDTO skuDTO = productRemote.getSku(productId, skuId);
            if (skuDTO == null) {
                throw new RuntimeException("sku不存在");
            }

            Integer calculatedPrice = skuDTO.getSkuPrice() * marketPriceReq.getSaleNum();
            MarketCheckOrderPriceDTO marketCheckOrderPriceDTO = new MarketCheckOrderPriceDTO(skuId, calculatedPrice);

            marketCheckOrderPriceDTOList.add(marketCheckOrderPriceDTO);
        }
        return marketCheckOrderPriceDTOList;
    }
}

package com.messi.system.market.api.impl;

import com.messi.system.market.api.MarketServiceApi;
import com.messi.system.market.domain.dto.MarketCheckOrderPriceDTO;
import com.messi.system.market.domain.request.MarketPriceReq;
import com.messi.system.market.service.MarketService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 营销 dubbo api 实现类
 */
@DubboService
public class MarketServiceApiImpl implements MarketServiceApi {

    @Autowired
    private MarketService marketService;

    /**
     * 营销中心计算订单价格
     *
     * @return marketPriceReqList 接收订单系统传递的计算价格入参
     */
    @Override
    public List<MarketCheckOrderPriceDTO> calculateOrderPrice(List<MarketPriceReq> marketPriceReqList) {
        return marketService.calculateOrderPrice(marketPriceReqList);
    }
}

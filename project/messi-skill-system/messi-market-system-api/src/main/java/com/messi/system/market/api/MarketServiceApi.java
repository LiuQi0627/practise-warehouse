package com.messi.system.market.api;

import com.messi.system.market.domain.dto.MarketCheckOrderPriceDTO;
import com.messi.system.market.domain.request.MarketPriceReq;

import java.util.List;
import java.util.Map;

/**
 * market service对外暴露的dubbo api接口
 */
public interface MarketServiceApi {

    /**
     * 营销中心计算订单价格
     *
     * @return marketPriceReqList 接收订单系统传递的计算价格入参
     */
    List<MarketCheckOrderPriceDTO> calculateOrderPrice(List<MarketPriceReq> marketPriceReqList);
}

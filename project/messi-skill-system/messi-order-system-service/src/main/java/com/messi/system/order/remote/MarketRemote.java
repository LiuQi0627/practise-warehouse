package com.messi.system.order.remote;

import com.messi.system.market.api.MarketServiceApi;
import com.messi.system.market.domain.dto.MarketCheckOrderPriceDTO;
import com.messi.system.market.domain.request.MarketPriceReq;
import com.messi.system.order.converter.OrderConverter;
import com.messi.system.order.domain.dto.CheckOrderPriceDTO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单系统封装的 调用营销服务远程接口 的组件
 */
@Component
public class MarketRemote {

    @DubboReference(retries = 0)
    public MarketServiceApi marketServiceApi;

    @Autowired
    private OrderConverter orderConverter;

    /**
     * 计算订单价格
     */
    public List<CheckOrderPriceDTO> calculateOrderPrice(List<MarketPriceReq> marketPriceReqList) {
        List<MarketCheckOrderPriceDTO> marketCheckOrderPriceDTOList = marketServiceApi.calculateOrderPrice(marketPriceReqList);
        return orderConverter.orderPriceConversion(marketCheckOrderPriceDTOList);
    }

}

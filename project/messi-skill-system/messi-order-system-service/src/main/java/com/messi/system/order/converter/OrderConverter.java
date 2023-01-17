package com.messi.system.order.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.messi.system.market.domain.dto.CouponDTO;
import com.messi.system.order.domain.dto.OrderDetailDTO;
import com.messi.system.order.domain.dto.OrderQueryDTO;
import com.messi.system.order.domain.entity.OrderInfoDO;
import com.messi.system.order.domain.request.CancelOrderReq;
import com.messi.system.order.elasticsearch.index.OrderEsIndex;
import com.messi.system.order.rocketmq.msg.NotPaidCancelOrderMsgReq;
import com.messi.system.product.domain.dto.DeductionStockProductDTO;
import com.messi.system.market.domain.dto.MarketCheckOrderPriceDTO;
import com.messi.system.market.domain.request.MarketPriceReq;
import com.messi.system.order.domain.dto.CheckCouponDTO;
import com.messi.system.order.domain.dto.CheckOrderPriceDTO;
import com.messi.system.order.domain.request.SubmitOrderReq;
import com.messi.system.product.domain.request.ProductReq;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单对象格式转换
 */
@Component
@Mapper(componentModel = "spring")
public interface OrderConverter {

    /**
     * 订单价格 转换 营销价格
     */
    List<MarketPriceReq> orderPriceToMarketPrice(List<SubmitOrderReq.OrderProduct> orderProductList);

    /**
     * 营销计算后的订单价格 转换 订单中心的订单价格
     */
    List<CheckOrderPriceDTO> orderPriceConversion(List<MarketCheckOrderPriceDTO> marketCheckOrderPriceDTOList);

    /**
     * CouponDTO 转换 CheckCouponDTO
     */
    CheckCouponDTO couponDTO2CheckCouponDTO(CouponDTO couponDTO);

    /**
     * CouponDTO 转换 CheckCouponDTO
     */
    CouponDTO checkCouponDTO2CouponDTO(CheckCouponDTO checkCouponDTO);

    /**
     * List<SubmitOrderReq.OrderProduct> 转换 List<ProductReq>
     */
    List<ProductReq> orderProductList2CheckProductList(List<SubmitOrderReq.OrderProduct> orderProductList);

    /**
     * List<SubmitOrderReq.OrderProduct> 转换 List<ProductReq>
     */
    List<ProductReq> orderProductList2ProductList(List<SubmitOrderReq.OrderProduct> orderProductList);

    /**
     * SubmitOrderReq.OrderProduct 转换 DeductionStockProductDTO
     */
    DeductionStockProductDTO orderProduct2DeductionStockProductDTO(SubmitOrderReq.OrderProduct orderProduct);

    /**
     * Page<OrderInfoDO> 转换 Page<OrderQueryDTO>
     */
    Page<OrderQueryDTO> orderInfoDOPage2OrderQueryDTOPage(Page<OrderInfoDO> orderInfoDOPage);

    /**
     * NotPaidCancelOrderMsgReq 转换 CancelOrderReq
     */
    CancelOrderReq msgReq2CancelOrderReq(NotPaidCancelOrderMsgReq notPaidCancelOrderMsgReq);

}

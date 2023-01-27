package com.messi.system.order.service.impl.submit;

import com.messi.system.core.ResResult;
import com.messi.system.order.domain.builder.Order;
import com.messi.system.order.domain.dto.CheckCouponDTO;
import com.messi.system.order.domain.dto.CheckOrderPriceDTO;
import com.messi.system.order.domain.dto.SubmitOrderDTO;
import com.messi.system.order.domain.request.SubmitOrderReq;
import com.messi.system.order.service.submit.SubmitOrderV1;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提交订单 v1
 * 使用分布式事务，做保存订单、扣优惠券、扣库存操作
 * 优点：
 * 弊端：分布式事务在系统中加全局锁，业务处理响应时间超级慢
 */
@Slf4j
@Service
public class SubmitOrderServiceV1Impl extends AbstractSubmitOrder implements SubmitOrderV1 {

    @Override
    @GlobalTransactional
    public ResResult<SubmitOrderDTO> submitOrder(SubmitOrderReq submitOrderReq) {

        //  1、入参检查
        super.checkReqParam(submitOrderReq);

        //  2、调用用户中心，验证用户
        super.checkUser(submitOrderReq);

        //  3、调用商品中心，验证商品
        super.checkProduct(submitOrderReq);

        //  4、调用营销中心，验证优惠券
        CheckCouponDTO checkCouponDTO = super.checkCoupon(submitOrderReq);

        //  5、调用营销中心，计算订单价格
        List<CheckOrderPriceDTO> calculatedOrderPrices = super.calculateOrderPrice(submitOrderReq);

        //  6、生成订单和条目id
        super.generateOrderAndOrderItemId(submitOrderReq);

        //  7、验证订单价格
        super.checkOrderPrice(submitOrderReq.getOrderItemPriceList(), calculatedOrderPrices);

        //  8、做订单价格优惠扣减
        Integer finalPrice = super.deductionOrderPrice(checkCouponDTO, calculatedOrderPrices);

        //  9、分布式事务扣优惠券
        this.distributedTransactionDeductionCoupon(checkCouponDTO);

        //  10、分布式事务扣库存
        this.distributedTransactionDeductionStock(submitOrderReq.getOrderProductList());

        //  11、构造订单
        Order order = super.buildOrder(submitOrderReq, finalPrice);

        //  12、保存订单
        super.saveOrder(order);

        //  13、发送延迟关单MQ
        super.sendDelayCancelOrder(order);

        //  14、返回响应信息
        log.info("v1创建订单完成,orderId:{}", order.getOrderInfoDO().getOrderId());
        return super.submitResponse(submitOrderReq);

    }

    @Override
    public void distributedTransactionDeductionCoupon(CheckCouponDTO checkCouponDTO) {
        couponRemote.deductionCoupon(checkCouponDTO);
    }

    @Override
    public void distributedTransactionDeductionStock(List<SubmitOrderReq.OrderProduct> orderProductList) {
        productRemote.deductionStock(orderProductList);
    }
}

package com.messi.system.order.service.impl.submit;

import com.messi.system.consistency.annotation.ConsistencyFramework;
import com.messi.system.core.ResResult;
import com.messi.system.order.domain.builder.Order;
import com.messi.system.order.domain.dto.CheckCouponDTO;
import com.messi.system.order.domain.dto.CheckOrderPriceDTO;
import com.messi.system.order.domain.dto.SubmitOrderDTO;
import com.messi.system.order.domain.request.SubmitOrderReq;
import com.messi.system.order.remote.CouponRemote;
import com.messi.system.order.remote.ProductRemote;
import com.messi.system.order.service.submit.SubmitOrderV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提交订单 v3
 * 使用最终一致性框架+本地消息表，做保存订单、扣优惠券、扣库存操作
 * 优点：
 * 弊端：1、需要另外开启一个定时任务，依赖数据库，2、或多或少会对业务代码有一定地书写要求，有一定程度上有依赖性
 */
@Slf4j
@Service
public class SubmitOrderServiceV3Impl extends AbstractSubmitOrder implements SubmitOrderV3 {

    @Autowired
    private CouponRemote couponRemote;

    @Autowired
    private ProductRemote productRemote;

    @Autowired
    private SubmitOrderV3 submitOrderV3;

    @Override
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

        //  9、最终一致性框架扣优惠券MQ
        submitOrderV3.deductionCoupon(checkCouponDTO);

        //  10、最终一致性框架扣库存MQ
        submitOrderV3.frameworkDeductionStock(submitOrderReq.getOrderProductList());

        //  11、构造订单
        Order order = super.buildOrder(submitOrderReq, finalPrice);

        //  12、最终一致性框架保存订单
        submitOrderV3.saveOrder(order);

        //  13、发送延迟关单MQ
        super.sendDelayCancelOrder(order);

        //  14、返回响应信息
        log.info("v3创建订单完成,orderId:{}", order.getOrderInfoDO().getOrderId());
        return super.submitResponse(submitOrderReq);

    }

    @Override
    @ConsistencyFramework(taskName = "frameworkDeductionCoupon")
    public void frameworkDeductionCoupon(CheckCouponDTO checkCouponDTO) {
        couponRemote.deductionCoupon(checkCouponDTO);
    }

    @Override
    public void frameworkDeductionStock(List<SubmitOrderReq.OrderProduct> orderProductList) {
        for (SubmitOrderReq.OrderProduct orderProduct : orderProductList) {
            //  单独执行扣减
            submitOrderV3.aloneDeductionStock(orderProduct);
        }
    }

    @Override
    @ConsistencyFramework(taskName = "saveOrder")
    public void saveOrder(Order order) {
        super.saveOrder(order);
    }

    @Override
    @ConsistencyFramework(taskName = "aloneDeductionStock")
    public void aloneDeductionStock(SubmitOrderReq.OrderProduct orderProduct) {
        productRemote.aloneDeductionStock(orderProduct);
    }

    @Override
    public void deductionCoupon(CheckCouponDTO checkCouponDTO) {
        if (checkCouponDTO != null) {
            submitOrderV3.frameworkDeductionCoupon(checkCouponDTO);
        }
    }

}

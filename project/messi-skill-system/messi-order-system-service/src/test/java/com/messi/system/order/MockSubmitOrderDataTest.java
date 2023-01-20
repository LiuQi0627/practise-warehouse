package com.messi.system.order;

import cn.hutool.core.date.DatePattern;
import com.messi.system.order.builder.OrderBuilder;
import com.messi.system.order.dao.*;
import com.messi.system.order.domain.builder.Order;
import com.messi.system.order.domain.entity.*;
import com.messi.system.order.domain.request.SubmitOrderReq;
import com.messi.system.order.enums.OrderAppraiseEnums;
import com.messi.system.order.enums.OrderPayTypeEnums;
import com.messi.system.order.enums.OrderStatusEnums;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static com.messi.system.order.constants.OrderSymbolConstants.*;

/**
 * 模拟生成订单数据
 */
@SpringBootTest(classes = OrderApp.class)
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@SuppressWarnings(value = "all")
public class MockSubmitOrderDataTest {

    @Autowired
    public OrderDAO orderDAO;

    @Autowired
    public OrderItemInfoDAO orderItemInfoDAO;

    @Autowired
    public OrderPriceDAO orderPriceDAO;

    @Autowired
    public OrderPriceDetailsDAO orderPriceDetailsDAO;

    @Autowired
    public OrderStatusRecordDAO orderStatusRecordDAO;

    /**
     * 生成模拟的订单数据
     */
    @Test
    public void mockSubmitOrderData() {
        Runnable r1 = this::mockCreateData;
        Runnable r2 = this::mockCreateData;
        Runnable r3 = this::mockCreateData;
        Runnable r4 = this::mockCreateData;
        Runnable r5 = this::mockCreateData;
        Runnable r6 = this::mockCreateData;
        Runnable r7 = this::mockCreateData;
        Runnable r8 = this::mockCreateData;
        Runnable r9 = this::mockCreateData;
        Runnable r10 = this::mockCreateData;

        r1.run();
        r2.run();
        r3.run();
        r4.run();
        r5.run();
        r6.run();
        r7.run();
        r8.run();
        r9.run();
        r10.run();
    }

    private void mockCreateData() {
        for (int i = 0; i < 10000; i++) {
            SubmitOrderReq submitOrderReq = new SubmitOrderReq();
            submitOrderReq.setChannel(0);
            submitOrderReq.setOrderType(0);
            submitOrderReq.setSellerId(generateSellerOrUserId());
            submitOrderReq.setUserId(generateSellerOrUserId());
            submitOrderReq.setOrderId(generateDistributedId(submitOrderReq.getUserId()));
            submitOrderReq.setTotalAmount(1000);
            submitOrderReq.setProvinceNo("1000");
            submitOrderReq.setCityNo("1000");
            submitOrderReq.setAreaNo("1000");
            submitOrderReq.setDetailAddress("测试详细地址");
            submitOrderReq.setRecipientName("收货人姓名" + submitOrderReq.getUserId());
            submitOrderReq.setRecipientPhone("13344441111");

            List<SubmitOrderReq.OrderItemReq> orderItemReqList = new LinkedList<>();
            SubmitOrderReq.OrderItemReq orderItemReq = new SubmitOrderReq.OrderItemReq();
            orderItemReq.setOrderItemId(submitOrderReq.getOrderId() + i);
            orderItemReq.setProductId("001");
            orderItemReq.setSkuId("10001");
            orderItemReq.setSaleNum(1);
            orderItemReq.setSkuPrice(1000);
            orderItemReqList.add(orderItemReq);
            submitOrderReq.setOrderItemReqList(orderItemReqList);

            List<SubmitOrderReq.OrderItemPrice> orderItemPriceList = new LinkedList<>();
            SubmitOrderReq.OrderItemPrice orderItemPrice = new SubmitOrderReq.OrderItemPrice();
            orderItemPrice.setOrderItemId(orderItemReq.getOrderItemId());
            orderItemPrice.setSkuId(orderItemReq.getSkuId());
            orderItemPrice.setSalePrice(orderItemReq.getSkuPrice());
            orderItemPriceList.add(orderItemPrice);
            submitOrderReq.setOrderItemPriceList(orderItemPriceList);

            LinkedList<SubmitOrderReq.OrderProduct> orderProductList = new LinkedList<>();
            SubmitOrderReq.OrderProduct orderProduct = new SubmitOrderReq.OrderProduct();
            orderProduct.setOrderItemId(orderItemReq.getOrderItemId());
            orderProduct.setProductId(orderItemReq.getProductId());
            orderProduct.setSkuId(orderItemReq.getSkuId());
            orderProduct.setSkuPrice(orderItemReq.getSkuPrice());
            orderProduct.setSaleNum(orderItemReq.getSaleNum());
            orderProductList.add(orderProduct);
            submitOrderReq.setOrderProductList(orderProductList);

            try {
                Order order = buildOrder(submitOrderReq, orderItemReq.getSkuPrice());
                saveOrder(order);
                log.info("mock生成订单,订单号:{}", order.getOrderInfoDO().getOrderId());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public String generateSellerOrUserId() {
        return String.valueOf(new Random().nextInt(2));
    }

    @Transactional
    public void saveOrder(Order order) {
        OrderInfoDO orderInfoDO = order.getOrderInfoDO();

        List<OrderItemInfoDO> orderItemList = order.getOrderItemList();

        OrderPriceDO orderPriceDO = order.getOrderPriceDO();

        List<OrderPriceDetailsDO> orderOrderPriceItemList = order.getOrderPriceItemList();

        OrderStatusRecordDO orderStatusRecordDO = order.getOrderStatusRecordDO();

        tempReplaceValue(orderInfoDO);

        orderDAO.save(orderInfoDO);

        orderItemInfoDAO.saveBatch(orderItemList);

        orderPriceDAO.save(orderPriceDO);

        orderPriceDetailsDAO.saveBatch(orderOrderPriceItemList);

        orderStatusRecordDAO.save(orderStatusRecordDO);
    }

    private void tempReplaceValue(OrderInfoDO orderInfoDO) {
        int random = new Random().nextInt(3);

        if (random == 0) {
            orderInfoDO.setOrderStatus(OrderStatusEnums.CREATED.getCode());
            orderInfoDO.setOrderPayType(OrderPayTypeEnums.WECHAT.getCode());
            orderInfoDO.setAppraiseStatus(OrderAppraiseEnums.NO_RATED.getCode());

        } else if (random == 1) {
            orderInfoDO.setOrderStatus(OrderStatusEnums.PAID.getCode());
            orderInfoDO.setOrderPayType(OrderPayTypeEnums.ALIPAY.getCode());
            orderInfoDO.setAppraiseStatus(OrderAppraiseEnums.RATED.getCode());

        } else {
            orderInfoDO.setOrderStatus(OrderStatusEnums.STORAGE.getCode());
            orderInfoDO.setOrderPayType(OrderPayTypeEnums.UNION_PAY.getCode());
            orderInfoDO.setAppraiseStatus(OrderAppraiseEnums.NO_RATED.getCode());
        }

    }

    private String generateDistributedId(String userId) {
        //  基于单位是毫秒的时间戳生成时间编号
        String dateFormat = DatePattern.NORM_DATETIME_MS_FORMAT.format(new Date());

        //  拼接业务用户id,格式：年+月+日+时+分+秒+毫秒+业务id
        return dateFormat
                .replace(SYMBOL_MIDLINE, SYMBOL_NONE)
                .replace(SYMBOL_SPOT, SYMBOL_NONE)
                .replace(SYMBOL_BLANK, SYMBOL_NONE)
                .replace(SYMBOL_COLON, SYMBOL_NONE) + userId;
    }


    private Order buildOrder(SubmitOrderReq submitOrderReq, Integer finalPrice) {
        OrderBuilder orderBuilder = new OrderBuilder(submitOrderReq, finalPrice);
        return orderBuilder
                .buildOrder()
                .buildOrderItem()
                .buildOrderPrice()
                .buildOrderPriceItem()
                .buildNewOrderStatusRecord()
                .build();
    }

}

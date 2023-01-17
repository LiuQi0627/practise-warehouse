package com.messi.system.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.messi.system.core.ResResult;
import com.messi.system.order.domain.dto.OrderDetailDTO;
import com.messi.system.order.domain.dto.OrderQueryDTO;
import com.messi.system.order.domain.dto.SubmitOrderDTO;
import com.messi.system.order.domain.query.OrderQueryCondition;
import com.messi.system.order.domain.request.SubmitOrderReq;
import com.messi.system.order.service.OrderQueryService;
import com.messi.system.order.service.submit.SubmitOrderV1;
import com.messi.system.order.service.submit.SubmitOrderV2;
import com.messi.system.order.service.submit.SubmitOrderV3;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Tags;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单controller
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private SubmitOrderV1 submitOrderV1;

    @Autowired
    private SubmitOrderV2 submitOrderV2;

    @Autowired
    private SubmitOrderV3 submitOrderV3;

    @Autowired
    private OrderQueryService orderQueryService;

    /**
     * 提交订单 v1
     *
     * @param submitOrderReq 前台订单表单
     * @return 响应结果
     */
    @PostMapping("/submit/v1")
    @Trace
    @Tags({@Tag(key = "SubmitOrderReq", value = "arg[0]"),
            @Tag(key = "ResResult<SubmitOrderDTO>", value = "returnObject")})
    public ResResult<SubmitOrderDTO> createOrderV1(@RequestBody SubmitOrderReq submitOrderReq) {
        return submitOrderV1.submitOrder(submitOrderReq);
    }

    /**
     * 提交订单 v2
     *
     * @param submitOrderReq 前台订单表单
     * @return 响应结果
     */
    @PostMapping("/submit/v2")
    @Trace
    @Tags({@Tag(key = "SubmitOrderReq", value = "arg[0]"),
            @Tag(key = "ResResult<SubmitOrderDTO>", value = "returnObject")})
    public ResResult<SubmitOrderDTO> createOrderV2(@RequestBody SubmitOrderReq submitOrderReq) {
        return submitOrderV2.submitOrder(submitOrderReq);
    }

    /**
     * 提交订单 v3
     *
     * @param submitOrderReq 前台订单表单
     * @return 响应结果
     */
    @PostMapping("/submit/v3")
    @Trace
    @Tags({@Tag(key = "SubmitOrderReq", value = "arg[0]"),
            @Tag(key = "ResResult<SubmitOrderDTO>", value = "returnObject")})
    public ResResult<SubmitOrderDTO> createOrderV3(@RequestBody SubmitOrderReq submitOrderReq) {
        return submitOrderV3.submitOrder(submitOrderReq);
    }

    /**
     * 查询订单分页，单表
     *
     * @return 订单分页数据
     */
    @GetMapping("/queryOrderPageBySingleTable")
    @Trace
    @Tags({@Tag(key = "OrderQueryCondition", value = "arg[0]"),
            @Tag(key = "ResResult<Page<OrderQueryDTO>>", value = "returnObject")})
    public ResResult<Page<OrderQueryDTO>> queryOrderPageBySingleTable(@RequestBody OrderQueryCondition orderQueryCondition) {
        log.info("查询订单单表分页");
        return orderQueryService.queryOrderPageBySingleTable(orderQueryCondition);
    }

    /**
     * 查询订单分页，数据库联表
     *
     * @return 订单分页数据
     */
    @GetMapping("/queryOrderPageByJoinTable")
    @Trace
    @Tags({@Tag(key = "OrderQueryCondition", value = "arg[0]"),
            @Tag(key = "ResResult<Page<OrderQueryDTO>>", value = "returnObject")})
    public ResResult<Page<OrderQueryDTO>> queryOrderPageByJoinTable(@RequestBody OrderQueryCondition orderQueryCondition) {
        return orderQueryService.queryOrderPageByJoinTable(orderQueryCondition);
    }

    /**
     * 查询订单详情，查询es大宽表
     */
    @GetMapping("queryOrderDetailByEs")
    @Trace
    @Tags({@Tag(key = "orderId", value = "arg[0]"),
            @Tag(key = "ResResult<OrderDetailDTO>", value = "returnObject")})
    public ResResult<OrderDetailDTO> queryOrderDetailByEs(@RequestParam("orderId") String orderId) {
        return orderQueryService.queryOrderDetailByEs(orderId);
    }

    /**
     * 查询订单详情，直接查询数据库
     */
    @GetMapping("queryOrderDetailByTable")
    @Trace
    @Tags({@Tag(key = "orderId", value = "arg[0]"),
            @Tag(key = "ResResult<OrderDetailDTO>", value = "returnObject")})
    public ResResult<OrderDetailDTO> queryOrderDetailByTable(@RequestParam("orderId") String orderId) {
        return orderQueryService.queryOrderDetailByTable(orderId);
    }

}

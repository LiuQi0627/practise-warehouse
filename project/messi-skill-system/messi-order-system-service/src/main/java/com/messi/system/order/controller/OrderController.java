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

    /**
     * 分库分表环境，管理端(B端)的分页查询，查询数据库，以userId作为数据维度
     */
    public ResResult<Page<OrderQueryDTO>> queryBSideOrderPage() {
        return null;
    }

    /**
     * 分库分表环境，管理端(B端)的详情查询，查询数据库，以userId作为数据维度
     */
    public ResResult<Page<OrderQueryDTO>> queryBSideOrderDetail() {
        return null;
    }

    /**
     * 分库分表环境，用户端(C端)的分页查询，查询ES，以userId作为数据维度
     */
    public ResResult<Page<OrderQueryDTO>> queryCSideOrderPage() {
        return null;
    }

    /**
     * 分库分表环境，用户端(C端)的详情查询，查询ES，以userId作为数据维度
     */
    public ResResult<Page<OrderQueryDTO>> queryCSideOrderDetail() {
        return null;
    }

    /**
     * 分库分表环境，全量+增量的数据异构同步存储
     * 在将数据从数据库迁移到ES的过程中，数据库还会有新的数据进入，
     * 这里的方案采用全量+增量同步。
     * 首先是串行化跑一个全量同步数据的线程，将所有的全量数据写完。
     * 然后启动一个增量线程，把每条binlog都跟已经同步过的数据做一个比对，如果存在不同，以最新的binlog为准。
     *
     * 优势：数据迁移过程的逻辑很清晰，将全量和增量分开执行，避免掉各种复杂的数据冲突和线程并发问题。
     * 劣势：因为是串行化，所以执行的时间会较长，全量执行完以后，还要执行一次增量。
     */


}

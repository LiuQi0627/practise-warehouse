package com.messi.system.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.messi.system.core.ResResult;
import com.messi.system.order.domain.dto.OrderDetailDTO;
import com.messi.system.order.domain.dto.OrderQueryDTO;
import com.messi.system.order.domain.query.OrderQueryCondition;

/**
 * 订单查询service
 */
public interface OrderQueryService {

    /**
     * 查询订单单表分页
     */
    ResResult<Page<OrderQueryDTO>> queryOrderPageBySingleTable(OrderQueryCondition orderQueryCondition);

    /**
     * 查询订单联表分页
     */
    ResResult<Page<OrderQueryDTO>> queryOrderPageByJoinTable(OrderQueryCondition orderQueryCondition);

    /**
     * 查询订单详情
     */
    ResResult<OrderDetailDTO> queryOrderDetailByEs(String orderId);

    /**
     * 查询订单详情
     */
    ResResult<OrderDetailDTO> queryOrderDetailByTable(String orderId);
}

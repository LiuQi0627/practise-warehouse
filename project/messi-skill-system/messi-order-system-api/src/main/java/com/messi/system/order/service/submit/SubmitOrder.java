package com.messi.system.order.service.submit;

import com.messi.system.core.ResResult;
import com.messi.system.order.domain.dto.SubmitOrderDTO;
import com.messi.system.order.domain.entity.OrderInfoDO;
import com.messi.system.order.domain.request.SubmitOrderReq;

/**
 * 提交订单接口
 */
public interface SubmitOrder {

    /**
     * 提交订单
     *
     * @param submitOrderReq 前台订单表单
     */
    ResResult<SubmitOrderDTO> submitOrder(SubmitOrderReq submitOrderReq);

}

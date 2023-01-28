package com.messi.snap.up.controller;

import com.messi.snap.up.reactor.context.SnapupContext;
import com.messi.sanp.up.domain.entity.SnapupRequest;
import com.messi.snap.up.enums.SnapupProcessEnums;
import com.messi.snap.up.reactor.bus.BossEventBus;
import com.messi.snap.up.reactor.listener.ServletAsyncListener;
import com.messi.snap.up.reactor.process.Process1CheckUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

/**
 * 抢购controller
 */
@RestController
@RequestMapping("/snapup")
public class SnapupController {

    @Autowired
    private BossEventBus bossEventBus;

    /**
     * 执行抢购请求
     *
     * @param snapupRequest  抢购入参
     * @param servletRequest servlet request
     */
    @PostMapping("/sku")
    public void snapup(@RequestBody SnapupRequest snapupRequest, HttpServletRequest servletRequest) {

        //  1、自定义每次请求的异步环境上下文
        AsyncContext asyncContext = customAsyncContext(servletRequest);

        //  2、自定义事件流转上下文
        SnapupContext snapupContext = customEventFlowContext(asyncContext, snapupRequest);

        //  3、事件分发
        bossEventBus.publishSnapUpEvent(
                SnapupProcessEnums.SNAPUP_PROCESS_1.getChannel(),
                new Process1CheckUser(),
                snapupContext
        );

    }

    private SnapupContext customEventFlowContext(AsyncContext asyncContext, SnapupRequest snapupRequest) {
        SnapupContext snapupContext = new SnapupContext();
        snapupContext.setPromotionId(snapupRequest.getPromotionId());
        snapupContext.setSkuId(snapupRequest.getSkuId());
        snapupContext.setUserId(snapupRequest.getUserId());
        snapupContext.setAsyncContext(asyncContext);

        return snapupContext;
    }

    private AsyncContext customAsyncContext(HttpServletRequest servletRequest) {
        AsyncContext asyncContext = servletRequest.startAsync();
        asyncContext.setTimeout(5000);
        asyncContext.addListener(new ServletAsyncListener());

        return asyncContext;
    }

}

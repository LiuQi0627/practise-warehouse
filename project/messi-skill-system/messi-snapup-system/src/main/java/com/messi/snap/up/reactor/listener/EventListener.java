package com.messi.snap.up.reactor.listener;

import com.messi.snap.up.reactor.event.BaseEvent;
import com.messi.snap.up.reactor.async.AsyncContext;

/**
 * 自定义事件监听器
 */
public interface EventListener<E extends BaseEvent> extends java.util.EventListener {

    /**
     * 接收事件
     * boss bus在分发事件时调用，用于查询全部的符合的事件Listener
     */
    Boolean accept(BaseEvent baseEvent);

    /**
     * 处理事件
     *
     * @param event        自定义的事件
     * @param asyncContext 异步环境的上下文
     */
    void onEvent(E event, AsyncContext asyncContext);

}

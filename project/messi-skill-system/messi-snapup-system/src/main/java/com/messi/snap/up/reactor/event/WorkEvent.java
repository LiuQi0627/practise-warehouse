package com.messi.snap.up.reactor.event;

import com.messi.snap.up.reactor.async.AsyncContext;
import com.messi.snap.up.reactor.listener.EventListener;

import java.util.List;

/**
 * 工作总线事件
 */
public class WorkEvent {
    /**
     * 基本事件
     */
    public BaseEvent event;

    /**
     * 异步环境上下文
     */
    public AsyncContext context;

    /**
     * 事件监听器list
     */
    public List<EventListener> eventListeners;

    public void clear() {
        event = null;
        context = null;
        eventListeners = null;
    }
}
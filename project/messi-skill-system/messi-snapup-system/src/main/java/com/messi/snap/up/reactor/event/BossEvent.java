package com.messi.snap.up.reactor.event;

import com.messi.snap.up.reactor.async.AsyncContext;

/**
 * 总线事件
 */
public class BossEvent {

    public String channel;

    /**
     * 基本事件
     */
    public BaseEvent event;

    /**
     * 异步环境上下文
     */
    public AsyncContext context;

    public void clear() {
        channel = null;
        event = null;
        context = null;
    }
}

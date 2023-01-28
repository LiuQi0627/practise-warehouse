package com.messi.snap.up.reactor.listener;

import com.messi.snap.up.reactor.async.AsyncContext;
import com.messi.snap.up.reactor.bus.BossEventBus;
import com.messi.snap.up.reactor.context.SnapupContext;
import com.messi.snap.up.reactor.event.BaseEvent;
import com.messi.snap.up.reactor.executor.WorkerThreadExecutor;
import com.messi.snap.up.redis.RedisCommand;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 抽象抢购事件监听父类
 */
public abstract class BaseSnapupListener<E extends BaseEvent> implements EventListener<BaseEvent> {

    @Autowired
    protected BossEventBus bossEventBus;

    @Autowired
    protected WorkerThreadExecutor workerThreadExecutor;

    @Autowired
    protected RedisCommand redisCommand;

    @Override
    public void onEvent(BaseEvent event, AsyncContext eventContext) {
        SnapupContext snapupContext = (SnapupContext) eventContext;
        doProcess(((E) event), snapupContext);
    }

    /**
     * 交由不同的子listener执行
     */
    protected abstract void doProcess(E event, SnapupContext snapupContext);

    /**
     * 模拟返回前台响应页面
     */
    protected void response(javax.servlet.AsyncContext asyncContext, Boolean result, String info) {
        ServletResponse response = asyncContext.getResponse();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        try (ServletOutputStream out = response.getOutputStream()) {
            String s = "{\"success\":" + result + ", \"info\":\"" + info + "\"}";
            out.write(s.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            asyncContext.complete();
        }
    }
}

package com.messi.snap.up.reactor.listener.snapup;

import com.messi.snap.up.annotation.Channel;
import com.messi.snap.up.enums.SnapupProcessEnums;
import com.messi.snap.up.reactor.context.SnapupContext;
import com.messi.snap.up.reactor.event.BaseEvent;
import com.messi.snap.up.reactor.listener.BaseSnapupListener;
import com.messi.snap.up.reactor.process.Process2CheckSku;
import com.messi.snap.up.reactor.process.Process1CheckUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Channel("Process1")   //  和yaml配置文件中的自定义配置相对应
public class ProcessListener1 extends BaseSnapupListener<Process1CheckUser> {

    @Override
    protected void doProcess(Process1CheckUser event, SnapupContext snapupContext) {
        workerThreadExecutor.execute(
                SnapupProcessEnums.SNAPUP_PROCESS_1.getChannel(),
                new Runnable() {
                    @Override
                    public void run() {
                        log.info("执行业务逻辑，调用风控系统,用户合法性检查通过");
                        log.info("ProcessListener1执行完成,用户ID:{}", snapupContext.getUserId());
                        bossEventBus.publishSnapUpEvent(
                                SnapupProcessEnums.SNAPUP_PROCESS_2.getChannel(),
                                new Process2CheckSku(),
                                snapupContext
                        );
                    }
                }
        );
    }

    @Override
    public Boolean accept(BaseEvent baseEvent) {
        return baseEvent instanceof Process1CheckUser;
    }
}

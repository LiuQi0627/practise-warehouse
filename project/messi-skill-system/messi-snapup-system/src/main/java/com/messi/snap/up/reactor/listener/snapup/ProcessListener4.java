package com.messi.snap.up.reactor.listener.snapup;

import com.messi.snap.up.annotation.Channel;
import com.messi.snap.up.enums.SnapupProcessEnums;
import com.messi.snap.up.reactor.context.SnapupContext;
import com.messi.snap.up.reactor.event.BaseEvent;
import com.messi.snap.up.reactor.listener.BaseSnapupListener;
import com.messi.snap.up.reactor.process.Process4CreateSnapUpOrder;
import com.messi.snap.up.reactor.process.Process5ResponseSuccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Channel("Process4")   //  和yaml配置文件中的自定义配置相对应
public class ProcessListener4 extends BaseSnapupListener<Process4CreateSnapUpOrder> {

    @Override
    protected void doProcess(Process4CreateSnapUpOrder event, SnapupContext snapupContext) {
        workerThreadExecutor.execute(
                SnapupProcessEnums.SNAPUP_PROCESS_4.getChannel(),
                new Runnable() {
                    @Override
                    public void run() {
                        log.info("创建抢购秒杀订单成功");
                        log.info("ProcessListener4执行完成,用户ID:{}", snapupContext.getUserId());
                        bossEventBus.publishSnapUpEvent(
                                SnapupProcessEnums.SNAPUP_PROCESS_5.getChannel(),
                                new Process5ResponseSuccess(),
                                snapupContext
                        );
                    }
                }
        );
    }

    @Override
    public Boolean accept(BaseEvent baseEvent) {
        return baseEvent instanceof Process4CreateSnapUpOrder;
    }
}

package com.messi.snap.up.reactor.listener.snapup;

import com.messi.snap.up.annotation.Channel;
import com.messi.snap.up.enums.SnapupProcessEnums;
import com.messi.snap.up.reactor.context.SnapupContext;
import com.messi.snap.up.reactor.event.BaseEvent;
import com.messi.snap.up.reactor.listener.BaseSnapupListener;
import com.messi.snap.up.reactor.process.Process5ResponseSuccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Channel("Process5")   //  和yaml配置文件中的自定义配置相对应
public class ProcessListener5 extends BaseSnapupListener<Process5ResponseSuccess> {

    @Override
    protected void doProcess(Process5ResponseSuccess event, SnapupContext snapupContext) {
        workerThreadExecutor.execute(
                SnapupProcessEnums.SNAPUP_PROCESS_4.getChannel(),
                new Runnable() {
                    @Override
                    public void run() {
                        log.info("返回抢购成功结果");
                        log.info("ProcessListener5执行完成,用户ID:{}", snapupContext.getUserId());
                        response(snapupContext.getAsyncContext(), true, "恭喜抢购成功");
                    }
                }
        );
    }

    @Override
    public Boolean accept(BaseEvent baseEvent) {
        return baseEvent instanceof Process5ResponseSuccess;
    }
}

package com.messi.snap.up.reactor.listener.snapup;

import com.messi.snap.up.annotation.Channel;
import com.messi.snap.up.enums.SnapupProcessEnums;
import com.messi.snap.up.reactor.context.SnapupContext;
import com.messi.snap.up.reactor.event.BaseEvent;
import com.messi.snap.up.reactor.listener.BaseSnapupListener;
import com.messi.snap.up.reactor.process.Process2CheckSku;
import com.messi.snap.up.reactor.process.Process3LockStock;
import com.messi.snap.up.redis.SnapupKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Channel("Process2")   //  和yaml配置文件中的自定义配置相对应
public class ProcessListener2 extends BaseSnapupListener<Process2CheckSku> {

    @Override
    protected void doProcess(Process2CheckSku event, SnapupContext snapupContext) {
        workerThreadExecutor.execute(
                SnapupProcessEnums.SNAPUP_PROCESS_2.getChannel(),
                new Runnable() {
                    @Override
                    public void run() {
                        Long promotionId = snapupContext.getPromotionId();
                        String skuId = snapupContext.getSkuId();
                        String userId = snapupContext.getUserId();
                        String snapUpKey = SnapupKey.buildCheckSkuKey(promotionId, skuId, userId);

                        //  该用户在当前活动中，已经抢购过此sku
                        if (redisCommand.exists(snapUpKey)) {
                            ProcessListener2.this.response(
                                    snapupContext.getAsyncContext(),
                                    false,
                                    "你已经抢购过此商品"
                            );
                        }

                        log.info("ProcessListener2执行完成,用户ID:{}", userId);
                        bossEventBus.publishSnapUpEvent(
                                SnapupProcessEnums.SNAPUP_PROCESS_3.getChannel(),
                                new Process3LockStock(),
                                snapupContext
                        );
                    }
                }
        );
    }

    @Override
    public Boolean accept(BaseEvent baseEvent) {
        return baseEvent instanceof Process2CheckSku;
    }
}

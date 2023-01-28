package com.messi.snap.up.reactor.handler;

import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.WorkHandler;
import com.messi.snap.up.reactor.bus.WorkEventBus;
import com.messi.snap.up.reactor.event.BossEvent;
import com.messi.snap.up.reactor.event.WorkEvent;
import com.messi.snap.up.reactor.listener.EventListener;
import com.messi.snap.up.reactor.manager.WorkEventBusManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 自定义管理总线事件执行器
 */
@Slf4j
public class BossEventHandler implements WorkHandler<BossEvent> {
    /**
     * 将事件分发给worker event
     */
    @Override
    @SuppressWarnings(value = "all")
    public void onEvent(BossEvent bossEvent) throws Exception {
        //  1、获取对应的worker event bus
        WorkEventBus workEventBus = WorkEventBusManager.getSingleton().getWorkEventBus(bossEvent.channel);

        //  2、获取对应的listener监听
        List<EventListener> eventListeners = workEventBus.getEventListeners(bossEvent.event);

        //  3、封装work event
        EventTranslator<WorkEvent> eventEventTranslator = new EventTranslator<WorkEvent>() {
            @Override
            public void translateTo(WorkEvent event, long sequence) {
                //  事件类型
                event.event = bossEvent.event;
                //  事件上下文
                event.context = bossEvent.context;
                //  事件监听器
                event.eventListeners = eventListeners;
            }
        };

        //  4、分发事件
        Boolean publishResult = workEventBus.publish(eventEventTranslator);
        if (!publishResult) {
            // todo: 2023/1/28 可以优化
            //  如果发送失败了，做幂等验证+消息补偿
            log.error("BossEventHandler分发事件失败,eventEventTranslator:{}", eventEventTranslator);
        }
    }
}

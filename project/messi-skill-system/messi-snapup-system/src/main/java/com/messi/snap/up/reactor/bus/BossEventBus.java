package com.messi.snap.up.reactor.bus;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.dsl.Disruptor;
import com.messi.snap.up.config.bus.BossConfig;
import com.messi.snap.up.config.bus.WorkerConfig;
import com.messi.snap.up.reactor.async.AsyncContext;
import com.messi.snap.up.reactor.event.BaseEvent;
import com.messi.snap.up.reactor.event.BossEvent;
import com.messi.snap.up.reactor.handler.BossEventHandler;
import com.messi.snap.up.reactor.manager.WorkEventBusManager;
import com.messi.snap.up.reactor.thread.CustomThreadNamedFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 消息事件总线
 * 负责从内存队列里取出一个一个的event事件，根据事件的不同类型，把事件交给对应的处理器
 */
@Slf4j
@Component
public class BossEventBus {
    private final Disruptor<BossEvent> bossRingBuffer;

    /**
     * 初始化管理总线时，就加载完毕管理总线和执行总线的配置
     * boss event:管理总线，全局唯一
     * worker event:工作总线，可以多个，执行不同类型的事件
     *
     * @param bossConfig 管理总线配置
     * @param workConfig 执行总线配置
     */
    public BossEventBus(BossConfig bossConfig, WorkerConfig workConfig) {
        //  1、初始化注册工作总线
        WorkEventBusManager workEventBusManager = WorkEventBusManager.getSingleton();
        for (WorkerConfig.WorkConfig workerConfig : workConfig.getWorkers()) {
            workEventBusManager.register(workerConfig);
        }

        //  2、初始化注册管理总线
        //  先初始化工作总线，这样再初始化管理总线时，可以直接扫到需要监听的事件
        bossRingBuffer = new Disruptor<>(
                new EventFactory<BossEvent>() {
                    @Override
                    public BossEvent newInstance() {
                        return new BossEvent();
                    }
                },
                bossConfig.getRingBufferSize(),
                CustomThreadNamedFactory.getInstance("BossEventBus")
        );
        BossEventHandler[] bossEventHandlers = new BossEventHandler[bossConfig.getEventHandlerNum()];
        for (int i = 0; i < bossEventHandlers.length; i++) {
            bossEventHandlers[i] = new BossEventHandler();
        }

        //  初始化队列
        bossRingBuffer.handleEventsWithWorkerPool(bossEventHandlers);
        bossRingBuffer.start();
    }

    /**
     * 发布抢购事件
     *
     * @param channel 事件channel
     * @param event   发生的事件，也就是具体要执行的业务流程
     * @param context 上下文
     */
    public Boolean publishSnapUpEvent(String channel, BaseEvent event, AsyncContext context) {
        EventTranslator<BossEvent> translator = (bossEvent, sequence) -> {
            bossEvent.channel = channel;
            bossEvent.event = event;
            bossEvent.context = context;
        };

        Boolean publishResult = bossRingBuffer.getRingBuffer().tryPublishEvent(translator);
        if (!publishResult) {
            // todo: 2023/1/28 可以优化
            //  如果发送失败了，做幂等验证+消息补偿
            log.error("BossEventBus分发初始抢购事件失败,translator:{}", translator);
        }
        return publishResult;
    }

}

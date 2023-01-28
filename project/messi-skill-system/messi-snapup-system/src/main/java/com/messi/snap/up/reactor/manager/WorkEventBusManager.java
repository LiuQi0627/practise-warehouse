package com.messi.snap.up.reactor.manager;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.WorkHandler;
import com.messi.snap.up.config.bus.WorkerConfig;
import com.messi.snap.up.reactor.event.WorkEvent;
import com.messi.snap.up.reactor.bus.WorkEventBus;
import com.messi.snap.up.reactor.handler.WorkEventHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class WorkEventBusManager {
    private static final WorkEventBusManager SINGLETON = new WorkEventBusManager();
    private final ConcurrentHashMap<String, WorkEventBus> buffer = new ConcurrentHashMap<>();

    private WorkEventBusManager() {
    }

    public static WorkEventBusManager getSingleton() {
        return SINGLETON;
    }

    public void register(WorkerConfig.WorkConfig workConfig) {
        buffer.computeIfAbsent(
                workConfig.getChannel(),
                new Function<String, WorkEventBus>() {
                    @Override
                    public WorkEventBus apply(String str) {
                        return new WorkEventBus<>(
                                //  获取内存队列的数量
                                workConfig.getRingBufferSize(),
                                //  获取事件执行器数量
                                workConfig.getEventHandlerNum(),
                                new EventFactory<WorkEvent>() {
                                    @Override
                                    public WorkEvent newInstance() {
                                        return new WorkEvent();
                                    }
                                },
                                new Supplier<WorkHandler<WorkEvent>>() {
                                    @Override
                                    public WorkHandler<WorkEvent> get() {
                                        return new WorkEventHandler();
                                    }
                                }
                        );
                    }
                }
        );
    }

    public WorkEventBus getWorkEventBus(String channel) {
        return buffer.get(channel);
    }

}
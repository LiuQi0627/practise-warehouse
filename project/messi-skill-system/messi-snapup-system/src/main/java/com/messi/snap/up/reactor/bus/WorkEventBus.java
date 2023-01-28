package com.messi.snap.up.reactor.bus;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.messi.snap.up.reactor.event.BaseEvent;
import com.messi.snap.up.reactor.listener.EventListener;
import com.messi.snap.up.reactor.thread.CustomThreadNamedFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 工作事件总线
 * 负责执行boss event 发过来的事件
 */
public class WorkEventBus<E> {
    /**
     * 读写锁
     */
    ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Disruptor<E> workRingBuffer;

    /**
     * 保存worker bus全部的listener监听
     */
    private final List<EventListener> allEventListeners = new ArrayList<>();

    public WorkEventBus(Integer ringBufferSize, Integer workerEventHandlerNum,
                        EventFactory<E> workEventEventFactory,
                        Supplier<WorkHandler<E>> workHandlerSupplier) {
        //  初始化内存队列
        workRingBuffer = new Disruptor<>(
                workEventEventFactory,
                ringBufferSize,
                CustomThreadNamedFactory.getInstance("WorkEventBus")
        );

        WorkHandler<E>[] workHandlers = new WorkHandler[workerEventHandlerNum];
        for (int i = 0; i < workHandlers.length; i++) {
            workHandlers[i] = workHandlerSupplier.get();
        }
        //  初始化队列
        workRingBuffer.handleEventsWithWorkerPool(workHandlers);
        workRingBuffer.start();
    }

    /**
     * 获取事件监听
     */
    public List<EventListener> getEventListeners(BaseEvent baseEvent) {
        //  设置读锁
        readWriteLock.readLock().lock();
        try {
            //  查询出所有的listener
            return allEventListeners.stream().filter(
                    new Predicate<EventListener>() {
                        @Override
                        public boolean test(EventListener eventListener) {
                            return eventListener.accept(baseEvent);
                        }
                    }
            ).collect(Collectors.toList());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * 发布事件
     */
    public Boolean publish(EventTranslator<E> eventEventTranslator) {
        return workRingBuffer.getRingBuffer().tryPublishEvent(eventEventTranslator);
    }

    /**
     * 注册事件监听
     */
    public Boolean register(EventListener eventListener) {
        //  设置写锁
        readWriteLock.writeLock().lock();
        try {
            if (this.allEventListeners.contains(eventListener)) {
                return false;
            }
            //  注册
            this.allEventListeners.add(eventListener);
            return true;

        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}

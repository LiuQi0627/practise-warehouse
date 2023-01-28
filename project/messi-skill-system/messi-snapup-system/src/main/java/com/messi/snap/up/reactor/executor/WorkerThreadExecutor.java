package com.messi.snap.up.reactor.executor;

import com.messi.snap.up.config.bus.ThreadPoolConfig;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 执行worker bus事件的线程执行器
 */
public class WorkerThreadExecutor {
    private static final ConcurrentHashMap<String, WorkerThreadPool> buffer = new ConcurrentHashMap<>();

    public WorkerThreadExecutor(ThreadPoolConfig threadPoolConfig) {
        for (ThreadPoolConfig.ThreadConfig threadConfig : threadPoolConfig.getThreads()) {
            buffer.put(
                    threadConfig.getThreadPool(),
                    new WorkerThreadPool(
                            threadConfig.getThreadPool(), threadConfig.getThreadCount()
                    )
            );
        }
    }

    public void execute(String channel, Runnable task) {
        Optional.ofNullable(
                buffer.get(channel)).ifPresent(
                workerThreadPool -> workerThreadPool.execute(task));
    }

}

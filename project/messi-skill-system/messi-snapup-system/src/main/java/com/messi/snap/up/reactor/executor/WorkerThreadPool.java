package com.messi.snap.up.reactor.executor;

import com.messi.snap.up.reactor.thread.CustomThreadNamedFactory;

import java.util.concurrent.*;

/**
 * 自定义线程池
 */
public class WorkerThreadPool {
    /**
     * 信号量
     */
    private final Semaphore semaphore;

    private final ThreadPoolExecutor threadPoolExecutor;

    public WorkerThreadPool(String name, int permits) {
        //  分配信号量，自定义执行线程是几个，信号量就是几个
        semaphore = new Semaphore(permits);
        //  这里不设置固定的核心线程数，最大线程数量是当前自定义执行线程数量*2
        //  因为抢购秒杀首先要保证的是高可用，所以不做阻塞队列
        //  就算抛异常也不怕，因为真实环境中，外部会做异常降级
        //  permits * 2的目的是让线程池有一定的承受能力
        threadPoolExecutor = new ThreadPoolExecutor(
                0,
                permits * 2,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                CustomThreadNamedFactory.getInstance(name)
        );
    }

    public void execute(Runnable r) {
        //  超过线程池最大数量的线程没有获取到信号，就现在这里阻塞等待，拿到信号后继续执行
        semaphore.acquireUninterruptibly();

        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    //  并发执行事件
                    r.run();
                } finally {
                    //  每次执行完，都释放信号量
                    semaphore.release();
                }
            }
        });
    }
}

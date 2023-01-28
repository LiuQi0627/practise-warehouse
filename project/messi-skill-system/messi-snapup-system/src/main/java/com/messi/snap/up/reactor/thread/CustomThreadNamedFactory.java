package com.messi.snap.up.reactor.thread;

import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 自定义线程命名工厂
 */
public class CustomThreadNamedFactory implements ThreadFactory {
    private static final ConcurrentHashMap<String, CustomThreadNamedFactory> factoryConcurrentHashMap = new ConcurrentHashMap<>();

    /**
     * 线程名称
     */
    private final String name;

    /**
     * 原子计数
     */
    private final AtomicInteger counter = new AtomicInteger(0);

    public CustomThreadNamedFactory(String name) {
        this.name = name;
    }

    public static CustomThreadNamedFactory getInstance(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new RuntimeException("没有传入自定义线程名称");
        }

        //  键和值还没有关联时，做一个函数，将函数值与key关联
        //  与put的区别：put会覆盖值，computeIfAbsent是计算函数值，和key做关联，不会覆盖旧值
        //  https://blog.csdn.net/wo415415/article/details/87469226
        return factoryConcurrentHashMap.computeIfAbsent(name, new Function<String, CustomThreadNamedFactory>() {
            @Override
            public CustomThreadNamedFactory apply(String s) {
                return new CustomThreadNamedFactory(s);
            }
        });

    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, name + "-" + counter.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    }
}

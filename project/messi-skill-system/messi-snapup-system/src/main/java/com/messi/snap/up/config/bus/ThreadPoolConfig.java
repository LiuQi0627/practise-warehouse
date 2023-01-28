package com.messi.snap.up.config.bus;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行事件的线程池配置
 */
@ConfigurationProperties(prefix = "messi.snap.up")
public class ThreadPoolConfig {
    /**
     * 关于yaml配置项目的坑：
     * 1、这里的变量threads，要和yaml配置中的messi.snap.up.threads名称相同
     * 2、threads的getter setter方法要配置正确，不能使用错误名称
     * 否则会在项目启动时读取不到配置
     */
    private List<ThreadConfig> threads = new ArrayList<>();

    public List<ThreadConfig> getThreads() {
        return threads;
    }

    public void setThreads(List<ThreadConfig> threads) {
        this.threads = threads;
    }

    public static class ThreadConfig {
        private String threadPool;

        private Integer threadCount;

        public String getThreadPool() {
            return threadPool;
        }

        public void setThreadPool(String threadPool) {
            this.threadPool = threadPool;
        }

        public Integer getThreadCount() {
            return threadCount;
        }

        public void setThreadCount(Integer threadCount) {
            this.threadCount = threadCount;
        }
    }

}

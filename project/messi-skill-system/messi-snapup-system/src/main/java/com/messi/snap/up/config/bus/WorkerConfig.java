package com.messi.snap.up.config.bus;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * worker event 配置
 */
@ConfigurationProperties(prefix = "messi.snap.up.event.bus")
public class WorkerConfig {
    private List<WorkConfig> workers = new ArrayList<>();

    public List<WorkConfig> getWorkers() {
        return workers;
    }

    public void setWorkers(List<WorkConfig> workers) {
        this.workers = workers;
    }

    public static class WorkConfig {

        private String channel;

        private Integer ringBufferSize;

        private Integer eventHandlerNum;

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public Integer getRingBufferSize() {
            return ringBufferSize;
        }

        public void setRingBufferSize(Integer ringBufferSize) {
            this.ringBufferSize = ringBufferSize;
        }

        public Integer getEventHandlerNum() {
            return eventHandlerNum;
        }

        public void setEventHandlerNum(Integer eventHandlerNum) {
            this.eventHandlerNum = eventHandlerNum;
        }
    }
}

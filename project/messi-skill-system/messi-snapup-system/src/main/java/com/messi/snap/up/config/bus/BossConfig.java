package com.messi.snap.up.config.bus;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * boss event 配置
 */
@ConfigurationProperties(prefix = "messi.snap.up.event.bus.boss")
public class BossConfig {

    private Integer ringBufferSize;

    private Integer eventHandlerNum;

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

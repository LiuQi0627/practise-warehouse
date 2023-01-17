package com.messi.system.consistency.query;

import java.util.Date;

/**
 * 查询任务实例的范围参数
 */
public interface TaskQueryRange {

    /**
     * 查询任务的起始时间
     */
    Date getStartTime();

    /**
     * 查询任务的结束时间
     */
    Date getEndTime();

    /**
     * 每次查询多少个任务
     */
    Long getLimitTask();
}

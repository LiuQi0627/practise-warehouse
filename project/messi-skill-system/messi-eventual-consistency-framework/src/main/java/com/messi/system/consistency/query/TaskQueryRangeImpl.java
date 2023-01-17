package com.messi.system.consistency.query;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 自定义查询实例的实现类
 */
@Service
public class TaskQueryRangeImpl implements TaskQueryRange {

    /**
     * 查询任务的起始时间,默认起始时间是前12个小时
     */
    @Override
    public Date getStartTime() {
        String formatTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        DateTime date = DateUtil.parse(formatTime);
        DateTime newDate = DateUtil.offset(date, DateField.HOUR, -12);
        return newDate.toJdkDate();
    }

    /**
     * 查询任务的起始时间，默认是截至当前时间
     */
    @Override
    public Date getEndTime() {
        return new Date();
    }

    /**
     * 每次查询的实例数量,默认是1000个
     *
     * @return
     */
    @Override
    public Long getLimitTask() {
        return 1000L;
    }

}

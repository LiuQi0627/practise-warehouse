package com.messi.system.data.migration;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
public class DataMigrationTest {

    /**
     * 获取指定时间的时间戳
     */
    public static void main(String[] args) {
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 22,
                0, 0, 0, 0);
        long l = dateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        log.info("获取到时间戳1：{}", l);

        LocalDateTime dateTime2 = LocalDateTime.of(2023, 1, 22,
                0, 0, 1, 0);
        long l2 = dateTime2.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        log.info("获取到时间戳2：{}", l2);
    }
}

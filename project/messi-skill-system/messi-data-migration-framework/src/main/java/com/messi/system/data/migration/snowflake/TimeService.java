package com.messi.system.data.migration.snowflake;

public class TimeService {
    public TimeService() {
    }

    public long getCurrentMillis() {
        return System.currentTimeMillis();
    }
}
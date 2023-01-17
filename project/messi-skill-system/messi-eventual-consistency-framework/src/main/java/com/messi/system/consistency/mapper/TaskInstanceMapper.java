package com.messi.system.consistency.mapper;

import com.messi.system.consistency.instance.ConsistencyTaskInstance;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 最终一致性任务实例DAO
 */
@Mapper
@Repository
@SuppressWarnings(value = "all")
public interface TaskInstanceMapper {

    @Insert("INSERT INTO consistency_info (" +
            "task," +
            "task_method_name," +
            "full_sign_name," +
            "param_types," +
            "params," +
            "exec_type," +
            "exec_interval," +
            "delay_ms," +
            "exec_total," +
            "exec_time," +
            "status," +
            "err_msg," +
            "downgrade_class," +
            "downgrade_error_msg," +
            "id," +
            "create_time," +
            "modified_time" +
            ") VALUES(" +
            "#{task}," +
            "#{taskMethodName}," +
            "#{fullSignName}," +
            "#{paramTypes}," +
            "#{params}," +
            "#{execType}," +
            "#{execInterval}," +
            "#{delayMs}," +
            "#{execTotal}," +
            "#{execTime}," +
            "#{status}," +
            "#{errMsg}," +
            "#{downgradeClass}," +
            "#{downgradeErrorMsg}," +
            "#{id}," +
            "#{createTime}," +
            "#{modifiedTime}" +
            ")")
    @Options(keyColumn = "id", keyProperty = "id", useGeneratedKeys = true)
    void saveTask(ConsistencyTaskInstance consistencyTaskInstance);

    @Update("UPDATE consistency_info SET " +
            "status=#{status}, " +
            "exec_total=#{execTotal}, " +
            "exec_time=#{execTime} " +
            "WHERE id=#{id} ")
    void update(ConsistencyTaskInstance consistencyTaskInstance);

    @Select("SELECT " +
            "task," +
            "task_method_name," +
            "full_sign_name," +
            "param_types," +
            "params," +
            "exec_type," +
            "exec_interval," +
            "delay_ms," +
            "exec_total," +
            "exec_time," +
            "status," +
            "err_msg," +
            "downgrade_class," +
            "downgrade_error_msg," +
            "id," +
            "create_time," +
            "modified_time " +
            "FROM consistency_info " +
            "WHERE " +
            "exec_type < 3 " +
            "AND exec_time >= #{startTime} AND exec_time <= #{endTime} " +
            "ORDER BY exec_time ASC " +
            "LIMIT #{limitTask}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "task", property = "task"),
            @Result(column = "task_method_name", property = "taskMethodName"),
            @Result(column = "full_sign_name", property = "fullSignName"),
            @Result(column = "param_types", property = "paramTypes"),
            @Result(column = "params", property = "params"),
            @Result(column = "exec_type", property = "execType"),
            @Result(column = "exec_interval", property = "execInterval"),
            @Result(column = "delay_ms", property = "delayMs"),
            @Result(column = "exec_total", property = "execTotal"),
            @Result(column = "exec_time", property = "execTime"),
            @Result(column = "status", property = "status"),
            @Result(column = "err_msg", property = "errMsg"),
            @Result(column = "downgrade_class", property = "downgradeClass"),
            @Result(column = "downgrade_error_msg", property = "downgradeErrorMsg"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "modified_time", property = "modifiedTime")
    })
    List<ConsistencyTaskInstance> getUnfinishedTaskList(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
                                                        @Param("limitTask") Long limitTask);

    @Delete("DELETE FROM consistency_info WHERE id=#{id}")
    void remove(ConsistencyTaskInstance consistencyTaskInstance);

    @Update("UPDATE consistency_info SET " +
            "status=2, " +
            "exec_total=#{execTotal}, " +
            "exec_time=#{execTime} " +
            "WHERE id=#{id} ")
    void updateFailed(ConsistencyTaskInstance consistencyTaskInstance);
}

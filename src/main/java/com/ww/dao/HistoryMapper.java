package com.ww.dao;

import com.ww.model.HistoryTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface HistoryMapper {

    List<HistoryTask> selectMyTasksCompleted(@Param("userId") String userId);

    List<HistoryTask> selectMyProcessStarted(String userId);

    Map<String, Object> selectEndEventByTaskId(String taskId);

    int deleteHiEndEvent(String taskId);

    List<Map<String, Object>> selectHiTaskByTaskId(String taskId);

    List<Map<String, Object>> selectHiTaskByTaskKey(String taskKey);

    List<Map<String, Object>> selectHiVariablesByProInsId(String proInsId);

    Map<String, Object> selectIdentitylinkByTaskId(String taskId);

    List<Map<String, Object>> getVarinstList();
}
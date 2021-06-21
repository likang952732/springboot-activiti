package com.ww.service;

import com.ww.model.HistoryTask;

import java.util.List;
import java.util.Map;

/**
 * @Auther: Ace Lee
 * @Date: 2019/3/7 16:55
 */
public interface HistoryInfoService {

    List<HistoryTask> myTasksCompleted(String userId);

    List<HistoryTask> myProcessStarted(String userId);

    List<Map<String, Object>> test(String userId);
}

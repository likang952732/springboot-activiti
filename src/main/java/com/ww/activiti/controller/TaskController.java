package com.ww.activiti.controller;

import cn.hutool.core.collection.CollUtil;
import com.ww.activiti.vo.RuTask;
import com.ww.service.ProcessInfoService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 @Description
 *@author kang.li
 *@date 2021/6/17 15:59   
 */
@RestController
@RequestMapping("/task")
public class TaskController {
    @Autowired
    private ProcessInfoService processInfoService;

    /**
     * 查询我的审批任务
     * @param userName
     * @return
     */
    @GetMapping("/list")
    public Object findMyTaskList(String userName){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        List<Task> tasks = processEngine.getTaskService()
                .createTaskQuery()
                .taskAssignee(userName)
                .list();
        List<RuTask> taskList = new ArrayList<>();
        if(CollUtil.isNotEmpty(tasks)){
            List<String> instIdList = tasks.stream().map(Task::getProcessInstanceId).collect(Collectors.toList());
            List<Map<String, Object>> mapList = processInfoService.getActId(instIdList);
            RuTask ruTask = null;
            for (int i = 0; i < tasks.size(); i++){
                ruTask = new RuTask(tasks.get(i));
                ruTask.setStartActId(mapList.get(i).get("START_ACT_ID_"));
                ruTask.setEndActId(mapList.get(i).get("END_ACT_ID_"));
                taskList.add(ruTask);
            }
        }
        return taskList;
    }

}

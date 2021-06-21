package com.ww.activiti.controller;

import com.ww.model.HistoryTask;
import com.ww.service.HistoryInfoService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 历史管理
 *
 * @Auther: Ace Lee
 * @Date: 2019/3/7 11:43
 */
@RestController
@RequestMapping("history")
public class HistoryController {

    @Autowired
    private HistoryInfoService historyInfoService;
    @Autowired
    private HistoryService historyService;

    /**
     * 我的审批记录
     *
     * @param userId
     * @return
     */
    @GetMapping(value = "/tasks/myc")
    public Object myTasksCompleted(@RequestParam("userId") String userId) {
        List<HistoryTask> list = new ArrayList<>();
        List<HistoryTask> hiTasks = historyInfoService.myTasksCompleted(userId);
        setVariables(list, hiTasks);
        return list;
    }


    @GetMapping(value = "/test")
    public Object test(@RequestParam("userId") String userId) {
        List<HistoryTask> list = new ArrayList<>();
        List<Map<String, Object>> hiTasks = historyInfoService.test(userId);
        for (Map<String, Object> map : hiTasks) {
            String TEXT_ = map.get("TEXT_").toString();
            System.out.println(TEXT_);
           if("lk".equals(TEXT_)){
               System.out.println("true");
           }
        }
        //setVariables(list, hiTasks);
        return list;
    }




    /**
     * 我发起的记录
     *
     * @param userId
     * @return
     */
    @GetMapping(value = "/process/mys")
    public Object myProcessStarted(@RequestParam("userId") String userId) {
        List<HistoryTask> list = new ArrayList<>();
        List<HistoryTask> hiPros = historyInfoService.myProcessStarted(userId);
        setVariables(list, hiPros);
        return list;
    }

    private void setVariables(List<HistoryTask> listNew, List<HistoryTask> listOld) {
        if (!CollectionUtils.isEmpty(listOld)) {
            for (HistoryTask hipro : listOld) {
                List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().
                        processInstanceId(hipro.getProcInstId()).list();
                if (!CollectionUtils.isEmpty(variables)) {
                   /* for (HistoricVariableInstance variable : variables) {
                        hipro.put(variable.getVariableName(), variable.getValue());
                    }*/
                }
                listNew.add(hipro);
            }
        }
    }
}

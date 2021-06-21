package com.ww.activiti.controller;

import cn.hutool.core.util.StrUtil;
import com.ww.model.ApproveReason;
import com.ww.service.RuntimeInfoService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运行接口
 *
 * @Auther: Ace Lee
 * @Date: 2019/3/7 11:43
 */
@Slf4j
@RestController
@RequestMapping("runtime")
public class RuntimeController {

    @Autowired
    private RuntimeInfoService runtimeInfoService;

    @Autowired
    private TaskService taskService;


    /**
     * 驳回到上一个节点
     */
    @GetMapping("/reject/{taskId}")
    public String reject(@PathVariable String taskId) {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        HistoryService historyService = processEngine.getHistoryService();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //String taskId = "cca32273fdd1491b9061a4168f944a0c";   //这里根据你自己的taskid来写
        Map variables = new HashMap<>();
        //获取当前任务
        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .singleResult();
        //获取流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(currTask.getProcessInstanceId())
                .singleResult();
        //获取流程定义
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(currTask.getProcessDefinitionId());
        if (processDefinitionEntity == null) {
            System.out.println("不存在的流程定义。");
        }
        //获取当前activity
        ActivityImpl currActivity = ((ProcessDefinitionImpl) processDefinitionEntity)
                .findActivity(currTask.getTaskDefinitionKey());
        //获取当前任务流入
        List<PvmTransition> histTransitionList = currActivity
                .getIncomingTransitions();
        //清除当前活动出口
        List<PvmTransition> originPvmTransitionList = new ArrayList<PvmTransition>();
        List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
        for (PvmTransition pvmTransition : pvmTransitionList) {
            originPvmTransitionList.add(pvmTransition);
        }
        pvmTransitionList.clear();
        //查找上一个user task节点
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery().activityType("userTask")
                .processInstanceId(processInstance.getId())
                .finished()
                .orderByHistoricActivityInstanceEndTime().desc().list();
        TransitionImpl transitionImpl = null;
        if (historicActivityInstances.size() > 0) {
            ActivityImpl lastActivity = ((ProcessDefinitionImpl) processDefinitionEntity)
                    .findActivity(historicActivityInstances.get(0).getActivityId());
            //创建当前任务的新出口
            transitionImpl = currActivity.createOutgoingTransition(lastActivity.getId());
            transitionImpl.setDestination(lastActivity);
        }else{
            System.out.println("上级节点不存在。");
        }
        variables = processInstance.getProcessVariables();
        // 完成任务
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskDefinitionKey(currTask.getTaskDefinitionKey()).list();
        for (Task task : tasks) {
            taskService.complete(task.getId(), variables);
            historyService.deleteHistoricTaskInstance(task.getId());
        }
        // 恢复方向
        currActivity.getOutgoingTransitions().remove(transitionImpl);
        for (PvmTransition pvmTransition : originPvmTransitionList) {
            pvmTransitionList.add(pvmTransition);
        }
        return "success";
    }


    /**
     * 完成任务
     *
     * 这两个是本节点的业务数据---增加到所有节点
     *      dealReason: 处理原因
     *      dealType: 处理类型
     *
     * 这两个是本节点的业务数据---增加到本节点
     *      dealUserId: 操作人id
     *      dealUnitId: 操作人单位id
     *
     * 注意顺序，先给值，后完成。
     *
     * @param taskId
     * dealUserId  当前用户名
     * @return
     */
    @PostMapping(value = "tasks/do/{taskId}")
    public Object tasks(@PathVariable String taskId, @RequestBody Map<String, Object> params ) {
        boolean taskDo = true;
        if (null==params || params.isEmpty()){
            taskService.complete(taskId);
            return taskDo;
        }
        //驳回
        String dealType = (String) params.get("dealType");
        String dealReason = (String) params.get("dealReason");
        String startActId = (String) params.get("startActId");
        if ("1".equals(dealType)){
            //获取驳回节点定义key
            try {
                String rejectNodeId = (String) params.get("rejectNodeId");
              /*  if ("S00000".equals(rejectNodeId)){
                    completeTasks(taskId, params);
                }*/
                if (StrUtil.isNotBlank(startActId)){
                    completeTasks(taskId, params);
                } else {
                    taskService.setVariableLocal(taskId,"dealUserId",params.get("dealUserId"));
                    taskService.setVariableLocal(taskId,"dealUnitId",params.get("dealUnitId"));
                    taskService.setVariable(taskId,"dealType",dealType);
                    taskService.setVariable(taskId,"dealReason",dealReason);
                }
                taskDo = runtimeInfoService.rejected(taskId,rejectNodeId,dealReason);
            } catch (Exception e) {
                taskDo = false;
                log.error("驳回处理异常：{}",e);
            }
        //通过
        } else if ("0".equals(dealType)){
            completeTasks(taskId, params);
        }
        // 保存审批意见
        ApproveReason approveReason = new ApproveReason();
        approveReason.setApproveReason(dealReason);
        approveReason.setUsername(params.get("dealUserId").toString());
        approveReason.setProcInstId(params.get("procInstId").toString());
        runtimeInfoService.saveApproveReason(approveReason);
        return taskDo;
    }

    private void completeTasks(@PathVariable String taskId, @RequestBody Map<String, Object> params) {
        log.info("完成任务参数：taskId={} ,params={}",taskId,params);
        Map<String, Object> variables = new HashMap<>();
        variables.put("dealUserId",params.get("dealUserId"));  //传当前用户名
        variables.put("dealUnitId",params.get("dealUnitId"));
       /* variables.put("dealReason",params.get("dealReason"));*/
        taskService.setVariablesLocal(taskId,variables);
        variables = new HashMap<>();
        variables.put("dealType",params.get("dealType"));
        variables.put("dealReason",params.get("dealReason"));
        taskService.complete(taskId,variables);
        log.info("完成任务：任务ID："+taskId);
    }

    /**
     * 根据任务ID查询当前业务数据
     *
     * @param taskId
     * @return
     */
    @GetMapping(value = "/tasks/buss")
    public Object bussNow(@RequestParam("taskId") String taskId) {
        Map<String, Object> variables = taskService.getVariables(taskId);
        return variables;
    }


    /**
     * 我的待办任务
     *
     * @param userId
     * @return
     */
    @GetMapping(value = "/tasks/ing")
    public Object myTasks(@RequestParam("userId") String userId) {
        List<Map<String,Object>> list = new ArrayList<>();
        List<Map<String,Object>> tasks = runtimeInfoService.myTasks(userId);
        if (!CollectionUtils.isEmpty(tasks)){
            for (Map<String,Object> task:tasks){
                Map<String, Object> variables = taskService.getVariables((String) task.get("ID_"));
                task.putAll(variables);
                list.add(task);
            }
        }
        return list;
        /*//节点指定的人
        List<RuTask> list = new ArrayList<>();
        List<Task> listTask = taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskCreateTime().asc()
                .active()
                .list();
        if (listTask != null && listTask.size() > 0) {
            for (Task task : listTask) {
                list.add(new RuTask(task));
                log.info("流程定义的ID："+task.getProcessDefinitionId());
                log.info("流程实例的ID："+task.getProcessInstanceId());
                log.info("执行对象ID："+task.getExecutionId());
                log.info("任务ID："+task.getId());
                log.info("任务名称："+task.getName());
                log.info("任务创建的时间："+task.getCreateTime());
                log.info("================================");
            }
        }

        //节点指定的组
        //先查出人的角色
        List<String> groupIds = authorizationService.selectRoleIdsByUserId(userId);
        if (!CollectionUtils.isEmpty(groupIds)){
            List<Task> lists = taskService.createTaskQuery()
                    .taskCandidateGroupIn(groupIds)
                    .orderByTaskCreateTime().asc()
                    .active()
                    .list();
            if (lists != null && lists.size() > 0) {
                for (Task task : lists) {
                    list.add(new RuTask(task));
                    log.info("G流程定义的ID："+task.getProcessDefinitionId());
                    log.info("G流程实例的ID："+task.getProcessInstanceId());
                    log.info("G执行对象ID："+task.getExecutionId());
                    log.info("G任务ID："+task.getId());
                    log.info("G任务名称："+task.getName());
                    log.info("G任务创建的时间："+task.getCreateTime());
                    log.info("G================================");
                }
            }
        }
        return list;*/
    }

}

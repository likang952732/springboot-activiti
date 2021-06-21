package com.ww;

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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivitiApplicationTests {

	/*@Test
	public void contextLoads() {
	}*/


	//查询我的个人任务列表
	@Test
    public void findMyTaskList(){
	    String userName = "k";
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        List<Task> list = processEngine.getTaskService()
                    .createTaskQuery()
                   .taskAssignee(userName)//指定个人任务查询
                  .list();
        for(Task task:list ){
             System.out.println("id= "+task.getId());
             System.out.println("name= "+task.getName());
             System.out.println("assinee= "+task.getAssignee());
             System.out.println("createTime= "+task.getCreateTime());
             System.out.println("executionId= "+task.getExecutionId());
        }
    }


    //查看我的审批记录
    @Test
    public void getMyHistory() {


    }

    /**
     * 驳回到上一个节点
     */
    @Test
    public void reject() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        HistoryService historyService = processEngine.getHistoryService();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        String taskId = "cca32273fdd1491b9061a4168f944a0c";   //这里根据你自己的taskid来写
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
    }
}

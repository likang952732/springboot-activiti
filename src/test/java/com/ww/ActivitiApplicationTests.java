package com.ww;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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


    public static void main(String[] args) {
        String str  = "{\n" +
                "  \"name\": \"622test\",\n" +
                "  \"description\": \"\",\n" +
                "  \"revision\": 1,\n" +
                "  \"id\": \"c9fb8b3d7c964cf194648282034d656a\",\n" +
                "  \"model\": {\n" +
                "    \"resourceId\": \"c60ea27a1b80488893e29483bafedbb2\",\n" +
                "    \"properties\": {\n" +
                "      \"process_id\": \"process\",\n" +
                "      \"name\": \"\",\n" +
                "      \"documentation\": \"\",\n" +
                "      \"process_author\": \"\",\n" +
                "      \"process_version\": \"\",\n" +
                "      \"process_namespace\": \"http://www.activiti.org/processdef\",\n" +
                "      \"executionlisteners\": \"\",\n" +
                "      \"eventlisteners\": \"\",\n" +
                "      \"signaldefinitions\": \"\",\n" +
                "      \"messagedefinitions\": \"\"\n" +
                "    },\n" +
                "    \"stencil\": {\n" +
                "      \"id\": \"BPMNDiagram\"\n" +
                "    },\n" +
                "    \"childShapes\": [\n" +
                "      {\n" +
                "        \"resourceId\": \"sid-F230BAFB-1E84-433C-8B07-FD0442217E05\",\n" +
                "        \"properties\": {\n" +
                "          \"overrideid\": \"Y0000\",\n" +
                "          \"name\": \"开始\",\n" +
                "          \"documentation\": \"\",\n" +
                "          \"executionlisteners\": \"\",\n" +
                "          \"initiator\": \"\",\n" +
                "          \"formkeydefinition\": \"\",\n" +
                "          \"formproperties\": \"\"\n" +
                "        },\n" +
                "        \"stencil\": {\n" +
                "          \"id\": \"StartNoneEvent\"\n" +
                "        },\n" +
                "        \"childShapes\": [],\n" +
                "        \"outgoing\": [\n" +
                "          {\n" +
                "            \"resourceId\": \"sid-FA492BF0-072A-4301-9A5C-A118B62EADFD\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"bounds\": {\n" +
                "          \"lowerRight\": {\n" +
                "            \"x\": 156.79999999999995,\n" +
                "            \"y\": 122\n" +
                "          },\n" +
                "          \"upperLeft\": {\n" +
                "            \"x\": 126.79999999999995,\n" +
                "            \"y\": 92\n" +
                "          }\n" +
                "        },\n" +
                "        \"dockers\": []\n" +
                "      },\n" +
                "      {\n" +
                "        \"resourceId\": \"sid-0AAF5F32-5698-481B-B515-40E12EBB2A42\",\n" +
                "        \"properties\": {\n" +
                "          \"overrideid\": \"zgsp-001\",\n" +
                "          \"name\": \"主管审批\",\n" +
                "          \"documentation\": \"\",\n" +
                "          \"asynchronousdefinition\": \"false\",\n" +
                "          \"exclusivedefinition\": \"false\",\n" +
                "          \"executionlisteners\": \"\",\n" +
                "          \"multiinstance_type\": \"None\",\n" +
                "          \"multiinstance_cardinality\": \"\",\n" +
                "          \"multiinstance_collection\": \"\",\n" +
                "          \"multiinstance_variable\": \"\",\n" +
                "          \"multiinstance_condition\": \"\",\n" +
                "          \"isforcompensation\": \"false\",\n" +
                "          \"usertaskassignment\": {\n" +
                "            \"assignment\": {\n" +
                "              \"assignee\": \"张三\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"formkeydefinition\": \"\",\n" +
                "          \"duedatedefinition\": \"\",\n" +
                "          \"prioritydefinition\": \"10\",\n" +
                "          \"formproperties\": \"\",\n" +
                "          \"tasklisteners\": \"\"\n" +
                "        },\n" +
                "        \"stencil\": {\n" +
                "          \"id\": \"UserTask\"\n" +
                "        },\n" +
                "        \"childShapes\": [],\n" +
                "        \"outgoing\": [\n" +
                "          {\n" +
                "            \"resourceId\": \"sid-D92A93D8-A63F-4CF7-9DD6-BB335C0DB90D\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"bounds\": {\n" +
                "          \"lowerRight\": {\n" +
                "            \"x\": 301.79999999999995,\n" +
                "            \"y\": 147\n" +
                "          },\n" +
                "          \"upperLeft\": {\n" +
                "            \"x\": 201.79999999999995,\n" +
                "            \"y\": 67\n" +
                "          }\n" +
                "        },\n" +
                "        \"dockers\": []\n" +
                "      },\n" +
                "      {\n" +
                "        \"resourceId\": \"sid-FA492BF0-072A-4301-9A5C-A118B62EADFD\",\n" +
                "        \"properties\": {\n" +
                "          \"overrideid\": \"\",\n" +
                "          \"name\": \"\",\n" +
                "          \"documentation\": \"\",\n" +
                "          \"conditionsequenceflow\": \"\",\n" +
                "          \"executionlisteners\": \"\",\n" +
                "          \"defaultflow\": \"false\"\n" +
                "        },\n" +
                "        \"stencil\": {\n" +
                "          \"id\": \"SequenceFlow\"\n" +
                "        },\n" +
                "        \"childShapes\": [],\n" +
                "        \"outgoing\": [\n" +
                "          {\n" +
                "            \"resourceId\": \"sid-0AAF5F32-5698-481B-B515-40E12EBB2A42\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"bounds\": {\n" +
                "          \"lowerRight\": {\n" +
                "            \"x\": 200.95624999999995,\n" +
                "            \"y\": 107\n" +
                "          },\n" +
                "          \"upperLeft\": {\n" +
                "            \"x\": 157.40937499999995,\n" +
                "            \"y\": 107\n" +
                "          }\n" +
                "        },\n" +
                "        \"dockers\": [\n" +
                "          {\n" +
                "            \"x\": 15,\n" +
                "            \"y\": 15\n" +
                "          },\n" +
                "          {\n" +
                "            \"x\": 50,\n" +
                "            \"y\": 40\n" +
                "          }\n" +
                "        ],\n" +
                "        \"target\": {\n" +
                "          \"resourceId\": \"sid-0AAF5F32-5698-481B-B515-40E12EBB2A42\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"resourceId\": \"sid-7F639AD6-54D4-4B8A-AF87-87105D8A9841\",\n" +
                "        \"properties\": {\n" +
                "          \"overrideid\": \"y1111\",\n" +
                "          \"name\": \"hr审批\",\n" +
                "          \"documentation\": \"\",\n" +
                "          \"asynchronousdefinition\": \"false\",\n" +
                "          \"exclusivedefinition\": \"false\",\n" +
                "          \"executionlisteners\": \"\",\n" +
                "          \"multiinstance_type\": \"None\",\n" +
                "          \"multiinstance_cardinality\": \"\",\n" +
                "          \"multiinstance_collection\": \"\",\n" +
                "          \"multiinstance_variable\": \"\",\n" +
                "          \"multiinstance_condition\": \"\",\n" +
                "          \"isforcompensation\": \"false\",\n" +
                "          \"usertaskassignment\": {\n" +
                "            \"assignment\": {\n" +
                "              \"assignee\": \"李四\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"formkeydefinition\": \"\",\n" +
                "          \"duedatedefinition\": \"\",\n" +
                "          \"prioritydefinition\": \"20\",\n" +
                "          \"formproperties\": \"\",\n" +
                "          \"tasklisteners\": \"\"\n" +
                "        },\n" +
                "        \"stencil\": {\n" +
                "          \"id\": \"UserTask\"\n" +
                "        },\n" +
                "        \"childShapes\": [],\n" +
                "        \"outgoing\": [\n" +
                "          {\n" +
                "            \"resourceId\": \"sid-AB088FE9-022D-424E-A55E-4E337F572107\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"bounds\": {\n" +
                "          \"lowerRight\": {\n" +
                "            \"x\": 446.79999999999995,\n" +
                "            \"y\": 147\n" +
                "          },\n" +
                "          \"upperLeft\": {\n" +
                "            \"x\": 346.79999999999995,\n" +
                "            \"y\": 67\n" +
                "          }\n" +
                "        },\n" +
                "        \"dockers\": []\n" +
                "      },\n" +
                "      {\n" +
                "        \"resourceId\": \"sid-D92A93D8-A63F-4CF7-9DD6-BB335C0DB90D\",\n" +
                "        \"properties\": {\n" +
                "          \"overrideid\": \"\",\n" +
                "          \"name\": \"\",\n" +
                "          \"documentation\": \"\",\n" +
                "          \"conditionsequenceflow\": \"\",\n" +
                "          \"executionlisteners\": \"\",\n" +
                "          \"defaultflow\": \"false\"\n" +
                "        },\n" +
                "        \"stencil\": {\n" +
                "          \"id\": \"SequenceFlow\"\n" +
                "        },\n" +
                "        \"childShapes\": [],\n" +
                "        \"outgoing\": [\n" +
                "          {\n" +
                "            \"resourceId\": \"sid-7F639AD6-54D4-4B8A-AF87-87105D8A9841\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"bounds\": {\n" +
                "          \"lowerRight\": {\n" +
                "            \"x\": 345.95624999999995,\n" +
                "            \"y\": 107\n" +
                "          },\n" +
                "          \"upperLeft\": {\n" +
                "            \"x\": 302.64374999999995,\n" +
                "            \"y\": 107\n" +
                "          }\n" +
                "        },\n" +
                "        \"dockers\": [\n" +
                "          {\n" +
                "            \"x\": 50,\n" +
                "            \"y\": 40\n" +
                "          },\n" +
                "          {\n" +
                "            \"x\": 50,\n" +
                "            \"y\": 40\n" +
                "          }\n" +
                "        ],\n" +
                "        \"target\": {\n" +
                "          \"resourceId\": \"sid-7F639AD6-54D4-4B8A-AF87-87105D8A9841\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"resourceId\": \"sid-F769E138-7B47-47A4-A97F-48DCB44F2EB8\",\n" +
                "        \"properties\": {\n" +
                "          \"overrideid\": \"p0000\",\n" +
                "          \"name\": \"结束\",\n" +
                "          \"documentation\": \"\",\n" +
                "          \"executionlisteners\": \"\"\n" +
                "        },\n" +
                "        \"stencil\": {\n" +
                "          \"id\": \"EndNoneEvent\"\n" +
                "        },\n" +
                "        \"childShapes\": [],\n" +
                "        \"outgoing\": [],\n" +
                "        \"bounds\": {\n" +
                "          \"lowerRight\": {\n" +
                "            \"x\": 519.8,\n" +
                "            \"y\": 121\n" +
                "          },\n" +
                "          \"upperLeft\": {\n" +
                "            \"x\": 491.79999999999995,\n" +
                "            \"y\": 93\n" +
                "          }\n" +
                "        },\n" +
                "        \"dockers\": []\n" +
                "      },\n" +
                "      {\n" +
                "        \"resourceId\": \"sid-AB088FE9-022D-424E-A55E-4E337F572107\",\n" +
                "        \"properties\": {\n" +
                "          \"overrideid\": \"\",\n" +
                "          \"name\": \"\",\n" +
                "          \"documentation\": \"\",\n" +
                "          \"conditionsequenceflow\": \"\",\n" +
                "          \"executionlisteners\": \"\",\n" +
                "          \"defaultflow\": \"false\"\n" +
                "        },\n" +
                "        \"stencil\": {\n" +
                "          \"id\": \"SequenceFlow\"\n" +
                "        },\n" +
                "        \"childShapes\": [],\n" +
                "        \"outgoing\": [\n" +
                "          {\n" +
                "            \"resourceId\": \"sid-F769E138-7B47-47A4-A97F-48DCB44F2EB8\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"bounds\": {\n" +
                "          \"lowerRight\": {\n" +
                "            \"x\": 491.17499999999995,\n" +
                "            \"y\": 107\n" +
                "          },\n" +
                "          \"upperLeft\": {\n" +
                "            \"x\": 447.19062499999995,\n" +
                "            \"y\": 107\n" +
                "          }\n" +
                "        },\n" +
                "        \"dockers\": [\n" +
                "          {\n" +
                "            \"x\": 50,\n" +
                "            \"y\": 40\n" +
                "          },\n" +
                "          {\n" +
                "            \"x\": 14,\n" +
                "            \"y\": 14\n" +
                "          }\n" +
                "        ],\n" +
                "        \"target\": {\n" +
                "          \"resourceId\": \"sid-F769E138-7B47-47A4-A97F-48DCB44F2EB8\"\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"bounds\": {\n" +
                "      \"lowerRight\": {\n" +
                "        \"x\": 1200,\n" +
                "        \"y\": 1050\n" +
                "      },\n" +
                "      \"upperLeft\": {\n" +
                "        \"x\": 0,\n" +
                "        \"y\": 0\n" +
                "      }\n" +
                "    },\n" +
                "    \"stencilset\": {\n" +
                "      \"url\": \"stencilsets/bpmn2.0/bpmn2.0.json\",\n" +
                "      \"namespace\": \"http://b3mn.org/stencilset/bpmn2.0#\"\n" +
                "    },\n" +
                "    \"ssextensions\": []\n" +
                "  }\n" +
                "}";

        JSONObject json = JSONUtil.parseObj(str);
        String modelStr = json.getStr("model");
        JSONObject model = JSONUtil.parseObj(modelStr);
        JSONArray childShapeArray = JSONUtil.parseArray(model.getStr("childShapes"));
        JSONObject jsonObject = null, properties = null;
        List<String> list = new ArrayList<>();
        for (int i = 0; i < childShapeArray.size(); i++){
            jsonObject = childShapeArray.getJSONObject(i);
            properties = JSONUtil.parseObj(jsonObject.getStr("properties"));
            if(StrUtil.isBlank(properties.getStr("usertaskassignment"))){
                continue;
            }
            JSONObject usertaskassignment = JSONUtil.parseObj(properties.getStr("usertaskassignment"));
            String assignmentStr = usertaskassignment.getStr("assignment");
            JSONObject assignment = JSONUtil.parseObj(assignmentStr);
            String assignee = assignment.getStr("assignee");
            list.add(assignee);
        }
        list.stream().forEach(e -> System.out.println("e: " + e));
    }
}

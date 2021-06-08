package com.ww.service.impl;

import com.ww.dao.LeaveDao;
import com.ww.model.LeaveApply;
import com.ww.service.LeaveService;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @ClassName LeaveServiceImpl
 * @Description TODO
 * 工作流文件由前端上传，测试用本地读取
 * @Author ch.zhang
 * @Date 2019/6/10 18:37
 **/
@Service
public class LeaveServiceImpl implements LeaveService {

    @Resource
    LeaveDao leaveDao;

//    @Autowired
//    ProcessEngine processEngine;

    @Autowired
    IdentityService identityService;
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    TaskService taskService;

    /**
     * Description userId作为variables，用于调整申请时根据id查询本人的请假信息
     * leaveapplyId作为businesskey，流程完成后保存获取的流程实例id在leaveapply表中
     * @Param [apply, userid, variables]
     * @Return org.activiti.engine.runtime.ProcessInstance
     * @Author zhangcheng
     */
    @Override
    public ProcessInstance startWorkflow(LeaveApply apply, int userid, Map<String, Object> variables) {
        apply.setApplyTime(new Date().toString());
        apply.setUserId(userid);
        leaveDao.save(apply);
        String businesskey=String.valueOf(apply.getId());//使用leaveapply表的主键作为businesskey,连接业务数据和流程数据
        identityService.setAuthenticatedUserId(String.valueOf(userid));
        //ProcessInstance instance=runtimeService.startProcessInstanceByKey("myProcess",businesskey,variables);
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("process",businesskey,variables);
        System.out.println(businesskey);
        String instanceid = instance.getId();
        apply.setProcessInstanceId(instanceid);
        leaveDao.update(apply);
        return instance;
    }

    /**
     * Description 通过CandidateGroup查询任务，根据任务的businesskey获取流程实例
     * @Param [type, userid]
     * @Return java.util.List<com.activiti.entity.LeaveApply>
     * @Author zhangcheng
     * @Date 2019/6/14 11:02
     */
    @Override
    public List<LeaveApply> getApproveTaskList(String type,String userid) {
        List<LeaveApply> results = new ArrayList<>();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup(type).list();
        for(Task task:tasks){
            String instanceId = task.getProcessInstanceId();
            ProcessInstance ins = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
            String businesskey = ins.getBusinessKey();
            LeaveApply a = leaveDao.get(Integer.parseInt(businesskey));
            a.setTask(task);
            results.add(a);
        }
        return results;
    }

    /**
     * Description
     * @Param [taskName, userId]
     * @Return java.util.List<com.activiti.entity.LeaveApply>
     * @Author zhangcheng
     * @Date 2019/6/14 11:35
     */
    @Override
    public List<LeaveApply> getSubmitTaskList(String taskName, String userId) {
        List<LeaveApply> results = new ArrayList<>();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(userId).taskName(taskName).list();
        for (Task task : tasks) {
            String instanceId = task.getProcessInstanceId();
            ProcessInstance ins = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
            String businessKey = ins.getBusinessKey();
            LeaveApply a = leaveDao.get(Integer.parseInt(businessKey));
            a.setTask(task);
            results.add(a);
        }
        return results;
    }

    @Override
    public void submitcomplete(String taskid, LeaveApply leaveApply, String submitType) {
        Task task = taskService.createTaskQuery().taskId(taskid).singleResult();
        if(task == null){
            throw new  ActivitiObjectNotFoundException("找不到任务");
        }
        String instanceId = task.getProcessInstanceId();
        ProcessInstance ins = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        String businessKey = ins.getBusinessKey();
        //更新提交信息
        LeaveApply a = leaveDao.get(Integer.parseInt(businessKey));
        a.setStartTime(leaveApply.getStartTime());
        a.setEndTime(leaveApply.getEndTime());
        a.setLeaveTime(leaveApply.getLeaveTime());
        a.setLeaveType(leaveApply.getLeaveType());
        a.setReason(leaveApply.getReason());
        a.setApplyTime(new Date().toString());
        a.setUserJobId(leaveApply.getUserJobId());
        Map<String,Object> variables = new HashMap<>();
        variables.put("submitType",submitType);
        if(submitType.equalsIgnoreCase("true")){
            leaveDao.update(a);
        }
        taskService.complete(taskid,variables);
    }

    @Override
    public LeaveApply getleave(int id) {
        return leaveDao.get(id);
    }

    @Override
    public void remove(int id){
        leaveDao.remove(id);
    }

}

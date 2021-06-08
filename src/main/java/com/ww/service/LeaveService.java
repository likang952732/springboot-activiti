package com.ww.service;

import com.ww.model.LeaveApply;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.List;
import java.util.Map;

/**
 * @ClassName LeaveService
 * @Description TODO
 * @Author ch.zhang
 * @Date 2019/6/10 18:07
 **/
public interface LeaveService {

    /**
     * Description
     * 创建引擎，部署文件，启动流程
     * @Author zhangcheng
     */
    ProcessInstance startWorkflow(LeaveApply apply, int userId, Map<String, Object> variables);

    /**
     * Description 获取三级审批列表
     * @Date 2019/6/14 13:01
     * @Author zhangcheng
     */
    List<LeaveApply> getApproveTaskList(String type, String userId);

    /**
     * Description 获取申请打回的请求流程列表
     * @Date 2019/6/14 13:02
     * @Author zhangcheng
     */
    List<LeaveApply> getSubmitTaskList(String taskName, String userId);

    /**
     * Description 重新提交请求
     * @Date 2019/6/14 13:04
     * @Author zhangcheng
     */
    void submitcomplete(String taskid, LeaveApply leaveApply, String submitType);

    LeaveApply getleave(int id);

    void remove(int id);

}

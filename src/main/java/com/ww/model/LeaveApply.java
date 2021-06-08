package com.ww.model;

import lombok.Data;
import org.activiti.engine.task.Task;

/**
 * @ClassName LeaveApply 流程记录表
 * @Description TODO
 * @Author ch.zhang
 * @Date 2019/6/10 18:22
 **/
@Data
public class LeaveApply {

    /**
     * 主键
     */
    int id;
    /**
     * 流程实例id
     */
    String processInstanceId;
    /**
     * 请假人id
     */
    int userId;
    /**
     * 请假起始时间
     */
    String startTime;
    /**
     * 请假结束时间
     */
    String endTime;
    /**
     * 请假时长
     */
    int leaveTime;
    /**
     * 请假类型
     */
    String leaveType;
    /**
     * 请假原因
     */
    String reason;
    /**
     * 申请时间
     */
    String applyTime;
    /**
     * 职务代理人
     */
    int userJobId;
    Task task;
}

package com.ww.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName LeaveTask
 * @Description TODO
 * @Author ch.zhang
 * @Date 2019/6/12 14:58
 **/
@Data
public class LeaveTask implements Serializable{

    private static final long serialVersionUID = 8342440687413302565L;

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

    /**
     *任务id
     */
    String taskid;
    /**
     *任务名
     */
    String taskname;
    /**
     *流程定义id
     */
    String processdefid;
    /**
     *任务创建时间
     */
    Date taskCreateTime;

}

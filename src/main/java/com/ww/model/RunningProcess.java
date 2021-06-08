package com.ww.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName RunningProcess
 * @Description TODO
 * @Author ch.zhang
 * @Date 2019/6/14 15:14
 **/
@Data
public class RunningProcess implements Serializable{

    private static final long serialVersionUID = 5468330888295310617L;
    /**
     * 执行id
     */
    private String executionId;
    /**
     * 流程实例id
     */
    private String processInstanceId;
    /**
     * 业务号，和本地leaveApplyId关联
     */
    private String businessKey;
    /**
     * 当前节点
     */
    private String activityId;
}

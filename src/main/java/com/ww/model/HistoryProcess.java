package com.ww.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName HistoryProcess
 * @Description TODO
 * @Author ch.zhang
 * @Date 2019/6/14 18:02
 **/
@Data
public class HistoryProcess implements Serializable{
    private static final long serialVersionUID = -4517127827720086731L;
    /**
     * 流程定义id
     */
    private String processDefinitionId;
    /**
     * 业务号，和本地leaveApplyId关联
     */
    private String businessKey;

    private LeaveApply leaveApply;
}

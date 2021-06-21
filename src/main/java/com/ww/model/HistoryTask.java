package com.ww.model;

import lombok.Data;

/*
 @Description 审批历史
 *@author kang.li
 *@date 2021/6/18 10:35   
 */
@Data
public class HistoryTask {
    private String id;

    private String name;

    /**
     * 审批人
     */
    private String assignee;

    private String procInstId;

    /**
     * 审批意见
     */
    private String approveReason;

    private String startTime;

    private String endTime;

}

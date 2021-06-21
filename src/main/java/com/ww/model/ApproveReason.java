package com.ww.model;

import lombok.Data;

/*
 @Description 审批意见
 *@author kang.li
 *@date 2021/6/18 18:01   
 */
@Data
public class ApproveReason {
    /**
     * 流程实例id
     */
    private String procInstId;

    /**
     * 当前用户名
     */
    private String username;

    /**
     * 审批意见
     */
    private String approveReason;
}

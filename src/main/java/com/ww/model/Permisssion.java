package com.ww.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName Permisssion
 * @Description TODO
 * @Author ch.zhang
 * @Date 2019/6/11 18:11
 **/
@Data
public class Permisssion implements Serializable{

    private static final long serialVersionUID = -4820159006797144768L;

    /**
     * id
     */
    private int pid;

    /**
     * 审批类型
     */
    private String permissionName;
}

package com.ww.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName UserInfo 偷懒 将5张用户表测试数据通过一个查询的结果
 * @Description TODO
 * @Author cheng
 * @Date 2019/6/18 14:35
 **/
@Data
public class UserInfo implements Serializable{
    private static final long serialVersionUID = 133639293184246979L;

    private int id;

    private String username;

    private String password;

    private String tel;

    private int age;

    /**
     * 角色名称
     */
    private String rolename;

    /**
     * 审批类型
     */
    private String permissionName;
}

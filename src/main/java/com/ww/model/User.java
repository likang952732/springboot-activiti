package com.ww.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName User
 * @Description TODO
 * @Author ch.zhang
 * @Date 2019/6/5 19:11
 **/
@Data
public class User implements Serializable{

    private static final long serialVersionUID = 58350307873341216L;

    private int id;

    private String userName;

    private String password;

    private Integer userType;

    private String tel;

    private int age;
}

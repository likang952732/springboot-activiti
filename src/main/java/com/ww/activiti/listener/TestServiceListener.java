package com.ww.activiti.listener;

import org.springframework.stereotype.Component;

/*
 @Description
 *@author kang.li
 *@date 2021/6/9 14:26   
 */
@Component
public class TestServiceListener {

    public String getUser(String name) {
        return name;
    }
}

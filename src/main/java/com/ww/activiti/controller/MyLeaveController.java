package com.ww.activiti.controller;

import com.ww.model.LeaveApply;
import com.ww.model.User;
import com.ww.service.LeaveService;
import com.ww.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/*
 @Description
 *@author kang.li
 *@date 2021/6/17 14:04   
 */
@RestController
@RequestMapping("/myleave")
@Slf4j
public class MyLeaveController {
    @Autowired
    LeaveService leaveService;
    @Autowired
    UserService userService;
    @Autowired
    TaskService taskService;
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    HistoryService historyService;

    /**
     * Description 开始流程 把userid当做请假审批流程的标识
     * @Param [apply, session]
     * @Return java.lang.String
     * @Author zhangcheng
     * @Date 2019/6/11 20:52
     */
    @PostMapping(value="/startleave")
    public String start_leave(@RequestBody LeaveApply apply, HttpSession session){
        User user =(User) session.getAttribute("user");
        if(user == null){
            user = new User();
            user.setId(1);
        }
        Map<String,Object> variables = new HashMap<String, Object>();
        variables.put("applyuserid", String.valueOf(user.getId()));
        leaveService.startWorkflow(apply, user.getId(), variables);
        log.info("流程id{}已经启动", apply.getProcessInstanceId());
        return "sucess";
    }
}

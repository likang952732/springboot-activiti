package com.ww.activiti.controller;

import cn.hutool.json.JSONUtil;
import com.ww.common.DataGrid;
import com.ww.common.ResponseData;
import com.ww.model.*;
import com.ww.service.LeaveService;
import com.ww.service.UserService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName LeaveController
 * @Description TODO
 * @Author ch.zhang
 * @Date 2019/6/11 16:55
 **/
@RestController
@RequestMapping("/leave")
public class LeaveController {

    private static final Logger logger = LoggerFactory.getLogger(LeaveController.class);

    private static final String SMNAME = "填写表单信息";
    private static final String TLROLE = "部门经理";
    private static final String PLROLE = "总经理";
    private static final String HRROLE= "人事";
    private static final String TLPERMISSION = "部门领导审批";
    private static final String PLPERMISSION = "总经理审批";
    private static final String HRPERMISSION = "人事审批";

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

    @RequestMapping(value="/activiti")
    public ModelAndView index(){
        return new ModelAndView("activiti");
    }
    @RequestMapping(value="/submitform")
    public ModelAndView submitform(){
        return new ModelAndView("submitform");
    }
    @RequestMapping(value="/tlapprove")
    public ModelAndView tlapprove(){
        return new ModelAndView("tlapprove");
    }
    @RequestMapping(value="/plapprove")
    public ModelAndView plapprove(){
        return new ModelAndView("plapprove");
    }
    @RequestMapping(value="/hrapprove")
    public ModelAndView hrapprove(){
        return new ModelAndView("hrapprove");
    }
    @RequestMapping(value="/myprocess")
    public ModelAndView myprocess(){
        return new ModelAndView("myprocess");
    }
    @RequestMapping(value="/myhistory")
    public ModelAndView myhistory(){
        return new ModelAndView("myhistory");
    }

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
        variables.put("proDefId",apply.getProDefId());
        ProcessInstance ins = leaveService.startWorkflow(apply, user.getId(), variables);
        logger.info("流程id{}已经启动",ins.getId());
        return "sucess";
    }
    /**
     * Description 查询部门领导/平台领导/人事 审批确认代办列表
     * @Param [session]
     * @Return com.activiti.general.ResponseData<com.activiti.entity.LeaveTask>
     * @Author zhangcheng
     * @Date 2019/6/12 15:05
     */
    @PostMapping(value="/getSMTaskList")
    public DataGrid<LeaveTask> getSMTaskList(HttpServletRequest request, @RequestParam("current") int current, @RequestParam("rowCount") int rowCount){
        DataGrid<LeaveTask> grid = getLeaveTaskDataGrid(current, rowCount);
        User user = (User)request.getSession().getAttribute("user");
        List<LeaveApply> submitTaskList = leaveService.getSubmitTaskList(LeaveController.SMNAME,String.valueOf(user.getId()));
        List<LeaveTask> tasks =new ArrayList<>();
        for(LeaveApply apply:submitTaskList){
            LeaveTask task = new LeaveTask();
            task.setProcessInstanceId(apply.getProcessInstanceId());
            task.setUserId(apply.getUserId());
            task.setStartTime(apply.getStartTime());
            task.setEndTime(apply.getEndTime());
            task.setLeaveTime(apply.getLeaveTime());
            task.setLeaveType(apply.getLeaveType());
            task.setReason(apply.getReason());
            task.setApplyTime(apply.getApplyTime());
            task.setUserJobId(apply.getUserJobId());
            task.setTaskid(apply.getTask().getId());
            task.setTaskname(apply.getTask().getName());
            task.setTaskCreateTime(apply.getTask().getCreateTime());
            task.setProcessdefid(apply.getTask().getProcessDefinitionId());
            tasks.add(task);
        }
            grid.setTotal(tasks.size());
            grid.setRows(tasks);
            return grid;
    }
    @PostMapping(value="/getTLTaskList")
    public DataGrid<LeaveTask> getTLTaskList(HttpServletRequest request, @RequestParam("current") int current, @RequestParam("rowCount") int rowCount){
        //判断登录用户是否有部门领导权限,无权限返回空，有权限查询Task
        ResponseData<LeaveTask> responseData = new ResponseData<>();
        DataGrid<LeaveTask> grid = getLeaveTaskDataGrid(current, rowCount);
        return getLeaveTaskResponseData(request, grid,LeaveController.TLPERMISSION,LeaveController.TLROLE);//填写表单信息
    }

    @PostMapping(value="/getPLTaskList")
    public DataGrid<LeaveTask> getPLTaskList(HttpServletRequest request, @RequestParam("current") int current, @RequestParam("rowCount") int rowCount){
        DataGrid<LeaveTask> grid = getLeaveTaskDataGrid(current, rowCount);
        return getLeaveTaskResponseData(request, grid,LeaveController.PLPERMISSION,LeaveController.PLROLE);
    }

    @PostMapping(value="/getHRTaskList")
    public DataGrid<LeaveTask> getHRTaskList(HttpServletRequest request, @RequestParam("current") int current, @RequestParam("rowCount") int rowCount){
        DataGrid<LeaveTask> grid = getLeaveTaskDataGrid(current, rowCount);
        return getLeaveTaskResponseData(request, grid,LeaveController.HRPERMISSION,LeaveController.HRROLE);
    }

    private DataGrid<LeaveTask> getLeaveTaskResponseData(HttpServletRequest request, DataGrid<LeaveTask> grid, String perm, String role) {
        User user = (User)request.getSession().getAttribute("user");
        if(user == null){
            user = new User();
            user.setId(1);
        }
       /* List<Permisssion> permissions = userService.getPermission(user.getId());
        boolean flag = false;
        for(Permisssion permission:permissions){
            if(perm.equalsIgnoreCase(permission.getPermissionName())){
                flag = true;
            }
        }*/
        boolean flag = true;
        if(flag == false){
            return grid;
        }else{
            List<LeaveApply> approveTaskList = leaveService.getApproveTaskList(role, String.valueOf(user.getId()));
            List<LeaveTask> tasks =new ArrayList<>();
            for(LeaveApply apply:approveTaskList){
                LeaveTask task = new LeaveTask();
                task.setProcessInstanceId(apply.getProcessInstanceId());
                task.setUserId(apply.getUserId());
                task.setStartTime(apply.getStartTime());
                task.setEndTime(apply.getEndTime());
                task.setLeaveTime(apply.getLeaveTime());
                task.setLeaveType(apply.getLeaveType());
                task.setReason(apply.getReason());
                task.setApplyTime(apply.getApplyTime());
                task.setUserJobId(apply.getUserJobId());
                task.setTaskid(apply.getTask().getId());
                task.setTaskname(apply.getTask().getName());
                task.setTaskCreateTime(apply.getTask().getCreateTime());
                task.setProcessdefid(apply.getTask().getProcessDefinitionId());
                tasks.add(task);
            }
            grid.setTotal(tasks.size());
            grid.setRows(tasks);
            return grid;
        }
    }
    /**
     * Description DataGrid返回值的通用构造器
     * @Date 2019/6/14 16:05
     * @Author zhangcheng
     */
    private <E extends Serializable> DataGrid<E> getLeaveTaskDataGrid(@RequestParam("current") int current, @RequestParam("rowCount") int rowCount) {
        DataGrid<E> grid=new DataGrid<>();
        grid.setRowCount(rowCount);
        grid.setCurrent(current);
        grid.setTotal(0);
        grid.setRows(new ArrayList<E>());
        return grid;
    }

    /**
     * Description 部门领导/平台领导/人事 审批确认请假申请
     * @Param [approveData, request]
     * @Return com.activiti.general.ResponseData
     * @Author zhangcheng
     * @Date 2019/6/13 20:58
     */
    @PostMapping(value="/task/submitcomplete/{taskid}/{submitType}")
    public ResponseData submitcomplete(@ModelAttribute("leaveApply") LeaveApply leaveApply, @PathVariable("taskid") String taskid, @PathVariable("submitType") String submitType){
        try {
            leaveService.submitcomplete(taskid,leaveApply,submitType);
        } catch (ActivitiObjectNotFoundException e) {
            return ResponseData.error(500,"任务已处理");
        }
        return ResponseData.success();
    }
    @PostMapping(value="/task/tlcomplete")
    public ResponseData tlcomplete(@RequestBody ApproveData approveData, HttpServletRequest request){
        User user = (User)request.getSession().getAttribute("user");
        Map<String,Object> variables=new HashMap<String,Object>();
        variables.put("tlApprove", approveData.getApprove());
        variables.put("leaveTime",approveData.getLeaveTime());
        try {
            taskService.claim(approveData.getTaskid(), String.valueOf(user.getId()));
        } catch (ActivitiObjectNotFoundException e) {
            return ResponseData.error(500,"任务已处理");
        }
        taskService.complete(approveData.getTaskid(), variables);
        return ResponseData.success();
    }
    @RequestMapping(value="/task/plcomplete",method = RequestMethod.POST)
    public ResponseData plcomplete(@RequestBody ApproveData approveData, HttpServletRequest request){
        User user = (User)request.getSession().getAttribute("user");
        Map<String,Object> variables=new HashMap<String,Object>();
        variables.put("plApprove", approveData.getApprove());
        try {
            taskService.claim(approveData.getTaskid(), String.valueOf(user.getId()));
        } catch (ActivitiObjectNotFoundException e) {
            return ResponseData.error(500,"任务已处理");
        }
        taskService.complete(approveData.getTaskid(), variables);
        return ResponseData.success();
    }
    @RequestMapping(value="/task/hrcomplete",method = RequestMethod.POST)
    public ResponseData hrcomplete(@RequestBody ApproveData approveData, HttpServletRequest request){
        User user = (User)request.getSession().getAttribute("user");
        Map<String,Object> variables=new HashMap<String,Object>();
        variables.put("hrApprove", approveData.getApprove());
        try {
            taskService.claim(approveData.getTaskid(), String.valueOf(user.getId()));
        } catch (ActivitiObjectNotFoundException e) {
            return ResponseData.error(500,"任务已处理");
        }
        taskService.complete(approveData.getTaskid(), variables);
        return ResponseData.success();
    }
    /**
     * Description 表单修改详情查询
     * @Date 2019/6/14 15:41
     * @Author zhangcheng
     */
    @PostMapping(value="/dealtask")
    public String taskdeal(@RequestParam("taskid") String taskid, HttpServletResponse response){
        Task task=taskService.createTaskQuery().taskId(taskid).singleResult();
        ProcessInstance process=runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        LeaveApply leave=leaveService.getleave(new Integer(process.getBusinessKey()));
        return JSONUtil.parse(leave).toString();
    }

    /**
     * Description 查询我发起的请假流程
     * @Param [session, current, rowCount]
     * @Return com.activiti.general.DataGrid<com.activiti.entity.RunningProcess>
     * @Author zhangcheng
     * @Date 2019/6/14 15:43
     */
    @GetMapping(value="getProcessList")
    public DataGrid<RunningProcess> getProcessList(HttpServletRequest request, int current, int rowCount){
        DataGrid<RunningProcess> grid = getLeaveTaskDataGrid(current, rowCount);
        User user = (User)request.getSession().getAttribute("user");
        if(user == null){
            user = new User();
            user.setId(1);
        }
      /*  List<ProcessInstance> a = runtimeService.createProcessInstanceQuery().processDefinitionKey("myProcess")
            .involvedUser(String.valueOf(user.getId())).list();*/
        List<ProcessInstance> a = runtimeService.createProcessInstanceQuery().processDefinitionKey("process")
                .involvedUser(String.valueOf(user.getId())).list();
        List<RunningProcess> list = new ArrayList<>();
        for (ProcessInstance p : a) {
            if(StringUtils.isEmpty(p.getBusinessKey())){
                continue;
            }
            LeaveApply leaveApply = leaveService.getleave(Integer.valueOf(p.getBusinessKey()));
            if(leaveApply == null || user.getId() != leaveApply.getUserId()){
                continue;
            }
            RunningProcess process = new RunningProcess();
            process.setExecutionId(p.getId());
            process.setProcessInstanceId(p.getProcessInstanceId());
            process.setBusinessKey(p.getBusinessKey());
            process.setActivityId(p.getActivityId());
            process.setDeploymentId(p.getDeploymentId());
            list.add(process);
        }
        grid.setTotal(list.size());
        grid.setRows(list);
        return grid;
    }

    /**
     * Description 查询我的请假历史记录
     * @Date 2019/6/14 19:36
     * @Author zhangcheng
     */
    @PostMapping(value = "getHistoryList")
    public DataGrid<HistoryProcess> getHistoryList(HttpServletRequest request, @RequestParam("current") int current, @RequestParam("rowCount") int rowCount){
        DataGrid<HistoryProcess> grid = getLeaveTaskDataGrid(current,rowCount);
        User user = (User)request.getSession().getAttribute("user");
        List<HistoricProcessInstance> historyList = historyService.createHistoricProcessInstanceQuery()
            .processDefinitionKey("myProcess").startedBy(String.valueOf(user.getId())).finished().list();
        List<HistoryProcess> list = new ArrayList<>();
        for (HistoricProcessInstance history : historyList) {
            HistoryProcess hp = new HistoryProcess();
            if(StringUtils.isEmpty(history.getBusinessKey())){
                continue;
            }
            LeaveApply leaveApply = leaveService.getleave(Integer.valueOf(history.getBusinessKey()));
            hp.setLeaveApply(leaveApply);
            hp.setBusinessKey(history.getBusinessKey());
            hp.setProcessDefinitionId(history.getProcessDefinitionId());
            list.add(hp);
        }
        grid.setTotal(list.size());
        grid.setRows(list);
        return grid;
    }
    @PostMapping(value="processinfo/{processInstanceId}")
    public List<HistoricActivityInstance> processinfo(@PathVariable("processInstanceId")String processInstanceId){
        List<HistoricActivityInstance> his = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();
        return his;
    }

    @GetMapping(value = "traceProcess/{processInstanceId}")
    public void traceProcess(@PathVariable("processInstanceId") String processInstanceId, HttpServletResponse response) throws IOException {
        //获取所有活动，用于测试
        //List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
        ProcessInstance process = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(process.getProcessDefinitionId());
        //获得所有历史活动，按时间升序排序
        List<HistoricActivityInstance> historicList = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();
        //计算活动线路，已执行的历史节点
        List<String> executedActivitiIds = new ArrayList<>();
        historicList.forEach(e->{executedActivitiIds.add(e.getActivityId());});
        //已执行的flow集合
        List<String> excutedFlowList = new ArrayList<>();
        for (HistoricActivityInstance historic : historicList) {
            FlowNode flowNode = (FlowNode)bpmnModel.getFlowElement(historic.getActivityId());
            List<SequenceFlow> sequenceFlows = flowNode.getOutgoingFlows();
            sequenceFlows.forEach(e->{
               /* if(e.getTargetFlowElement().getId().equalsIgnoreCase(historic.getActivityId())){
                    excutedFlowList.add(e.getId());
                }*/
            });
        }
        InputStream png = new DefaultProcessDiagramGenerator().generateDiagram(bpmnModel, "png", executedActivitiIds,
            excutedFlowList,"黑体","黑体","黑体",null,1.0);
        ServletOutputStream outputStream = response.getOutputStream();
        IOUtils.copy(png,outputStream);
    }

    /**
     * Description 撤销流程 流程未结束
     * @Param [processInstanceId]
     * @Return com.activiti.general.ResponseData
     * @Author zhangcheng
     * @Date 2019/6/17 19:40
     */
    @GetMapping(value = "endProcess/{processInstanceId}")
    public ResponseData processInstanceId(@PathVariable("processInstanceId")String processInstanceId){
        try {
            runtimeService.deleteProcessInstance(processInstanceId,"任务主动撤销");
            historyService.deleteHistoricProcessInstance(processInstanceId);
        } catch (ActivitiObjectNotFoundException e) {
            return ResponseData.error(5000,"资源已经删除");
        }
        return ResponseData.success();
    }

    /**
     * Description 删除历史记录 流程已经结束
     * @Param [processInstanceId]
     * @Return com.activiti.general.ResponseData
     * @Author zhangcheng
     * @Date 2019/6/17 19:40
     */
    @GetMapping(value = "removeProcess/{processInstanceId}")
    public ResponseData removeProcess(@PathVariable("processInstanceId")String processInstanceId){
        try {
            historyService.deleteHistoricProcessInstance(processInstanceId);
            leaveService.remove(Integer.valueOf(processInstanceId));
        } catch (ActivitiObjectNotFoundException e) {
            return ResponseData.error(5000,"资源已经删除");
        }
        return ResponseData.success();
    }

}

package com.ww.activiti.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ww.common.RestServiceController;
import com.ww.common.Result;
import com.ww.service.ProcessInfoService;
import com.ww.util.Status;
import com.ww.util.ToWeb;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 模型管理
 *
 * @auther: Ace Lee
 * @date: 2019/3/7 20:14
 */
@RestController
@RequestMapping("/model")
public class ModelerController implements RestServiceController<Model, String>{

    @Autowired
    RepositoryService repositoryService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private ProcessInfoService processInfoService;

    /**
     * 新建一个空模型
     * @return
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/newModel")
    public String newModel() throws UnsupportedEncodingException {
        //初始化一个空模型
        Model model = repositoryService.newModel();

        //设置一些默认信息
        String name = "new-process";
        String description = "";
        int revision = 1;
        String key = "process";

        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

        model.setName(name);
        model.setKey(key);
        model.setMetaInfo(modelNode.toString());

        repositoryService.saveModel(model);
        String id = model.getId();

        //完善ModelEditorSource
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace",
                "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.put("stencilset", stencilSetNode);
        repositoryService.addModelEditorSource(id,editorNode.toString().getBytes("utf-8"));
        //return ToWeb.buildResult().redirectUrl("/editor?modelId="+id);
        return id;
    }

    /**
     * 获取流程定义json数据
     *
     * @param modelId
     * @return
     */
    @GetMapping(value = "/{modelId}/json")
    public ObjectNode getEditorJson(@PathVariable String modelId) {
        ObjectNode modelNode = null;

        Model model = repositoryService.getModel(modelId);

        if (model != null) {
            try {
                if (StringUtils.isNotEmpty(model.getMetaInfo())) {
                    modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
                } else {
                    modelNode = objectMapper.createObjectNode();
                    modelNode.put("name", model.getName());
                }
                modelNode.put("id", model.getId());
                byte[] modelEditorSource = repositoryService.getModelEditorSource(model.getId());
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(new String(modelEditorSource, StandardCharsets.UTF_8));
                modelNode.putPOJO("model", editorJsonNode);
            } catch (Exception e) {
                throw new ActivitiException("Error creating model JSON", e);
            }
        }
        return modelNode;
    }


    /**
     * 发布模型为流程定义
     * @param id
     * @return
     * @throws Exception
     */
    @PostMapping("{id}/deployment")
    public Object deploy(@PathVariable("id")String id) throws Exception {
        //获取模型
        Model modelData = repositoryService.getModel(id);
        if(StrUtil.isNotBlank(modelData.getDeploymentId())) {
            return "模型已发布";
        }
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
        if (bytes == null) {
            return ToWeb.buildResult().status(Status.FAIL)
                    .msg("模型数据为空，请先设计流程并成功保存，再进行发布。");
        }
        JsonNode modelNode = new ObjectMapper().readTree(bytes);
        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if(model.getProcesses().size()==0){
            return ToWeb.buildResult().status(Status.FAIL)
                    .msg("数据模型不符要求，请至少设计一条主线流程。");
        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "UTF-8"))
                .deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);
        try {
            //创建流程文件
            ProcessDefinition processDefinition=repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
            InputStream processDiagram = repositoryService.getProcessDiagram(processDefinition.getId());
            FileUtils.copyInputStreamToFile(processDiagram, new File("D:/deployments/"+modelData.getName()+".png"));

            InputStream processBpmn = repositoryService.getResourceAsStream(deployment.getId(), processDefinition.getResourceName());
            FileUtils.copyInputStreamToFile(processBpmn,new File("D:/deployments/"+modelData.getName()+".bpmn"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ToWeb.buildResult().refresh();
    }

    /**
     *
     * 获取所有模型
     *
     * @auther: Ace Lee
     * @date: 2019/3/7 16:27
     */
    @GetMapping("models")
    public Result<List<Map<String,Object>>> list() {
        List<Map<String,Object>> list = processInfoService.models();
        return new Result<>(list);
    }


    /**
     * 获取已启动的流程
     * @return
     */
    @GetMapping("/start/models")
    public Result<List<Map<String,Object>>> getStartList() {
        List<Map<String,Object>> list = processInfoService.getStartList();
        return new Result<>(list);
    }


    @Override
    public Object getOne(@PathVariable("id") String id) {
        Model model = repositoryService.createModelQuery().modelId(id).singleResult();
        return ToWeb.buildResult().setObjData(model);
    }

    @Override
    public Object getList(@RequestParam(value = "rowSize", defaultValue = "1000", required = false) Integer rowSize, @RequestParam(value = "page", defaultValue = "1", required = false) Integer page) {
        List<Model> list = repositoryService.createModelQuery().listPage(rowSize * (page - 1)
                , rowSize);
        long count = repositoryService.createModelQuery().count();

        return ToWeb.buildResult().setRows(
                ToWeb.Rows.buildRows().setCurrent(page)
                        .setTotalPages((int) (count/rowSize+1))
                        .setTotalRows(count)
                        .setList(list)
                        .setRowSize(rowSize)
        );
    }

    @GetMapping("/delete/{id}")
    public Object deleteOne(@PathVariable("id")String id){
        repositoryService.deleteModel(id);
        return ToWeb.buildResult().refresh();
    }

    @Override
    public Object postOne(@RequestBody Model entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object putOne(@PathVariable("id") String s, @RequestBody Model entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object patchOne(@PathVariable("id") String s, @RequestBody Model entity) {
        throw new UnsupportedOperationException();
    }


    private List<String> getAsssList(ObjectNode editorJsonNode){
        List<String> list = new ArrayList<>();
        //JsonNode childShapes = editorJsonNode.get("childShapes");
        JSONObject model = JSONUtil.parseObj(editorJsonNode.toString());
        JSONArray childShapeArray = JSONUtil.parseArray(model.getStr("childShapes"));
        JSONObject jsonObject = null, properties = null;
        for (int i = 0; i < childShapeArray.size(); i++){
            jsonObject = childShapeArray.getJSONObject(i);
            properties = JSONUtil.parseObj(jsonObject.getStr("properties"));
            if(StrUtil.isBlank(properties.getStr("usertaskassignment"))){
                continue;
            }
            JSONObject usertaskassignment = JSONUtil.parseObj(properties.getStr("usertaskassignment"));
            String assignmentStr = usertaskassignment.getStr("assignment");
            JSONObject assignment = JSONUtil.parseObj(assignmentStr);
            String assignee = assignment.getStr("assignee");
            list.add(assignee);
        }
        return list;
    }
}

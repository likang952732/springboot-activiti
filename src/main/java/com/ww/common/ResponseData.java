package com.ww.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @ClassName ResponseData
 * @Description TODO
 * @Author ch.zhang
 * @Date 2019/6/11 19:39
 **/
@Data
public class ResponseData<T> implements Serializable{
    private static final long serialVersionUID = 8093964726153220408L;

    public static final int SUCCESS_CODE = 0;

    public static final String SUCCESS_MSG = "";

    private DataSetDTO<T> data;
    /**
     * 返回码
     */
    private int code;

    /**
     * 返回消息
     */
    private String msg;

    /**
     * Description 成功响应
     * @Return com.activiti.general.ResponseData<T>
     * @Author zhangcheng
     * @Date 2019/6/11 20:04
     */
    public static <T> ResponseData<T> success(){
        ResponseData<T> response = new ResponseData<>();
        response.setData(new DataSetDTO<>());
        response.getData().setItems(Collections.emptyList());
        response.getData().setCount(0);
        response.getData().setPage(null);
        response.setCode(SUCCESS_CODE);
        response.setMsg(SUCCESS_MSG);
        return response;
    }
    public static <T> ResponseData<T> success(List<T> items){
        ResponseData<T> response = new ResponseData<>();
        response.setData(new DataSetDTO<>());
        response.getData().setItems(items);
        response.getData().setCount(items.size());
        response.getData().setPage(null);
        response.setCode(SUCCESS_CODE);
        response.setMsg(SUCCESS_MSG);
        return response;
    }

    /**
     * Description 失败响应
     * @Param [code, message]
     * @Return com.activiti.general.ResponseData<T>
     * @Author zhangcheng
     * @Date 2019/6/11 20:05
     */
    public static <T> ResponseData<T> error(int code,String message){
        ResponseData response = success();
        return response.failure(code,message);
    }

    public ResponseData<T> failure(int code,String message){
        this.setCode(code);
        this.setMsg(message);
        return this;
    }
}

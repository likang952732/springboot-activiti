package com.ww.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName ApproveData
 * @Description TODO
 * @Author ch.zhang
 * @Date 2019/6/13 20:07
 **/
@Data
public class ApproveData implements Serializable {
    private static final long serialVersionUID = 8138932077903842668L;

    private String leaveTime;
    private String taskid;
    private String approve;
}

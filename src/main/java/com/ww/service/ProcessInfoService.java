package com.ww.service;

import java.util.List;
import java.util.Map;

/**
 * @Auther: Ace Lee
 * @Date: 2019/3/7 16:55
 */
public interface ProcessInfoService {

    List<Map<String, Object>> models();

    List<Map<String, Object>> process();

    List<Map<String, Object>> getStartList();


    List<Map<String, Object>> getActId(List<String> instIdList);

    int addAssList(Map<String, Object> map);

    Map<String, Object> getAssList(String procInsId);

    Map<String, Object> getModel(String proDefId);

}

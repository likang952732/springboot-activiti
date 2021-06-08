package com.ww.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ww.dao.ProcessMapper;
import com.ww.service.ProcessInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @Auther: Ace Lee
 * @Date: 2019/3/7 16:55
 */
@Service
public class ProcessInfoServiceImpl implements ProcessInfoService {

    @Autowired
    private ProcessMapper processMapper;

    @Override
    public List<Map<String, Object>> models() {
        List<Map<String, Object>> list = processMapper.selectModels();
        for (Map<String, Object> map: list) {
            JSONObject json = JSONUtil.parseObj(map.get("META_INFO_"));
            map.put("name",json.getStr("name"));
            map.put("revision",json.getStr("revision"));
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> process() {
        return processMapper.selectProcess();
    }

}

package com.ww.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProcessMapper {

    List<Map<String, Object>> selectModels();

    List<Map<String, Object>> selectProcess();

    List<Map<String, Object>> getStartList();

    List<Map<String, Object>> getActId(@Param("instIdList") List<String> instIdList);

}
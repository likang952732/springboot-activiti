package com.ww.dao;

import com.ww.model.LeaveApply;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName LeaveDao
 * @Description TODO
 * @Author ch.zhang
 * @Date 2019/6/11 20:54
 **/
@Mapper
public interface LeaveDao {

    void save(LeaveApply leaveApply);

    void update(LeaveApply leaveApply);

    LeaveApply get(int id);

    void remove(int id);
}

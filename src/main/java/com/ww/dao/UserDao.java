package com.ww.dao;

import com.ww.model.Permisssion;
import com.ww.model.User;
import com.ww.model.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface UserDao {
    List<UserInfo> getUserInfoList();

    User login(@Param("user") User user);

    List<Permisssion> getPermission(int id);
}

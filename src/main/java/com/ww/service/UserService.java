package com.ww.service;

import com.ww.model.Permisssion;
import com.ww.model.User;
import com.ww.model.UserInfo;

import java.util.List;

public interface UserService {

    User queryById(Long id);

    List<UserInfo> getUserInfoList();

    User login(User user);

    List<Permisssion> getPermission(int id);
}

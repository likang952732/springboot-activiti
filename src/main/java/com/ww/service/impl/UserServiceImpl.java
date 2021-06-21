package com.ww.service.impl;

import cn.hutool.crypto.digest.MD5;
import com.ww.dao.UserDao;
import com.ww.model.Permisssion;
import com.ww.model.User;
import com.ww.model.UserInfo;
import com.ww.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName UserServiceImpl
 * @Description TODO
 * @Author ch.zhang
 * @Date 2019/6/5 19:15
 **/
@Service
public class UserServiceImpl implements UserService {

    @Resource
    UserDao userDao;

    @Override
    public User queryById(Long id) {
        return null;
    }

    /**
     * Description 查询用户列表
     * @Date 2019/6/12 15:18
     * @Author zhangcheng
     */
    @Override
    public List<UserInfo> getUserInfoList() {
        return userDao.getUserInfoList();
    }

    /**
     * Description 根据登录用户名称获取用户信息
     * @Date 2019/6/12 15:18
     * @Author zhangcheng
     */
    @Override
    public User login(User user) {
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        return userDao.login(user);
    }

    /**
     * Description 根据用户id获取用户权限
     * @Date 2019/6/12 15:19
     * @Author zhangcheng
     */
    @Override
    public List<Permisssion> getPermission(int id) {
        return userDao.getPermission(id);
    }
}

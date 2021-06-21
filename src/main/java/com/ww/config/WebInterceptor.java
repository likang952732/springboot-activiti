package com.ww.config;

import com.ww.common.exception.BusinessException;
import com.ww.model.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ClassName WebInterceptor
 * @Description
 * @Author ch.zhang
 * @Date 2019/6/11 18:16
 **/
@Component
public class WebInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        User user = (User)request.getSession().getAttribute("user");
       /* if(user == null){
            throw new BusinessException("未登录");
            //response.sendRedirect(request.getContextPath()+"/toLogin");
        }*/
        return true;
    }
}

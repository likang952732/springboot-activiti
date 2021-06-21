package com.ww.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 @Description
 *@author kang.li
 *@date 2021/6/9 9:27   
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    WebInterceptor webInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/activiti/**")
                .allowedOrigins("*","https://www.baidu.com/")
                .allowCredentials(true)
                .allowedMethods("GET","POST","PUT","DELETE","HEAD");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/toLogin").setViewName("login");
    }


    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webInterceptor).addPathPatterns("/**").excludePathPatterns(
                "/toLogin","/login","/error","/img/**","/css/**","/js/**","/font/**","/ico/**"
        );
    }

}

package com.ww.activiti.controller;

import org.activiti.engine.ActivitiException;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.Objects;

/*
 @Description
 *@author kang.li
 *@date 2021/6/5 14:06   
 */
@RestController
@RequestMapping("/editor")
public class StencilsetController {

    @GetMapping(value = "/stencilset")
    public String getStencilset() {
        InputStream stencilsetStream = this.getClass().getClassLoader().getResourceAsStream("stencilset.json");
        try {
            return IOUtils.toString(Objects.requireNonNull(stencilsetStream), "utf-8");
        } catch (Exception e) {
            throw new ActivitiException("Error while loading stencil set", e);
        }
    }
}

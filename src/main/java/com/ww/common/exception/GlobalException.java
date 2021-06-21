
package com.ww.common.exception;

import com.ww.common.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import javax.xml.bind.ValidationException;

/*
 @Description 全局异常处理
 *@author kang.li
 *@date 2021/2/19 13:48   
 */
@Slf4j
@ControllerAdvice
public class GlobalException {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public CommonResult handle(Exception e) {
        if (e instanceof BusinessException) {
            log.error("业务逻辑处理异常：{}", ((BusinessException) e).getMsg());
            return CommonResult.failed(((BusinessException) e).getMsg());
        } else if (e instanceof DataIntegrityViolationException) {
            return CommonResult.failed("数据库异常");
        } else if (e instanceof BadSqlGrammarException){
            return CommonResult.failed("sql查询异常");
        }
        log.error("系统异常：{}", e);
        return CommonResult.failed("系统正忙,请稍后在试......");
    }

    @ExceptionHandler(value = ValidationException.class)
    @ResponseBody
    public CommonResult handle(ValidationException e) {
        if (e instanceof ValidationException) {
            String message = "";
            if(e.getCause() instanceof BusinessException) {
                message = ((BusinessException) e.getCause()).getMsg();
                log.error("业务参数验证异常：{}", e);
                return CommonResult.failed(message);
            }
            return CommonResult.failed("系统正忙,请稍后在试......");
        }
        log.error("系统异常：{}", e);
        return CommonResult.failed(e.getMessage());
    }

    /*@ExceptionHandler(value = SQLException.class)
    @ResponseBody
    public CommonResult handle(SQLException e) {
        if (e instanceof SQLException) {
            return CommonResult.failed("sql异常");
        }
        log.error("sql异常：{}", e);
        return CommonResult.failed(e.getMessage());
    }*/


    @ExceptionHandler(MultipartException.class)
    @ResponseBody
    public CommonResult exception(MaxUploadSizeExceededException e){
        if (e.getCause().getCause() instanceof FileSizeLimitExceededException){//单个文件大小超出限制抛出的异常
            log.error("message异常信息: {}, cause异常信息: {}", e.getMessage(), e.getCause().getCause());
            return CommonResult.failed("单个上传文件大小不能超过2MB");
        }/*else if (e.getCause().getCause() instanceof SizeLimitExceededException){//总文件大小超出限制抛出的异常
            log.error("message异常信息为=========》"+e.getMessage());
            log.error("cause异常信息为=========》"+e.getCause().getCause());
            Result result = new Result(104, null, "总上传文件大小不能超过200MB", null);
            String s = JSON.toJSONString(result);
            return s;
        }*/
        return  CommonResult.failed("上传文件异常");
    }

}

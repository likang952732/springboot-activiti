package com.ww.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 @Description 业务异常处理
 *@author kang.li
 *@date 2021/2/19 13:46   
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessException extends RuntimeException {
    private String msg;
}

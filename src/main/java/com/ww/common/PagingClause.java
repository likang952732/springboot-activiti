package com.ww.common;

import java.io.Serializable;

/**
 * @ClassName PagingClause
 * @Description TODO
 * @Author ch.zhang
 * @Date 2019/6/11 19:51
 **/
public class PagingClause implements Serializable{
    private static final long serialVersionUID = -4252019456288636276L;

    /**
     * 当前页码
     */
    private int no;

    /**
     * 页面大小
     */
    private int size;

    public PagingClause(int no, int size){
        super();
        this.no = no;
        this.size = size;
    }
}

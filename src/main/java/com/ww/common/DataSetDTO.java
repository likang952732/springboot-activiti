package com.ww.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName DataSetDTO
 * @Description TODO
 * @Author ch.zhang
 * @Date 2019/6/11 19:47
 **/
@Getter
@Setter
public class DataSetDTO<T> implements Serializable {
    private static final long serialVersionUID = 8764362428377920870L;

    /**
     * 数据集合
     */
    private List<T> items;

    /**
     * 数据总量
     */
    private long count;

    /**
     *分页
     */
    private PagingClause page;

    public DataSetDTO(){
        super();
        this.items = new ArrayList<>();
    }


}

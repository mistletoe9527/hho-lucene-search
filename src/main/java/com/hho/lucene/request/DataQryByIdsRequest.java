package com.hho.lucene.request;

import lombok.Data;

import java.util.List;

@Data
public class DataQryByIdsRequest extends Page {


    /**
     * 查询查询的ids
     */
    private List<Long> ids;


    /**
     * 排序 true倒叙
     */
    private Boolean desc;
}

package com.hho.lucene.request;

import lombok.Data;


@Data
public class DataQryByTitleRequest extends Page {

    /**
     * 模糊查询title
     */
    private String title;

    /**
     * 相似度
     */
    private Boolean prefixSearch;

    /**
     * 查询串
     */
    private String queryStr;
}

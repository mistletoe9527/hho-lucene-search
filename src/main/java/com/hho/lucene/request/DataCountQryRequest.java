package com.hho.lucene.request;

import lombok.Data;


@Data
public class DataCountQryRequest  {

    /**
     * 查询串
     */
    private String queryStr;

    /**
     * 查询字段
     */
    private String field;
}

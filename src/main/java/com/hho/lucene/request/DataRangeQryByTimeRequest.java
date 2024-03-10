package com.hho.lucene.request;

import lombok.Data;

@Data
public class DataRangeQryByTimeRequest extends Page {

    /**
     * 范围查询 开始时间
     */
    private Long startTime;


    /**
     * 范围查询 结束时间
     */
    private Long endTime;

}

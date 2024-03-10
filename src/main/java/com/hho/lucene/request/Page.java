package com.hho.lucene.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Page {

    /**
     * 分页大小
     */
    private Integer pageSize = 10;

    /**
     * 页码
     */
    private Integer pageNo = 1;




}

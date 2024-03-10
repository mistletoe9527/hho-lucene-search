package com.hho.lucene.request;

import com.hho.lucene.constant.Status;
import lombok.Data;

import java.util.List;

@Data
public class DataQryByStatusRequest extends Page {

    /**
     * 需要查询的状态列表
     *
     * @see Status
     */
    private List<Status> statusList;
}

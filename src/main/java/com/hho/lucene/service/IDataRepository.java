package com.hho.lucene.service;


import com.hho.lucene.entity.Data;
import com.hho.lucene.request.*;

import java.util.List;

public interface IDataRepository {

    /**
     * 批量更新
     *
     * @see Data
     */
    void batchUpdate(List<Data> entities);


    /**
     * 根据id查询数据
     *
     * @see DataQryByIdsRequest
     */
    List<Data> queryDataByIds(DataQryByIdsRequest dataQryByIdsRequest);

    /**
     * 根据title查数据
     *
     * @see DataQryByTitleRequest
     */
    List<Data> queryDataByTitle(DataQryByTitleRequest dataQryByTitleRequest);

    /**
     * 根据状态查数据
     *
     * @see DataQryByStatusRequest
     */
    List<Data> queryDataByStatus(DataQryByStatusRequest dataQryByStatusRequest);

    /**
     * 根据时间范围查数据
     *
     * @see DataRangeQryByTimeRequest
     */
    List<Data> queryDataByTime(DataRangeQryByTimeRequest dataRangeQryByTimeRequest);

    /**
     * 查询数量
     */
    long queryDataCount(DataCountQryRequest dataCountQryRequest);
}

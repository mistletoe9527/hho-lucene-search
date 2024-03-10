package com.hho.lucene;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson2.JSONObject;
import com.hho.lucene.constant.Status;
import com.hho.lucene.entity.Data;
import com.hho.lucene.repo.DataRepository;
import com.hho.lucene.repo.LuceneSearchManager;
import com.hho.lucene.request.*;
import com.hho.lucene.service.IDataRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LuceneSearchTest {


    private IDataRepository dataRepository = null;

    @Before
    public void setUp() {
        // change your path
        LuceneSearchManager.path = "C:\\Users\\admin\\Desktop\\index";

        dataRepository = new DataRepository();
    }

    /**
     * 根据id查询文档
     */
    @Test
    public void testQueryDataByIds() {
        DataQryByIdsRequest dataQryByIdsRequest = new DataQryByIdsRequest();
        List<Long> ids = ListUtil.of(10000L, 2L, 3L);
        dataQryByIdsRequest.setIds(ids);
        dataQryByIdsRequest.setPageNo(1);
        dataQryByIdsRequest.setPageSize(10);
        //倒叙排序
        dataQryByIdsRequest.setDesc(true);
        List<Data> data = dataRepository.queryDataByIds(dataQryByIdsRequest);
        for (Data d : data) {
            System.out.println(JSONObject.toJSONString(d));
        }
        Assert.assertTrue(CollectionUtils.isEmpty(data) || ids.contains(data.get(0).getId()));
    }


    /**
     * 根据title查询文档
     */
    @Test
    public void testQueryDataByTitle() {
        DataQryByTitleRequest dataQryByIdsRequest = new DataQryByTitleRequest();
        dataQryByIdsRequest.setTitle("ask");
        dataQryByIdsRequest.setPageNo(1);
        dataQryByIdsRequest.setPageSize(5);
        //是否前缀撇配
        dataQryByIdsRequest.setPrefixSearch(true);
        //自定义查询
//        dataQryByIdsRequest.setQueryStr("askghjhskdjl1~");
        List<Data> data = dataRepository.queryDataByTitle(dataQryByIdsRequest);
        for (Data d : data) {
            System.out.println(JSONObject.toJSONString(d));
        }
        Assert.assertTrue(CollectionUtils.isEmpty(data) || data.get(0).getTitle().contains("ask"));
    }

    /**
     * 根据状态 查询文档
     */
    @Test
    public void testQueryDataByStatus() {

        List<Status> statuses = ListUtil.of(Status.INIT, Status.PENDING);

        DataQryByStatusRequest dataQryByStatusRequest = new DataQryByStatusRequest();
        dataQryByStatusRequest.setStatusList(statuses);
        dataQryByStatusRequest.setPageNo(1);
        dataQryByStatusRequest.setPageSize(5);
        List<Data> data = dataRepository.queryDataByStatus(dataQryByStatusRequest);
        for (Data d : data) {
            System.out.println(JSONObject.toJSONString(d));
        }
        Assert.assertTrue(CollectionUtils.isEmpty(data) || statuses.contains(data.get(0).getStatus()));
    }

    /**
     * 根据时间范围查询文档
     */
    @Test
    public void testQueryDataByTime() {

        DataRangeQryByTimeRequest dataRangeQryByTimeRequest = new DataRangeQryByTimeRequest();
        dataRangeQryByTimeRequest.setStartTime(System.currentTimeMillis() - 3600 * 24 * 1000);
        dataRangeQryByTimeRequest.setEndTime(System.currentTimeMillis());
        dataRangeQryByTimeRequest.setPageNo(1);
        dataRangeQryByTimeRequest.setPageSize(10);
        List<Data> data = dataRepository.queryDataByTime(dataRangeQryByTimeRequest);
        for (Data d : data) {
            System.out.println(JSONObject.toJSONString(d));
        }

    }

    /**
     * 查询文档数
     */
    @Test
    public void testQueryDataCount() {

        DataRangeQryByTimeRequest dataRangeQryByTimeRequest = new DataRangeQryByTimeRequest();
        dataRangeQryByTimeRequest.setStartTime(System.currentTimeMillis() - 3600 * 24 * 1000);
        dataRangeQryByTimeRequest.setEndTime(System.currentTimeMillis());
        dataRangeQryByTimeRequest.setPageNo(1);
        dataRangeQryByTimeRequest.setPageSize(10);

        DataCountQryRequest dataCountQryRequest = new DataCountQryRequest();
        dataCountQryRequest.setQueryStr("id 1 2 3");
        dataCountQryRequest.setField("id");
        long l = dataRepository.queryDataCount(dataCountQryRequest);
        Assert.assertTrue(l == 3);
    }

    /**
     * 批量更新文档
     *
     * @throws InterruptedException
     */
    @Test()
    public void testBatchUpdate() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        for (; ; ) {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    DataQryByIdsRequest dataQryByIdsRequest = new DataQryByIdsRequest();
                    dataQryByIdsRequest.setIds(ListUtil.of(1L, 2L, 3L));
                    dataQryByIdsRequest.setPageNo(1);
                    dataQryByIdsRequest.setPageSize(10);
                    List<Data> dataList = dataRepository.queryDataByIds(dataQryByIdsRequest);
                    for (Data d : dataList) {
                        System.out.println("更改前：" + JSONObject.toJSONString(d));
                        d.setTitle("xxx");
                    }
                    dataRepository.batchUpdate(dataList);

                    dataList = dataRepository.queryDataByIds(dataQryByIdsRequest);

                    for (Data d : dataList) {
                        System.out.println("更改后：" + JSONObject.toJSONString(d));
                    }

                    countDownLatch.countDown();
                });
            }
            countDownLatch.await();
        }

    }

}

package com.hho.lucene;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson2.JSONObject;
import com.hho.lucene.entity.Data;
import com.hho.lucene.repo.DataRepository;
import com.hho.lucene.repo.LuceneSearchManager;
import com.hho.lucene.request.DataQryByIdsRequest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {

    // change your path




    public static void main(String[] args) throws Exception{
        DataRepository dataRepository = new DataRepository();
        LuceneSearchManager.path = "C:\\Users\\admin\\Desktop\\index";
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

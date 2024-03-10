package com.hho.lucene.repo;

import cn.hutool.core.lang.Assert;
import com.hho.lucene.convert.DataConvert;
import com.hho.lucene.entity.Data;
import com.hho.lucene.request.*;
import com.hho.lucene.service.IDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DataRepository implements IDataRepository {
    private LuceneSearchManager luceneSearchManager = new LuceneSearchManager();

    private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);

    @Override
    public void batchUpdate(List<Data> entities) {
        try {
            luceneSearchManager.batchUpdate(DataConvert.toDocumentList(entities));
        } catch (IOException e) {
            log.error("DataRepository 更新错误 msg{}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("DataRepository 未知异常 msg{}", e.getMessage(), e);
        }
    }

    @Override
    public List<Data> queryDataByIds(DataQryByIdsRequest request) {
        List<String> ids = request.
                getIds()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        try {
            Page page = new Page();
            page.setPageSize(request.getPageSize());
            page.setPageNo(request.getPageNo());
            return DataConvert.toDataList(luceneSearchManager.pageQuery4sort(QueryBuild.
                    buildMutilQuery(ids, "id"), page, request.getDesc() == null
                    ? null : new Sort(new SortField("id", SortField.Type.LONG, request.getDesc()))));
        } catch (IOException e) {
            log.error("DataRepository id查询错误 msg{}", e.getMessage(), e);
        } catch (ParseException e) {
            log.error("DataRepository id查询错误 msg{}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("DataRepository id查询未知异常 msg{}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Data> queryDataByTitle(DataQryByTitleRequest request) {
        QueryParser parser = new QueryParser(Version.LUCENE_47, "title", analyzer);
        try {

            String queryStr = StringUtils.isBlank(request.getQueryStr())
                    ? (request.getPrefixSearch() ? request.getTitle() + "*" : request.getTitle()) : request.getQueryStr();

            // 解析查询语句
            Query query = parser.parse(queryStr);
            List<Document> documents = luceneSearchManager.pageQuery(query, new Page()
                    .setPageNo(request.getPageNo())
                    .setPageSize(request.getPageSize()));
            return DataConvert.toDataList(documents);
        } catch (ParseException e) {
            log.error("DataRepository title查询错误 msg{}", e.getMessage(), e);
        } catch (IOException e) {
            log.error("DataRepository title查询错误 msg{}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Data> queryDataByStatus(DataQryByStatusRequest request) {
        List<String> statusList = request.
                getStatusList()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        try {
            Page page = new Page();
            page.setPageSize(request.getPageSize());
            page.setPageNo(request.getPageNo());
            return DataConvert.toDataList(luceneSearchManager.pageQuery(QueryBuild.buildMutilQuery(statusList, "status"), page));
        } catch (IOException e) {
            log.error("DataRepository status查询错误 msg{}", e.getMessage(), e);
        } catch (ParseException e) {
            log.error("DataRepository status查询错误 msg{}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("DataRepository status查询未知异常 msg{}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Data> queryDataByTime(DataRangeQryByTimeRequest request) {
        NumericRangeQuery<Long> query = NumericRangeQuery.newLongRange("time", request.getStartTime(), request.getEndTime(), true, true);
        Page page = new Page();
        page.setPageSize(request.getPageSize());
        page.setPageNo(request.getPageNo());
        try {
            List<Document> documents = luceneSearchManager.pageQuery(query, page);
            return DataConvert.toDataList(documents);
        } catch (IOException e) {
            log.error("DataRepository status查询错误 msg{}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("DataRepository status查询未知异常 msg{}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public long queryDataCount(DataCountQryRequest dataCountQryRequest) {
        Assert.notNull(dataCountQryRequest.getField(), "field不能为空");
        QueryParser parser = new QueryParser(Version.LUCENE_47, dataCountQryRequest.getField(), analyzer);
        // 解析查询语句
        Query query = null;
        try {
            query = parser.parse(dataCountQryRequest.getQueryStr());
            return luceneSearchManager.searchTotalRecord(query);
        } catch (ParseException e) {
            log.error("DataRepository 查询文档数量错误 msg{}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("DataRepository status查询未知异常 msg{}", e.getMessage(), e);
        }
        return 0L;
    }
}

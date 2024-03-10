package com.hho.lucene.repo;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import com.hho.lucene.constant.Status;
import com.hho.lucene.convert.DataConvert;
import com.hho.lucene.entity.Data;
import com.hho.lucene.request.Page;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@lombok.Data
@Accessors(chain = true)
@Slf4j
public class LuceneSearchManager {

    public static String path;
    private final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
    private final int DEFAULT_INIT_SIZE = 50000;

    private final int DEFAULT_RANDOM_TIME = 36000 * 24 * 365;

    private final int MAX_DOC_PAGE_SIZE = 500;

    private final int MAX_SEARCH_DOC_COUNT = 5000;


    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Object readlock = new Object();

    private IndexSearcher indexSearcher = null;

    public LuceneSearchManager() {
        try {
            Assert.notNull(path, "path不能为空");
            log.info("LuceneSearchManager 数据初始化开始");
            long start = System.currentTimeMillis();
            IndexWriter indexWrite = createIndexWrite();
            indexWrite.deleteAll();
            List<Status> statuses = ListUtil.of(Status.INIT, Status.END, Status.PENDING, Status.END);
            int batchSize = 50000;
            List<Document> documents = new ArrayList<>((int) Math.ceil(batchSize * 1.5));
            for (int i = 0; i < DEFAULT_INIT_SIZE; i++) {
                Data build = Data.
                        builder()
                        .id((long) i)
                        .time(System.currentTimeMillis() - new Random().nextInt(DEFAULT_RANDOM_TIME))
                        .title("askghjhskdjl" + i)
                        .status(statuses.get(new Random().nextInt(statuses.size()))).build();
                documents.add(DataConvert.toDocument(build));
                if (documents.size() >= batchSize) {
                    indexWrite.addDocuments(documents);
                    documents.clear();
                }
                //help gc
                Thread.sleep(0);
            }

            indexWrite.addDocuments(documents);
            indexWrite.commit();
            indexWrite.close();
            log.info("LuceneSearchManager 数据初始化完成 耗费时间:{}s", (System.currentTimeMillis() - start) / 1000);
        } catch (Exception e) {
            log.error("LuceneSearchManager 数据初始化失败", e);
            System.exit(1);
        }
    }


    /**
     * 创建索引阅读器
     */
    private IndexWriter createIndexWrite() throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_47, analyzer);
        return new IndexWriter(new MMapDirectory(new File(path)), indexWriterConfig);
    }


    /**
     * 创建索引阅读器
     */
    private IndexReader createIndexReader() throws IOException {
        return DirectoryReader.open(new MMapDirectory(new File(path)));
    }

    /**
     * 创建索引查询器
     */
    private IndexSearcher createIndexSearcher() throws IOException {
        if (indexSearcher == null) {
            synchronized (readlock) {
                if (indexSearcher == null) {
                    indexSearcher = new IndexSearcher(createIndexReader());
                }
                return indexSearcher;
            }
        }
        return indexSearcher;
    }


    /**
     * Lucene分页查询
     */
    public List<Document> pageQuery4sort(Query query, Page page, Sort sort) throws IOException {
        maxDocControl(page);
        lock.readLock().lock();
        try {
            IndexSearcher searcher = createIndexSearcher();
            TopDocs topDocs = null;

            if (sort == null) {
                topDocs = searcher.search(query, page.getPageSize() * page.getPageNo());
            } else {
                topDocs = searcher.search(query, page.getPageSize() * page.getPageNo(), sort);
            }

            ScoreDoc[] scoreDocs = topDocs.scoreDocs;

            List<Document> docList = new ArrayList<>((int) Math.ceil(page.getPageSize() * 1.5));

            // 计算记录起始数
            int start = (page.getPageNo() - 1) * page.getPageSize();

            int end = start + page.getPageSize();

            for (int i = start; i < end; i++) {
                if (i >= scoreDocs.length) {
                    break;
                }
                // 获取文档Id和评分
                int docId = scoreDocs[i].doc;
                Document doc = searcher.doc(docId);
                docList.add(doc);
            }

            return docList;
        } finally {
            lock.readLock().unlock();
        }

    }


    /**
     * Lucene分页查询
     */
    public List<Document> pageQuery(Query query, Page page) throws IOException {
        return pageQuery4sort(query, page, null);
    }


    /**
     * Lucene分页查询
     */
    public void batchUpdate(List<Document> documents) throws IOException {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        long start = System.currentTimeMillis();
        log.info("LuceneSearchManager 数据更新开始 文档数量：{}", documents.size());
        lock.writeLock().lock();
        try {
            IndexWriter indexWrite = createIndexWrite();
            for (Document document : documents) {
                indexWrite.updateDocument(new Term("id", document.get("id")), document);
            }
            indexWrite.commit();
            indexWrite.close();

            //TODO 频繁更新的话 放队列 合并重建任务
            indexSearcher.getIndexReader().close();
            indexSearcher = new IndexSearcher(createIndexReader());
        } finally {
            lock.writeLock().unlock();
        }

        log.info("LuceneSearchManager 数据更新完成 文档数量：{} 花费time:{}ms", documents.size(), System.currentTimeMillis() - start);
    }

    /**
     * 查询松鼠
     */
    public int searchTotalRecord(Query query) throws IOException {
        TopDocs topDocs = createIndexSearcher().search(query, Integer.MAX_VALUE);
        if (topDocs == null || topDocs.scoreDocs == null || topDocs.scoreDocs.length == 0) {
            return 0;
        }
        ScoreDoc[] docs = topDocs.scoreDocs;
        return docs.length;
    }

    private void maxDocControl(Page page) {

        if (page.getPageSize() == null) {
            page.setPageSize(10);
        }
        if (page.getPageNo() == null) {
            page.setPageNo(1);
        }
        if (page.getPageSize() > MAX_DOC_PAGE_SIZE) {
            page.setPageSize(MAX_DOC_PAGE_SIZE);
        }
        if (page.getPageNo() * page.getPageSize() > MAX_SEARCH_DOC_COUNT) {
            page.setPageNo(MAX_SEARCH_DOC_COUNT / page.getPageSize());
        }

    }
}

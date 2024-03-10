package com.hho.lucene.repo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class QueryBuild {

    private static Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);

    public static Query buildMutilQuery(List<String> queryList, String fieldName) throws ParseException {
        if (CollectionUtils.isEmpty(queryList)) {
            return null;
        }

        QueryParser queryParser = new QueryParser(Version.LUCENE_47, fieldName, analyzer);
        // 创建BooleanQuery
        BooleanQuery booleanQuery = new BooleanQuery();
        for (String value : queryList) {
            Query query = queryParser.parse(value);
            booleanQuery.add(query, BooleanClause.Occur.SHOULD);
        }

        return booleanQuery;
    }

}

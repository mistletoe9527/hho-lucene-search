package com.hho.lucene.repo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class QueryBuild {

    private static Analyzer analyzer = new StandardAnalyzer();

    public static String buildMutilQuery(List<String> queryList) throws ParseException {
        return String.join(" ", queryList);
    }

}

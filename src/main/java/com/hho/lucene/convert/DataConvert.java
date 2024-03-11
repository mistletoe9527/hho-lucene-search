package com.hho.lucene.convert;

import com.hho.lucene.constant.Status;
import com.hho.lucene.entity.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.util.BytesRef;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataConvert {


    /**
     * 文档数据转化
     */
    public static Data toData(Document document) {

        String id = document.get("id");

        String title = document.get("title");

        String status = document.get("status");

        String time = document.get("time");

        if (StringUtils.isBlank(id) || StringUtils.isBlank(time) || StringUtils.isBlank(title) || StringUtils.isBlank(status)) {
            return Data.builder().build();
        }

        return Data.builder()
                .id(Long.parseLong(id))
                .status(Status.valueOf(status.toUpperCase()))
                .title(title)
                .time(Long.parseLong(time)).build();
    }

    public static List<Data> toDataList(List<Document> documents) {
        return Optional.ofNullable(documents)
                .orElse(Collections.emptyList())
                .stream()
                .map(DataConvert::toData)
                .collect(Collectors.toList());
    }


    /**
     * 数据转文档
     */
    public static Document toDocument(Data data) {
        if (data.getId() == null
                || data.getTime() == null
                || data.getStatus() == null
                || StringUtils.isBlank(data.getTitle())) {
            throw new IllegalArgumentException("参数错误");
        }
        StringField id = new StringField("id", String.valueOf(data.getId()), Field.Store.YES);
        StringField status = new StringField("status", data.getStatus().name().toLowerCase(), Field.Store.YES);
        StringField title = new StringField("title", data.getTitle(), Field.Store.YES);
        StringField timeStr = new StringField("time", String.valueOf(data.getTime()), Field.Store.YES);
        LongPoint time = new LongPoint("time", data.getTime());
        Document document = new Document();
        document.add(id);
        document.add(new SortedDocValuesField("id", new BytesRef(String.valueOf(data.getId()))));
        document.add(status);
        document.add(title);
        document.add(time);
        document.add(timeStr);
        return document;
    }

    public static List<Document> toDocumentList(List<Data> dataList) {
        return Optional.ofNullable(dataList)
                .orElse(Collections.emptyList())
                .stream()
                .map(DataConvert::toDocument)
                .collect(Collectors.toList());
    }


}

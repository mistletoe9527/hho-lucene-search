package com.hho.lucene.entity;

import com.hho.lucene.constant.Status;
import lombok.Builder;

@lombok.Data
@Builder
public class Data {

    private Long id;

    private String title;

    private Status status;

    private Long time;

}

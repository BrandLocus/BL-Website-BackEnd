package com.hvc.brandlocus.dto.request;

import lombok.Data;

import java.util.List;


@Data
public class MessageClassification {
    private String sector;
    private List<String> keywords;
    private String topic;
}

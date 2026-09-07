package com.documentlm.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchResponse {

    private List<Point> result;

    @Data
    public static class Point {

        private Long id;

        private Double score;

        private Map<String, Object> payload;
    }
}
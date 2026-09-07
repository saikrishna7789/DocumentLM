package com.documentlm.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class PointRequest {

    private List<Point> points;

    @Data
    @Builder
    public static class Point {

        private Long id;

        private List<Double> vector;

        private Map<String, Object> payload;
    }

}
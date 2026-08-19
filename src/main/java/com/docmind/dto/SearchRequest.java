package com.docmind.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchRequest {

    private List<Double> vector;

    private int limit;

    private boolean with_payload;
}
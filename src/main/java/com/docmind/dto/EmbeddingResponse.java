package com.docmind.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EmbeddingResponse {

    private List<Double> embedding;

}
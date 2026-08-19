package com.docmind.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmbeddingRequest {

    private String model;

    private String prompt;

}
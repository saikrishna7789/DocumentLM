package com.documentlm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResponse {

    private String answer;
    private java.util.List<Source> sources = java.util.List.of();

    public ChatResponse(String answer) {
        this.answer = answer;
    }

    @Data
    @AllArgsConstructor
    public static class Source {
        private String documentName;
        private String location;
    }
}
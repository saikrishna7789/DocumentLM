package com.docmind.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KimiRequest {

    private String model;

    private List<Message> messages;

    public KimiRequest(String model, String prompt) {
        this.model = model;
        this.messages = List.of(new Message("user", prompt));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {

        private String role;

        private String content;
    }
}

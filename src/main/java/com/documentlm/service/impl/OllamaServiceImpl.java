package com.documentlm.service.impl;

import com.documentlm.dto.request.OllamaRequest;
import com.documentlm.dto.response.OllamaResponse;
import com.documentlm.service.OllamaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class OllamaServiceImpl implements OllamaService {

    private final RestClient restClient =
            RestClient.create("http://localhost:11434");

    @Override
    public String ask(String prompt) {
        log.info("Provider: " + "OLLAMA");
        OllamaRequest request =
                new OllamaRequest(
                        "qwen2.5-coder:7b",
                        prompt,
                        false
                );

        OllamaResponse response =
                restClient.post()
                        .uri("/api/generate")
                        .body(request)
                        .retrieve()
                        .body(OllamaResponse.class);

        return response.getResponse();
    }
}
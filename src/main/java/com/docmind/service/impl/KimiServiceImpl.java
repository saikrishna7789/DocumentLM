package com.docmind.service.impl;

import com.docmind.dto.request.KimiRequest;
import com.docmind.dto.response.KimiResponse;
import com.docmind.service.KimiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnProperty(name = "llm.provider", havingValue = "kimi")
public class KimiServiceImpl implements KimiService {

    private final RestClient restClient;

    private final String model;

    private final String apiKey;

    public KimiServiceImpl(
            @Value("${kimi.base-url:https://api.moonshot.cn}") String baseUrl,
            @Value("${kimi.model:moonshot-v1-8k}") String model,
            @Value("${kimi.api-key:}") String apiKey) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.model = model;
        this.apiKey = apiKey;
    }

    @Override
    public String ask(String prompt) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Kimi API key is not configured. Set kimi.api-key or the KIMI_API_KEY environment variable."
            );
        }

        KimiRequest request = new KimiRequest(model, prompt);

        KimiResponse response = restClient.post()
                .uri("/v1/chat/completions")
                .body(request)
                .retrieve()
                .body(KimiResponse.class);

        if (response == null
                || response.getChoices() == null
                || response.getChoices().isEmpty()) {
            throw new IllegalStateException("Empty response from Kimi");
        }

        KimiResponse.Choice choice = response.getChoices().get(0);

        if (choice.getMessage() == null || choice.getMessage().getContent() == null) {
            throw new IllegalStateException(
                    "Kimi response does not contain message content"
            );
        }

        return choice.getMessage().getContent().trim();
    }
}

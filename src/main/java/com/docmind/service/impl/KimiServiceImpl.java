package com.docmind.service.impl;

import com.docmind.dto.request.KimiRequest;
import com.docmind.dto.response.KimiResponse;
import com.docmind.service.KimiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@ConditionalOnProperty(name = "llm.provider", havingValue = "kimi")
public class KimiServiceImpl implements KimiService {

    private final RestClient restClient;

    private final String model;

    private final String apiKey;

    private final String baseUrl;

    public KimiServiceImpl(
            @Value("${kimi.base-url}") String baseUrl,
            @Value("${kimi.model}") String model,
            @Value("${kimi.api-key}") String apiKey) {

        String normalizedBaseUrl = baseUrl == null ? "" : baseUrl.trim();
        if (normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }

        this.restClient = RestClient.builder()
                .baseUrl(normalizedBaseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.model = model;
        this.apiKey = apiKey;
        this.baseUrl = normalizedBaseUrl;
    }

    private String buildChatCompletionsPath() {
        return baseUrl.toLowerCase().contains("/v1") ? "/chat/completions" : "/v1/chat/completions";
    }

    @Override
    public String ask(String prompt) {
        log.info("Provider: " + model);
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Kimi API key is not configured. Set kimi.api-key or the KIMI_API_KEY environment variable."
            );
        }

        KimiRequest request = new KimiRequest(model, prompt);

        KimiResponse response = restClient.post()
                .uri(buildChatCompletionsPath())
                .body(request)
                .retrieve()
                .body(KimiResponse.class);


        if (response == null
                || response.getChoices() == null
                || response.getChoices().isEmpty()) {
            throw new IllegalStateException("Empty response from Kimi");
        }

        log.info(response.toString());
        KimiResponse.Choice choice = response.getChoices().get(0);

        if (choice.getMessage() == null || choice.getMessage().getContent() == null) {
            throw new IllegalStateException(
                    "Kimi response does not contain message content"
            );
        }

        return choice.getMessage().getContent().trim();
    }
}

package com.documentlm.service.impl;

import com.documentlm.dto.EmbeddingRequest;
import com.documentlm.dto.EmbeddingResponse;
import com.documentlm.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    private final RestClient restClient = RestClient.create("http://localhost:11434");

    @Override
    public List<Double> generateEmbedding(String text) {

        EmbeddingRequest request =
                new EmbeddingRequest("nomic-embed-text", text);

        EmbeddingResponse response =
                restClient.post()
                        .uri("/api/embeddings")
                        .body(request)
                        .retrieve()
                        .body(EmbeddingResponse.class);

        return response.getEmbedding();
    }
}
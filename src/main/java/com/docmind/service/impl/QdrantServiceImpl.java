package com.docmind.service.impl;

import com.docmind.dto.PointRequest;
import com.docmind.dto.SearchRequest;
import com.docmind.dto.SearchResponse;
import com.docmind.service.QdrantService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

@Service
public class QdrantServiceImpl implements QdrantService {

    private static final String COLLECTION_NAME = "documents";

    // Ollama's nomic-embed-text model produces 768-dimensional embeddings.
    private static final int VECTOR_DIM = 768;

    private final RestClient restClient =
            RestClient.create("http://localhost:6333");

    @Override
    public void storeEmbedding(Long documentId,
                               Integer chunkNumber,
                               String chunk,
                               List<Double> embedding) {

        PointRequest.Point point =
                PointRequest.Point.builder()
                        .id(System.currentTimeMillis())
                        .vector(embedding)
                        .payload(
                                Map.of(
                                        "documentId", documentId,
                                        "chunkNumber", chunkNumber,
                                        "text", chunk
                                )
                        )
                        .build();

        PointRequest request =
                PointRequest.builder()
                        .points(List.of(point))
                        .build();

        try {
            restClient.put()
                    .uri("/collections/{collection}/points", COLLECTION_NAME)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound notFound) {
            createCollection();
            retryStorePoints(request);
        } catch (HttpClientErrorException.BadRequest badRequest) {
            String response = badRequest.getResponseBodyAsString();
            if (response != null && response.contains("Vector dimension error")) {
                createCollection();
                retryStorePoints(request);
                return;
            }
            throw badRequest;
        }
    }

    private void createCollection() {
        try {
            restClient.delete()
                    .uri("/collections/{collection}", COLLECTION_NAME)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) {
            // Collection may already be missing or locked during startup; continue with creation.
        }

        Map<String, Object> vectors = Map.of(
                "size", VECTOR_DIM,
                "distance", "Cosine"
        );
        Map<String, Object> collBody = Map.of(
                "vectors", vectors
        );

        restClient.put()
                .uri("/collections/{collection}", COLLECTION_NAME)
                .body(collBody)
                .retrieve()
                .toBodilessEntity();
    }

    private void retryStorePoints(PointRequest request) {
        restClient.put()
                .uri("/collections/{collection}/points", COLLECTION_NAME)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void deleteDocument(Long documentId) {
        String body = """
                {
                  "filter": {
                    "must": [
                      {
                        "key": "documentId",
                        "match": {
                          "value": %d
                        }
                      }
                    ]
                  }
                }
                """.formatted(documentId);

        restClient.post()
                .uri("/collections/{collection}/points/delete", COLLECTION_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void clearCollection() {
        try {
            restClient.delete()
                    .uri("/collections/{collection}", COLLECTION_NAME)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) {
            String body = "{\"delete_all\":true}";
            restClient.post()
                    .uri("/collections/{collection}/points/delete", COLLECTION_NAME)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        }
    }

    @Override
    public List<String> search(List<Double> embedding) {

        SearchRequest request =
                new SearchRequest(
                        embedding,
                        3,
                        true
                );

        SearchResponse response =
                restClient.post()
                        .uri("/collections/{collection}/points/search", COLLECTION_NAME)
                        .body(request)
                        .retrieve()
                        .body(SearchResponse.class);

        System.out.println(response);

        return response.getResult()
                .stream()
                .map(point -> point.getPayload().get("text").toString())
                .toList();
    }
}
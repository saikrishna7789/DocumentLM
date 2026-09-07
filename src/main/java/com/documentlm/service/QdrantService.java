package com.documentlm.service;

import java.util.List;
import java.util.Map;

public interface QdrantService {

    void storeEmbedding(Long documentId,
                        Integer chunkNumber,
                        String chunk,
                        List<Double> embedding);

    void deleteDocument(Long documentId);

    void clearCollection();

    List<Map<String, Object>> searchWithMetadata(List<Double> embedding);

    List<String> search(List<Double> embedding);
}
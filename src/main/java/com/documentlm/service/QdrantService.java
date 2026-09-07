package com.documentlm.service;

import java.util.List;

public interface QdrantService {

    void storeEmbedding(Long documentId,
                        Integer chunkNumber,
                        String chunk,
                        List<Double> embedding);

    void deleteDocument(Long documentId);

    void clearCollection();

    List<String> search(List<Double> embedding);
}
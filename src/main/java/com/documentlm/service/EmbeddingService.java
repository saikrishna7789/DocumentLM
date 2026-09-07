package com.documentlm.service;

import java.util.List;

public interface EmbeddingService {

    List<Double> generateEmbedding(String text);

}
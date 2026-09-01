package com.docmind.service.impl;

import com.docmind.service.TextChunkService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkServiceImpl implements TextChunkService {

    private static final int CHUNK_SIZE = 1500;
    private static final int OVERLAP = 250;

    @Override
    public List<String> chunkText(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalizedText = text.replace("\r\n", "\n").trim();
        List<String> chunks = new ArrayList<>();
        int step = CHUNK_SIZE - OVERLAP;

        for (int i = 0; i < normalizedText.length(); i += step) {
            int end = Math.min(i + CHUNK_SIZE, normalizedText.length());
            String chunk = normalizedText.substring(i, end).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            if (end >= normalizedText.length()) {
                break;
            }
        }

        return chunks;
    }
}
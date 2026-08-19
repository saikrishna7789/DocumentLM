package com.docmind.service.impl;

import com.docmind.service.TextChunkService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkServiceImpl implements TextChunkService {

    private static final int CHUNK_SIZE = 500;

    @Override
    public List<String> chunkText(String text) {

        List<String> chunks = new ArrayList<>();

        for (int i = 0; i < text.length(); i += CHUNK_SIZE) {

            int end = Math.min(i + CHUNK_SIZE, text.length());

            chunks.add(text.substring(i, end));
        }

        return chunks;
    }
}
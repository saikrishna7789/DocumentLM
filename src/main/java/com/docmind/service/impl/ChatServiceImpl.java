package com.docmind.service.impl;

import com.docmind.service.ChatService;
import com.docmind.service.EmbeddingService;
import com.docmind.service.OllamaService;
import com.docmind.service.QdrantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;
    private final OllamaService ollamaService;

    @Override
    public String ask(String question) {

        List<Double> embedding =
                embeddingService.generateEmbedding(question);

        List<String> chunks =
                qdrantService.search(embedding);

        String context =
                String.join("\n", chunks);

        String prompt = """
                You are an AI assistant.

                Use ONLY the provided context to answer the question.

                Response rules:
                1. If the answer is present in the context, reply with one complete sentence only.
                2. The answer must be a natural-sentence response, not a number, keyword, JSON, list, or bare phrase.
                3. If the answer is NOT present, reply exactly:
                   "I checked every corner of my tiny AI brain 📚... nothing found!"
                4. Do NOT mention any other people, names, facts, or topics from the context.
                5. Do NOT explain what the document contains.
                6. Do NOT guess or make assumptions.

                Context:
                %s

                Question:
                %s
                """.formatted(context, question);

        return ollamaService.ask(prompt);
    }
}
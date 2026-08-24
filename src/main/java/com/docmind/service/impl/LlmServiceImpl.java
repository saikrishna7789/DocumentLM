package com.docmind.service.impl;

import com.docmind.service.LlmService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class LlmServiceImpl implements LlmService {

    private final RestClient restClient;

    @Value("${ollama.model:qwen2.5-coder:7b}")
    private String model;

    public LlmServiceImpl(
            @Value("${ollama.base-url:http://localhost:11434}") String baseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public String classifyIntent(String question) {

        String prompt = """
                Classify the user's message into exactly one category.

                Categories:

                GENERAL
                DOCUMENT_QUERY

                GENERAL means:
                - greetings
                - thanks
                - goodbye
                - casual conversation
                - asking for help
                - questions about AI
                - programming or general knowledge
                - questions that do not require information from uploaded documents

                DOCUMENT_QUERY means:
                - asking about information contained in uploaded documents
                - company policies
                - leave policies
                - employee information
                - dates
                - numbers
                - rules
                - procedures
                - any question that should be answered using uploaded documents

                Return ONLY:
                GENERAL

                or:

                DOCUMENT_QUERY

                User message:
                %s
                """.formatted(question);

        return generate(prompt);
    }

    @Override
    public String answerGeneralQuestion(String question) {

        String prompt = """
                You are Personal AI, a helpful AI assistant.

                Answer the user's question naturally and concisely.

                Rules:
                1. Answer the question directly.
                2. Be helpful and conversational.
                3. Do not mention RAG, Qdrant, embeddings, or document retrieval.
                4. Do not invent personal information about the user.
                5. If you do not know something, say that you don't know.
                6. Keep the response concise unless the user asks for a detailed explanation.

                User question:
                %s
                """.formatted(question);

        return generate(prompt);
    }

    @Override
    public String generateAnswer(String question, String context) {

        String prompt = """
                You are Personal AI, a document-based AI assistant.

                Answer the question using ONLY the information provided in the context.

                Response rules:
                1. If the answer is present in the context, provide a concise,
                   direct answer in 1-2 complete sentences.
                2. Clearly include the specific information requested by the user.
                3. Use natural language.
                4. Do not return a bare number, keyword, or fragment.
                5. Do not add information that is not explicitly supported by the context.
                6. Do not guess, assume, or infer missing information.
                7. Do not mention unrelated people, names, facts, or topics from the context.
                8. Do not explain the document or retrieval process unless explicitly asked.
                9. If the answer is not present in the context, reply exactly:

                   I checked every corner of my tiny AI brain 📚... nothing found!

                Context:
                %s

                Question:
                %s
                """.formatted(context, question);

        return generate(prompt);
    }

    private String generate(String prompt) {

        Map<String, Object> request = Map.of(
                "model", "qwen2.5-coder:7b",
                "prompt", prompt,
                "stream", false
        );

        Map<String, Object> response = restClient
                .post()
                .uri("/api/generate")
                .body(request)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException("Empty response from Ollama");
        }

        Object result = response.get("response");

        if (result == null) {
            throw new IllegalStateException(
                    "Ollama response does not contain 'response'"
            );
        }

        return result.toString().trim();
    }
}
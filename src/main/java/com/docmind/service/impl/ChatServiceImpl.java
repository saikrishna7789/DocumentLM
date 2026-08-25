package com.docmind.service.impl;

import com.docmind.dto.response.ChatResponse;
import com.docmind.enums.MessageIntent;
import com.docmind.service.*;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ChatServiceImpl implements ChatService {

    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;
    private final OllamaService ollamaService;
    private final KimiService kimiService;
    private final MessageClassifier messageClassifier;
    private final String llmProvider;

    public ChatServiceImpl(
            EmbeddingService embeddingService,
            QdrantService qdrantService,
            OllamaService ollamaService,
            @Autowired(required = false) KimiService kimiService,
            MessageClassifier messageClassifier,
            @Value("${llm.provider:kimi}") String llmProvider) {

        this.embeddingService = embeddingService;
        this.qdrantService = qdrantService;
        this.ollamaService = ollamaService;
        this.kimiService = kimiService;
        this.messageClassifier = messageClassifier;
        this.llmProvider = llmProvider;
    }

    @Override
    public ChatResponse ask(String question) {

        String normalizedQuestion = question.trim().toLowerCase();

        /*if (normalizedQuestion.matches(
                "^(hi|hello|hey|hii|good morning|good afternoon|good evening)[.!? ]*$")) {

            List<String> responses = List.of(
                    "Hi there! How can I help you?",
                    "Hello! What document can I assist with today?"
            );
            return responses.get(ThreadLocalRandom.current().nextInt(responses.size()));
        }

        if (normalizedQuestion.matches(
                "^(thanks|thank you|thx|bye|goodbye|thanks bye|thank you bye)[.!? ]*$")) {

            List<String> responses = List.of(
                    "You're welcome! Have a great day! 👋",
                    "No problem — happy to help! 👋",
                    "Glad I could help! Take care! 👋"
            );
            return responses.get(ThreadLocalRandom.current().nextInt(responses.size()));
        }
*/

        return MsgIntentResponse(question);
    }

    private @NonNull ChatResponse MsgIntentResponse(String question) {
        MessageIntent intent = messageClassifier.classify(question);

        return switch (intent) {

            case EMPTY -> new ChatResponse(
                    "Please enter a question to get started.");

            case GREETING -> {
                List<String> responses = List.of(
                        "Hi there! How can I help?",
                        "Hello! What can I do for you?",
                        "Hey! How can I assist you today?",
                        "Hi! Need any help?",
                        "Hello! How may I help?",
                        "Hey there! What would you like to do?",
                        "Hi! How can I be of service?",
                        "Hello! Ask me anything.",
                        "Hey! What can I help with?",
                        "Hi! How can I help?"
                );
                yield new ChatResponse(responses.get(ThreadLocalRandom.current().nextInt(responses.size())));
            }

            case THANKS -> {
                List<String> responses = List.of(
                        "You're welcome!",
                        "No problem — happy to help!",
                        "Glad I could help!",
                        "Anytime!",
                        "Happy to help!",
                        "My pleasure!"
                );
                yield new ChatResponse(responses.get(ThreadLocalRandom.current().nextInt(responses.size())));
            }

            case GOODBYE -> {
                List<String> responses = List.of(
                        "Goodbye! Have a great day!",
                        "Take care!",
                        "See you later!",
                        "Bye! Stay well!",
                        "Farewell!",
                        "Catch you later!"
                );
                yield new ChatResponse(responses.get(ThreadLocalRandom.current().nextInt(responses.size())));
            }

            case HOW_ARE_YOU -> {
                List<String> responses = List.of(
                        "I'm doing well, thanks! How can I assist?",
                        "All good here — how can I help?",
                        "I'm fine, ready to help!",
                        "Doing great! What do you need help with?"
                );
                yield new ChatResponse(responses.get(ThreadLocalRandom.current().nextInt(responses.size())));
            }

            case CAPABILITY -> {
                List<String> responses = List.of(
                        "I can answer questions and provide summaries.",
                        "I can help find information and explain it clearly.",
                        "I can search, summarize, and explain content.",
                        "I can assist with searching, summarizing, and answering questions."
                );
                yield new ChatResponse(responses.get(ThreadLocalRandom.current().nextInt(responses.size())));
            }

            case IDENTITY -> {
                List<String> responses = List.of(
                        "I'm a personal assistant here to help you.",
                        "I'm an AI assistant ready to help.",
                        "I'm your assistant for quick help and answers.",
                        "I'm an AI helper — ask me anything."
                );
                yield new ChatResponse(responses.get(ThreadLocalRandom.current().nextInt(responses.size())));
            }

            case OUT_OF_SCOPE -> {
                List<String> responses = List.of(
                        "I can help with general questions and tasks.",
                        "That request is outside my scope, but I can try to help with related questions.",
                        "I might not be able to handle that, but feel free to ask something else."
                );
                yield new ChatResponse(responses.get(ThreadLocalRandom.current().nextInt(responses.size())));
            }

            case DOCUMENT_QUERY -> askFromDocuments(question);
        };
    }

    private ChatResponse askFromDocuments(String question) {
        List<Double> embedding =
                embeddingService.generateEmbedding(question);

        List<String> chunks =
                qdrantService.search(embedding);

        boolean nameRelatedQuery = isNameRelatedQuery(question);

        if ((chunks == null || chunks.isEmpty() || chunks.stream().allMatch(String::isBlank)) && nameRelatedQuery) {
            return new ChatResponse("I checked every corner of my tiny AI brain 📚... nothing found!");
        }

        String context =
                String.join("\n", chunks == null ? List.of() : chunks);

        String prompt = """
                You are Personal AI, a helpful AI assistant with access to user-provided documents.
                 
                Follow these rules:
                 
                1. For general conversation and general questions unrelated to names or people,
                   you may answer naturally and helpfully using general knowledge.
                2. For questions about a person, name, identity, or named entity, do not use
                   general training data or memory if the answer is not explicitly supported by
                   the provided document context.
                   If the answer is not in the context, reply exactly:
                   "I checked every corner of my tiny AI brain 📚... nothing found!"
                3. If the answer is present in the context, give a clean, structured answer.
                   Format it as:
                   - Heading: a short answer sentence
                   - Key details: 3-5 concise bullet points
                   - Important note: only if relevant
                4. Keep each bullet direct and based only on the document context.
                5. Do not mention unrelated people, names, or topics.
                6. Do not mention RAG, embeddings, retrieval, or vector databases.
                 
                Context:
                %s
                 
                Question:
                %s
                """.formatted(context, question);

        String answer = switch (llmProvider.toLowerCase()) {

            case "kimi" -> {
                if (kimiService == null) {
                    throw new IllegalStateException(
                            "Kimi is selected as LLM provider but KimiService is not available. "
                                    + "Check that llm.provider=kimi and that kimi.api-key is configured."
                    );
                }
                yield kimiService.ask(prompt);
            }

            default -> ollamaService.ask(prompt);
        };

        return new ChatResponse(answer);
    }

    private boolean isNameRelatedQuery(String question) {
        String normalized = question.toLowerCase();
        return normalized.contains("who is")
                || normalized.contains("who was")
                || normalized.contains("what is the name")
                || normalized.contains("what's the name")
                || normalized.contains("tell me about")
                || normalized.contains("identify")
                || normalized.contains("person")
                || normalized.contains("name");
    }
}

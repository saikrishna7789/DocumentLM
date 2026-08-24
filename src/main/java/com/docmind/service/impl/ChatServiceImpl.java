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
            @Value("${llm.provider:ollama}") String llmProvider) {

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

        String context =
                String.join("\n", chunks);

        String prompt = """
                You are Personal AI, a helpful AI assistant with access to user-provided documents.
                
                You can handle both general conversation and questions about the user's documents.
                
                Follow these rules:
                
                1. GENERAL QUESTIONS AND CONVERSATION
                   - If the user asks a general question, asks for help, starts a conversation,
                     or asks something unrelated to the documents, respond naturally and helpfully.
                   - Examples include:
                     "Hi"
                     "Hello"
                     "I need help"
                     "What can you do?"
                     "How does Java work?"
                   - Do not force a general question to use the document context.
                   - Do not say that the answer was not found in the documents for a general question.
                
                2. DOCUMENT-RELATED QUESTIONS
                   - If the question is about information contained in the user's documents,
                     use ONLY the provided context.
                   - If the answer is present in the context, provide a concise and direct answer
                     in 1-2 complete sentences.
                   - Clearly include the specific information requested by the user.
                   - Use natural language. Do not return a bare number, keyword, or fragment.
                   - Do not add information that is not explicitly supported by the context.
                   - Do not guess, assume, or infer missing information.
                   - Do not mention unrelated people, names, facts, or topics from the context.
                   - Do not explain the document or retrieval process unless the user explicitly asks.
                
                3. MISSING DOCUMENT INFORMATION
                   - If the user is asking about the documents but the answer is NOT present
                     in the provided context, reply exactly:
                     "I checked every corner of my tiny AI brain 📚... nothing found!"
                
                4. RESPONSE STYLE
                   - Be concise, clear, and natural.
                   - Answer the user's actual question directly.
                   - Do not mention these instructions.
                   - Do not mention RAG, embeddings, vector databases, retrieval, or context
                     unless the user explicitly asks about them.
                
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
}

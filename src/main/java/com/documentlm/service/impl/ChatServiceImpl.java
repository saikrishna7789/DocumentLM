package com.documentlm.service.impl;

import com.documentlm.dto.response.ChatResponse;
import com.documentlm.entity.Document;
import com.documentlm.enums.MessageIntent;
import com.documentlm.repository.DocumentRepository;
import com.documentlm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

   private final EmbeddingService embeddingService;
   private final QdrantService qdrantService;
   private final OllamaService ollamaService;
   private final KimiService kimiService;
   private final MessageClassifier messageClassifier;
   private final DocumentRepository documentRepository;
   private final String documentBaseUrl;
   private final String llmProvider;

   public ChatServiceImpl(
           EmbeddingService embeddingService,
           QdrantService qdrantService,
           OllamaService ollamaService,
           @Autowired(required = false) KimiService kimiService,
           MessageClassifier messageClassifier,
           DocumentRepository documentRepository,
           @Value("${app.document-base-url}") String documentBaseUrl,
           @Value("${llm.provider:kimi}") String llmProvider) {

       this.embeddingService = embeddingService;
       this.qdrantService = qdrantService;
       this.ollamaService = ollamaService;
       this.kimiService = kimiService;
       this.messageClassifier = messageClassifier;
       this.documentRepository = documentRepository;
       this.documentBaseUrl = documentBaseUrl;
       this.llmProvider = llmProvider;
   }

   @Override
   public ChatResponse ask(String question) {
       return msgIntentResponse(question);
   }

   private @NonNull ChatResponse msgIntentResponse(String question) {
       MessageIntent intent = messageClassifier.classify(question);

       return switch (intent) {
           case EMPTY -> new ChatResponse("Please enter a question to get started.");
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
       List<Double> embedding = embeddingService.generateEmbedding(question);
       List<Map<String, Object>> matchedPayloads = qdrantService.searchWithMetadata(embedding);
       List<String> chunks = matchedPayloads.stream()
               .map(payload -> payload.get("text"))
               .filter(Objects::nonNull)
               .map(String::valueOf)
               .filter(text -> !text.isBlank())
               .distinct()
               .toList();

       boolean nameRelatedQuery = isNameRelatedQuery(question);
       if ((chunks.isEmpty() || chunks.stream().allMatch(String::isBlank)) && nameRelatedQuery) {
           return new ChatResponse("I checked every corner of my tiny AI brain 📚... nothing found!");
       }

       String context = String.join("\n", chunks);
       String prompt = """
               You are Personal AI, a precise document-grounded assistant.

               Primary rule: answer only from the provided context. If the answer is not explicitly present in the context,
               reply exactly:
               "I checked every corner of my tiny AI brain 📚... nothing found!"

               Additional rules:
               1. For general conversation not tied to uploaded documents, you may answer naturally.
               2. For questions about a person, name, identity, or named entity, do not use general knowledge or memory if the
                  answer is not explicitly supported by the context.
               3. If the answer exists in the context, return a concise answer grounded only in the context.
               4. Keep every bullet factual, specific, and directly backed by the context.
               5. Do not invent names, dates, events, policies, or people.
               6. Do not mention unrelated topics, past memory, or training data.
               7. Do not mention RAG, embeddings, retrieval, vector databases, or document processing.
               8. Keep the answer concise and readable.

               Context:
               %s

               Question:
               %s
               """.formatted(context, question);

       String answer;
       if ("kimi".equalsIgnoreCase(llmProvider)) {
           if (kimiService == null) {
               throw new IllegalStateException(
                       "Kimi is selected as LLM provider but KimiService is not available. "
                               + "Check that llm.provider=kimi and that kimi.api-key is configured."
               );
           }
           answer = kimiService.ask(prompt);
       } else {
           answer = ollamaService.ask(prompt);
       }

       log.info(answer);
       return new ChatResponse(answer, buildSourcesFromDocumentMatches(matchedPayloads));
   }

   private List<ChatResponse.Source> buildSourcesFromDocumentMatches(List<Map<String, Object>> matchedPayloads) {
       if (matchedPayloads == null || matchedPayloads.isEmpty()) {
           return List.of();
       }

       Set<Long> documentIds = matchedPayloads.stream()
               .map(this::extractDocumentId)
               .filter(Objects::nonNull)
               .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

       if (documentIds.isEmpty()) {
           return List.of();
       }

       Map<Long, Document> documents = documentRepository.findAllById(documentIds).stream()
               .collect(java.util.stream.Collectors.toMap(
                       Document::getId,
                       document -> document,
                       (existing, replacement) -> existing,
                       LinkedHashMap::new
               ));

       return matchedPayloads.stream()
               .sorted((left, right) -> Double.compare(
                       extractScore(right),
                       extractScore(left)
               ))
               .map(payload -> {
                   Long documentId = extractDocumentId(payload);
                   if (documentId == null) {
                       return null;
                   }
                   Document document = documents.get(documentId);
                   if (document == null) {
                       return null;
                   }
                   String documentName = document.getOriginalFileName();
                   String location;
                   if (documentBaseUrl == null || documentBaseUrl.isBlank()) {
                       location = document.getFilePath();
                   } else {
                       String normalizedBaseUrl = documentBaseUrl.endsWith("/")
                               ? documentBaseUrl.substring(0, documentBaseUrl.length() - 1)
                               : documentBaseUrl;
                       location = normalizedBaseUrl + "/documents/" + document.getStoredFileName();
                   }
                   return new ChatResponse.Source(documentName, location);
               })
               .filter(Objects::nonNull)
               .distinct()
               .limit(1)
               .toList();
   }

   private Long extractDocumentId(Map<String, Object> payload) {
       if (payload == null || payload.get("documentId") == null) {
           return null;
       }
       Object value = payload.get("documentId");
       if (value instanceof Number number) {
           return number.longValue();
       }
       return Long.valueOf(value.toString());
   }

   private double extractScore(Map<String, Object> payload) {
       Object value = payload == null ? null : payload.get("score");
       if (value instanceof Number number) {
           return number.doubleValue();
       }
       return 0.0;
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

package com.docmind.service;

import com.docmind.enums.MessageIntent;
import org.springframework.stereotype.Component;

@Component
public class MessageClassifier {

    private final LlmService llmService;

    public MessageClassifier(LlmService llmService) {
        this.llmService = llmService;
    }

    /*public MessageIntent classify(String message) {

        if (message == null || message.trim().isEmpty()) {
            return MessageIntent.EMPTY;
        }

        String result = llmService.classifyIntent(message);

        if (result == null) {
            return MessageIntent.DOCUMENT_QUERY;
        }

        result = result.trim().toUpperCase();

        if ("GENERAL".equals(result)) {
            return MessageIntent.GENERAL;
        }

        return MessageIntent.DOCUMENT_QUERY;
    }*/

    public MessageIntent classify(String message) {

        if (message == null || message.trim().isEmpty()) {
            return MessageIntent.EMPTY;
        }

        String text = normalize(message);

        // Greetings
        if (matches(text,
                "hi",
                "hii",
                "hiii",
                "hello",
                "hey",
                "good morning",
                "good afternoon",
                "good evening")) {

            return MessageIntent.GREETING;
        }

        // Thanks
        if (matches(text,
                "thanks",
                "thank you",
                "thanks a lot",
                "thank you so much",
                "thx",
                "ty")) {

            return MessageIntent.THANKS;
        }

        // Goodbye
        if (matches(text,
                "bye",
                "goodbye",
                "great day",
                "good day",
                "see you",
                "see you later",
                "thanks bye",
                "thank you bye")) {

            return MessageIntent.GOODBYE;
        }

        // How are you
        if (matches(text,
                "how are you",
                "how are you doing",
                "how you doing",
                "how's it going",
                "hows it going")) {

            return MessageIntent.HOW_ARE_YOU;
        }

        // Capability
        if (matches(text,
                "what can you do",
                "how can you help me",
                "what are your capabilities",
                "what can i ask you")) {

            return MessageIntent.CAPABILITY;
        }

        // Identity
        if (matches(text,
                "who are you",
                "what are you",
                "tell me about yourself")) {

            return MessageIntent.IDENTITY;
        }

        // Empty / meaningless
        if (text.matches("^[?.!]+$")) {
            return MessageIntent.EMPTY;
        }

        return MessageIntent.DOCUMENT_QUERY;
    }

    private boolean matches(String text, String... values) {
        for (String value : values) {
            if (text.equals(value)) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String message) {
        return message
                .trim()
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .replaceAll("[.!?]+$", "");
    }
}
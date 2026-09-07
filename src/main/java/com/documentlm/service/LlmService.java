package com.documentlm.service;


public interface LlmService {

    String classifyIntent(String question);

    String answerGeneralQuestion(String question);

    String generateAnswer(String question, String context);
}
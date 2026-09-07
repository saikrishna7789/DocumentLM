package com.documentlm.service;


import com.documentlm.dto.response.ChatResponse;

public interface ChatService {

    ChatResponse ask(String question);

}
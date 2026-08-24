package com.docmind.service;


import com.docmind.dto.response.ChatResponse;

public interface ChatService {

    ChatResponse ask(String question);

}
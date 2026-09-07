package com.documentlm.controller;

import com.documentlm.dto.request.ChatRequest;
import com.documentlm.dto.response.ChatResponse;
import com.documentlm.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "Chat API")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "Ask a question")
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {

        return chatService.ask(request.getQuestion());
    }
}
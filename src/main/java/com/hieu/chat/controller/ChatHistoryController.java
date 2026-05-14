package com.hieu.chat.controller;

import com.hieu.chat.entity.Message;
import com.hieu.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final MessageRepository messageRepository;

    @GetMapping("/{conversationId}")
    public ResponseEntity<List<Message>> getChatHistory(@PathVariable Long conversationId) {
        List<Message> history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return ResponseEntity.ok(history);
    }
}
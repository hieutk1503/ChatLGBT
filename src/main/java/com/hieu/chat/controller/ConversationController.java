package com.hieu.chat.controller;

import com.hieu.chat.entity.Conversation;
import com.hieu.chat.entity.User;
import com.hieu.chat.repository.ConversationRepository; // Bổ sung import
import com.hieu.chat.repository.UserRepository;         // Bổ sung import
import com.hieu.chat.service.ConversationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    // ========== THÊM 2 REPOSITORY NÀY VÀO ĐỂ SỬA LỖI ĐỎ ==========
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    // 1. API: Mở phòng chat với một người bạn (Chat 1-1)
    @PostMapping("/one-to-one/{friendId}")
    public ResponseEntity<?> openConversation(@PathVariable Long friendId) {
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Conversation conv = conversationService.getOrCreateOneToOneConversation(myEmail, friendId);
        return ResponseEntity.ok(conv);
    }

    // DTO nhận dữ liệu tạo nhóm
    @Data
    public static class GroupChatRequest {
        private String groupName;
        private List<String> memberEmails;
    }

    // 2. API: Tạo nhóm chat mới
    @PostMapping("/group")
    public ResponseEntity<?> createGroupConversation(@RequestBody GroupChatRequest request) {
        // Lấy danh sách User từ Database dựa vào list email truyền lên
        List<User> members = userRepository.findByEmailIn(request.getMemberEmails());

        if (members.size() < 2) {
            return ResponseEntity.badRequest().body("Nhóm chat phải có ít nhất 2 thành viên trở lên!");
        }

        // Tạo phòng chat mới
        Conversation groupConversation = Conversation.builder()
                .name(request.getGroupName())
                .isGroup(true)
                .createdAt(LocalDateTime.now())
                .members(members)
                .build();

        conversationRepository.save(groupConversation);

        return ResponseEntity.ok(groupConversation);
    }
}
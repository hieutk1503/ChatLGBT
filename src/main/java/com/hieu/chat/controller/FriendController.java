package com.hieu.chat.controller;

import com.hieu.chat.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/friends") // Khai báo đường dẫn gốc
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    // Hàm dùng chung: Lấy email của người đang cầm JWT Token
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // API 1: Gửi lời mời kết bạn
    @PostMapping("/request/{receiverId}")
    public ResponseEntity<?> sendRequest(@PathVariable Long receiverId) {
        String message = friendService.sendFriendRequest(getCurrentUserEmail(), receiverId);
        return ResponseEntity.ok(message);
    }

    // API 2: Chấp nhận lời mời kết bạn
    @PutMapping("/accept/{friendshipId}")
    public ResponseEntity<?> acceptRequest(@PathVariable Long friendshipId) {
        String message = friendService.acceptFriendRequest(friendshipId, getCurrentUserEmail());
        return ResponseEntity.ok(message);
    }

    // API 3: Lấy danh sách bạn bè
    @GetMapping("/list")
    public ResponseEntity<?> getMyFriends() {
        return ResponseEntity.ok(friendService.getMyFriends(getCurrentUserEmail()));
    }
    // API 4: Lấy danh sách lời mời kết bạn đang chờ xác nhận
    @GetMapping("/requests")
    public ResponseEntity<?> getPendingRequests() {
        // Lưu ý: Bạn cần viết thêm hàm getPendingRequests trong FriendService nhé
        // Hàm này sẽ SELECT các Friendship có trạng thái là PENDING và receiver là user hiện tại
        return ResponseEntity.ok(friendService.getPendingRequests(getCurrentUserEmail()));
    }
}
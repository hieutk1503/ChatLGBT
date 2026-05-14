package com.hieu.chat.controller;

import com.hieu.chat.entity.Conversation;
import com.hieu.chat.entity.Message;
import com.hieu.chat.entity.User;
import com.hieu.chat.repository.ConversationRepository;
import com.hieu.chat.repository.MessageRepository;
import com.hieu.chat.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final SimpMessagingTemplate messagingTemplate;

    // THÊM MỚI: Gọi 3 kho chứa dữ liệu (Repository)
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    // DTO để Nhận/Gửi tin nhắn (Đã nâng cấp thêm ID và Trạng thái)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatMessage {
        private Long messageId; // ID thật của tin nhắn dưới DB
        private Long conversationId;
        private String senderEmail;
        private String content;
        private boolean recalled; // false: bình thường, true: đã thu hồi
    }

    // DTO chuyên dùng để gửi yêu cầu thu hồi
    @Data
    public static class RecallRequest {
        private Long messageId;
        private Long conversationId;
        private String senderEmail;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        User sender = userRepository.findByEmail(chatMessage.getSenderEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người gửi"));

        Conversation conversation = conversationRepository.findById(chatMessage.getConversationId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chat"));

        // Lưu tin nhắn xuống DB
        Message message = Message.builder()
                .content(chatMessage.getContent())
                .sender(sender)
                .conversation(conversation)
                .createdAt(LocalDateTime.now())
                // Lưu ý: Nếu entity Message của bạn dùng kiểu boolean (nguyên thủy) hoặc Boolean,
                // nhớ cấu hình setter tương ứng (vd: setIsRecalled(false) hoặc setRecalled(false))
                .build();

        Message savedMessage = messageRepository.save(message);

        // Lấy ID vừa được DB cấp phát nhét ngược lại vào gói tin phát sóng
        chatMessage.setMessageId(savedMessage.getId());
        chatMessage.setRecalled(false);

        String destination = "/topic/conversation/" + chatMessage.getConversationId();
        messagingTemplate.convertAndSend(destination, chatMessage);
    }

    // ========== THÊM MỚI: HÀM XỬ LÝ THU HỒI ==========
    @MessageMapping("/chat.recall")
    @Transactional
    public void recallMessage(@Payload RecallRequest request) {
        Message message = messageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn"));

        // Bảo mật: Chỉ cho phép chính chủ thu hồi tin nhắn của mình
        if (message.getSender().getEmail().equals(request.getSenderEmail())) {
            // Giả sử Entity Message của bạn có setter cho trường này
            message.setRecalled(true); // Hoặc setIsRecalled(true) tùy theo code của bạn
            messageRepository.save(message);

            // Bắn tín hiệu thu hồi cho cả phòng biết
            String destination = "/topic/conversation/" + request.getConversationId() + "/recall";
            messagingTemplate.convertAndSend(destination, request);
        }
    }
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingRequest request) {
        // Phát tán trạng thái đang gõ tới kênh riêng: /topic/conversation/{id}/typing
        String destination = "/topic/conversation/" + request.getConversationId() + "/typing";
        messagingTemplate.convertAndSend(destination, request);
    }

    @Data
    public static class TypingRequest {
        private Long conversationId;
        private String senderEmail;
        private boolean typing; // true là đang gõ, false là đã dừng
    }
    @MessageMapping("/chat.seen")
    public void handleSeen(@Payload SeenRequest request) {
        // Phát sóng lại cho người kia biết là mình đã xem
        String destination = "/topic/conversation/" + request.getConversationId() + "/seen";
        messagingTemplate.convertAndSend(destination, request);
    }

    // Cái hộp đựng dữ liệu Đã xem
    @Data
    public static class SeenRequest {
        private Long conversationId;
        private String readerEmail; // Email của người vừa xem tin nhắn
    }
}
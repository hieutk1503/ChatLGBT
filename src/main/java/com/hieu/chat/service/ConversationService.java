package com.hieu.chat.service;

import com.hieu.chat.entity.Conversation;
import com.hieu.chat.entity.Participant;
import com.hieu.chat.entity.User;
import com.hieu.chat.repository.ConversationRepository;
import com.hieu.chat.repository.ParticipantRepository;
import com.hieu.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public Conversation getOrCreateOneToOneConversation(String myEmail, Long friendId) {
        User me = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin của bạn"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người bạn này"));

        // 1. Kiểm tra xem đã có phòng chat giữa 2 người chưa
        Optional<Conversation> existingConv = participantRepository.findOneToOneConversation(me, friend);
        if (existingConv.isPresent()) {
            return existingConv.get(); // Nếu có rồi thì trả về phòng cũ
        }

        // 2. Nếu chưa có, tạo Phòng mới (isGroup = false)
        Conversation newConv = Conversation.builder()
                .isGroup(false)
                .build();
        conversationRepository.save(newConv);

        // 3. Thêm cả 2 người vào làm Participant (Thành viên) của phòng đó
        Participant p1 = Participant.builder().conversation(newConv).user(me).role("MEMBER").build();
        Participant p2 = Participant.builder().conversation(newConv).user(friend).role("MEMBER").build();
        participantRepository.save(p1);
        participantRepository.save(p2);

        return newConv;
    }
}
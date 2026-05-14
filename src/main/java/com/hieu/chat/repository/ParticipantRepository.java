package com.hieu.chat.repository;

import com.hieu.chat.entity.Conversation;
import com.hieu.chat.entity.Participant;
import com.hieu.chat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    // Tìm phòng chat 1-1 chung giữa 2 user
    @Query("SELECT p1.conversation FROM Participant p1 JOIN Participant p2 ON p1.conversation = p2.conversation " +
            "WHERE p1.user = :u1 AND p2.user = :u2 AND p1.conversation.isGroup = false")
    Optional<Conversation> findOneToOneConversation(@Param("u1") User u1, @Param("u2") User u2);
}
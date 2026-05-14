package com.hieu.chat.repository;

import com.hieu.chat.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ConversationRepository extends JpaRepository<Conversation, Long> {}
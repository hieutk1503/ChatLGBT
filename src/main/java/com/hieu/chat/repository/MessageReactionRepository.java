package com.hieu.chat.repository;

import com.hieu.chat.entity.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {}
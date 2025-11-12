package com.hvc.brandlocus.repositories;

import com.hvc.brandlocus.entities.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>,
        JpaSpecificationExecutor<ChatMessage> {
    Page<ChatMessage> findAllByChatSessionId(Long sessionId, Pageable pageable);


    // Optional: find all messages by session ID
    // List<ChatMessage> findByChatSessionId(Long sessionId);
}

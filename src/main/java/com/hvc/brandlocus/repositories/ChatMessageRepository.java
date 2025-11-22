package com.hvc.brandlocus.repositories;

import com.hvc.brandlocus.entities.ChatMessage;
import com.hvc.brandlocus.entities.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>,
        JpaSpecificationExecutor<ChatMessage> {
    Page<ChatMessage> findAllByChatSessionId(Long sessionId, Pageable pageable);
    List<ChatMessage> findByChatSessionOrderByCreatedAtAsc(ChatSession chatSession);


    // Optional: find all messages by session ID
    // List<ChatMessage> findByChatSessionId(Long sessionId);
}

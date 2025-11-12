package com.hvc.brandlocus.repositories;

import com.hvc.brandlocus.entities.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long>,
        JpaSpecificationExecutor<ChatSession> {

    List<ChatSession> findAllByUserId(Long userId);
    Page<ChatSession> findAllByUserId(Long userId, Pageable pageable);


    // Optional: find all sessions by user ID
    // List<ChatSession> findByUserId(Long userId);
}

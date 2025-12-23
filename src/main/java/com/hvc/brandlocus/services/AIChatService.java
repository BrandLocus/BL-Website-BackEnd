package com.hvc.brandlocus.services;

import com.hvc.brandlocus.dto.request.ChatMessageRequest;
import com.hvc.brandlocus.dto.request.EditAIResponseRequest;
import com.hvc.brandlocus.dto.request.PaginationRequest;
import com.hvc.brandlocus.dto.request.RegisterUserRequest;
import com.hvc.brandlocus.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

public interface AIChatService {
    ResponseEntity<ApiResponse<?>> startChat(Principal principal,ChatMessageRequest request);
    ResponseEntity<ApiResponse<?>> getChats(Principal principal, @RequestParam(required = false) Long sessionId, PaginationRequest paginationRequest);
    public ResponseEntity<ApiResponse<?>> getAllChats(Principal principal,Long sessionId);
    ResponseEntity<ApiResponse<?>> reviewAIResponse(Principal principal, Long messageId, EditAIResponseRequest request);

}

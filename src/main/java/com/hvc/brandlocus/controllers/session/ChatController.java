package com.hvc.brandlocus.controllers.session;

import com.hvc.brandlocus.dto.request.ChatMessageRequest;
import com.hvc.brandlocus.dto.request.EditAIResponseRequest;
import com.hvc.brandlocus.dto.request.PaginationRequest;
import com.hvc.brandlocus.services.AIChatService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {
    private final AIChatService chatService;

    /**
     * Start a new chat or continue an existing chat with AI
     *
     * @param principal logged-in user
     * @param request   chat request DTO
     * @return ApiResponse with the user and AI messages
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<?>> startChat(
            Principal principal,
            @RequestBody ChatMessageRequest request
    ) {
        return chatService.startChat(principal, request);
    }



    @GetMapping("/")
    public ResponseEntity<ApiResponse<?>> getChats(
            Principal principal,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order
    ) {
        PaginationRequest paginationRequest = PaginationRequest.builder()
                .page(page)
                .limit(limit)
                .sortBy(sortBy)
                .order(order)
                .build();

        return chatService.getChats(principal, sessionId, paginationRequest);
    }

    @PatchMapping("/review/{messageId}")
    public ResponseEntity<ApiResponse<?>> reviewAICHAT(
            Principal principal,
            @PathVariable Long messageId,
            @RequestBody EditAIResponseRequest request
            ) {
        return chatService.reviewAIResponse(principal,messageId,request);
    }

}

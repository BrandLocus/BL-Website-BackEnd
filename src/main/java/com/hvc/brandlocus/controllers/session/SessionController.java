package com.hvc.brandlocus.controllers.session;

import com.hvc.brandlocus.dto.request.PaginationRequest;
import com.hvc.brandlocus.services.SessionService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/session")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class SessionController {
    private final SessionService sessionService;


    @GetMapping("/admin/chat-sessions")
    public ResponseEntity<ApiResponse<?>> getAllChatSessions(
            Principal principal,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "alltime") String timeFilter,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
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

        return sessionService.adminSessionAndChatLogs(
                principal, sessionId, userId, searchTerm, timeFilter, startDate, endDate, paginationRequest
        );
    }



}

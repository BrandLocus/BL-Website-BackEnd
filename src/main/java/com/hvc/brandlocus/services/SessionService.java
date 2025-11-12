package com.hvc.brandlocus.services;

import com.hvc.brandlocus.dto.request.PaginationRequest;
import com.hvc.brandlocus.utils.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

public interface SessionService {
    ResponseEntity<ApiResponse<?>> adminSessionAndChatLogs(Principal principal, Long sessionId, Long userId, String searchTerm, String timeFilter, String startDate, String endDate, PaginationRequest paginationRequest);
}

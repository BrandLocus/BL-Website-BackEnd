package com.hvc.brandlocus.services;

import com.hvc.brandlocus.dto.request.ChatMessageRequest;
import com.hvc.brandlocus.utils.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.LocalDate;

public interface AdminDashboardService {
    ResponseEntity<ApiResponse<?>> adminDashboard(Principal principal,String filter, LocalDate startDate, LocalDate endDate);
    ResponseEntity<ApiResponse<?>> userGraph(Principal principal, LocalDate startDate, LocalDate endDate);
}

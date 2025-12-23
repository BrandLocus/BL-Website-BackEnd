package com.hvc.brandlocus.services;

import com.hvc.brandlocus.dto.request.PaginationRequest;
import com.hvc.brandlocus.utils.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

public interface UserService {

    ResponseEntity<ApiResponse<?>> getAllUsers(
            Principal principal,
            Long userId,
            String searchTerm,
            String timeFilter,
            String state,
            String country,
            String startDate,
            String endDate,
            PaginationRequest paginationRequest
    );

    ResponseEntity<ApiResponse<?>> getAllUsers(Principal principal);
}

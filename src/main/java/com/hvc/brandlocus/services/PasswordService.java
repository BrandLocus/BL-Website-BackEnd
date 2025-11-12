package com.hvc.brandlocus.services;

import com.hvc.brandlocus.dto.request.ChangePasswordRequest;
import com.hvc.brandlocus.dto.request.CompletePasswordReset;
import com.hvc.brandlocus.utils.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

public interface PasswordService {
    ResponseEntity<ApiResponse<?>> changePassword(Principal principal, ChangePasswordRequest request);
    ResponseEntity<ApiResponse<?>> completePasswordReset(CompletePasswordReset request);
}

package com.hvc.brandlocus.services;

import com.hvc.brandlocus.dto.request.LoginRequest;
import com.hvc.brandlocus.dto.request.RegisterUserRequest;
import com.hvc.brandlocus.utils.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<ApiResponse<?>> registerUser(RegisterUserRequest request);
    ResponseEntity<ApiResponse<?>> loginUser(LoginRequest request);
}

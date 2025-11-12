package com.hvc.brandlocus.services;

import com.hvc.brandlocus.dto.request.TokenRequest;
import com.hvc.brandlocus.dto.request.VerifyTokenRequest;
import com.hvc.brandlocus.utils.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface TokenService {
    ResponseEntity<ApiResponse<?>> getToken(TokenRequest request);
    ResponseEntity<ApiResponse<?>> verifyToken(VerifyTokenRequest request);
}

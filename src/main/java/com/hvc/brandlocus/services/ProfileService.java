package com.hvc.brandlocus.services;

import com.hvc.brandlocus.dto.request.UpdateProfileRequest;
import com.hvc.brandlocus.utils.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

public interface ProfileService {
    ResponseEntity<ApiResponse<?>> getUserProfile(Principal principal);
    ResponseEntity<ApiResponse<?>> updateProfile(Principal principal, UpdateProfileRequest updateProfileRequest);
}

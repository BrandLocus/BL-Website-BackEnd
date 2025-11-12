package com.hvc.brandlocus.controllers.auth;

import com.hvc.brandlocus.dto.request.LoginRequest;
import com.hvc.brandlocus.dto.request.RegisterUserRequest;
import com.hvc.brandlocus.services.AuthService;
import com.hvc.brandlocus.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> createUser(@Valid @RequestBody RegisterUserRequest request){
        return authService.registerUser(request);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login( @Valid @RequestBody LoginRequest request){
        log.info("log user in");
        return authService.loginUser(request);
    }
}

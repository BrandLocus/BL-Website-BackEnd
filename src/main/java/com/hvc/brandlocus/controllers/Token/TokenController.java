package com.hvc.brandlocus.controllers.Token;

import com.hvc.brandlocus.dto.request.EmailOtpRequest;
import com.hvc.brandlocus.dto.request.TokenRequest;
import com.hvc.brandlocus.dto.request.VerifyTokenRequest;
import com.hvc.brandlocus.services.TokenService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/token")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class TokenController {
    private final TokenService tokenService;

    @PostMapping("/get")
    public ResponseEntity<ApiResponse<?>> getToken(@RequestBody TokenRequest request){
        return tokenService.getToken(request);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> validateToken(@RequestBody VerifyTokenRequest verifyTokenRequest){
        return tokenService.verifyToken(verifyTokenRequest);
    }
}

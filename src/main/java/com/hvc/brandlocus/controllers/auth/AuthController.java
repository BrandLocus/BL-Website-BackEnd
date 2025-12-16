package com.hvc.brandlocus.controllers.auth;

import com.hvc.brandlocus.dto.request.CompletePasswordReset;
import com.hvc.brandlocus.dto.request.LoginRequest;
import com.hvc.brandlocus.dto.request.RegisterUserRequest;
import com.hvc.brandlocus.dto.response.AuthTokenResponse;
import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.RefreshToken;
import com.hvc.brandlocus.security.filter.filterservice.JwtService;
import com.hvc.brandlocus.security.filter.filterservice.RefreshTokenServiceInterface;
import com.hvc.brandlocus.services.AuthService;
import com.hvc.brandlocus.services.PasswordService;
import com.hvc.brandlocus.utils.ApiResponse;
import com.hvc.brandlocus.utils.ResponseUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenServiceInterface refreshTokenService;
    private final JwtService jwtService;
    private final PasswordService passwordService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> createUser(@Valid @RequestBody RegisterUserRequest request){
        return authService.registerUser(request);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login( @Valid @RequestBody LoginRequest request){
        log.info("log user in");
        return authService.loginUser(request);
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refreshToken(@RequestParam String refreshToken) {
        try {
            RefreshToken token = refreshTokenService.getByToken(refreshToken);

            if (token == null || !refreshTokenService.isValid(token)) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtils.createFailureResponse(
                                "Invalid or expired refresh token",
                                "Please login again"
                        ));
            }

            BaseUser user = token.getUser();


            String newJwt = jwtService.generateToken(user);


            RefreshToken updatedToken = refreshTokenService.createRefreshToken(user);

            return ResponseEntity.ok(
                    ResponseUtils.createSuccessResponse(
                            AuthTokenResponse.builder()
                                    .jwtToken(newJwt)
                                    .refreshToken(updatedToken.getToken())
                                    .userId(user.getId())
                                    .role(user.getRole().getName())
                                    .isActive(user.getIsActive())
                                    .email(user.getEmail())
                                    .build(),
                            "Token refreshed successfully"
                    ));

        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(ResponseUtils.createFailureResponse(
                            "Error",
                            "Could not refresh token"
                    ));
        }
    }

    @PostMapping("/password-reset")
    public ResponseEntity<ApiResponse<?>> completePasswordReset(@RequestBody CompletePasswordReset completePasswordReset){
        return passwordService.completePasswordReset(completePasswordReset);
    }


    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@RequestParam String refreshToken) {
        RefreshToken token = refreshTokenService.getByToken(refreshToken);
        if (token != null) {
            refreshTokenService.revokeToken(token);
        }
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse(null, "Logged out successfully"));
    }
}

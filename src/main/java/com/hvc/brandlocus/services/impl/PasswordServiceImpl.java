package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.request.ChangePasswordRequest;
import com.hvc.brandlocus.dto.request.CompletePasswordReset;
import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.Token;
import com.hvc.brandlocus.repositories.BaseUserRepository;
import com.hvc.brandlocus.repositories.TokenRepository;
import com.hvc.brandlocus.services.PasswordService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.hvc.brandlocus.utils.ResponseUtils.createFailureResponse;
import static com.hvc.brandlocus.utils.ResponseUtils.createSuccessResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {
    private final BaseUserRepository baseUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;


    @Override
    public ResponseEntity<ApiResponse<?>> changePassword(Principal principal, ChangePasswordRequest request) {
        try {
            String phoneNo = principal.getName();

            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(phoneNo);

            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("User not found", "User does not exist"));
            }

            BaseUser user = optionalUser.get();

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createFailureResponse("Invalid operation", "Invalid old password"));
            }

            if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createFailureResponse("Invalid operation", "New password and confirm password do not match"));
            }


            String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(hashedNewPassword);

            baseUserRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(createSuccessResponse(null,"password successfully updated"));
        }catch (Exception e) {
            log.warn("error occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createFailureResponse("Invalid operation", "invalid operation"));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<?>> completePasswordReset(CompletePasswordReset request) {
        try {

            log.info("password reset: {} {}", request.getPassword(),request.getConfirmPassword());

            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createFailureResponse("", "Passwords do not match"));
            }

            Optional<Token> tokenOpt = tokenRepository.findByToken(request.getOtp());
            if (tokenOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createFailureResponse("", "Invalid OTP"));
            }

            Token token = tokenOpt.get();

            if (token.isUsed()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createFailureResponse("", "OTP has already been used"));
            }

            if (!token.isValidated()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createFailureResponse("", "OTP not validated"));
            }

            if (token.getExpiry().isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createFailureResponse("", "OTP has expired"));
            }

            String email = token.getEmail();


            BaseUser user = baseUserRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setPassword(passwordEncoder.encode(request.getPassword()));

            baseUserRepository.save(user);

            token.setUsed(true);
            token.setExpired(true);
            tokenRepository.save(token);

            return ResponseEntity.ok(createSuccessResponse("", "Password reset successful"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse(e.getMessage(), "An unexpected error occurred"));
        }
    }



}

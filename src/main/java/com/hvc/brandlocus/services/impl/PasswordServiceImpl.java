package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.request.ChangePasswordRequest;
import com.hvc.brandlocus.dto.request.CompletePasswordReset;
import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.repositories.BaseUserRepository;
import com.hvc.brandlocus.services.PasswordService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

import static com.hvc.brandlocus.utils.ResponseUtils.createFailureResponse;
import static com.hvc.brandlocus.utils.ResponseUtils.createSuccessResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {
    private final BaseUserRepository baseUserRepository;
    private final PasswordEncoder passwordEncoder;

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
            if (!request.getPassword().equals(request.getConfirmPassword())) {

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("Passwords do not match", "Passwords do not match"));
            }

            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(request.getEmail());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("User not found", "User not found"));
            }

            BaseUser user = optionalUser.get();

            String encodedPassword = passwordEncoder.encode(request.getPassword());
            user.setPassword(encodedPassword);

            baseUserRepository.save(user);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(createSuccessResponse(null,"password reset successfully "));
        }catch (Exception e) {
            log.warn("error occurred while resetting password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createFailureResponse("Invalid operation", "invalid operation"));
        }
    }


}

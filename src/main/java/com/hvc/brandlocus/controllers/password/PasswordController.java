package com.hvc.brandlocus.controllers.password;

import com.hvc.brandlocus.dto.request.ChangePasswordRequest;
import com.hvc.brandlocus.dto.request.CompletePasswordReset;
import com.hvc.brandlocus.services.PasswordService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/password")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/change")
    public ResponseEntity<ApiResponse<?>> changePassword(Principal principal,@RequestBody ChangePasswordRequest changePasswordRequest){
        return passwordService.changePassword(principal,changePasswordRequest);
    }

//    @PostMapping("/reset")
//    public ResponseEntity<ApiResponse<?>> completePasswordReset(@RequestBody CompletePasswordReset completePasswordReset){
//        return passwordService.completePasswordReset(completePasswordReset);
//    }
}

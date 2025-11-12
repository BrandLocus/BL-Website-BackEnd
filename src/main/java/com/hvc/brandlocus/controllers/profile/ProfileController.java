package com.hvc.brandlocus.controllers.profile;


import com.hvc.brandlocus.dto.request.UpdateProfileRequest;
import com.hvc.brandlocus.services.ProfileService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profile")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<?>> getUserProfile(Principal principal){
        return profileService.getUserProfile(principal);
    }

    @GetMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateUserProfile(Principal principal,@RequestBody UpdateProfileRequest request){
        return profileService.updateProfile(principal, request);
    }
}

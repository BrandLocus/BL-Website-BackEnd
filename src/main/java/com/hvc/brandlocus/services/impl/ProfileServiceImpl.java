package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.request.UpdateProfileRequest;
import com.hvc.brandlocus.dto.response.ProfileResponse;
import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.Profile;
import com.hvc.brandlocus.repositories.BaseUserRepository;
import com.hvc.brandlocus.services.ProfileService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

import static com.hvc.brandlocus.utils.ResponseUtils.createFailureResponse;
import static com.hvc.brandlocus.utils.ResponseUtils.createSuccessResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final BaseUserRepository baseUserRepository;

    @Override
    public ResponseEntity<ApiResponse<?>> getUserProfile(Principal principal) {
        try {
            String phoneNo = principal.getName();

            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(phoneNo);

            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("User not found", "User does not exist"));
            }

            BaseUser user = optionalUser.get();
            Profile profile = user.getProfile();

            if (profile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("Profile missing", "User profile not found"));
            }

            ProfileResponse response = ProfileResponse.builder().email(profile.getEmail())
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .businessName(profile.getBusinessName())
                    .industryName(profile.getIndustryName())
                    .role(profile.getRole())
                    .state(profile.getState())
                    .country(profile.getCountry())
                    .businessBrief(profile.getBusinessBrief())
                    .profileImageUrl(profile.getProfileImageUrl())
                    .email(profile.getEmail()).build();

            return ResponseEntity.ok(createSuccessResponse(response, "Profile fetched successfully"));
        } catch (Exception ex) {
            log.error("Failed to fetch user profile: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse("Error", "Failed to fetch user profile"));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<?>> updateProfile(Principal principal, UpdateProfileRequest request) {
        try {
            String email = principal.getName();
            BaseUser user = baseUserRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
                user.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null && !request.getLastName().isBlank()) {
                user.setLastName(request.getLastName());
            }
            if (request.getIndustryName() != null && !request.getIndustryName().isBlank()) {
                user.setIndustryName(request.getIndustryName());
            }
            if (request.getBusinessName() != null && !request.getBusinessName().isBlank()) {
                user.setBusinessName(request.getBusinessName());
            }
            if (request.getBusinessBrief() != null && !request.getBusinessBrief().isBlank()) {
                user.setBusinessBrief(request.getBusinessBrief());
            }

            if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().isBlank()) {
                user.setProfileImageUrl(request.getProfileImageUrl());
            }


            Profile profile = user.getProfile();
            if (profile != null) {
                if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
                    profile.setFirstName(request.getFirstName());
                }
                if (request.getLastName() != null && !request.getLastName().isBlank()) {
                    profile.setLastName(request.getLastName());
                }
                if (request.getIndustryName() != null && !request.getIndustryName().isBlank()) {
                    profile.setIndustryName(request.getIndustryName());
                }
                if (request.getBusinessName() != null && !request.getBusinessName().isBlank()) {
                    profile.setBusinessName(request.getBusinessName());
                }
                if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().isBlank()) {
                    profile.setProfileImageUrl(request.getProfileImageUrl());
                }
                if (request.getBusinessBrief() != null && !request.getBusinessBrief().isBlank()) {
                    profile.setBusinessBrief(request.getBusinessBrief());
                }
                if (request.getCountry() != null && !request.getCountry().isBlank()) {
                    profile.setCountry(request.getCountry());
                }
                if (request.getState() != null && !request.getState().isBlank()) {
                    profile.setState(request.getState());
                }
            }

            baseUserRepository.save(user);

            return ResponseEntity.ok(createSuccessResponse(null, "Profile updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse("Error updating profile", e.getMessage()));
        }
    }

}

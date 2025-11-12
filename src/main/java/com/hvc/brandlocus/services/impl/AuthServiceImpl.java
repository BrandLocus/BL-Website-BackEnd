package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.request.LoginRequest;
import com.hvc.brandlocus.dto.request.RegisterUserRequest;
import com.hvc.brandlocus.dto.response.LoginResponse;
import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.Profile;
import com.hvc.brandlocus.entities.Roles;
import com.hvc.brandlocus.enums.UserRoles;
import com.hvc.brandlocus.enums.UserStatus;
import com.hvc.brandlocus.repositories.BaseUserRepository;
import com.hvc.brandlocus.repositories.RolesRepository;
import com.hvc.brandlocus.security.filter.filterservice.JwtService;
import com.hvc.brandlocus.services.AuthService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.hvc.brandlocus.utils.ResponseUtils.createFailureResponse;
import static com.hvc.brandlocus.utils.ResponseUtils.createSuccessResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final BaseUserRepository baseUserRepository;
    private final RolesRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public ResponseEntity<ApiResponse<?>> registerUser(RegisterUserRequest request) {
        try {
            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(request.getEmail());

            if (optionalUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createFailureResponse("invalid input", "User with email " + request.getEmail() + " already exists"));

            }

            Optional<Roles> optionalRole = roleRepository.findByName(UserRoles.USER.getValue());

            if (optionalRole.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createFailureResponse("role not found", "Reason: role not found"));
            }


            Roles role = optionalRole.get();

            BaseUser newUser = BaseUser.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .industryName(request.getIndustryName())
                    .businessName(request.getBusinessName())
                    .email(request.getEmail().trim())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(role)
                    .isActive(true)
                    .userStatus(UserStatus.ACTIVE.getValue())
                    .agreementToReceiveAIGeneratedResponse(request.isAgreementToReceiveAIGeneratedResponse())
                    .agreementToReceiveAIGeneratedResponseTimestamp(LocalDateTime.now())
                    .build();

            Profile profile = Profile.builder()
                    .user(newUser)
                    .firstName(newUser.getFirstName())
                    .lastName(newUser.getLastName())
                    .email(newUser.getEmail())
                    .industryName(newUser.getIndustryName())
                    .businessName(newUser.getBusinessName())
                    .role(role.getName())
                    .build();

            newUser.setProfile(profile);

            baseUserRepository.save(newUser);


            return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse(null,"User registered successfully"));



        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse("error", e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<?>> loginUser(LoginRequest request) {
        try {
            BaseUser baseUser = baseUserRepository.findByEmail(request.getEmail()).orElse(null);
            if (baseUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createFailureResponse("User does not exist", "User does not exist"));
            }

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail().trim(),request.getPassword()));
            log.info("authenticated user : {}",baseUser.getEmail());
            var jwtToken = jwtService.generateToken(baseUser);

            LoginResponse response = LoginResponse.builder().userId(baseUser.getId())
                    .role(baseUser.getRole().getName())
                    .isActive(baseUser.getIsActive())
                    .jwtToken(jwtToken)
                    .email(baseUser.getEmail()).build();

            return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse(response,"login successful"));
        }catch (AuthenticationException e) {
            log.warn("login failed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createFailureResponse("login failed", "invalid email or password"));
        }
    }


}

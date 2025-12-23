package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.request.PaginationRequest;
import com.hvc.brandlocus.dto.response.PaginationResponse;
import com.hvc.brandlocus.dto.response.UserResponse;
import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.enums.UserRoles;
import com.hvc.brandlocus.repositories.BaseUserRepository;
import com.hvc.brandlocus.repositories.specification.BaseUserSpecification;
import com.hvc.brandlocus.services.UserService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.hvc.brandlocus.utils.ResponseUtils.createFailureResponse;
import static com.hvc.brandlocus.utils.ResponseUtils.createSuccessResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final BaseUserRepository userRepository;

    @Override
    public ResponseEntity<ApiResponse<?>> getAllUsers(
            Principal principal,
            Long userId,
            String searchTerm,
            String timeFilter,
            String state,
            String country,
            String startDate,
            String endDate,
            PaginationRequest paginationRequest
    ) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createFailureResponse("Unauthorized", "User is not authenticated"));
            }

            BaseUser loggedInUser = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            // 🔐 3. Check ADMIN role
            if (loggedInUser.getRole() == null ||
                    !UserRoles.ADMIN.getValue().equalsIgnoreCase(loggedInUser.getRole().getName())) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createFailureResponse(
                                "Access denied",
                                "Only administrators are allowed to access this resource"
                        ));
            }



            if (userId != null) {
                Optional<BaseUser> userOpt = userRepository.findById(userId);
                if (userOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(createFailureResponse("User not found", "No user found with ID: " + userId));
                }

                BaseUser user = userOpt.get();

                UserResponse userResponse = UserResponse.builder()
                        .userId(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .industryName(user.getIndustryName())
                        .businessName(user.getBusinessName())
                        .businessBrief(user.getBusinessBrief())
                        .profileImageUrl(user.getProfileImageUrl())
                        .role(user.getRole() != null ? user.getRole().getName() : null)
                        .state(user.getState())
                        .country(user.getCountry())
                        .build();

                return ResponseEntity.ok(createSuccessResponse(userResponse, "User fetched successfully"));
            }

            Sort.Direction direction = paginationRequest.getOrder().equalsIgnoreCase("asc") ?
                    Sort.Direction.ASC : Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(
                    paginationRequest.getPage(),
                    paginationRequest.getLimit(),
                    Sort.by(direction, paginationRequest.getSortBy())
            );

            LocalDate start = null;
            LocalDate end = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            if (startDate != null && endDate != null) {
                start = LocalDate.parse(startDate, formatter);
                end = LocalDate.parse(endDate, formatter);
            }

            Specification<BaseUser> spec = Specification.allOf(
                    BaseUserSpecification.searchTerm(searchTerm),
                    BaseUserSpecification.byTimeFilter(timeFilter),
                    BaseUserSpecification.hasState(state),           // Add this
                    BaseUserSpecification.hasCountry(country),
                    BaseUserSpecification.createdBetween(start, end),
                    BaseUserSpecification.excludeAdmin()
            );


            Page<BaseUser> userPage = userRepository.findAll(spec, pageable);

            List<UserResponse> users = userPage.getContent().stream()
                    .map(user -> UserResponse.builder()
                            .userId(user.getId())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .industryName(user.getIndustryName())
                            .businessName(user.getBusinessName())
                            .businessBrief(user.getBusinessBrief())
                            .profileImageUrl(user.getProfileImageUrl())
                            .role(user.getRole() != null ? user.getRole().getName() : null)
                            .state(user.getState())
                            .country(user.getCountry())
                            .build()
                    )
                    .toList();

            PaginationResponse<UserResponse> response = PaginationResponse.<UserResponse>builder()
                    .content(users)
                    .page(userPage.getNumber())
                    .size(userPage.getSize())
                    .totalElements(userPage.getTotalElements())
                    .totalPages(userPage.getTotalPages())
                    .last(userPage.isLast())
                    .build();

            return ResponseEntity.ok(createSuccessResponse(response, "Users fetched successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse(e.getLocalizedMessage(), "Failed to fetch users: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<?>> getAllUsers(Principal principal) {
        try {
            // Fetch the requesting user
            Optional<BaseUser> optionalUser = userRepository.findByEmail(principal.getName().trim());

            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("User not found", "User does not exist"));
            }

            BaseUser admin = optionalUser.get();

            if (admin.getRole() == null || !"ADMIN".equalsIgnoreCase(admin.getRole().getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createFailureResponse("Access denied", "Admin access required"));
            }

            // Fetch all users except those with ADMIN role
            List<BaseUser> users = userRepository.findAll().stream()
                    .filter(user -> user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole().getName()))
                    .toList();

            List<UserResponse> userResponses = users.stream()
                    .map(user -> UserResponse.builder()
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .industryName(user.getIndustryName())
                            .businessName(user.getBusinessName())
                            .businessBrief(user.getBusinessBrief())
                            .profileImageUrl(user.getProfileImageUrl())
                            .role(user.getRole() != null ? user.getRole().getName() : null)
                            .state(user.getState())
                            .country(user.getCountry())
                            .build()
                    )
                    .toList();

            return ResponseEntity.ok(
                    createSuccessResponse(userResponses, "Users fetched successfully")
            );

        } catch (Exception e) {
            log.error("Error fetching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse(e.getLocalizedMessage(), "Failed to fetch users"));
        }
    }


}

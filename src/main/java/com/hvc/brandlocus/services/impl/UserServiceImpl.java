package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.request.PaginationRequest;
import com.hvc.brandlocus.dto.response.PaginationResponse;
import com.hvc.brandlocus.dto.response.UserResponse;
import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.repositories.BaseUserRepository;
import com.hvc.brandlocus.repositories.specification.BaseUserSpecification;
import com.hvc.brandlocus.services.UserService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
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
public class UserServiceImpl implements UserService {

    private final BaseUserRepository userRepository;

    @Override
    public ResponseEntity<ApiResponse<?>> getAllUsers(
            Principal principal,
            Long userId,
            String searchTerm,
            String timeFilter,
            String startDate,
            String endDate,
            PaginationRequest paginationRequest
    ) {
        try {
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
                        .profileImageUrl(user.getProfileImageUrl())
                        .role(user.getRole() != null ? user.getRole().getName() : null)
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
                    BaseUserSpecification.createdBetween(start, end)
            );


            Page<BaseUser> userPage = userRepository.findAll(spec, pageable);

            List<UserResponse> users = userPage.getContent().stream()
                    .map(user -> UserResponse.builder()
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .industryName(user.getIndustryName())
                            .businessName(user.getBusinessName())
                            .profileImageUrl(user.getProfileImageUrl())
                            .role(user.getRole() != null ? user.getRole().getName() : null)
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
}

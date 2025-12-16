package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.request.AdminFormReplyRequest;
import com.hvc.brandlocus.dto.request.CreateFormRequest;
import com.hvc.brandlocus.dto.request.PaginationRequest;
import com.hvc.brandlocus.dto.response.FormResponse;
import com.hvc.brandlocus.dto.response.PaginationResponse;
import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.Forms;
import com.hvc.brandlocus.enums.FormStatus;
import com.hvc.brandlocus.enums.ServiceNeeded;
import com.hvc.brandlocus.repositories.BaseUserRepository;
import com.hvc.brandlocus.repositories.FormRepository;
import com.hvc.brandlocus.repositories.specification.FormSpecification;
import com.hvc.brandlocus.services.FormService;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.hvc.brandlocus.utils.ResponseUtils.createFailureResponse;
import static com.hvc.brandlocus.utils.ResponseUtils.createSuccessResponse;


@Service
@RequiredArgsConstructor
@Slf4j
public class FormServiceImpl implements FormService {

    private final FormRepository formRepository;
    private final BaseUserRepository baseUserRepository;


    @Override
    public ResponseEntity<ApiResponse<?>> submitForm(CreateFormRequest createFormRequest) {
        try {


//            String email = principal.getName();
//            log.info("submit form for email: {}", email);
//
//            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(email);
//
//            if (optionalUser.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(createFailureResponse("User not found", "User does not exist"));
//            }
//
//            BaseUser user = optionalUser.get();

            Forms form = Forms.builder()
                    .firstName(createFormRequest.getFirstName())
                    .lastName(createFormRequest.getLastName())
                    .email(createFormRequest.getEmail())
                    .serviceNeeded(ServiceNeeded.valueOf(createFormRequest.getServiceNeeded()))
                    .companyName(createFormRequest.getCompanyName())
                    .message(createFormRequest.getMessage())
//                    .user(user)
                    .isActive(true)
                    .status(FormStatus.ACTIVE)
                    .build();

            formRepository.save(form);

            return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse(null, "Form submitted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse("error", e.getMessage()));

        }

    }

    @Override
    public ResponseEntity<ApiResponse<?>> getForm(
            Principal principal,
            String searchTerm,
            String timeFilter,
            String filterTerm,
            String startDate,
            String endDate,
            PaginationRequest paginationRequest) {

        try {
            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(principal.getName().trim());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("User not found", "User does not exist"));
            }

            BaseUser user = optionalUser.get();
            boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());

            Sort.Direction direction = "asc".equalsIgnoreCase(paginationRequest.getOrder()) ?
                    Sort.Direction.ASC : Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(
                    paginationRequest.getPage(),
                    paginationRequest.getLimit(),
                    Sort.by(direction, paginationRequest.getSortBy())
            );

            LocalDate start = startDate != null ? LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;

            // VALIDATE DATE RANGE WITH TIME FILTER
            ResponseEntity<ApiResponse<?>> validationResponse = validateDateRangeWithTimeFilter(timeFilter, start, end);
            if (validationResponse != null) {
                return validationResponse;
            }

            // Specification for paginated forms
            Specification<Forms> spec = Specification.allOf(
                    FormSpecification.isActive(),
                    FormSpecification.searchTerm(searchTerm),
                    FormSpecification.filterByServiceNeeded(filterTerm),
                    FormSpecification.byTimeFilter(timeFilter),
                    FormSpecification.createdBetween(start, end)
            );

            if (!isAdmin) {
                spec = spec.and(FormSpecification.byUser(user));
            }

            Page<Forms> formPage = formRepository.findAll(spec, pageable);

            List<FormResponse> responseList = formPage.getContent().stream()
                    .map(f -> FormResponse.builder()
                            .formId(f.getId())
                            .firstName(f.getFirstName())
                            .lastName(f.getLastName())
                            .email(f.getEmail())
                            .serviceNeeded(f.getServiceNeeded().name())
                            .status(f.getStatus().name())
                            .adminReply(f.getAdminReply())
                            .submittedAt(f.getCreatedAt())
                            .repliedAt(f.getRepliedAt())
                            .isActive(f.getIsActive())
                            .companyName(f.getCompanyName())
                            .message(f.getMessage())
                            .build()
                    )
                    .toList();

            PaginationResponse<FormResponse> paginationResponse = PaginationResponse.<FormResponse>builder()
                    .content(responseList)
                    .page(formPage.getNumber())
                    .size(formPage.getSize())
                    .totalElements(formPage.getTotalElements())
                    .totalPages(formPage.getTotalPages())
                    .last(formPage.isLast())
                    .build();

            // ========== CURRENT PERIOD STATISTICS ==========
            Specification<Forms> dashboardSpec = Specification.allOf(
                    FormSpecification.isActive(),
                    FormSpecification.byTimeFilter(timeFilter),
                    FormSpecification.createdBetween(start, end)
            );

            List<Forms> dashboardForms = formRepository.findAll(dashboardSpec);

            long totalActiveForms = dashboardForms.stream()
                    .filter(f -> f.getStatus() == FormStatus.ACTIVE)
                    .count();

            long totalRepliedForms = dashboardForms.stream()
                    .filter(f -> f.getStatus() == FormStatus.REPLIED)
                    .count();

            // Service breakdown counts
            long businessDevelopment = dashboardForms.stream()
                    .filter(f -> f.getServiceNeeded() == ServiceNeeded.BUSINESS_DEVELOPMENT)
                    .count();

            long brandDevelopment = dashboardForms.stream()
                    .filter(f -> f.getServiceNeeded() == ServiceNeeded.BRAND_DEVELOPMENT)
                    .count();

            long capacityBuilding = dashboardForms.stream()
                    .filter(f -> f.getServiceNeeded() == ServiceNeeded.CAPACITY_BUILDING)
                    .count();

            long tradeAndInvestmentFacilitation = dashboardForms.stream()
                    .filter(f -> f.getServiceNeeded() == ServiceNeeded.TRADE_AND_INVESTMENT_FACILITATION)
                    .count();

            long businessQuest = dashboardForms.stream()
                    .filter(f -> f.getServiceNeeded() == ServiceNeeded.BUSINESS_QUEST)
                    .count();

            // ========== PREVIOUS PERIOD STATISTICS ==========
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime compareFrom = null;
            LocalDateTime compareTo = null;

            if (start != null && end != null) {
                long days = Duration.between(
                        start.atStartOfDay(),
                        end.atTime(23, 59, 59)
                ).toDays();

                compareFrom = start.minusDays(days + 1).atStartOfDay();
                compareTo = start.minusDays(1).atTime(23, 59, 59);
            } else if (!"alltime".equalsIgnoreCase(timeFilter)) {
                LocalDateTime from = FormSpecification.getFromDateTime(timeFilter);
                Duration duration = Duration.between(from, now);
                compareTo = from.minusSeconds(1);
                compareFrom = compareTo.minus(duration);
            }

            Double activeFormsChange = null;
            Double repliedFormsChange = null;

            if (compareFrom != null && compareTo != null) {
                final LocalDateTime finalCompareFrom = compareFrom;
                final LocalDateTime finalCompareTo = compareTo;

                Specification<Forms> prevDashboardSpec = Specification.allOf(
                        FormSpecification.isActive(),
                        (root, query, cb) -> cb.between(root.get("createdAt"), finalCompareFrom, finalCompareTo)
                );

                List<Forms> prevDashboardForms = formRepository.findAll(prevDashboardSpec);

                long prevActiveForms = prevDashboardForms.stream()
                        .filter(f -> f.getStatus() == FormStatus.ACTIVE)
                        .count();

                long prevRepliedForms = prevDashboardForms.stream()
                        .filter(f -> f.getStatus() == FormStatus.REPLIED)
                        .count();

                activeFormsChange = calculatePercentageChange(prevActiveForms, totalActiveForms);
                repliedFormsChange = calculatePercentageChange(prevRepliedForms, totalRepliedForms);
            }

            FormResponse statistics = FormResponse.builder()
                    .activeConversations(totalActiveForms)
                    .activeConversationsChange(activeFormsChange)
                    .repliedForms(totalRepliedForms)
                    .repliedFormsChange(repliedFormsChange)
                    .businessDevelopment(businessDevelopment)
                    .brandDevelopment(brandDevelopment)
                    .capacityBuilding(capacityBuilding)
                    .tradeAndInvestmentFacilitation(tradeAndInvestmentFacilitation)
                    .businessQuest(businessQuest)
                    .build();

            Map<String, Object> finalResponse = Map.of(
                    "pagination", paginationResponse,
                    "statistics", statistics
            );

            return ResponseEntity.ok(createSuccessResponse(finalResponse, "Forms fetched successfully"));

        } catch (Exception e) {
            log.error("Error fetching forms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse(e.getLocalizedMessage(), "Failed to fetch forms"));
        }
    }



    private double calculatePercentageChange(long previous, long current) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100;
    }

    @Override
    public ResponseEntity<ApiResponse<?>> replyToForm(Principal principal, Long formId, AdminFormReplyRequest replyRequest) {
        try {
            Optional<BaseUser> optionalAdmin = baseUserRepository.findByEmail(principal.getName().trim());
            if (optionalAdmin.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("User not found", "User does not exist"));
            }

            BaseUser admin = optionalAdmin.get();
            if (admin.getRole() == null || !"ADMIN".equalsIgnoreCase(admin.getRole().getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createFailureResponse("Access denied", "Only admins can reply to forms"));
            }

            Optional<Forms> optionalForm = formRepository.findById(formId);
            if (optionalForm.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("Form not found", "Form does not exist"));
            }

            Forms form = optionalForm.get();

            if (form.getStatus() == FormStatus.REPLIED) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createFailureResponse("Form already replied", "This form has already been replied to"));
            }

            form.setAdminReply(replyRequest.getReply());
            form.setStatus(FormStatus.REPLIED);
            form.setRepliedAt(LocalDateTime.now());

            formRepository.save(form);

            return ResponseEntity.ok(createSuccessResponse(null, "Reply sent successfully"));

        } catch (Exception e) {
            log.error("Error replying to form", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse("error", e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<?>> getFormById(Principal principal, Long formId) {
        try {

            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(principal.getName().trim());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("User not found", "User does not exist"));
            }

            BaseUser user = optionalUser.get();
            boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());

            Optional<Forms> optionalForm = formRepository.findById(formId);
            if (optionalForm.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("Form not found", "Form does not exist"));
            }

            Forms form = optionalForm.get();

            // AUTHORIZATION CHECK - Only admin
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createFailureResponse("Access denied", "You do not have permission to view this form"));
            }


            FormResponse formResponse = FormResponse.builder()
                    .formId(form.getId())
                    .firstName(form.getFirstName())
                    .lastName(form.getLastName())
                    .email(form.getEmail())
                    .serviceNeeded(form.getServiceNeeded().name())
                    .companyName(form.getCompanyName())
                    .message(form.getMessage())
                    .status(form.getStatus().name())
                    .isActive(form.getIsActive())
                    .adminReply(form.getAdminReply())
                    .submittedAt(form.getCreatedAt())
                    .repliedAt(form.getRepliedAt())
                    .build();

            return ResponseEntity.ok(createSuccessResponse(formResponse, "Form fetched successfully"));

        } catch (Exception e) {
            log.error("Error fetching form by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse("error", e.getMessage()));
        }
    }

    private ResponseEntity<ApiResponse<?>> validateDateRangeWithTimeFilter(
            String timeFilter,
            LocalDate startDate,
            LocalDate endDate) {

        // If no custom date range provided, no validation needed
        if (startDate == null || endDate == null) {
            return null; // Valid, proceed
        }

        // If "alltime" filter, custom date range is allowed
        if ("alltime".equalsIgnoreCase(timeFilter)) {
            return null; // Valid, proceed
        }

        LocalDate now = LocalDate.now();
        LocalDate allowedStart;
        String filterDescription;

        switch (timeFilter.toLowerCase()) {
            case "24hrs":
                allowedStart = now.minusDays(1);
                filterDescription = "24 hours (last 1 day)";
                break;
            case "7days":
                allowedStart = now.minusDays(7);
                filterDescription = "7 days";
                break;
            case "30days":
                allowedStart = now.minusDays(30);
                filterDescription = "30 days";
                break;
            case "12months":
                allowedStart = now.minusMonths(12);
                filterDescription = "12 months";
                break;
            default:
                return null; // No filter, proceed
        }

        // Check if custom start date is before the allowed range
        if (startDate.isBefore(allowedStart)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createFailureResponse(
                            "Invalid date range",
                            "When using '" + timeFilter + "' filter, date range must be within " + filterDescription +
                                    ". Allowed start date: " + allowedStart + ", but got: " + startDate
                    ));
        }

        // Check if end date is in the future
        if (endDate.isAfter(now)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createFailureResponse(
                            "Invalid date range",
                            "End date cannot be in the future. Today is: " + now
                    ));
        }

        // Check if start date is after end date
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createFailureResponse(
                            "Invalid date range",
                            "Start date must be before end date"
                    ));
        }

        return null; // Valid, proceed
    }


}

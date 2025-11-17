package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.request.CreateFormRequest;
import com.hvc.brandlocus.dto.request.PaginationRequest;
import com.hvc.brandlocus.dto.response.FormResponse;
import com.hvc.brandlocus.dto.response.PaginationResponse;
import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.Forms;
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
import java.time.LocalDate;
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
    public ResponseEntity<ApiResponse<?>> submitForm(Principal principal,CreateFormRequest createFormRequest) {
        try {


            String email = principal.getName();
            log.info("submit form for email: {}", email);

            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(email);

            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("User not found", "User does not exist"));
            }

            BaseUser user = optionalUser.get();

            Forms form = Forms.builder()
                    .firstName(createFormRequest.getFirstName())
                    .lastName(createFormRequest.getLastName())
                    .email(createFormRequest.getEmail())
                    .serviceNeeded(String.valueOf(ServiceNeeded.valueOf(createFormRequest.getServiceNeeded())))
                    .industryName(createFormRequest.getIndustryName())
                    .companyName(createFormRequest.getCompanyName())
                    .message(createFormRequest.getMessage())
                    .user(user)
                    .isActive(true)
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

            // Specification for paginated forms
            Specification<Forms> spec = Specification.allOf(
                    FormSpecification.isActive(),
                    FormSpecification.searchTerm(searchTerm),
                    FormSpecification.filterByIndustry(filterTerm),
                    FormSpecification.byTimeFilter(timeFilter),
                    FormSpecification.createdBetween(start, end)
            );

            if (!isAdmin) {
                spec = spec.and(FormSpecification.byUser(user));
            }

            Page<Forms> formPage = formRepository.findAll(spec, pageable);

            List<FormResponse> responseList = formPage.getContent().stream().map(f -> FormResponse.builder()
                    .firstName(f.getFirstName())
                    .lastName(f.getLastName())
                    .email(f.getEmail())
                    .serviceNeeded(f.getServiceNeeded())
                    .industryName(f.getIndustryName())
                    .companyName(f.getCompanyName())
                    .message(f.getMessage())
                    .build()
            ).toList();

            PaginationResponse<FormResponse> paginationResponse = PaginationResponse.<FormResponse>builder()
                    .content(responseList)
                    .page(formPage.getNumber())
                    .size(formPage.getSize())
                    .totalElements(formPage.getTotalElements())
                    .totalPages(formPage.getTotalPages())
                    .last(formPage.isLast())
                    .build();

            // Dashboard counts - same time filter, start/end
            Specification<Forms> dashboardSpec = Specification.allOf(
                    FormSpecification.isActive(),
                    FormSpecification.byTimeFilter(timeFilter),
                    FormSpecification.createdBetween(start, end)
            );


            List<Forms> dashboardForms = formRepository.findAll(dashboardSpec);

            long totalForms = dashboardForms.size();

            long businessDevelopment = dashboardForms.stream()
                    .filter(f -> ServiceNeeded.BUSINESS_DEVELOPMENT.name().equalsIgnoreCase(f.getServiceNeeded()))
                    .count();

            long brandDevelopment = dashboardForms.stream()
                    .filter(f -> ServiceNeeded.BRAND_DEVELOPMENT.name().equalsIgnoreCase(f.getServiceNeeded()))
                    .count();

            long capacityBuilding = dashboardForms.stream()
                    .filter(f -> ServiceNeeded.CAPACITY_BUILDING.name().equalsIgnoreCase(f.getServiceNeeded()))
                    .count();

            long tradeAndInvestmentFacilitation = dashboardForms.stream()
                    .filter(f -> ServiceNeeded.TRADE_AND_INVESTMENT_FACILITATION.name().equalsIgnoreCase(f.getServiceNeeded()))
                    .count();

            long businessQuest = dashboardForms.stream()
                    .filter(f -> ServiceNeeded.BUSINESS_QUEST.name().equalsIgnoreCase(f.getServiceNeeded()))
                    .count();

            FormResponse dashboardCounts = FormResponse.builder()
                    .totalFormSubmitted(String.valueOf(totalForms))
                    .businessDevelopment(String.valueOf(businessDevelopment))
                    .brandDevelopment(String.valueOf(brandDevelopment))
                    .capacityBuilding(String.valueOf(capacityBuilding))
                    .tradeAndInvestmentFacilitation(String.valueOf(tradeAndInvestmentFacilitation))
                    .businessQuest(String.valueOf(businessQuest))
                    .build();

            Map<String, Object> finalResponse = Map.of(
                    "pagination", paginationResponse,
                    "dashboard", dashboardCounts
            );

            return ResponseEntity.ok(createSuccessResponse(finalResponse, "Forms fetched successfully"));

        } catch (Exception e) {
            log.error("Error fetching forms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse(e.getLocalizedMessage(), "Failed to fetch forms"));
        }
    }



}

package com.hvc.brandlocus.controllers.form;

import com.hvc.brandlocus.dto.request.AdminFormReplyRequest;
import com.hvc.brandlocus.dto.request.CreateFormRequest;
import com.hvc.brandlocus.dto.request.PaginationRequest;
import com.hvc.brandlocus.enums.ServiceNeeded;
import com.hvc.brandlocus.services.FormService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
public class FormController {
    private final FormService formService;

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<?>> submitForm(
            @RequestBody CreateFormRequest createFormRequest
    ) {
        return formService.submitForm(createFormRequest);
    }

    /**
     * Get forms with pagination, search, time filter, and industry filter
     */
    @GetMapping("/get")
    public ResponseEntity<ApiResponse<?>> getForms(
            Principal principal,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "allTime") String timeFilter,
            @RequestParam(required = false) String filterTerm,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order

    ) {
        PaginationRequest paginationRequest = PaginationRequest.builder()
                .page(page)
                .limit(limit)
                .sortBy(sortBy)
                .order(order)
                .build();

        return formService.getForm(principal, searchTerm, timeFilter, filterTerm, startDate, endDate, paginationRequest);
    }


    @GetMapping("/service-needed")
    public ResponseEntity<List<String>> getAllServicesNeeded() {
        List<String> services = Arrays.stream(ServiceNeeded.values())
                .map(ServiceNeeded::getValue)
                .toList();

        return ResponseEntity.ok(services);
    }

    @PatchMapping("/{formId}/reply")
    public ResponseEntity<ApiResponse<?>> replyToForm(
            Principal principal,
            @PathVariable Long formId,
            @RequestBody AdminFormReplyRequest replyRequest) {
        return formService.replyToForm(principal, formId, replyRequest);
    }

    @GetMapping("/{formId}")
    public ResponseEntity<ApiResponse<?>> getFormById(
            Principal principal,
            @PathVariable Long formId) {
        return formService.getFormById(principal, formId);
    }

}

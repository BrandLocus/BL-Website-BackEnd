package com.hvc.brandlocus.services;

import com.hvc.brandlocus.dto.request.AdminFormReplyRequest;
import com.hvc.brandlocus.dto.request.CreateFormRequest;
import com.hvc.brandlocus.dto.request.PaginationRequest;
import com.hvc.brandlocus.utils.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

public interface FormService {
    ResponseEntity<ApiResponse<?>> submitForm(Principal principal,CreateFormRequest createFormRequest);
    ResponseEntity<ApiResponse<?>> getForm(Principal principal,String searchTerm, String timeFilter,String filterTerm, String startDate, String endDate, PaginationRequest paginationRequest);
    ResponseEntity<ApiResponse<?>> replyToForm(Principal principal, Long formId, AdminFormReplyRequest replyRequest);



    ResponseEntity<ApiResponse<?>> getFormById(Principal principal, Long formId);
}

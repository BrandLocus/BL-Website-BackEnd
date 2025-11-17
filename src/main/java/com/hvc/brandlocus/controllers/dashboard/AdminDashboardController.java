package com.hvc.brandlocus.controllers.dashboard;

import com.hvc.brandlocus.dto.response.DashboardResponse;
import com.hvc.brandlocus.services.AdminDashboardService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;


    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<?>> adminDashboard(
            Principal principal,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Object serviceResponse = adminDashboardService.adminDashboard(principal, filter, startDate, endDate);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .data(serviceResponse)
                        .message("Dashboard data fetched successfully")
                        .build()
        );
    }


    @GetMapping("/user-graph")
    public ResponseEntity<ApiResponse<?>> UserDashboard(
            Principal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return adminDashboardService.userGraph(principal,startDate,endDate);

    }





}

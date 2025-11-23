package com.hvc.brandlocus.controllers.users;

import com.hvc.brandlocus.dto.request.PaginationRequest;
import com.hvc.brandlocus.services.UserService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class UserController {

    private final UserService userService;


    @GetMapping("/")
    public ResponseEntity<ApiResponse<?>> getAllUsers(
            Principal principal,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "alltime") String timeFilter, // alltime, 12months, 30days, 7days, 24hrs
            @RequestParam(required = false) String state, // yyyy-MM-dd
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String startDate, // yyyy-MM-dd
            @RequestParam(required = false) String endDate,   // yyyy-MM-dd
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

        return userService.getAllUsers(
                principal,
                userId,
                searchTerm,
                timeFilter,
                state,
                country,
                startDate,
                endDate,
                paginationRequest
        );

}}




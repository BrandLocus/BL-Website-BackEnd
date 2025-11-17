package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.response.ChartPoint;
import com.hvc.brandlocus.dto.response.DashboardResponse;
import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.ChatMessage;
import com.hvc.brandlocus.enums.UserRoles;
import com.hvc.brandlocus.repositories.BaseUserRepository;
import com.hvc.brandlocus.repositories.ChatMessageRepository;
import com.hvc.brandlocus.repositories.ChatSessionRepository;
import com.hvc.brandlocus.repositories.specification.BaseUserSpecification;
import com.hvc.brandlocus.repositories.specification.ChatMessageSpecification;
import com.hvc.brandlocus.services.AdminDashboardService;
import com.hvc.brandlocus.utils.ApiResponse;
import com.hvc.brandlocus.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.hvc.brandlocus.utils.ResponseUtils.createFailureResponse;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardImpl implements AdminDashboardService {

    private final ChatSessionRepository chatSessionRepository;
    private final BaseUserRepository baseUserRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final DashboardHelperService dashboardHelperService;

    @Override
    public ResponseEntity<ApiResponse<?>> adminDashboard(Principal principal, String filter, LocalDate startDate, LocalDate endDate) {
        try {
            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(principal.getName().trim());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("User not found", "User with email " + principal.getName() + " does not exist"));
            }
            BaseUser admin = optionalUser.get();

            if (!UserRoles.ADMIN.getValue().equalsIgnoreCase(admin.getRole().getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseUtils.createFailureResponse("access denied", "only admins can access this resource"));
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime from = resolveStartDate(filter, startDate, endDate, now);

            // Previous comparison range (for % change)
            LocalDateTime compareFrom = null;
            LocalDateTime compareTo = null;
            if (!"alltime".equalsIgnoreCase(filter) && startDate == null && endDate == null) {
                Duration duration = Duration.between(from, now);
                compareTo = from.minusSeconds(1);
                compareFrom = from.minus(duration);
            }

            // ✅ Build specifications
            Specification<BaseUser> userSpec = BaseUserSpecification.byTimeFilter(filter);
            Specification<ChatMessage> chatSpec = ChatMessageSpecification.byTimeFilter(filter);

            if (startDate != null && endDate != null) {
                userSpec = BaseUserSpecification.createdBetween(startDate, endDate);
                chatSpec = ChatMessageSpecification.createdBetween(startDate, endDate);
            }


            // ✅ Compute totals using specification
            long totalUsers = baseUserRepository.count(userSpec);
            long totalConversations = chatMessageRepository.count(chatSpec);

            // ✅ Active users
            Specification<BaseUser> activeUserSpec = userSpec.and((root, query, cb) -> cb.isTrue(root.get("isActive")));
            long activeUsers = baseUserRepository.count(activeUserSpec);

            // ✅ Compute % changes
            Double userChange = null;
            Double chatChange = null;

            if (compareFrom != null && compareTo != null) {
                // Use your existing createdBetween specification
                Specification<BaseUser> prevUserSpec = BaseUserSpecification.createdBetween(compareFrom.toLocalDate(), compareTo.toLocalDate());
                Specification<ChatMessage> prevChatSpec = ChatMessageSpecification.createdBetween(compareFrom.toLocalDate(), compareTo.toLocalDate());

                long prevUsers = baseUserRepository.count(prevUserSpec);
                long prevChats = chatMessageRepository.count(prevChatSpec);

                userChange = calculatePercentageChange(prevUsers, totalUsers);
                chatChange = calculatePercentageChange(prevChats, totalConversations);
            }




            // ✅ Chart data (will depend on filter granularity)
            List<ChartPoint> chartData = dashboardHelperService.buildChartData(filter, startDate, endDate);

            // ✅ Build response DTO
            DashboardResponse dashboard = DashboardResponse.builder()
                    .totalConversations(totalConversations)
                    .activeUsers(activeUsers)
                    .conversationChange(chatChange)
                    .userChange(userChange)
                    .chartData(chartData)
                    .build();

            return ResponseEntity.ok(ResponseUtils.createSuccessResponse(dashboard,"Dashboard data fetched successfully"));


        }catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ResponseUtils.createFailureResponse("Server error", ex.getMessage()));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<?>> userGraph(Principal principal, LocalDate startDate, LocalDate endDate) {
        try {
            log.info("fetch user analytics");
            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(principal.getName().trim());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("User not found", "User with email " + principal.getName() + " does not exist"));
            }
            BaseUser admin = optionalUser.get();

            if (!UserRoles.ADMIN.getValue().equalsIgnoreCase(admin.getRole().getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseUtils.createFailureResponse("access denied", "only admins can access this resource"));
            }

            if (startDate == null || endDate == null) {
                YearMonth currentMonth = YearMonth.now();
                startDate = currentMonth.atDay(1);
                endDate = currentMonth.atEndOfMonth();
            }

            log.info("this is the start date: {}",startDate);
            log.info("this is the end date: {}",endDate);

            log.info("Date filter range: {} -> {}", startDate.atStartOfDay(), endDate.atTime(23,59,59));


            var dateSpec = BaseUserSpecification.createdBetween(startDate, endDate);

            List<BaseUser> users = baseUserRepository.findAll(dateSpec);

            log.info("Fetched {} users from DB for that range", users.size());
            users.forEach(u -> log.info("User: {}, createdAt: {}, state: {}", u.getEmail(), u.getCreatedAt(), u.getState()));


            Map<String, Long> stateUserCount = users.stream()
                    .filter(u -> u.getState() != null && !u.getState().isBlank())
                    .collect(Collectors.groupingBy(BaseUser::getState, Collectors.counting()));

            long totalUsers = stateUserCount.values().stream().mapToLong(Long::longValue).sum();

            List<Map<String, Object>> stateStats = stateUserCount.entrySet().stream()
                    .map(entry -> {
                        String state = entry.getKey();
                        long count = entry.getValue();
                        double percentage = totalUsers > 0 ? (count * 100.0 / totalUsers) : 0.0;
                        Map<String, Object> map = new HashMap<>();
                        map.put("state", state);
                        map.put("users", count);
                        map.put("percentage", String.format("%.2f%%", percentage));
                        return map;
                    })
                    .sorted((a, b) -> Long.compare((Long) b.get("users"), (Long) a.get("users")))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ResponseUtils.createSuccessResponse(stateStats,"User distribution fetched successfully"));

        }catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ResponseUtils.createFailureResponse("Server error", ex.getMessage()));
        }


    }


    private LocalDateTime resolveStartDate(String filter, LocalDate start, LocalDate end, LocalDateTime now) {
        if (start != null && end != null)
            return start.atStartOfDay();

        switch (filter.toLowerCase()) {
            case "12months": return now.minusMonths(12);
            case "30days": return now.minusDays(30);
            case "7days": return now.minusDays(7);
            case "24hrs": return now.minusHours(24);
            default: return LocalDateTime.MIN; // all time
        }
    }

    private double calculatePercentageChange(long previous, long current) {
        if (previous == 0) return 0;
        return ((double) (current - previous) / previous) * 100;
    }


}

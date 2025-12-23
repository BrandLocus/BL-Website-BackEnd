package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.response.ChartPoint;
import com.hvc.brandlocus.dto.response.DashboardResponse;
import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.ChatMessage;
import com.hvc.brandlocus.enums.SenderType;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public ResponseEntity<ApiResponse<?>> adminDashboard(
            Principal principal,
            String filter,
            LocalDate startDate,
            LocalDate endDate
    ) {
        try {
            // NORMALIZE FILTER
            String safeFilter = (filter == null ? "" : filter.trim().toLowerCase());

            // USER VALIDATION
            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(principal.getName().trim());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResponseUtils.createFailureResponse("User not found",
                                "User with email " + principal.getName() + " does not exist"));
            }

            BaseUser admin = optionalUser.get();

            if (!UserRoles.ADMIN.getValue().equalsIgnoreCase(admin.getRole().getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseUtils.createFailureResponse("access denied",
                                "only admins can access this resource"));
            }

            // VALIDATE DATE RANGE WITH TIME FILTER
            ResponseEntity<ApiResponse<?>> validationResponse = validateDateRangeWithTimeFilter(safeFilter, startDate, endDate);
            if (validationResponse != null) {
                return validationResponse;
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime from = resolveStartDate(safeFilter, startDate, endDate, now);

            // PREVIOUS RANGE
            LocalDateTime compareFrom = null;
            LocalDateTime compareTo = null;

            // ----------- FIX FOR DATE RANGE MODE ---------------
            if (startDate != null && endDate != null) {
                long days = Duration.between(
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59)
                ).toDays();

                compareFrom = startDate.minusDays(days + 1).atStartOfDay();
                compareTo = startDate.minusDays(1).atTime(23, 59, 59);
            }
            // ----------------------------------------------------

            // FOR FILTER MODE (no custom date range)
            if (!"alltime".equals(safeFilter) && startDate == null && endDate == null) {
                Duration duration = Duration.between(from, now);
                compareTo = from.minusSeconds(1);
                compareFrom = from.minus(duration);
            }


            Specification<BaseUser> userSpec;
            Specification<ChatMessage> chatSpec;

            if (startDate != null && endDate != null) {

                userSpec = BaseUserSpecification.createdBetween(startDate, endDate)
                        .and(BaseUserSpecification.excludeAdmin());

                chatSpec = ChatMessageSpecification.createdBetween(startDate, endDate);
            } else {

                userSpec = BaseUserSpecification.byTimeFilter(safeFilter)
                        .and(BaseUserSpecification.excludeAdmin());

                chatSpec = ChatMessageSpecification.byTimeFilter(safeFilter);
            }


            long totalUsers = baseUserRepository.count(userSpec);
            long totalConversations = chatMessageRepository.count(chatSpec);

            // ACTIVE USERS
            long activeUsers = baseUserRepository.count(
                    userSpec.and((root, query, cb) -> cb.isTrue(root.get("isActive")))
            );

            // PERCENTAGE CHANGE
            Double userChange = null;
            Double chatChange = null;

            if (compareFrom != null && compareTo != null) {
                long prevUsers = baseUserRepository.count(
                        BaseUserSpecification.createdBetween(compareFrom.toLocalDate(), compareTo.toLocalDate())
                                .and(BaseUserSpecification.excludeAdmin())
                );

                long prevChats = chatMessageRepository.count(
                        ChatMessageSpecification.createdBetween(compareFrom.toLocalDate(), compareTo.toLocalDate())
                );

                userChange = calculatePercentageChange(prevUsers, totalUsers);
                chatChange = calculatePercentageChange(prevChats, totalConversations);
            }

            // CHART DATA
            List<ChartPoint> chartData =
                    dashboardHelperService.buildChartData(safeFilter, startDate, endDate, BaseUserSpecification.excludeAdmin());

            DashboardResponse dashboard = DashboardResponse.builder()
                    .totalConversations(totalConversations)
                    .activeUsers(activeUsers)
                    .conversationChange(chatChange)
                    .userChange(userChange)
                    .chartData(chartData)
                    .build();

            return ResponseEntity.ok(ResponseUtils.createSuccessResponse(dashboard,
                    "Dashboard data fetched successfully"));

        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ResponseUtils.createFailureResponse("Server error", ex.getMessage()));
        }
    }




//    @Override
//    public ResponseEntity<ApiResponse<?>> userGraph(Principal principal, LocalDate startDate, LocalDate endDate) {
//        try {
//            log.info("fetch user analytics");
//            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(principal.getName().trim());
//            if (optionalUser.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(createFailureResponse("User not found", "User with email " + principal.getName() + " does not exist"));
//            }
//            BaseUser admin = optionalUser.get();
//
//            if (!UserRoles.ADMIN.getValue().equalsIgnoreCase(admin.getRole().getName())) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body(ResponseUtils.createFailureResponse("access denied", "only admins can access this resource"));
//            }
//
//            if (startDate == null || endDate == null) {
//                YearMonth currentMonth = YearMonth.now();
//                startDate = currentMonth.atDay(1);
//                endDate = currentMonth.atEndOfMonth();
//            }
//
//            log.info("this is the start date: {}",startDate);
//            log.info("this is the end date: {}",endDate);
//
//            log.info("Date filter range: {} -> {}", startDate.atStartOfDay(), endDate.atTime(23,59,59));
//
//
//            var dateSpec = BaseUserSpecification.createdBetween(startDate, endDate);
//
//            List<BaseUser> users = baseUserRepository.findAll(dateSpec);
//
//            log.info("Fetched {} users from DB for that range", users.size());
//            users.forEach(u -> log.info("User: {}, createdAt: {}, state: {}", u.getEmail(), u.getCreatedAt(), u.getState()));
//
//
//            Map<String, Long> stateUserCount = users.stream()
//                    .filter(u -> u.getState() != null && !u.getState().isBlank())
//                    .collect(Collectors.groupingBy(BaseUser::getState, Collectors.counting()));
//
//            long totalUsers = stateUserCount.values().stream().mapToLong(Long::longValue).sum();
//
//            List<Map<String, Object>> stateStats = stateUserCount.entrySet().stream()
//                    .map(entry -> {
//                        String state = entry.getKey();
//                        long count = entry.getValue();
//                        double percentage = totalUsers > 0 ? (count * 100.0 / totalUsers) : 0.0;
//                        Map<String, Object> map = new HashMap<>();
//                        map.put("state", state);
//                        map.put("users", count);
//                        map.put("percentage", String.format("%.2f%%", percentage));
//                        return map;
//                    })
//                    .sorted((a, b) -> Long.compare((Long) b.get("users"), (Long) a.get("users")))
//                    .collect(Collectors.toList());
//
//
//
//            return ResponseEntity.ok(ResponseUtils.createSuccessResponse(stateStats,"User distribution fetched successfully"));
//
//        }catch (Exception ex) {
//            return ResponseEntity.internalServerError()
//                    .body(ResponseUtils.createFailureResponse("Server error", ex.getMessage()));
//        }
//
//
//    }

    @Override
    public ResponseEntity<ApiResponse<?>> userGraph(Principal principal, LocalDate startDate, LocalDate endDate) {
        try {
            log.info("Fetching user analytics");

            // ---------------------------
            // 1️⃣ Validate admin user
            // ---------------------------
            BaseUser admin = baseUserRepository.findByEmail(principal.getName().trim())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (!UserRoles.ADMIN.getValue().equalsIgnoreCase(admin.getRole().getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseUtils.createFailureResponse("Access denied", "Only admins can access this resource"));
            }

            // ---------------------------
            // 2️⃣ Default date range = current month
            // ---------------------------
            if (startDate == null || endDate == null) {
                YearMonth currentMonth = YearMonth.now();
                startDate = currentMonth.atDay(1);
                endDate = currentMonth.atEndOfMonth();
            }

            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(23, 59, 59);
            log.info("Date range: {} -> {}", start, end);

            // ---------------------------
            // 3️⃣ State distribution
            // ---------------------------
            var dateSpec = BaseUserSpecification.createdBetween(startDate, endDate);
            List<BaseUser> users = baseUserRepository.findAll(dateSpec);

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

            // ---------------------------
            // 4️⃣ Fetch AI messages in date range
            // ---------------------------
            List<ChatMessage> aiMessages = chatMessageRepository.findMessagesWithinRange(start, end).stream()
                    .filter(m -> m.getSender() == SenderType.AI)
                    .collect(Collectors.toList());

            // ---------------------------
            // 5️⃣ Top 5 sectors
            // ---------------------------
            Map<String, Long> topSectors = aiMessages.stream()
                    .filter(m -> m.getSector() != null && !m.getSector().isBlank())
                    .collect(Collectors.groupingBy(ChatMessage::getSector, Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue, (a,b)->a, LinkedHashMap::new
                    ));

            // ---------------------------
            // 6️⃣ Top 15 keywords
            // ---------------------------
            Map<String, Long> topKeywords = aiMessages.stream()
                    .flatMap(m -> Arrays.stream(Optional.ofNullable(m.getKeywords()).orElse("").split(","))
                            .map(String::trim)
                            .filter(k -> !k.isBlank()))
                    .collect(Collectors.groupingBy(k -> k, Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(15)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue, (a,b)->a, LinkedHashMap::new
                    ));

            // ---------------------------
            // 7️⃣ Top 5 topics with number of users
            // ---------------------------
            Map<String, Long> topTopics = aiMessages.stream()
                    .filter(m -> m.getTopic() != null && !m.getTopic().isBlank())
                    .collect(Collectors.groupingBy(
                            ChatMessage::getTopic,
                            Collectors.mapping(m -> m.getChatSession().getUser().getId(), Collectors.toSet())
                    )) // set ensures unique users
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> (long) e.getValue().size()
                    ))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue, (a,b)->a, LinkedHashMap::new
                    ));

            // ---------------------------
            // 8️⃣ Build final response
            // ---------------------------
            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("stateDistribution", stateStats);
            finalResponse.put("topSectors", topSectors);
            finalResponse.put("topKeywords", topKeywords);
            finalResponse.put("topTopics", topTopics);

            return ResponseEntity.ok(ResponseUtils.createSuccessResponse(finalResponse, "User analytics fetched successfully"));

        } catch (Exception ex) {
            log.error("Error fetching user analytics", ex);
            return ResponseEntity.internalServerError()
                    .body(ResponseUtils.createFailureResponse("Server error", ex.getMessage()));
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
                    .body(ResponseUtils.createFailureResponse(
                            "Invalid date range",
                            "When using '" + timeFilter + "' filter, date range must be within " + filterDescription +
                                    ". Allowed start date: " + allowedStart + ", but got: " + startDate
                    ));
        }

        // Check if end date is in the future
        if (endDate.isAfter(now)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseUtils.createFailureResponse(
                            "Invalid date range",
                            "End date cannot be in the future. Today is: " + now
                    ));
        }

        // Check if start date is after end date
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseUtils.createFailureResponse(
                            "Invalid date range",
                            "Start date must be before end date"
                    ));
        }

        return null; // Valid, proceed
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
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100;
    }



}

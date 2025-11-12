package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.response.ChartPoint;
import com.hvc.brandlocus.repositories.BaseUserRepository;
import com.hvc.brandlocus.repositories.ChatMessageRepository;
import com.hvc.brandlocus.repositories.specification.BaseUserSpecification;
import com.hvc.brandlocus.repositories.specification.ChatMessageSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DashboardHelperService {

    private final BaseUserRepository baseUserRepository;
    private final ChatMessageRepository chatMessageRepository;


    public List<ChartPoint> buildChartData(String filter, LocalDate startDate, LocalDate endDate) {
        List<ChartPoint> chartData = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = resolveStartDate(filter, startDate, endDate, now);
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : now;

        switch (filter.toLowerCase()) {
            case "24hrs":
                chartData = buildHourlyChart(start, end);
                break;
            case "7days":
                chartData = buildWeeklyChart(start, end);
                break;
            case "30days":
                chartData = buildDailyChart(start, end);
                break;
            case "12months":
                chartData = buildMonthlyChart(start, end);
                break;
            default:
                chartData = buildYearlyChart(start, end);
                break;
        }

        return chartData;
    }

    private LocalDateTime resolveStartDate(String filter, LocalDate start, LocalDate end, LocalDateTime now) {
        if (start != null && end != null)
            return start.atStartOfDay();

        switch (filter.toLowerCase()) {
            case "12months": return now.minusMonths(12);
            case "30days": return now.minusDays(30);
            case "7days": return now.minusDays(7);
            case "24hrs": return now.minusHours(24);
            default: return LocalDateTime.of(2000, 1, 1, 0, 0); // all time
        }
    }

    private List<ChartPoint> buildHourlyChart(LocalDateTime start, LocalDateTime end) {
        List<ChartPoint> result = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            LocalDateTime from = start.withHour(hour).withMinute(0);
            LocalDateTime to = from.plusHours(1);

            long totalUsers = baseUserRepository.count(
                    BaseUserSpecification.createdBetween(from.toLocalDate(), to.toLocalDate()));
            long totalConversations = chatMessageRepository.count(
                    ChatMessageSpecification.createdBetween(from.toLocalDate(), to.toLocalDate()));

            result.add(ChartPoint.builder()
                    .label(String.format("%02d:00", hour))
                    .totalUsers(totalUsers)
                    .totalConversations(totalConversations)
                    .build());
        }

        return result;
    }

    private List<ChartPoint> buildWeeklyChart(LocalDateTime start, LocalDateTime end) {
        List<ChartPoint> result = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate day = start.toLocalDate().plusDays(i);
            String label = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH); // Mon, Tue, Wed...

            long totalUsers = baseUserRepository.count(BaseUserSpecification.createdOn(day));
            long totalConversations = chatMessageRepository.count(ChatMessageSpecification.createdOn(day));

            result.add(ChartPoint.builder()
                    .label(label)
                    .totalUsers(totalUsers)
                    .totalConversations(totalConversations)
                    .build());
        }

        return result;
    }

    private List<ChartPoint> buildDailyChart(LocalDateTime start, LocalDateTime end) {
        List<ChartPoint> result = new ArrayList<>();

        LocalDate current = start.toLocalDate();
        while (!current.isAfter(end.toLocalDate())) {
            String label = current.format(DateTimeFormatter.ofPattern("MMM dd")); // e.g. Nov 01

            long totalUsers = baseUserRepository.count(BaseUserSpecification.createdOn(current));
            long totalConversations = chatMessageRepository.count(ChatMessageSpecification.createdOn(current));

            result.add(ChartPoint.builder()
                    .label(label)
                    .totalUsers(totalUsers)
                    .totalConversations(totalConversations)
                    .build());

            current = current.plusDays(1);
        }

        return result;
    }

    private List<ChartPoint> buildMonthlyChart(LocalDateTime start, LocalDateTime end) {
        List<ChartPoint> result = new ArrayList<>();

        YearMonth current = YearMonth.from(start);
        YearMonth endMonth = YearMonth.from(end);

        while (!current.isAfter(endMonth)) {
            LocalDate firstDay = current.atDay(1);
            LocalDate lastDay = current.atEndOfMonth();

            String label = current.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH); // January, February...

            long totalUsers = baseUserRepository.count(BaseUserSpecification.createdBetween(firstDay, lastDay));
            long totalConversations = chatMessageRepository.count(ChatMessageSpecification.createdBetween(firstDay, lastDay));

            result.add(ChartPoint.builder()
                    .label(label)
                    .totalUsers(totalUsers)
                    .totalConversations(totalConversations)
                    .build());

            current = current.plusMonths(1);
        }

        return result;
    }

    private List<ChartPoint> buildYearlyChart(LocalDateTime start, LocalDateTime end) {
        List<ChartPoint> result = new ArrayList<>();

        int startYear = start.getYear();
        int endYear = end.getYear();

        for (int year = startYear; year <= endYear; year++) {
            LocalDate first = LocalDate.of(year, 1, 1);
            LocalDate last = LocalDate.of(year, 12, 31);

            long totalUsers = baseUserRepository.count(BaseUserSpecification.createdBetween(first, last));
            long totalConversations = chatMessageRepository.count(ChatMessageSpecification.createdBetween(first, last));

            result.add(ChartPoint.builder()
                    .label(String.valueOf(year))
                    .totalUsers(totalUsers)
                    .totalConversations(totalConversations)
                    .build());
        }

        return result;
    }







}

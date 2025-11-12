package com.hvc.brandlocus.repositories.specification;

import com.hvc.brandlocus.entities.ChatMessage;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ChatMessageSpecification {

    public static Specification<ChatMessage> createdBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null || endDate == null) return null;
            return cb.between(root.get("createdAt"),
                    startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        };
    }

    public static Specification<ChatMessage> createdOn(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return null;
            return cb.between(
                    root.get("createdAt"),
                    date.atStartOfDay(),
                    date.atTime(23, 59, 59)
            );
        };
    }

    public static Specification<ChatMessage> byTimeFilter(String filter) {
        if (filter == null) return null;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from;

        switch (filter.toLowerCase()) {
            case "12months":
                from = now.minusMonths(12);
                break;
            case "30days":
                from = now.minusDays(30);
                break;
            case "7days":
                from = now.minusDays(7);
                break;
            case "24hrs":
                from = now.minusHours(24);
                break;
            case "alltime":
            default:
                return null;
        }

        LocalDateTime finalFrom = from;
        return (root, query, cb) -> cb.between(root.get("createdAt"), finalFrom, now);
    }
}

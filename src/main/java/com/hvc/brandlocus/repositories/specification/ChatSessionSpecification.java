package com.hvc.brandlocus.repositories.specification;

import com.hvc.brandlocus.entities.ChatSession;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ChatSessionSpecification {
    public static Specification<ChatSession> byUserId(Long userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;
            return cb.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<ChatSession> searchTerm(String term) {
        return (root, query, cb) -> {
            if (term == null || term.isEmpty()) return null;
            String likeTerm = "%" + term.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("title")), likeTerm);
        };
    }


    public static Specification<ChatSession> byTimeFilter(String filter) {
        if (filter == null) return null;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from;

        switch (filter.toLowerCase()) {
            case "12months": from = now.minusMonths(12); break;
            case "30days": from = now.minusDays(30); break;
            case "7days": from = now.minusDays(7); break;
            case "24hrs": from = now.minusHours(24); break;
            case "alltime":
            default: return null;
        }

        LocalDateTime finalFrom = from;
        return (root, query, cb) -> cb.between(root.get("createdAt"), finalFrom, now);
    }

    public static Specification<ChatSession> createdBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null || endDate == null) return null;
            return cb.between(root.get("createdAt"),
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59, 59));
        };
    }
}

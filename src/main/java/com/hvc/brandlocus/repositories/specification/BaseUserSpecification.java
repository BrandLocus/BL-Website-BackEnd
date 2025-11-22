package com.hvc.brandlocus.repositories.specification;

import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.enums.UserRoles;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BaseUserSpecification {

    public static Specification<BaseUser> excludeAdmin() {
        return (root, query, cb) -> cb.notEqual(
                cb.lower(root.get("role").get("name")), "admin"
        );
    }



    public static Specification<BaseUser> searchTerm(String term) {
        return (root, query, cb) -> {
            if (term == null || term.isEmpty()) return null;
            String likeTerm = "%" + term.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), likeTerm),
                    cb.like(cb.lower(root.get("lastName")), likeTerm),
                    cb.like(cb.lower(root.get("email")), likeTerm)
            );
        };
    }
    public static Specification<BaseUser> hasState(String state) {
        return (root, query, cb) -> state == null ? cb.conjunction() :
                cb.equal(cb.lower(root.get("state")), state.toLowerCase());
    }

    public static Specification<BaseUser> createdBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null || endDate == null) return null;
            return cb.between(root.get("createdAt"),
                    startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        };
    }

    public static Specification<BaseUser> byTimeFilter(String filter) {
        if (filter == null || filter.isEmpty())
            return (root, query, cb) -> cb.conjunction();

        String f = filter.toLowerCase();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from;

        switch (f) {
            case "12months": from = now.minusMonths(12); break;
            case "30days":   from = now.minusDays(30);   break;
            case "7days":    from = now.minusDays(7);    break;
            case "24hrs":    from = now.minusHours(24);  break;
            case "alltime":
                return (root, query, cb) -> cb.conjunction();
            default:
                return (root, query, cb) -> cb.conjunction();
        }

        LocalDateTime finalFrom = from;
        return (root, query, cb) -> cb.between(root.get("createdAt"), finalFrom, now);
    }



    public static Specification<BaseUser> createdOn(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return null;
            return cb.between(
                    root.get("createdAt"),
                    date.atStartOfDay(),
                    date.atTime(23, 59, 59)
            );
        };
    }
}

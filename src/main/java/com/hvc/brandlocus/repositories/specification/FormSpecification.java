package com.hvc.brandlocus.repositories.specification;

import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.Forms;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FormSpecification {

    public static Specification<Forms> isActiveForm() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }


    public static Specification<Forms> searchTerm(String term) {
        return (root, query, cb) -> {
            if (term == null || term.isEmpty()) return cb.conjunction();
            String likeTerm = "%" + term.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), likeTerm),
                    cb.like(cb.lower(root.get("lastName")), likeTerm),
                    cb.like(cb.lower(root.get("email")), likeTerm),
                    cb.like(cb.lower(root.get("companyName")), likeTerm)
            );
        };
    }

    public static Specification<Forms> filterByIndustry(String filterTerm) {
        return (root, query, cb) -> {
            if (filterTerm == null || filterTerm.isEmpty()) return cb.conjunction();
            return cb.equal(cb.lower(root.get("industryName")), filterTerm.toLowerCase());
        };
    }

    public static Specification<Forms> byTimeFilter(String filter) {
        if (filter == null) return null;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from;

        switch (filter.toLowerCase()) {
            case "12months": from = now.minusMonths(12); break;
            case "30days": from = now.minusDays(30); break;
            case "7days": from = now.minusDays(7); break;
            case "24hrs": from = now.minusHours(24); break;
            case "alltime": return (root, query, cb) -> cb.conjunction();
            default: return null;
        }

        LocalDateTime finalFrom = from;
        return (root, query, cb) -> cb.between(root.get("createdAt"), finalFrom, now);
    }

    public static LocalDateTime getFromDateTime(String timeFilter) {
        if (timeFilter == null || timeFilter.equalsIgnoreCase("allTime")) {
            return null; // No restriction for all time
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from;

        switch (timeFilter.toLowerCase()) {
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
            default:
                from = null; // fallback to allTime
        }
        return from;
    }



    public static Specification<Forms> createdBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null || endDate == null) return cb.conjunction();
            return cb.between(root.get("createdAt"),
                    startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        };
    }

    public static Specification<Forms> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<Forms> byUser(BaseUser user) {
        if (user == null) return null;
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }

    public static Specification<Forms> byServiceNeeded(String serviceNeeded) {
        return (root, query, cb) -> {
            if (serviceNeeded == null || serviceNeeded.isEmpty()) return cb.conjunction();
            return cb.equal(cb.lower(root.get("serviceNeeded")), serviceNeeded.toLowerCase());
        };
    }
}

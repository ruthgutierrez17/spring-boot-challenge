package com.challenge.specification;

import com.challenge.entity.CallHistory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Specifications para consultas dinámicas de CallHistory
 */
public class CallHistorySpecifications {

    /**
     * Filtra por endpoint (case insensitive, contiene)
     */
    public static Specification<CallHistory> withEndpoint(String endpoint) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(endpoint)) {
                return criteriaBuilder.conjunction(); // No aplica filtro
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("endpoint")),
                "%" + endpoint.toLowerCase() + "%"
            );
        };
    }

    /**
     * Filtra por fecha de inicio (timestamp >= startDate)
     */
    public static Specification<CallHistory> withStartDate(LocalDateTime startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) {
                return criteriaBuilder.conjunction(); // No aplica filtro
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startDate);
        };
    }

    /**
     * Filtra por fecha de fin (timestamp <= endDate)
     */
    public static Specification<CallHistory> withEndDate(LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) {
                return criteriaBuilder.conjunction(); // No aplica filtro
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), endDate);
        };
    }

}

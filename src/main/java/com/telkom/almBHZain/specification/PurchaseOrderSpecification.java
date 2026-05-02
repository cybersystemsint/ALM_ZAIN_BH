package com.telkom.almBHZain.specification;

import com.telkom.almBHZain.dto.request.FilterOperator;
import com.telkom.almBHZain.dto.request.FilterRequest;
import com.telkom.almBHZain.dto.request.SearchRequest;
import com.telkom.almBHZain.model.PurchaseOrder;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class PurchaseOrderSpecification {

    private PurchaseOrderSpecification() {}

    public static Specification<PurchaseOrder> fromSearchRequest(SearchRequest req) {
        List<Specification<PurchaseOrder>> specs = new ArrayList<>();

        if (req == null) {
            return null;
        }

        if (req.getSearchQuery() != null && !req.getSearchQuery().trim().isEmpty()) {
            specs.add(buildSearchSpecification(req.getSearchQuery(), req.getSearchColumn()));
        }

        if (req.getFilterBy() != null) {
            for (FilterRequest f : req.getFilterBy()) {
                Specification<PurchaseOrder> fs = buildFilterSpecification(f);
                if (fs != null) {
                    specs.add(fs);
                }
            }
        }

        Specification<PurchaseOrder> result = null;
        for (Specification<PurchaseOrder> s : specs) {
            result = (result == null) ? Specification.where(s) : result.and(s);
        }
        return result;
    }

    private static Specification<PurchaseOrder> buildSearchSpecification(String query, String searchColumn) {
        final String q = query.toLowerCase(Locale.ROOT);
        return (root, cq, cb) -> {
            if (searchColumn != null && !searchColumn.trim().isEmpty()) {
                try {
                    Path<?> path = root.get(searchColumn);
                    Class<?> javaType = path.getJavaType();

                    if (LocalDateTime.class.isAssignableFrom(javaType)) {
                        try {
                            LocalDate d = LocalDate.parse(query);
                            LocalDateTime start = d.atStartOfDay();
                            LocalDateTime end = d.atTime(23,59,59,999_999_999);
                            return cb.between(path.as(LocalDateTime.class), start, end);
                        } catch (DateTimeParseException ex) {
                        }
                    }

                    return cb.like(cb.lower(path.as(String.class)), "%" + q + "%");
                } catch (IllegalArgumentException ex) {
                    return cb.conjunction();
                }
            } else {
                List<Predicate> predicates = new ArrayList<>();
                try { predicates.add(cb.like(cb.lower(root.get("poNumber").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
                try { predicates.add(cb.like(cb.lower(root.get("approvalStatus").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
                try { predicates.add(cb.like(cb.lower(root.get("createdBy").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
                try { predicates.add(cb.like(cb.lower(root.get("updatedBy").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
                if (predicates.isEmpty()) return cb.conjunction();
                return cb.or(predicates.toArray(new Predicate[0]));
            }
        };
    }

    private static Specification<PurchaseOrder> buildFilterSpecification(FilterRequest f) {
        if (f == null || f.getOperator() == null || f.getColumn() == null) return null;

        String column = f.getColumn();
        FilterOperator op = f.getOperator();

        return (root, cq, cb) -> {
            try {
                Path<?> path = root.get(column);
                Class<?> javaType = path.getJavaType();

                // LocalDateTime columns (createdAt/updatedAt)
                if (LocalDateTime.class.isAssignableFrom(javaType)) {
                    switch (op) {
                        case EQUALS: {
                            try {
                                LocalDate d = LocalDate.parse(f.getValue());
                                LocalDateTime start = d.atStartOfDay();
                                LocalDateTime end = d.atTime(23,59,59,999_999_999);
                                return cb.between(path.as(LocalDateTime.class), start, end);
                            } catch (DateTimeParseException ex) {
                                return cb.conjunction();
                            }
                        }
                        case IS_ANY_OF: {
                            if (f.getValues() == null || f.getValues().isEmpty()) return cb.conjunction();
                            List<Predicate> orPreds = new ArrayList<>();
                            for (String v : f.getValues()) {
                                try {
                                    LocalDate d = LocalDate.parse(v);
                                    LocalDateTime s = d.atStartOfDay();
                                    LocalDateTime e = d.atTime(23,59,59,999_999_999);
                                    orPreds.add(cb.between(path.as(LocalDateTime.class), s, e));
                                } catch (DateTimeParseException ignore) {}
                            }
                            if (orPreds.isEmpty()) return cb.conjunction();
                            return cb.or(orPreds.toArray(new Predicate[0]));
                        }
                        case IS_EMPTY:
                            return cb.or(cb.isNull(path), cb.equal(path.as(String.class), ""));
                        case IS_NOT_EMPTY:
                            return cb.and(cb.isNotNull(path), cb.notEqual(path.as(String.class), ""));
                        default:
                            return cb.conjunction();
                    }
                }

                switch (op) {
                    case CONTAINS: {
                        String val = safeLower(f.getValue());
                        return cb.like(cb.lower(path.as(String.class)), "%" + val + "%");
                    }
                    case STARTS_WITH: {
                        String val = safeLower(f.getValue());
                        return cb.like(cb.lower(path.as(String.class)), val + "%");
                    }
                    case ENDS_WITH: {
                        String val = safeLower(f.getValue());
                        return cb.like(cb.lower(path.as(String.class)), "%" + val);
                    }
                    case EQUALS: {
                        String val = f.getValue();
                        return cb.equal(cb.lower(path.as(String.class)), val == null ? null : val.toLowerCase(Locale.ROOT));
                    }
                    case IS_EMPTY: {
                        return cb.or(cb.isNull(path), cb.equal(path.as(String.class), ""));
                    }
                    case IS_NOT_EMPTY: {
                        return cb.and(cb.isNotNull(path), cb.notEqual(path.as(String.class), ""));
                    }
                    case IS_ANY_OF: {
                        if (f.getValues() == null || f.getValues().isEmpty()) {
                            return cb.conjunction();
                        }
                        List<Predicate> inPreds = new ArrayList<>();
                        for (String v : f.getValues()) {
                            inPreds.add(cb.equal(cb.lower(path.as(String.class)), v == null ? null : v.toLowerCase(Locale.ROOT)));
                        }
                        return cb.or(inPreds.toArray(new Predicate[0]));
                    }
                    default:
                        return cb.conjunction();
                }
            } catch (IllegalArgumentException ex) {
                return cb.conjunction();
            }
        };
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }
}
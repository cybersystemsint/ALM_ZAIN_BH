package com.zain.jo.alm.pomanagement.specification;

import com.zain.jo.alm.pomanagement.dto.request.FilterOperator;
import com.zain.jo.alm.pomanagement.dto.request.FilterRequest;
import com.zain.jo.alm.pomanagement.dto.request.SearchRequest;
import com.zain.jo.alm.pomanagement.entity.Workflow;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class WorkflowSpecification {

    private WorkflowSpecification() {}

public static Specification<Workflow> fromSearchRequest(boolean updatedStatusIsNull, SearchRequest req) {
    List<Specification<Workflow>> specs = new ArrayList<>();
    // Status predicate: updatedStatus IS NULL (pending) or IS NOT NULL (processed)
    specs.add((root, cq, cb) -> updatedStatusIsNull ? cb.isNull(root.get("updatedStatus")) : cb.isNotNull(root.get("updatedStatus")));

    if (req == null) {
        return combineAnd(specs);
    }
    if (req.getStartDate() != null || req.getEndDate() != null) {
        specs.add((root, cq, cb) -> {
            Path<?> p = resolvePath(root, "insertDate"); 
            if (p == null) return cb.conjunction();

            Date start = null;
            Date end = null;
            if (req.getStartDate() != null) {
                start = Date.from(req.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            if (req.getEndDate() != null) {
                end = Date.from(req.getEndDate().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
            }

            if (start != null && end != null) {
                return cb.between(p.as(Date.class), start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(p.as(Date.class), start);
            } else { 
                return cb.lessThanOrEqualTo(p.as(Date.class), end);
            }
        });
    }

    // searchQuery handling
    if (req.getSearchQuery() != null && !req.getSearchQuery().trim().isEmpty()) {
        specs.add(buildSearchSpecification(req.getSearchQuery(), req.getSearchColumn()));
    }

    // filterBy handling
    if (req.getFilterBy() != null) {
        for (FilterRequest f : req.getFilterBy()) {
            Specification<Workflow> fs = buildFilterSpecification(f);
            if (fs != null) specs.add(fs);
        }
    }

    return combineAnd(specs);
}

    private static Specification<Workflow> combineAnd(List<Specification<Workflow>> specs) {
        Specification<Workflow> result = null;
        for (Specification<Workflow> s : specs) {
            result = (result == null) ? Specification.where(s) : result.and(s);
        }
        return result;
    }


    private static Specification<Workflow> buildSearchSpecification(String query, String searchColumn) {
        final String rawQuery = query == null ? "" : query;
        final String rawSearchColumn = searchColumn == null ? "" : searchColumn;

        return (root, cq, cb) -> {
            if (!rawSearchColumn.trim().isEmpty()) {
                Path<?> path = resolvePath(root, rawSearchColumn);
                String valueToMatch = rawQuery;

                if (path == null) {
                    Path<?> alt = resolvePath(root, rawQuery);
                    if (alt != null) {
                        path = alt;
                        valueToMatch = rawSearchColumn; 
                    } else {
                        return cb.conjunction();
                    }
                }
                if (valueToMatch == null) valueToMatch = "";
                Class<?> javaType = path.getJavaType();

                if (java.util.Date.class.isAssignableFrom(javaType)) {
                    try {
                        LocalDate d = LocalDate.parse(valueToMatch);
                        Date start = Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        Date end = Date.from(d.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
                        return cb.between(path.as(Date.class), start, end);
                    } catch (DateTimeParseException ex) {
                    }
                }

                return cb.like(cb.lower(path.as(String.class)), "%" + valueToMatch.toLowerCase(Locale.ROOT) + "%");
            } else {
                final String q = rawQuery.toLowerCase(Locale.ROOT);
                List<Predicate> predicates = new ArrayList<>();
                try { predicates.add(cb.like(cb.lower(root.get("poNumber").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
                try { predicates.add(cb.like(cb.lower(root.get("originalStatus").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
                try { predicates.add(cb.like(cb.lower(root.get("updatedStatus").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
                try { predicates.add(cb.like(cb.lower(root.get("insertedBy").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
                try { predicates.add(cb.like(cb.lower(root.get("changedBy").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
                try { predicates.add(cb.like(cb.lower(root.get("comments").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
                try { predicates.add(cb.like(cb.lower(root.get("processId").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
                if (predicates.isEmpty()) return cb.conjunction();
                return cb.or(predicates.toArray(new Predicate[0]));
            }
        };
    }

    private static Specification<Workflow> buildFilterSpecification(FilterRequest f) {
        if (f == null || f.getOperator() == null || f.getColumn() == null) return null;

        final String column = f.getColumn();
        final FilterOperator op = f.getOperator();

        return (root, cq, cb) -> {
            try {
                Path<?> path = resolvePath(root, column);
                if (path == null) return cb.conjunction();
                Class<?> javaType = path.getJavaType();

                if (java.util.Date.class.isAssignableFrom(javaType)) {
                    switch (op) {
                        case EQUALS: {
                            try {
                                LocalDate d = LocalDate.parse(f.getValue());
                                Date start = Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
                                Date end = Date.from(d.atTime(23,59,59).atZone(ZoneId.systemDefault()).toInstant());
                                return cb.between(path.as(Date.class), start, end);
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
                                    Date s = Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
                                    Date e = Date.from(d.atTime(23,59,59).atZone(ZoneId.systemDefault()).toInstant());
                                    orPreds.add(cb.between(path.as(Date.class), s, e));
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

                boolean isNumber = Number.class.isAssignableFrom(javaType) || javaType.equals(long.class) || javaType.equals(int.class);

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
                        if (isNumber) {
                            try {
                                Long num = (f.getValue() == null) ? null : Long.valueOf(f.getValue());
                                if (num == null) return cb.isNull(path);
                                return cb.equal(path.as(Number.class), num);
                            } catch (NumberFormatException nfe) {
                                return cb.equal(cb.lower(path.as(String.class)), f.getValue() == null ? null : f.getValue().toLowerCase(Locale.ROOT));
                            }
                        } else {
                            return cb.equal(cb.lower(path.as(String.class)), f.getValue() == null ? null : f.getValue().toLowerCase(Locale.ROOT));
                        }
                    }
                    case IS_EMPTY: {
                        return cb.or(cb.isNull(path), cb.equal(path.as(String.class), ""));
                    }
                    case IS_NOT_EMPTY: {
                        return cb.and(cb.isNotNull(path), cb.notEqual(path.as(String.class), ""));
                    }
                    case IS_ANY_OF: {
                        if (f.getValues() == null || f.getValues().isEmpty()) return cb.conjunction();
                        if (isNumber) {
                            List<Predicate> numPreds = new ArrayList<>();
                            for (String v : f.getValues()) {
                                try {
                                    Long num = (v == null) ? null : Long.valueOf(v);
                                    if (num == null) numPreds.add(cb.isNull(path));
                                    else numPreds.add(cb.equal(path.as(Number.class), num));
                                } catch (NumberFormatException ignore) {}
                            }
                            if (numPreds.isEmpty()) return cb.conjunction();
                            return cb.or(numPreds.toArray(new Predicate[0]));
                        } else {
                            List<Predicate> inPreds = new ArrayList<>();
                            for (String v : f.getValues()) {
                                inPreds.add(cb.equal(cb.lower(path.as(String.class)), v == null ? null : v.toLowerCase(Locale.ROOT)));
                            }
                            return cb.or(inPreds.toArray(new Predicate[0]));
                        }
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

 

    private static Path<?> resolvePath(Root<?> root, String column) {
        if (column == null || column.trim().isEmpty()) return null;
        String[] candidates = generateNameCandidates(column);
        for (String candidate : candidates) {
            try {
                return root.get(candidate);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    private static String[] generateNameCandidates(String column) {
        String asIs = column;
        String decap = decapitalize(column);
        String camel = snakeToCamel(column);
        String upper = column.toUpperCase(Locale.ROOT);
        String lower = column.toLowerCase(Locale.ROOT);
        return new String[] { asIs, decap, camel, upper, lower };
    }

    private static String decapitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private static String snakeToCamel(String s) {
        if (s == null) return null;
        String in = s.trim();
        if (!in.contains("_")) {
            if (in.equals(in.toUpperCase())) {
                return decapitalize(in.toLowerCase(Locale.ROOT));
            }
            return decapitalize(in);
        }
        StringBuilder sb = new StringBuilder();
        String[] parts = in.split("_+");
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].toLowerCase(Locale.ROOT);
            if (i == 0) sb.append(p);
            else sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }
}
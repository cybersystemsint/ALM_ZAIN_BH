package com.telkom.almBHZain.specification;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.telkom.almBHZain.dto.Request.FilterOperator;
import com.telkom.almBHZain.dto.Request.FilterRequest;
import com.telkom.almBHZain.dto.Request.SearchRequest;
import com.telkom.almBHZain.model.POItem;


public final class POItemSpecification {

    private POItemSpecification() {}

    public static Specification<POItem> fromSearchRequest(SearchRequest req) {
        if (req == null) return null;
        List<Specification<POItem>> specs = new ArrayList<>();

        if (req.getSearchQuery() != null && !req.getSearchQuery().trim().isEmpty()) {
            specs.add(buildSearchSpecification(req.getSearchQuery(), req.getSearchColumn()));
        }

        if (req.getFilterBy() != null) {
            for (FilterRequest f : req.getFilterBy()) {
                Specification<POItem> fs = buildFilterSpecification(f);
                if (fs != null) specs.add(fs);
            }
        }

        Specification<POItem> result = null;
        for (Specification<POItem> s : specs) {
            result = (result == null) ? Specification.where(s) : result.and(s);
        }
        return result;
    }
private static Specification<POItem> buildSearchSpecification(String query, String searchColumn) {
    final String rawQuery = query == null ? "" : query;
    final String rawSearchColumn = searchColumn == null ? "" : searchColumn;

    return (root, cq, cb) -> {

        if (!rawSearchColumn.trim().isEmpty()) {
            try {
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
                String valueLower = valueToMatch.toLowerCase(Locale.ROOT);

                if (java.sql.Date.class.isAssignableFrom(javaType)
                        || java.util.Date.class.isAssignableFrom(javaType)
                        || java.sql.Timestamp.class.isAssignableFrom(javaType)) {
                    try {
                        LocalDate d = LocalDate.parse(valueToMatch);
                        java.sql.Date sql = java.sql.Date.valueOf(d);
                        if (java.sql.Date.class.isAssignableFrom(javaType)) {
                            return cb.equal(path.as(java.sql.Date.class), sql);
                        } else {
                            java.util.Date start = java.util.Date.from(d.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                            java.util.Date end = java.util.Date.from(d.atTime(23,59,59).atZone(java.time.ZoneId.systemDefault()).toInstant());
                            return cb.between(path.as(java.util.Date.class), start, end);
                        }
                    } catch (DateTimeParseException ex) {
                    }
                }

                return cb.like(cb.lower(path.as(String.class)), "%" + valueLower + "%");
            } catch (IllegalArgumentException ex) {
                return cb.conjunction();
            }
        } else {
            final String q = rawQuery.toLowerCase(Locale.ROOT);
            List<Predicate> predicates = new ArrayList<>();
            try { predicates.add(cb.like(cb.lower(root.get("poNumber").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("modelNumber").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("partNumber").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("vendorName").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("level1Description").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("l3").as(String.class)), "%" + q + "%")); } catch (Exception e) {}
            if (predicates.isEmpty()) return cb.conjunction();
            return cb.or(predicates.toArray(new Predicate[0]));
        }
    };
}
    private static Specification<POItem> buildFilterSpecification(FilterRequest f) {
        if (f == null || f.getOperator() == null || f.getColumn() == null) return null;

        final String column = f.getColumn();
        final FilterOperator op = f.getOperator();

        return (root, cq, cb) -> {
            try {
                Path<?> path = resolvePath(root, column);
                if (path == null) return cb.conjunction();
                Class<?> javaType = path.getJavaType();

                if (java.sql.Date.class.isAssignableFrom(javaType)
                        || java.util.Date.class.isAssignableFrom(javaType)
                        || java.sql.Timestamp.class.isAssignableFrom(javaType)) {

                    switch (op) {
                        case EQUALS: {
                            try {
                                LocalDate d = LocalDate.parse(f.getValue());
                                java.util.Date start = java.util.Date.from(d.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                                java.util.Date end = java.util.Date.from(d.atTime(23,59,59).atZone(java.time.ZoneId.systemDefault()).toInstant());
                                return cb.between(path.as(java.util.Date.class), start, end);
                            } catch (Exception ex) {
                                return cb.conjunction();
                            }
                        }
                        case IS_ANY_OF: {
                            if (f.getValues() == null || f.getValues().isEmpty()) return cb.conjunction();
                            List<Predicate> orPreds = new ArrayList<>();
                            for (String v : f.getValues()) {
                                try {
                                    LocalDate d = LocalDate.parse(v);
                                    java.util.Date s = java.util.Date.from(d.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                                    java.util.Date e = java.util.Date.from(d.atTime(23,59,59).atZone(java.time.ZoneId.systemDefault()).toInstant());
                                    orPreds.add(cb.between(path.as(java.util.Date.class), s, e));
                                } catch (Exception ignore) {}
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

                boolean isNumber = Number.class.isAssignableFrom(javaType)
                        || javaType.equals(int.class) || javaType.equals(long.class)
                        || javaType.equals(double.class) || javaType.equals(float.class)
                        || javaType.equals(short.class) || javaType.equals(byte.class);

                if (isNumber) {
                    switch (op) {
                        case EQUALS: {
                            try {
                                Number num = parseNumber(f.getValue(), javaType);
                                if (num == null) return cb.conjunction();
                                return cb.equal(path.as(Number.class), num);
                            } catch (NumberFormatException ex) {
                                return cb.conjunction();
                            }
                        }
                        case IS_ANY_OF: {
                            if (f.getValues() == null || f.getValues().isEmpty()) return cb.conjunction();
                            List<Predicate> orPreds = new ArrayList<>();
                            for (String v : f.getValues()) {
                                try {
                                    Number num = parseNumber(v, javaType);
                                    if (num != null) orPreds.add(cb.equal(path.as(Number.class), num));
                                } catch (NumberFormatException ignore) {}
                            }
                            if (orPreds.isEmpty()) return cb.conjunction();
                            return cb.or(orPreds.toArray(new Predicate[0]));
                        }
                        case IS_EMPTY:
                            return cb.or(cb.isNull(path), cb.equal(path.as(String.class), ""));
                        case IS_NOT_EMPTY:
                            return cb.and(cb.isNotNull(path), cb.notEqual(path.as(String.class), ""));
                        case CONTAINS:
                        case STARTS_WITH:
                        case ENDS_WITH:
                        default:
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
                        if (val == null) return cb.isNull(path);
                        return cb.equal(cb.lower(path.as(String.class)), val.toLowerCase(Locale.ROOT));
                    }
                    case IS_EMPTY: {
                        return cb.or(cb.isNull(path), cb.equal(path.as(String.class), ""));
                    }
                    case IS_NOT_EMPTY: {
                        return cb.and(cb.isNotNull(path), cb.notEqual(path.as(String.class), ""));
                    }
                    case IS_ANY_OF: {
                        if (f.getValues() == null || f.getValues().isEmpty()) return cb.conjunction();
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

    private static Number parseNumber(String s, Class<?> targetType) {
        if (s == null) return null;
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return null;
        if (targetType == Integer.class || targetType == int.class) return Integer.valueOf(trimmed);
        if (targetType == Long.class || targetType == long.class) return Long.valueOf(trimmed);
        if (targetType == Double.class || targetType == double.class) return Double.valueOf(trimmed);
        if (targetType == Float.class || targetType == float.class) return Float.valueOf(trimmed);
        if (targetType == Short.class || targetType == short.class) return Short.valueOf(trimmed);
        if (targetType == Byte.class || targetType == byte.class) return Byte.valueOf(trimmed);
        return Long.valueOf(trimmed);
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
                // try next
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
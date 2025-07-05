package com.telkom.almBHZain.repo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.telkom.almBHZain.model.Workflow;

public class WorkflowRepositoryImpl implements WorkflowRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private static final List<String> STRING_COLUMNS = Arrays.asList(
        "poNumber", "oldPoNumber", "newPoNumber", "originalStatus", "updatedStatus",
        "processId", "insertedBy", "changedBy", "comments"
    );

    private static final List<String> DATE_COLUMNS = Arrays.asList(
        "insertDate", "changeDate"
    );

    @Override
    public Page<Workflow> searchByColumn(boolean updatedStatusIsNull, String columnName, String searchQuery, Pageable pageable) {
        String baseWhere = "(:updatedStatusIsNull = TRUE AND w.updatedStatus IS NULL OR :updatedStatusIsNull = FALSE AND w.updatedStatus IS NOT NULL) ";

        String jpql;
        TypedQuery<Workflow> query;
        String countJpql;
        TypedQuery<Long> countQuery;

        if (STRING_COLUMNS.contains(columnName)) {
            jpql = "SELECT w FROM Workflow w WHERE " + baseWhere +
                   "AND LOWER(w." + columnName + ") LIKE :searchQuery";
            query = entityManager.createQuery(jpql, Workflow.class)
                .setParameter("updatedStatusIsNull", updatedStatusIsNull)
                .setParameter("searchQuery", "%" + searchQuery.toLowerCase() + "%");

            countJpql = "SELECT COUNT(w) FROM Workflow w WHERE " + baseWhere +
                        "AND LOWER(w." + columnName + ") LIKE :searchQuery";
            countQuery = entityManager.createQuery(countJpql, Long.class)
                .setParameter("updatedStatusIsNull", updatedStatusIsNull)
                .setParameter("searchQuery", "%" + searchQuery.toLowerCase() + "%");

        } else if (DATE_COLUMNS.contains(columnName)) {
            Date startDate = parseDateToStartOfDay(searchQuery);
            Date endDate = parseDateToEndOfDay(searchQuery);
            if (startDate == null || endDate == null) {
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }

            jpql = "SELECT w FROM Workflow w WHERE " + baseWhere +
                   "AND w." + columnName + " >= :startDate AND w." + columnName + " < :endDate";
            query = entityManager.createQuery(jpql, Workflow.class)
                .setParameter("updatedStatusIsNull", updatedStatusIsNull)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate);

            countJpql = "SELECT COUNT(w) FROM Workflow w WHERE " + baseWhere +
                        "AND w." + columnName + " >= :startDate AND w." + columnName + " < :endDate";
            countQuery = entityManager.createQuery(countJpql, Long.class)
                .setParameter("updatedStatusIsNull", updatedStatusIsNull)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate);

        } else {
            throw new IllegalArgumentException("Invalid or unsupported column name");
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Workflow> results = query.getResultList();
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }

    private Date parseDateToStartOfDay(String value) {
        Date date = parseDate(value);
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date parseDateToEndOfDay(String value) {
        Date date = parseDate(value);
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date parseDate(String value) {
        if (value == null) return null;
        List<String> patterns = Arrays.asList(
            "yyyy-MM-dd",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        );
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern).parse(value);
            } catch (ParseException ignored) {}
        }
        return null;
    }
}
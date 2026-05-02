package com.telkom.almBHZain.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.telkom.almBHZain.dto.request.FilterOperator;
import com.telkom.almBHZain.dto.request.FilterRequest;
import com.telkom.almBHZain.dto.request.SearchRequest;
import com.telkom.almBHZain.service.ExportService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Streaming export implementation.
 * Uses JdbcTemplate cursor streaming + EasyExcel for Excel and BufferedWriter for CSV.
 * No HTTP concerns belong here — callers supply the OutputStream.
 */
@Service
public class ExportServiceImpl implements ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportServiceImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int    LOG_INTERVAL      = 50_000;
    private static final int    BATCH_SIZE        = 1_000;
    private static final int    MAX_ROWS_PER_SHEET = 1_000_000;
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String[] PO_COLUMNS = {
            "id", "poNumber", "Approval_Status", "created_by", "created_at"
    };
    private static final String[] POITEM_COLUMNS = {
            "recordNo", "recordDateTime", "poNumber", "modelNumber", "uom", "qtyPerSite",
            "totalNumberOfSites", "totalQty", "accumulatedDepreciation", "salvageValue",
            "faCategoryNew", "L1", "L2", "L3", "L4", "oldFaCategory", "accumulatedDepreciationCode",
            "depreciationCode", "lifeYearsNew", "vendorName", "vendorNumber", "projectNumber",
            "datePlacedInService", "poDate", "currency", "unitPrice", "poLine",
            "Level1Description", "partNumber", "L3Description", "costCenter",
            "Approval_Status", "createdBy", "createdDateTime", "updatedBy", "updatedDatetime"
    };
    private static final String[] WORKFLOW_COLUMNS = {
            "ID", "PO_NUMBER", "RECORD_NO", "OLD_PO_NUMBER", "NEW_PO_NUMBER",
            "ORIGINAL_STATUS", "UPDATED_STATUS", "PROCESS_ID", "INSERTEDBY",
            "INSERTDATE", "CHANGEDBY", "CHANGEDATE", "COMMENTS"
    };

    private static final Map<String, String> CUSTOM_HEADER_MAP;
    static {
        Map<String, String> m = new HashMap<>();
        m.put("recordNo",            "Record No");
        m.put("recordDateTime",      "Record Date");
        m.put("poNumber",            "PO Number");
        m.put("Approval_Status",     "Approval Status");
        m.put("created_by",          "Created By");
        m.put("created_at",          "Created Date");
        m.put("createdDateTime",     "Created Date");
        m.put("updated_by",          "Updated By");
        m.put("updated_at",          "Updated Date");
        m.put("updatedDatetime",     "Updated Date");
        m.put("Level1Description",   "Level 1 Description");
        m.put("L3Description",       "L3 Description");
        m.put("poLine",              "PO Line");
        m.put("vendorName",          "Vendor Name");
        m.put("vendorNumber",        "Vendor Number");
        m.put("datePlacedInService", "Date Placed In Service");
        m.put("poDate",              "PO Date");
        m.put("ID",                  "ID");
        m.put("PO_NUMBER",           "PO Number");
        m.put("INSERTDATE",          "Insert Date");
        m.put("CHANGEDATE",          "Change Date");
        CUSTOM_HEADER_MAP = Collections.unmodifiableMap(m);
    }

    private static final Pattern CAMEL_SPLIT =
            Pattern.compile("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Za-z])(?=[0-9])|_+");

    // ──────────────────────────────────────────────
    // Public API (ExportService contract)
    // ──────────────────────────────────────────────

    @Override
    public void exportPurchaseOrders(SearchRequest req, OutputStream os, String format) throws IOException {
        exportGeneric(req, "tb_PONumber", Arrays.asList(PO_COLUMNS),
                os, format, Collections.singleton("created_at"));
    }

    @Override
    public void exportPOItems(SearchRequest req, OutputStream os, String format) throws IOException {
        Set<String> dateCols = new HashSet<>(Arrays.asList(
                "recordDateTime", "datePlacedInService", "poDate", "createdDateTime", "updatedDatetime"));
        exportGeneric(req, "tb_Po", Arrays.asList(POITEM_COLUMNS), os, format, dateCols);
    }

    @Override
    public void exportWorkflows(SearchRequest req, OutputStream os, String format) throws IOException {
        Set<String> dateCols = new HashSet<>(Arrays.asList("INSERTDATE", "CHANGEDATE"));
        exportGeneric(req, "tb_WF_PO_Approval_Request", Arrays.asList(WORKFLOW_COLUMNS), os, format, dateCols);
    }

    // ──────────────────────────────────────────────
    // Core streaming logic (private)
    // ──────────────────────────────────────────────

    private void exportGeneric(SearchRequest req, String table, List<String> columns,
                                OutputStream os, String format, Set<String> dateColumns) throws IOException {

        List<Object> params = new ArrayList<>();
        String where = buildWhereClause(req, columns, params);
        String sql   = "SELECT " + String.join(", ", columns)
                + " FROM " + table
                + (where.isEmpty() ? "" : " WHERE " + where);
        logger.info("Streaming export SQL: {}", sql);

        List<String> headerNames = columns.stream()
                .map(c -> capitalizeHeaderWords(formatHeader(c)))
                .collect(Collectors.toList());
        List<List<String>> headerForExcel = Collections.singletonList(new ArrayList<>(headerNames));

        AtomicInteger sheetIndex     = new AtomicInteger(0);
        AtomicInteger rowsInSheet    = new AtomicInteger(0);
        AtomicInteger totalRows      = new AtomicInteger(0);

        try (ExcelWriter excelWriter = "excel".equalsIgnoreCase(format) ? EasyExcel.write(os).build() : null;
             BufferedWriter csvWriter = "csv".equalsIgnoreCase(format)
                     ? new BufferedWriter(new OutputStreamWriter(os, "UTF-8")) : null) {

            if (csvWriter != null) {
                csvWriter.write(headerNames.stream().map(this::escapeCsv).collect(Collectors.joining(",")));
                csvWriter.newLine();
            }

            class SheetHolder { WriteSheet sheet; }
            SheetHolder holder = new SheetHolder();
            if (excelWriter != null) {
                holder.sheet = createSheet(excelWriter, headerForExcel, sheetIndex.get());
            }

            List<List<Object>> batch = new ArrayList<>(BATCH_SIZE);

            jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
            jdbcTemplate.query(sql, params.toArray(), (ResultSet rs) -> {
                try {
                    while (rs.next()) {
                        List<Object> row = new ArrayList<>(columns.size());
                        for (int i = 0; i < columns.size(); i++) {
                            Object val = rs.getObject(i + 1);
                            if (val != null) {
                                String colName = columns.get(i);
                                boolean isDate = dateColumns.contains(colName)
                                        || dateColumns.contains(colName.toUpperCase());
                                if (isDate) {
                                    if (val instanceof java.sql.Date)
                                        val = DATE_ONLY_FORMAT.format((java.sql.Date) val);
                                    else if (val instanceof java.sql.Timestamp)
                                        val = DATE_ONLY_FORMAT.format(new java.util.Date(((java.sql.Timestamp) val).getTime()));
                                    else if (val instanceof java.util.Date)
                                        val = DATE_ONLY_FORMAT.format((java.util.Date) val);
                                    else
                                        val = val.toString();
                                } else if (val instanceof java.sql.Timestamp) {
                                    val = DATE_TIME_FORMAT.format(new java.util.Date(((java.sql.Timestamp) val).getTime()));
                                }
                            }
                            row.add(val);
                        }

                        totalRows.incrementAndGet();
                        rowsInSheet.incrementAndGet();
                        batch.add(row);

                        if (excelWriter != null && rowsInSheet.get() >= MAX_ROWS_PER_SHEET) {
                            if (!batch.isEmpty()) { excelWriter.write(new ArrayList<>(batch), holder.sheet); batch.clear(); }
                            holder.sheet = createSheet(excelWriter, headerForExcel, sheetIndex.incrementAndGet());
                            rowsInSheet.set(0);
                        }

                        if (batch.size() >= BATCH_SIZE) {
                            if (excelWriter != null)  excelWriter.write(new ArrayList<>(batch), holder.sheet);
                            else if (csvWriter != null) writeCsvRows(csvWriter, batch);
                            batch.clear();
                        }

                        if (totalRows.get() % LOG_INTERVAL == 0) {
                            logger.info("Export progress: rows={}, sheet={}, rowsInSheet={}",
                                    totalRows.get(), sheetIndex.get() + 1, rowsInSheet.get());
                        }
                    }

                    if (!batch.isEmpty()) {
                        if (excelWriter != null)  excelWriter.write(batch, holder.sheet);
                        else if (csvWriter != null) writeCsvRows(csvWriter, batch);
                    }
                } catch (IOException ioe) {
                    throw new RuntimeException("I/O during export: " + ioe.getMessage(), ioe);
                }
                return null;
            });

            if (csvWriter != null) csvWriter.flush();
            logger.info("Export finished. total={}, sheets={}", totalRows.get(), sheetIndex.get() + 1);

        } catch (Exception ex) {
            logger.error("Export failed", ex);
            throw new IOException("Export failed: " + ex.getMessage(), ex);
        } finally {
            jdbcTemplate.setFetchSize(0);
        }
    }

    // ──────────────────────────────────────────────
    // SQL builder
    // ──────────────────────────────────────────────

    private String buildWhereClause(SearchRequest req, List<String> allowedColumns, List<Object> params) {
        if (req == null) return "";
        List<String> clauses = new ArrayList<>();

        Map<String, String> allowedMap = new HashMap<>();
        for (String a : allowedColumns) allowedMap.put(normalizeName(a), a);
        java.util.function.Function<String, String> findAllowed = s ->
                s == null ? null : allowedMap.get(normalizeName(s));

        String q      = req.getSearchQuery();
        String colHint = req.getSearchColumn();
        if (q != null && !q.trim().isEmpty()) {
            q = q.trim();
            if (colHint != null && !colHint.trim().isEmpty()) {
                String matched = findAllowed.apply(colHint.trim());
                if (matched == null) {
                    String matchedFromQuery = findAllowed.apply(q);
                    if (matchedFromQuery != null) { String tmp = q; q = colHint; matched = matchedFromQuery; colHint = tmp; }
                }
                if (matched != null) {
                    clauses.add("(" + matched + " LIKE ?)");
                    params.add("%" + q + "%");
                } else {
                    appendMultiColumnLike(clauses, params, allowedColumns, q);
                }
            } else {
                appendMultiColumnLike(clauses, params, allowedColumns, q);
            }
        }

        if (req.getFilterBy() != null) {
            for (FilterRequest fr : req.getFilterBy()) {
                if (fr == null || fr.getColumn() == null) continue;
                String allowed = findAllowed.apply(fr.getColumn().trim());
                if (allowed == null) continue;
                FilterOperator op = fr.getOperator();
                switch (op) {
                    case CONTAINS:
                        clauses.add("LOWER(" + allowed + ") LIKE LOWER(?)");
                        params.add("%" + nullSafe(fr.getValue()) + "%"); break;
                    case STARTS_WITH:
                        clauses.add("LOWER(" + allowed + ") LIKE LOWER(?)");
                        params.add(nullSafe(fr.getValue()) + "%"); break;
                    case ENDS_WITH:
                        clauses.add("LOWER(" + allowed + ") LIKE LOWER(?)");
                        params.add("%" + nullSafe(fr.getValue())); break;
                    case EQUALS:
                        clauses.add(allowed + " = ?");
                        params.add(fr.getValue()); break;
                    case IS_EMPTY:
                        clauses.add("(" + allowed + " IS NULL OR " + allowed + " = '')"); break;
                    case IS_NOT_EMPTY:
                        clauses.add("(" + allowed + " IS NOT NULL AND " + allowed + " != '')"); break;
                    case IS_ANY_OF:
                        if (fr.getValues() != null && !fr.getValues().isEmpty()) {
                            String ph = fr.getValues().stream().map(v -> "?").collect(Collectors.joining(","));
                            clauses.add(allowed + " IN (" + ph + ")");
                            params.addAll(fr.getValues());
                        }
                        break;
                    default: break;
                }
            }
        }
        return String.join(" AND ", clauses);
    }

    private void appendMultiColumnLike(List<String> clauses, List<Object> params,
                                        List<String> columns, String q) {
        List<String> or = new ArrayList<>();
        for (String c : columns) { or.add(c + " LIKE ?"); params.add("%" + q + "%"); }
        clauses.add("(" + String.join(" OR ", or) + ")");
    }

    // ──────────────────────────────────────────────
    // Header / CSV helpers
    // ──────────────────────────────────────────────

    private WriteSheet createSheet(ExcelWriter writer, List<List<String>> headers, int idx) {
        WriteSheet sheet = EasyExcel.writerSheet("Data_Sheet_" + (idx + 1)).build();
        writer.write(headers, sheet);
        return sheet;
    }

    private void writeCsvRows(BufferedWriter w, List<List<Object>> rows) throws IOException {
        for (List<Object> row : rows) {
            w.write(row.stream().map(v -> v == null ? "" : escapeCsv(v.toString())).collect(Collectors.joining(",")));
            w.newLine();
        }
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private String formatHeader(String column) {
        if (column == null || column.isEmpty()) return "";
        String override = CUSTOM_HEADER_MAP.get(column);
        if (override != null) return override;
        if (column.contains("_")) {
            String[] parts = column.split("_+");
            List<String> words = new ArrayList<>();
            for (String p : parts) {
                if (p.isEmpty()) continue;
                words.add(isAcronym(p) ? p : capitalize(p.toLowerCase()));
            }
            return String.join(" ", words);
        }
        String spaced = CAMEL_SPLIT.matcher(column).replaceAll(" ");
        List<String> words = new ArrayList<>();
        for (String t : spaced.split("\\s+")) {
            if (t.isEmpty()) continue;
            words.add(isAcronym(t) ? t : t.matches("\\d+") ? t : capitalize(t));
        }
        return String.join(" ", words);
    }

    private String capitalizeHeaderWords(String header) {
        if (header == null || header.isEmpty()) return header;
        StringBuilder sb = new StringBuilder();
        String[] words = header.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0)));
            if (w.length() > 1) sb.append(w.substring(1));
            if (i < words.length - 1) sb.append(' ');
        }
        return sb.toString();
    }

    private boolean isAcronym(String t) { return t.length() <= 3 && t.equals(t.toUpperCase()); }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + (s.length() > 1 ? s.substring(1).toLowerCase() : "");
    }

    private String normalizeName(String s) {
        return s == null ? "" : s.replaceAll("[_\\s]", "").toLowerCase(Locale.ROOT);
    }

    private String nullSafe(String s) { return s == null ? "" : s; }
}
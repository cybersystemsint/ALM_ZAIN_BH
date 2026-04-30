package com.telkom.almBHZain.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.telkom.almBHZain.dto.Request.FilterOperator;
import com.telkom.almBHZain.dto.Request.FilterRequest;
import com.telkom.almBHZain.dto.Request.SearchRequest;

/**
 * Streaming export for PurchaseOrders, POItems and Workflows.
 * Uses JdbcTemplate cursor streaming + EasyExcel for Excel output and a BufferedWriter for CSV.
 */
@Service
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int LOG_INTERVAL = 50_000;
    private static final int BATCH_SIZE = 1_000;
    private static final int MAX_ROWS_PER_SHEET = 1_000_000;
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    // Column white-lists (DB column names). Adjust to your actual column names.
    private static final String[] PO_COLUMNS = {
            "id", "poNumber", "Approval_Status", "created_by", "created_at"
    };

    private static final String[] POITEM_COLUMNS = {
            "recordNo", "recordDateTime", "poNumber", "modelNumber", "uom", "qtyPerSite", "totalNumberOfSites",
            "totalQty", "accumulatedDepreciation", "salvageValue", "faCategoryNew", "L1", "L2", "L3", "L4",
            "oldFaCategory", "accumulatedDepreciationCode", "depreciationCode", "lifeYearsNew", "vendorName",
            "vendorNumber", "projectNumber", "datePlacedInService", "poDate", "currency", "unitPrice", "poLine",
            "Level1Description", "partNumber", "L3Description", "costCenter", "Approval_Status", "createdBy",
            "createdDateTime", "updatedBy", "updatedDatetime"
    };

    private static final String[] WORKFLOW_COLUMNS = {
            "ID", "PO_NUMBER", "RECORD_NO", "OLD_PO_NUMBER", "NEW_PO_NUMBER", "ORIGINAL_STATUS", "UPDATED_STATUS",
            "PROCESS_ID", "INSERTEDBY", "INSERTDATE", "CHANGEDBY", "CHANGEDATE", "COMMENTS"
    };

    // Small custom header overrides for particularly common fields
    private static final Map<String, String> CUSTOM_HEADER_MAP;
    static {
        Map<String,String> m = new HashMap<>();
        m.put("recordNo", "Record No");
        m.put("recordDateTime", "Record Date");
        m.put("poNumber", "PO Number");
        m.put("Approval_Status", "Approval Status");
        m.put("created_by", "Created By");
        m.put("created_at", "Created Date");
        m.put("createdDateTime", "Created Date");
        m.put("updated_by", "Updated By");
        m.put("updated_at", "Updated Date");
        m.put("updatedDatetime", "Updated Date");
        m.put("Level1Description", "Level 1 Description");
        m.put("L3Description", "L3 Description");
        m.put("poLine", "PO Line");
        m.put("vendorName", "Vendor Name");
        m.put("vendorNumber", "Vendor Number");
        m.put("datePlacedInService", "Date Placed In Service");
        m.put("poDate", "PO Date");
        m.put("ID", "ID");
        m.put("PO_NUMBER", "PO Number");
        m.put("INSERTDATE", "Insert Date");
        m.put("CHANGEDATE", "Change Date");
        CUSTOM_HEADER_MAP = Collections.unmodifiableMap(m);
    }

    // Pattern to split camelCase boundaries and letter-digit boundaries
    private static final Pattern CAMEL_SPLIT = Pattern.compile("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Za-z])(?=[0-9])|_+");

    // Public entry points

    public void exportPurchaseOrders(SearchRequest req, OutputStream os, String format) throws IOException {
        String table = "tb_PONumber";
        List<String> columns = Arrays.asList(PO_COLUMNS);
        exportGeneric(req, table, columns, os, format, Collections.singleton("created_at"));
    }

    public void exportPOItems(SearchRequest req, OutputStream os, String format) throws IOException {
        String table = "tb_Po";
        List<String> columns = Arrays.asList(POITEM_COLUMNS);
        // dateColumns: these should be returned as date-only
        Set<String> dateColumns = new HashSet<>(Arrays.asList("recordDateTime", "datePlacedInService", "poDate", "createdDateTime", "updatedDatetime"));
        exportGeneric(req, table, columns, os, format, dateColumns);
    }

    public void exportWorkflows(SearchRequest req, OutputStream os, String format) throws IOException {
        String table = "tb_WF_PO_Approval_Request";
        List<String> columns = Arrays.asList(WORKFLOW_COLUMNS);
        Set<String> dateColumns = new HashSet<>(Arrays.asList("INSERTDATE", "CHANGEDATE"));
        exportGeneric(req, table, columns, os, format, dateColumns);
    }

    // Generic exporter used by the three above
    private void exportGeneric(SearchRequest req,
                               String table,
                               List<String> allowedColumns,
                               OutputStream os,
                               String format,
                               Set<String> dateColumns) throws IOException {

        // Build select list (use exact column names)
        String select = allowedColumns.stream().collect(Collectors.joining(", "));
        // Build where + params
        List<Object> params = new ArrayList<>();
        String where = buildWhereClauseFromSearchRequest(req, allowedColumns, params);

        String sql = "SELECT " + select + " FROM " + table + (where.isEmpty() ? "" : " WHERE " + where);
        logger.info("Streaming export SQL: {}", sql);

        // Excel writer variables
        AtomicInteger currentSheetIndex = new AtomicInteger(0);
        AtomicInteger rowsInCurrentSheet = new AtomicInteger(0);
        AtomicInteger totalRows = new AtomicInteger(0);

        // Prepare formatted headers (human-friendly)
        List<String> formattedHeaderNames = allowedColumns.stream()
                .map(this::formatHeader)
                .map(this::capitalizeHeaderWords) 
                .collect(Collectors.toList());

        // For Excel we write headers as a single header row (EasyExcel expects a List<List<String>>),
        // so we create one inner list that contains all header names in order.
        List<List<String>> headerForExcel = new ArrayList<>();
        headerForExcel.add(new ArrayList<>(formattedHeaderNames));

        // For Excel
        try (ExcelWriter excelWriter = "excel".equalsIgnoreCase(format) ? EasyExcel.write(os).build() : null;
             BufferedWriter csvWriter = "csv".equalsIgnoreCase(format) ? new BufferedWriter(new OutputStreamWriter(os, "UTF-8")) : null) {

            // If CSV -> write header line immediately (use formatted headers)
            if (csvWriter != null) {
                csvWriter.write(formattedHeaderNames.stream()
                        .map(this::escapeCsv)
                        .collect(Collectors.joining(",")));
                csvWriter.newLine();
            }

            class SheetHolder { WriteSheet sheet; }
            final SheetHolder sheetHolder = new SheetHolder();
            if (excelWriter != null) {
                sheetHolder.sheet = createSheet(excelWriter, headerForExcel, currentSheetIndex.get());
            }

            List<List<Object>> batch = new ArrayList<>(BATCH_SIZE);

            // MySQL: stream results by using fetchSize = Integer.MIN_VALUE
            jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
            jdbcTemplate.query(sql, params.toArray(), (ResultSet rs) -> {
                try {
                    while (rs.next()) {
                        List<Object> row = new ArrayList<>(allowedColumns.size());
                        for (int i = 0; i < allowedColumns.size(); i++) {
                            Object val = rs.getObject(i + 1); // JDBC 1-based
                            // format dates to date-only if configured
                            if (val != null) {
                                String colName = allowedColumns.get(i);
                                if (dateColumns.contains(colName) || dateColumns.contains(colName.toUpperCase())) {
                                    // Accept java.sql.Date or Timestamp
                                    if (val instanceof java.sql.Date) {
                                        val = DATE_ONLY_FORMAT.format((java.sql.Date) val);
                                    } else if (val instanceof java.sql.Timestamp) {
                                        val = DATE_ONLY_FORMAT.format(new java.util.Date(((java.sql.Timestamp) val).getTime()));
                                    } else if (val instanceof java.util.Date) {
                                        val = DATE_ONLY_FORMAT.format((java.util.Date) val);
                                    } else {
                                        // fallback: toString
                                        val = val.toString();
                                    }
                                } else {
                                    // non-date: if it's a timestamp and we don't want date-only, keep as string full datetime
                                    if (val instanceof java.sql.Timestamp) {
                                        val = DATE_TIME_FORMAT.format(new java.util.Date(((java.sql.Timestamp) val).getTime()));
                                    }
                                }
                            }
                            row.add(val);
                        }

                        totalRows.incrementAndGet();
                        rowsInCurrentSheet.incrementAndGet();
                        batch.add(row);

                        // Excel sheet rollover
                        if (excelWriter != null && rowsInCurrentSheet.get() >= MAX_ROWS_PER_SHEET) {
                            if (!batch.isEmpty()) {
                                excelWriter.write(new ArrayList<>(batch), sheetHolder.sheet);
                                batch.clear();
                            }
                            int newSheetIdx = currentSheetIndex.incrementAndGet();
                            sheetHolder.sheet = createSheet(excelWriter, headerForExcel, newSheetIdx);
                            rowsInCurrentSheet.set(0);
                            logger.info("Created new sheet {}", sheetHolder.sheet.getSheetName());
                        }

                        // Write batch
                        if (batch.size() >= BATCH_SIZE) {
                            if (excelWriter != null) {
                                excelWriter.write(new ArrayList<>(batch), sheetHolder.sheet);
                            } else if (csvWriter != null) {
                                writeCsvRows(csvWriter, batch);
                            }
                            batch.clear();
                        }

                        if (totalRows.get() % LOG_INTERVAL == 0) {
                            logger.info("Export progress: rows processed = {}, current sheet = {}, rows in sheet = {}",
                                    totalRows.get(), currentSheetIndex.get() + 1, rowsInCurrentSheet.get());
                        }
                    }

                    // final batch flush
                    if (!batch.isEmpty()) {
                        if (excelWriter != null) {
                            excelWriter.write(batch, sheetHolder.sheet);
                        } else if (csvWriter != null) {
                            writeCsvRows(csvWriter, batch);
                        }
                        batch.clear();
                    }
                } catch (IOException ioe) {
                    throw new RuntimeException("I/O during export write: " + ioe.getMessage(), ioe);
                }
                return null;
            });

            // after streaming finishes, if excelWriter != null it's closed by try-with-resources,
            // for CSV we must flush
            if (csvWriter != null) {
                csvWriter.flush();
            }

            logger.info("Export finished. total rows = {}, sheets = {}", totalRows.get(), currentSheetIndex.get() + 1);
        } catch (Exception ex) {
            logger.error("Export failed", ex);
            throw new IOException("Export failed: " + ex.getMessage(), ex);
        } finally {
            jdbcTemplate.setFetchSize(0);
        }
    }

    // Helper to write CSV rows (very simple escaping)
    private void writeCsvRows(BufferedWriter w, List<List<Object>> rows) throws IOException {
        for (List<Object> row : rows) {
            String line = row.stream()
                    .map(v -> v == null ? "" : escapeCsv(v.toString()))
                    .collect(Collectors.joining(","));
            w.write(line);
            w.newLine();
        }
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private WriteSheet createSheet(ExcelWriter excelWriter, List<List<String>> headers, int sheetIndex) {
        String sheetName = String.format("Data_Sheet_%d", sheetIndex + 1);
        WriteSheet sheet = EasyExcel.writerSheet(sheetName).build();

        // Write headers for new sheet (single header row)
        excelWriter.write(headers, sheet);
        return sheet;
    }

// (Replace the existing buildWhereClauseFromSearchRequest method with the following)

private String buildWhereClauseFromSearchRequest(SearchRequest req, List<String> allowedColumns, List<Object> params) {
    if (req == null) return "";
    List<String> clauses = new ArrayList<>();

    // Build a normalization map for allowed columns (normalize -> originalAllowedName)
    Map<String, String> allowedMap = new HashMap<>();
    for (String a : allowedColumns) {
        allowedMap.put(normalizeName(a), a);
    }

    // Helper to find allowed column name by a loose match
    java.util.function.Function<String, String> findAllowed = (s) -> {
        if (s == null) return null;
        String n = normalizeName(s);
        return allowedMap.get(n);
    };

    // searchQuery logic
    String q = req.getSearchQuery();
    String searchColumn = req.getSearchColumn();
    if (q != null && !q.trim().isEmpty()) {
        q = q.trim();
        if (searchColumn != null && !searchColumn.trim().isEmpty()) {
            String providedCol = searchColumn.trim();
            String matched = findAllowed.apply(providedCol);

            // Detect swapped case: maybe searchQuery is actually the column name and searchColumn is the value
            if (matched == null) {
                String matchedFromQuery = findAllowed.apply(q);
                if (matchedFromQuery != null) {
                    // swap: q is value, matchedFromQuery is column
                    String tempVal = q;
                    q = providedCol;
                    matched = matchedFromQuery;
                }
            }

            if (matched != null) {
                clauses.add("(" + matched + " LIKE ?)");
                params.add("%" + q + "%");
            } else {
                // Fallback: do multi-column LIKE across allowedColumns (original behavior when no valid column)
                List<String> or = new ArrayList<>();
                for (String c : allowedColumns) {
                    or.add(c + " LIKE ?");
                    params.add("%" + q + "%");
                }
                clauses.add("(" + String.join(" OR ", or) + ")");
            }
        } else {
            // No column specified: do multi-column search across allowed columns
            List<String> or = new ArrayList<>();
            for (String c : allowedColumns) {
                or.add(c + " LIKE ?");
                params.add("%" + q + "%");
            }
            clauses.add("(" + String.join(" OR ", or) + ")");
        }
    }

    // filterBy
    if (req.getFilterBy() != null && !req.getFilterBy().isEmpty()) {
        for (FilterRequest fr : req.getFilterBy()) {
            if (fr == null || fr.getColumn() == null) continue;
            String col = fr.getColumn().trim();
            String allowedCol = findAllowed.apply(col);
            if (allowedCol == null) {
                // skip unknown filter columns
                continue;
            }
            FilterOperator op = fr.getOperator();
            switch (op) {
                case CONTAINS:
                    clauses.add("LOWER(" + allowedCol + ") LIKE LOWER(?)");
                    params.add("%" + (fr.getValue() == null ? "" : fr.getValue()) + "%");
                    break;
                case STARTS_WITH:
                    clauses.add("LOWER(" + allowedCol + ") LIKE LOWER(?)");
                    params.add((fr.getValue() == null ? "" : fr.getValue()) + "%");
                    break;
                case ENDS_WITH:
                    clauses.add("LOWER(" + allowedCol + ") LIKE LOWER(?)");
                    params.add("%" + (fr.getValue() == null ? "" : fr.getValue()));
                    break;
                case EQUALS:
                    clauses.add(allowedCol + " = ?");
                    params.add(fr.getValue());
                    break;
                case IS_EMPTY:
                    clauses.add("(" + allowedCol + " IS NULL OR " + allowedCol + " = '')");
                    break;
                case IS_NOT_EMPTY:
                    clauses.add("(" + allowedCol + " IS NOT NULL AND " + allowedCol + " != '')");
                    break;
                case IS_ANY_OF:
                    if (fr.getValues() != null && !fr.getValues().isEmpty()) {
                        String placeholders = fr.getValues().stream().map(v -> "?").collect(Collectors.joining(","));
                        clauses.add(allowedCol + " IN (" + placeholders + ")");
                        params.addAll(fr.getValues());
                    }
                    break;
                default:
                    // unsupported operator -> skip
            }
        }
    }

    return String.join(" AND ", clauses);
}


private String normalizeName(String s) {
    if (s == null) return "";
    return s.replaceAll("[_\\s]", "").toLowerCase(Locale.ROOT);
}
    // ------------------
    // Header formatting
    // ------------------
    private String formatHeader(String column) {
        if (column == null || column.isEmpty()) return "";

        // Check custom override first
        String override = CUSTOM_HEADER_MAP.get(column);
        if (override != null) return override;

        // If column contains underscores, treat as snake_case or DB_UPPER_STYLE
        if (column.contains("_")) {
            String[] parts = column.split("_+");
            List<String> words = new ArrayList<>();
            for (String p : parts) {
                if (p.isEmpty()) continue;
                if (isAcronym(p)) {
                    words.add(p); // keep "PO", "ID"
                } else {
                    words.add(capitalize(p.toLowerCase()));
                }
            }
            return String.join(" ", words);
        }

        // Otherwise split camelCase / PascalCase / letter-digit boundaries
        String spaced = CAMEL_SPLIT.matcher(column).replaceAll(" ");
        String[] tokens = spaced.split("\\s+");
        List<String> words = new ArrayList<>();
        for (String t : tokens) {
            if (t.isEmpty()) continue;
            if (isAcronym(t)) words.add(t);
            else if (t.matches("\\d+")) words.add(t);           // pure digits (e.g., "3")
            else words.add(capitalize(t));
        }
        return String.join(" ", words);
    }

    private boolean isAcronym(String token) {
        return token.length() <= 3 && token.equals(token.toUpperCase());
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        if (s.length() == 1) return s.toUpperCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    private String capitalizeHeaderWords(String header) {
    if (header == null || header.isEmpty()) return header;
    String[] words = header.split("\\s+");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < words.length; i++) {
        String w = words[i];
        if (w.isEmpty()) continue;
        char first = w.charAt(0);
        String rest = w.length() > 1 ? w.substring(1) : "";
        sb.append(Character.toUpperCase(first)).append(rest);
        if (i < words.length - 1) sb.append(' ');
    }
    return sb.toString();
}
}
package com.telkom.almBHZain.dto.Request;

import java.util.List;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

public class SearchRequest {

    /** Page number, 0-based */
    private Integer page;

    /** Page size */
    private Integer size;

    /** Free-text value to search */
    private String searchQuery;

    /** Column to search in — if null, searches ALL columns */
    private String searchColumn;

    /** Column-specific filters with operators */
    private List<FilterRequest> filterBy;

    /** Optional export format (if client wants format in payload) */
    private ExportFormat format;
    private String poNumber;

    // New: optional date range for filtering (yyyy-MM-dd)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }

    public String getSearchColumn() { return searchColumn; }
    public void setSearchColumn(String searchColumn) { this.searchColumn = searchColumn; }

    public List<FilterRequest> getFilterBy() { return filterBy; }
    public void setFilterBy(List<FilterRequest> filterBy) { this.filterBy = filterBy; }

    public ExportFormat getFormat() { return format; }
    public void setFormat(ExportFormat format) { this.format = format; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    /**
     * Return a string format value, falling back to the provided default (e.g. "excel").
     */
    public String getFormatOrDefault(String fallback) {
        return (format == null) ? fallback : format.getValue();
    }
}
package com.zain.jo.alm.pomanagement.service;

import com.zain.jo.alm.pomanagement.dto.request.SearchRequest;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Contract for streaming data exports (Excel / CSV).
 * Each method writes directly to the provided {@link OutputStream} so that
 * large result sets are streamed without loading the full dataset into memory.
 */
public interface ExportService {

    /**
     * Streams purchase orders matching the supplied search criteria.
     *
     * @param request search / filter parameters (may be empty, not null)
     * @param os      HTTP response output stream
     * @param format  {@code "excel"} or {@code "csv"}
     */
    void exportPurchaseOrders(SearchRequest request, OutputStream os, String format) throws IOException;

    /**
     * Streams PO items matching the supplied search criteria.
     *
     * @param request search / filter parameters (may be empty, not null)
     * @param os      HTTP response output stream
     * @param format  {@code "excel"} or {@code "csv"}
     */
    void exportPOItems(SearchRequest request, OutputStream os, String format) throws IOException;

    /**
     * Streams workflow records matching the supplied search criteria.
     *
     * @param request search / filter parameters (may be empty, not null)
     * @param os      HTTP response output stream
     * @param format  {@code "excel"} or {@code "csv"}
     */
    void exportWorkflows(SearchRequest request, OutputStream os, String format) throws IOException;
}
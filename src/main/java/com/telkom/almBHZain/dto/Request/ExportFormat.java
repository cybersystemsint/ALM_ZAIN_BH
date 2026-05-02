package com.telkom.almBHZain.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Allowed export formats. Keeps parsing robust and self-documenting.
 */
public enum ExportFormat {
    EXCEL("excel"),
    CSV("csv");

    private final String value;

    ExportFormat(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }

    @JsonCreator
    public static ExportFormat from(String v) {
        if (v == null) return EXCEL;
        String s = v.trim().toLowerCase();
        if ("csv".equals(s)) return CSV;
        return EXCEL;
    }
}
package com.telkom.almBHZain.dto.Request;

public class FilterRequest {
    /**
     * The entity field name (camelCase), e.g. "warehouseName", "status", "zone"
     */
    private String column;

    /**
     * Operator: CONTAINS, EQUALS, STARTS_WITH, ENDS_WITH, IS_EMPTY, IS_NOT_EMPTY, IS_ANY_OF
     */
    private FilterOperator operator;

    /**
     * Single value — used by CONTAINS, EQUALS, STARTS_WITH, ENDS_WITH
     */
    private String value;

    /**
     * Multi-value list — used by IS_ANY_OF
     */
    private java.util.List<String> values;

    public String getColumn() { return column; }
    public void setColumn(String column) { this.column = column; }

    public FilterOperator getOperator() { return operator; }
    public void setOperator(FilterOperator operator) { this.operator = operator; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public java.util.List<String> getValues() { return values; }
    public void setValues(java.util.List<String> values) { this.values = values; }
}
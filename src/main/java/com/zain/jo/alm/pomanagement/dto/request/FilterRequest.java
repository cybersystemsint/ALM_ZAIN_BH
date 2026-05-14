package com.zain.jo.alm.pomanagement.dto.request;

public class FilterRequest {

    private String column;
    private FilterOperator operator;
    private String value;
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
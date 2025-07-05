package com.telkom.almBHZain.dto;


public class SearchRequest {
 private String searchTerm;   
    private String searchQuery;  
    private String columnName;  
    private Integer page;
    private Long afterId;
    private Integer size;
    
    public String getSearchTerm() { return searchTerm; }
    public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }

    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }

    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Long getAfterId() { return afterId; }
    public void setAfterId(Long afterId) { this.afterId = afterId; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
}


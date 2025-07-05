package com.telkom.almBHZain.dto;

import java.util.List;

public class PageResult<T> {
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
    private List<T> data;

    
    public PageResult(long totalElements, int totalPages, int page, int size, List<T> data) {
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
        this.data = data;
    }
    // getters/setters
    public long getTotalElements() {
        return totalElements;
    }
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    public int getTotalPages() {
        return totalPages;
    }
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public List<T> getData() {
        return data;
    }
    public void setData(List<T> data) {
        this.data = data;
    }

}

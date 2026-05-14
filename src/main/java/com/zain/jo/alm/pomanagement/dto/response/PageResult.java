package com.zain.jo.alm.pomanagement.dto.response;

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

    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public List<T> getData() { return data; }
}
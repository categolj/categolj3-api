package com.example;

import java.util.List;

@lombok.Data
public class Page<T> {
    private List<T> content;
    private String sort;
    private long totalPages;
    private long totalElements;
    private boolean firstPage;
    private boolean lastPage;
    private long numberOfElements;
    private int size;
    private int number;
}
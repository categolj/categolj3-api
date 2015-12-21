package com.example;

import java.util.List;

@lombok.Data
public class Page<T> {
    private List<T> content;
    private String sort;
    private long totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private long numberOfElements;
    private int size;
    private int number;
}
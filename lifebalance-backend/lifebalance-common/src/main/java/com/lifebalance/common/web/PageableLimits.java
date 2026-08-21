package com.lifebalance.common.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableLimits {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 100;

    private PageableLimits() {
    }

    public static Pageable of(int page, int size) {
        return PageRequest.of(normalizePage(page), normalizeSize(size));
    }

    public static Pageable of(int page, int size, Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return of(page, size);
        }

        return PageRequest.of(normalizePage(page), normalizeSize(size), sort);
    }

    public static Pageable normalize(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return of(DEFAULT_PAGE, DEFAULT_SIZE);
        }

        int page = normalizePage(pageable.getPageNumber());
        int size = normalizeSize(pageable.getPageSize());
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(page, size);
        }

        return PageRequest.of(page, size, pageable.getSort());
    }

    private static int normalizePage(int page) {
        return Math.max(page, DEFAULT_PAGE);
    }

    private static int normalizeSize(int size) {
        if (size < 1) {
            return DEFAULT_SIZE;
        }

        return Math.min(size, MAX_SIZE);
    }
}

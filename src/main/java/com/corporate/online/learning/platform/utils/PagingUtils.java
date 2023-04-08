package com.corporate.online.learning.platform.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PagingUtils {

    public static Pageable getPaging(String sortBy, String sortMode, int pageNo, int pageSize) {
        Pageable paging;
        if (sortMode.equals("asc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
        } else {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        return paging;
    }

    public static Pageable getPaging(int pageNo, int pageSize) {
        return PageRequest.of(pageNo, pageSize);
    }
}

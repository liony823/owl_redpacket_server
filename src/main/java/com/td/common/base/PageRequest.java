package com.td.common.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest<T> {
    private int pageNumber;
    private int pageSize;
    private T data;
}

package com.hhk.pathfinderbacked.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private Long total;
    private Long pageNo;
    private Long pageSize;
    private List<T> records;
}

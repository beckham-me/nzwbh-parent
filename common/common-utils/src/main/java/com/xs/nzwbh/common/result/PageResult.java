package com.xs.nzwbh.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

@Data
public class PageResult<T> {
    private long total;
    private int pages;
    private int current;
    private int size;
    private T records;

    /**
     * 静态方法，用于将 MyBatis Plus 的 IPage 转换为 PageResult
     */
    public static <T> PageResult<T> from(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPages((int) page.getPages());
        result.setCurrent((int) page.getCurrent());
        result.setSize((int) page.getSize());
        result.setRecords((T) page.getRecords());
        return result;
    }
}

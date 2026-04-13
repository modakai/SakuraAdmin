package com.sakura.boot_init.support.common;

import com.sakura.boot_init.support.constant.CommonConstant;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页请求
 *
 * @author sakura
 * @from sakura
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    @Min(value = 1, message = "当前页号必须大于等于 1")
    private int current = 1;

    /**
     * 页面大小
     */
    @Min(value = 1, message = "页面大小必须大于等于 1")
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序，默认升序
     */
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;
}

package com.sakura.boot_init.web.dto.dict;

import lombok.Data;

import java.io.Serializable;

/**
 * 新增字典明细请求
 *
 * @author sakura
 */
@Data
public class DictItemAddRequest implements Serializable {

    /**
     * 字典类型 id
     */
    private Long dictTypeId;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典值
     */
    private String dictValue;

    /**
     * 排序值
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 标签类型
     */
    private String tagType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展 JSON
     */
    private String extJson;

    private static final long serialVersionUID = 1L;
}

package com.sakura.boot_init.web.dto.dict;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新字典类型请求
 *
 * @author sakura
 */
@Data
public class DictTypeUpdateRequest implements Serializable {

    /**
     * 主键 id
     */
    private Long id;

    /**
     * 字典编码
     */
    private String dictCode;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}

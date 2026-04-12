package com.sakura.boot_init.web.dto.dict;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 字典批量查询请求
 *
 * @author sakura
 */
@Data
public class DictBatchQueryRequest implements Serializable {

    /**
     * 字典编码列表
     */
    private List<String> dictCodes;

    private static final long serialVersionUID = 1L;
}

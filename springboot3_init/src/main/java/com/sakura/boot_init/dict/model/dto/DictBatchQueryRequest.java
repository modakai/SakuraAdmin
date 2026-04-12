package com.sakura.boot_init.dict.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 瀛楀吀鎵归噺鏌ヨ璇锋眰
 *
 * @author sakura
 */
@Data
public class DictBatchQueryRequest implements Serializable {

    /**
     * 瀛楀吀缂栫爜鍒楄〃
     */
    @NotEmpty(message = "瀛楀吀缂栫爜鍒楄〃涓嶈兘涓虹┖")
    private List<@NotBlank(message = "瀛楀吀缂栫爜涓嶈兘涓虹┖") String> dictCodes;

    private static final long serialVersionUID = 1L;
}



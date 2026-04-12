package com.sakura.boot_init.support.common;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

/**
 * 鍒犻櫎璇锋眰
 *
 * @author sakura
 * @from sakura
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    @NotNull(message = "id 涓嶈兘涓虹┖")
    @Positive(message = "id 蹇呴』澶т簬 0")
    private Long id;

    private static final long serialVersionUID = 1L;
}



package com.sakura.boot_init.support.common;

import com.sakura.boot_init.support.constant.CommonConstant;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 鍒嗛〉璇锋眰
 *
 * @author sakura
 * @from sakura
 */
@Data
public class PageRequest {

    /**
     * 褰撳墠椤靛彿
     */
    @Min(value = 1, message = "褰撳墠椤靛彿蹇呴』澶т簬绛変簬 1")
    private int current = 1;

    /**
     * 椤甸潰澶у皬
     */
    @Min(value = 1, message = "椤甸潰澶у皬蹇呴』澶т簬绛変簬 1")
    private int pageSize = 10;

    /**
     * 鎺掑簭瀛楁
     */
    private String sortField;

    /**
     * 鎺掑簭椤哄簭锛堥粯璁ゅ崌搴忥級
     */
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;
}




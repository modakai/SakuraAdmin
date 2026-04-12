package com.sakura.boot_init.support.util;

import org.apache.commons.lang3.StringUtils;

/**
 * SQL 宸ュ叿
 *
 * @author sakura
 * @from sakura
 */
public class SqlUtils {

    /**
     * 鏍￠獙鎺掑簭瀛楁鏄惁鍚堟硶锛堥槻姝?SQL 娉ㄥ叆锛?     *
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }
}



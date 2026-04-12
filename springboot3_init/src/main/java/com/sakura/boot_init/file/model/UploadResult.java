package com.sakura.boot_init.file.model;

import lombok.Builder;
import lombok.Data;

/**
 * 涓婁紶杩斿洖浣?
 *
 * @author sakura
 */
@Data
@Builder
public class UploadResult {

    /**
     * 鏂囦欢璺緞
     */
    private String url;

    /**
     * 鏂囦欢鍚?
     */
    private String filename;

    /**
     * 宸蹭笂浼犲璞＄殑瀹炰綋鏍囪锛堢敤鏉ユ牎楠屾枃浠讹級
     */
    private String eTag;

}


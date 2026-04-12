package com.sakura.boot_init.dict.service;

import com.sakura.boot_init.dict.model.vo.DictItemSimpleVO;

import java.util.List;
import java.util.Map;

/**
 * 瀛楀吀鏄犲皠鏈嶅姟
 *
 * @author sakura
 */
public interface DictMappingService {

    /**
     * 鏍规嵁瀛楀吀缂栫爜鑾峰彇鍚敤涓殑瀛楀吀椤?     *
     * @param dictCode 瀛楀吀缂栫爜
     * @return 瀛楀吀椤瑰垪琛?     */
    List<DictItemSimpleVO> getEnabledItemsByCode(String dictCode);

    /**
     * 鎵归噺鑾峰彇瀛楀吀鏄犲皠
     *
     * @param dictCodes 瀛楀吀缂栫爜鍒楄〃
     * @return 瀛楀吀鏄犲皠
     */
    Map<String, List<DictItemSimpleVO>> getEnabledItemMap(List<String> dictCodes);

    /**
     * 鏍规嵁缂栫爜鍜屽€艰幏鍙栨爣绛?     *
     * @param dictCode 瀛楀吀缂栫爜
     * @param value 瀛楀吀鍊?     * @return 鏍囩鏂囨湰
     */
    String getLabelByCodeAndValue(String dictCode, String value);
}




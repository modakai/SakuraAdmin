package com.sakura.boot_init.dict.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.sakura.boot_init.dict.model.entity.DictItem;
import com.sakura.boot_init.dict.model.dto.DictItemAddRequest;
import com.sakura.boot_init.dict.model.dto.DictItemQueryRequest;
import com.sakura.boot_init.dict.model.dto.DictItemUpdateRequest;
import com.sakura.boot_init.dict.model.vo.DictItemVO;

import java.util.List;

/**
 * 瀛楀吀鏄庣粏鏈嶅姟
 *
 * @author sakura
 */
public interface DictItemService extends IService<DictItem> {

    /**
     * 鏂板瀛楀吀鏄庣粏
     *
     * @param request 璇锋眰鍙傛暟
     * @return 鏂板璁板綍 id
     */
    Long addDictItem(DictItemAddRequest request);

    /**
     * 鏇存柊瀛楀吀鏄庣粏
     *
     * @param request 璇锋眰鍙傛暟
     * @return 鏄惁鎴愬姛
     */
    boolean updateDictItem(DictItemUpdateRequest request);

    /**
     * 鍒犻櫎瀛楀吀鏄庣粏
     *
     * @param id 鏄庣粏 id
     * @return 鏄惁鎴愬姛
     */
    boolean removeDictItem(Long id);

    /**
     * 鏋勯€犲垎椤垫煡璇㈡潯浠?     *
     * @param queryRequest 鏌ヨ鍙傛暟
     * @return 鏌ヨ鏉′欢
     */
    QueryWrapper getQueryWrapper(DictItemQueryRequest queryRequest);

    /**
     * 鑾峰彇鍙敤鏄庣粏鏁伴噺
     *
     * @param dictTypeId 瀛楀吀绫诲瀷 id
     * @return 鏁伴噺
     */
    long countEnabledByTypeId(Long dictTypeId);

    /**
     * 鑾峰彇鍙敤鏄庣粏鍒楄〃
     *
     * @param dictTypeId 瀛楀吀绫诲瀷 id
     * @return 鏄庣粏鍒楄〃
     */
    List<DictItem> listEnabledByTypeId(Long dictTypeId);

    /**
     * 鍒ゆ柇鍚岀被鍨嬩笅瀛楀吀鍊兼槸鍚﹀凡瀛樺湪
     *
     * @param dictTypeId 绫诲瀷 id
     * @param dictValue 瀛楀吀鍊?     * @param excludeId 鎺掗櫎 id
     * @return 鏄惁瀛樺湪
     */
    boolean existsByTypeAndValue(Long dictTypeId, String dictValue, Long excludeId);

    /**
     * 鍗曚釜杞?VO
     *
     * @param dictItem 瀛楀吀鏄庣粏
     * @return 杩斿洖瀵硅薄
     */
    DictItemVO getDictItemVO(DictItem dictItem);

    /**
     * 鍒楄〃杞?VO
     *
     * @param dictItemList 瀛楀吀鏄庣粏鍒楄〃
     * @return 杩斿洖瀵硅薄鍒楄〃
     */
    List<DictItemVO> getDictItemVO(List<DictItem> dictItemList);
}




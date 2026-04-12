package com.sakura.boot_init.dict.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.sakura.boot_init.dict.model.entity.DictType;
import com.sakura.boot_init.dict.model.dto.DictTypeAddRequest;
import com.sakura.boot_init.dict.model.dto.DictTypeQueryRequest;
import com.sakura.boot_init.dict.model.dto.DictTypeUpdateRequest;
import com.sakura.boot_init.dict.model.vo.DictTypeVO;

import java.util.List;

/**
 * 瀛楀吀绫诲瀷鏈嶅姟
 *
 * @author sakura
 */
public interface DictTypeService extends IService<DictType> {

    /**
     * 鏂板瀛楀吀绫诲瀷
     *
     * @param request 璇锋眰鍙傛暟
     * @return 鏂板璁板綍 id
     */
    Long addDictType(DictTypeAddRequest request);

    /**
     * 鏇存柊瀛楀吀绫诲瀷
     *
     * @param request 璇锋眰鍙傛暟
     * @return 鏄惁鎴愬姛
     */
    boolean updateDictType(DictTypeUpdateRequest request);

    /**
     * 鍒犻櫎瀛楀吀绫诲瀷
     *
     * @param id 绫诲瀷 id
     * @return 鏄惁鎴愬姛
     */
    boolean removeDictType(Long id);

    /**
     * 鏋勯€犲垎椤垫煡璇㈡潯浠?     *
     * @param queryRequest 鏌ヨ鍙傛暟
     * @return 鏌ヨ鏉′欢
     */
    QueryWrapper getQueryWrapper(DictTypeQueryRequest queryRequest);

    /**
     * 鍒ゆ柇瀛楀吀缂栫爜鏄惁宸插瓨鍦?     *
     * @param dictCode 瀛楀吀缂栫爜
     * @param excludeId 鎺掗櫎 id
     * @return 鏄惁瀛樺湪
     */
    boolean existsByDictCode(String dictCode, Long excludeId);

    /**
     * 鎸夌紪鐮佹煡璇㈠瓧鍏哥被鍨?     *
     * @param dictCode 瀛楀吀缂栫爜
     * @return 瀛楀吀绫诲瀷
     */
    DictType getByDictCode(String dictCode);

    /**
     * 鍗曚釜杞?VO
     *
     * @param dictType 瀛楀吀绫诲瀷
     * @return 杩斿洖瀵硅薄
     */
    DictTypeVO getDictTypeVO(DictType dictType);

    /**
     * 鍒楄〃杞?VO
     *
     * @param dictTypeList 瀛楀吀绫诲瀷鍒楄〃
     * @return 杩斿洖瀵硅薄鍒楄〃
     */
    List<DictTypeVO> getDictTypeVO(List<DictType> dictTypeList);
}




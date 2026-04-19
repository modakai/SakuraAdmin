package com.sakura.boot_init.system.service;

import com.sakura.boot_init.system.model.dto.SystemConfigAddRequest;
import com.sakura.boot_init.system.model.dto.SystemConfigUpdateRequest;
import com.sakura.boot_init.system.model.vo.SystemConfigVO;

/**
 * 系统配置服务。
 */
public interface SystemConfigService {

    /**
     * 根据配置键查询系统配置。
     *
     * @param key 配置键
     * @return 系统配置，不存在时返回 null
     */
    SystemConfigVO getByKey(String key);

    /**
     * 新增系统配置。
     *
     * @param request 新增请求
     * @return 是否成功
     */
    boolean addConfig(SystemConfigAddRequest request);

    /**
     * 更新系统配置。
     *
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateConfig(SystemConfigUpdateRequest request);
}

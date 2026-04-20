package com.sakura.boot_init.observability.controller.admin;

import com.sakura.boot_init.observability.model.vo.SystemStatusVO;
import com.sakura.boot_init.observability.service.SystemStatusService;
import com.sakura.boot_init.shared.annotation.AuthCheck;
import com.sakura.boot_init.shared.common.BaseResponse;
import com.sakura.boot_init.shared.common.ResultUtils;
import com.sakura.boot_init.shared.constant.UserConstant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端系统状态接口。
 *
 * @author Sakura
 */
@RestController
@RequestMapping("/admin/observability/status")
public class ObservabilityStatusController {

    /**
     * 系统状态服务。
     */
    private final SystemStatusService systemStatusService;

    public ObservabilityStatusController(SystemStatusService systemStatusService) {
        this.systemStatusService = systemStatusService;
    }

    /**
     * 获取系统状态聚合数据。
     */
    @GetMapping
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<SystemStatusVO> getSystemStatus() {
        return ResultUtils.success(systemStatusService.getSystemStatus());
    }
}

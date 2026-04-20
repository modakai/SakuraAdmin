package com.sakura.boot_init.observability.controller.admin;

import com.mybatisflex.core.paginate.Page;
import com.sakura.boot_init.observability.model.dto.ObservabilityEventQueryRequest;
import com.sakura.boot_init.observability.model.vo.ObservabilityEventVO;
import com.sakura.boot_init.observability.service.ObservabilityEventService;
import com.sakura.boot_init.shared.annotation.AuthCheck;
import com.sakura.boot_init.shared.common.BaseResponse;
import com.sakura.boot_init.shared.common.ResultUtils;
import com.sakura.boot_init.shared.constant.UserConstant;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端安全事件接口。
 *
 * @author Sakura
 */
@RestController
@RequestMapping("/admin/observability/security")
public class ObservabilitySecurityController {

    /**
     * 运维事件服务。
     */
    private final ObservabilityEventService eventService;

    public ObservabilitySecurityController(ObservabilityEventService eventService) {
        this.eventService = eventService;
    }

    /**
     * 分页查询安全事件。
     */
    @PostMapping("/events/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ObservabilityEventVO>> listSecurityEvents(
            @Valid @RequestBody ObservabilityEventQueryRequest request) {
        return ResultUtils.success(eventService.listSecurityEvents(request));
    }
}

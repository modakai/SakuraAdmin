package com.sakura.boot_init.observability.controller.admin;

import com.mybatisflex.core.paginate.Page;
import com.sakura.boot_init.observability.model.dto.ObservabilityEventQueryRequest;
import com.sakura.boot_init.observability.model.vo.ApiSummaryVO;
import com.sakura.boot_init.observability.model.vo.ErrorTrendBucketVO;
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

import java.util.List;

/**
 * 管理端接口监控接口。
 *
 * @author Sakura
 */
@RestController
@RequestMapping("/admin/observability/api")
public class ObservabilityApiMonitorController {

    /**
     * 运维事件服务。
     */
    private final ObservabilityEventService eventService;

    public ObservabilityApiMonitorController(ObservabilityEventService eventService) {
        this.eventService = eventService;
    }

    /**
     * 获取接口质量摘要。
     */
    @PostMapping("/summary")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<ApiSummaryVO> getSummary(@Valid @RequestBody ObservabilityEventQueryRequest request) {
        return ResultUtils.success(eventService.getApiSummary(request));
    }

    /**
     * 分页查询慢接口。
     */
    @PostMapping("/slow/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ObservabilityEventVO>> listSlowApis(@Valid @RequestBody ObservabilityEventQueryRequest request) {
        return ResultUtils.success(eventService.listSlowApiEvents(request));
    }

    /**
     * 查询错误趋势。
     */
    @PostMapping("/errors/trend")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<ErrorTrendBucketVO>> listErrorTrend(@Valid @RequestBody ObservabilityEventQueryRequest request) {
        return ResultUtils.success(eventService.listErrorTrend(request));
    }
}

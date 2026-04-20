package com.sakura.boot_init.observability.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.sakura.boot_init.observability.config.ObservabilityProperties;
import com.sakura.boot_init.observability.enums.ObservabilityEventTypeEnum;
import com.sakura.boot_init.observability.model.dto.ObservabilityEventQueryRequest;
import com.sakura.boot_init.observability.model.dto.RequestObservationCommand;
import com.sakura.boot_init.observability.model.entity.ObservabilityEvent;
import com.sakura.boot_init.observability.model.vo.ApiSummaryVO;
import com.sakura.boot_init.observability.model.vo.ErrorTrendBucketVO;
import com.sakura.boot_init.observability.model.vo.ObservabilityEventVO;
import com.sakura.boot_init.observability.repository.ObservabilityEventMapper;
import com.sakura.boot_init.observability.service.ObservabilityEventService;
import com.sakura.boot_init.observability.support.ObservabilitySanitizer;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 运维观测事件服务实现。
 *
 * @author Sakura
 */
@Service
@Slf4j
public class ObservabilityEventServiceImpl extends ServiceImpl<ObservabilityEventMapper, ObservabilityEvent>
        implements ObservabilityEventService {

    /**
     * 异步写入执行器，避免观测事件影响主请求。
     */
    private final Executor eventExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "observability-event-writer");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * 运维事件 Mapper。
     */
    private final ObservabilityEventMapper eventMapper;

    /**
     * 可观测性配置。
     */
    private final ObservabilityProperties properties;

    /**
     * 摘要脱敏工具。
     */
    private final ObservabilitySanitizer sanitizer = new ObservabilitySanitizer();

    public ObservabilityEventServiceImpl(ObservabilityEventMapper eventMapper, ObservabilityProperties properties) {
        this.eventMapper = eventMapper;
        this.properties = properties;
    }

    @Override
    public void recordRequest(RequestObservationCommand command) {
        if (command == null || StringUtils.isBlank(command.getRequestPath())) {
            return;
        }
        if (isSlowRequest(command)) {
            saveSafely(buildRequestEvent(command, ObservabilityEventTypeEnum.SLOW_API));
        }
        if (isErrorRequest(command)) {
            saveSafely(buildRequestEvent(command, ObservabilityEventTypeEnum.API_ERROR));
        }
    }

    @Override
    public ObservabilityEvent saveEvent(ObservabilityEvent event) {
        ThrowUtils.throwIf(event == null, ErrorCode.PARAMS_ERROR);
        if (event.getEventTime() == null) {
            event.setEventTime(new Date());
        }
        event.setExceptionSummary(sanitizer.sanitize(event.getExceptionSummary()));
        event.setDetail(sanitizer.sanitize(event.getDetail()));
        eventMapper.insertSelective(event);
        return event;
    }

    @Override
    public Page<ObservabilityEventVO> listSlowApiEvents(ObservabilityEventQueryRequest request) {
        ObservabilityEventQueryRequest query = request == null ? new ObservabilityEventQueryRequest() : request;
        QueryWrapper wrapper = getBaseQueryWrapper(query)
                .eq("event_type", ObservabilityEventTypeEnum.SLOW_API.getValue())
                .orderBy("event_time", false)
                .orderBy("duration_millis", false);
        Page<ObservabilityEvent> page = this.page(Page.of(query.getPage(), query.getPageSize()), wrapper);
        Page<ObservabilityEventVO> voPage = new Page<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow());
        voPage.setRecords(page.getRecords().stream().map(this::getEventVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Page<ObservabilityEventVO> listSecurityEvents(ObservabilityEventQueryRequest request) {
        ObservabilityEventQueryRequest query = request == null ? new ObservabilityEventQueryRequest() : request;
        QueryWrapper wrapper = getBaseQueryWrapper(query)
                .in("event_type", List.of(
                        ObservabilityEventTypeEnum.LOGIN_FAILURE.getValue(),
                        ObservabilityEventTypeEnum.ABNORMAL_IP.getValue(),
                        ObservabilityEventTypeEnum.FORCE_LOGOUT.getValue(),
                        ObservabilityEventTypeEnum.SECURITY_ALERT.getValue()))
                .orderBy("event_time", false);
        Page<ObservabilityEvent> page = this.page(Page.of(query.getPage(), query.getPageSize()), wrapper);
        Page<ObservabilityEventVO> voPage = new Page<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow());
        voPage.setRecords(page.getRecords().stream().map(this::getEventVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public ApiSummaryVO getApiSummary(ObservabilityEventQueryRequest request) {
        QueryWrapper wrapper = getBaseQueryWrapper(request == null ? new ObservabilityEventQueryRequest() : request);
        List<ObservabilityEvent> events = this.list(wrapper.in("event_type", List.of(
                ObservabilityEventTypeEnum.SLOW_API.getValue(), ObservabilityEventTypeEnum.API_ERROR.getValue())));
        ApiSummaryVO vo = new ApiSummaryVO();
        vo.setSlowApiCount(events.stream()
                .filter(event -> ObservabilityEventTypeEnum.SLOW_API.getValue().equals(event.getEventType()))
                .count());
        vo.setErrorCount(events.stream()
                .filter(event -> ObservabilityEventTypeEnum.API_ERROR.getValue().equals(event.getEventType()))
                .count());
        vo.setAverageSlowDurationMillis(events.stream()
                .filter(event -> ObservabilityEventTypeEnum.SLOW_API.getValue().equals(event.getEventType()))
                .map(ObservabilityEvent::getDurationMillis)
                .filter(value -> value != null && value > 0)
                .mapToLong(Long::longValue)
                .average()
                .orElse(0));
        return vo;
    }

    @Override
    public List<ErrorTrendBucketVO> listErrorTrend(ObservabilityEventQueryRequest request) {
        QueryWrapper wrapper = getBaseQueryWrapper(request == null ? new ObservabilityEventQueryRequest() : request)
                .eq("event_type", ObservabilityEventTypeEnum.API_ERROR.getValue())
                .orderBy("event_time", true);
        Map<String, ErrorTrendBucketVO> bucketMap = new LinkedHashMap<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:00");
        for (ObservabilityEvent event : this.list(wrapper)) {
            String bucket = formatter.format(event.getEventTime() == null ? new Date() : event.getEventTime());
            ErrorTrendBucketVO vo = bucketMap.computeIfAbsent(bucket, key -> {
                ErrorTrendBucketVO item = new ErrorTrendBucketVO();
                item.setBucket(key);
                return item;
            });
            if (event.getStatusCode() != null && event.getStatusCode() >= 500) {
                vo.setServerErrorCount(vo.getServerErrorCount() + 1);
            } else if (event.getStatusCode() != null && event.getStatusCode() >= 400) {
                vo.setClientErrorCount(vo.getClientErrorCount() + 1);
            }
            if (StringUtils.isNotBlank(event.getExceptionSummary())) {
                vo.setExceptionCount(vo.getExceptionCount() + 1);
            }
        }
        return new ArrayList<>(bucketMap.values());
    }

    @Override
    public ObservabilityEventVO getEventVO(ObservabilityEvent event) {
        if (event == null) {
            return null;
        }
        ObservabilityEventVO vo = new ObservabilityEventVO();
        BeanUtils.copyProperties(event, vo);
        return vo;
    }

    /**
     * 构造通用查询条件。
     */
    private QueryWrapper getBaseQueryWrapper(ObservabilityEventQueryRequest request) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.eq("event_type", request.getEventType(), StringUtils.isNotBlank(request.getEventType()));
        wrapper.eq("event_level", request.getEventLevel(), StringUtils.isNotBlank(request.getEventLevel()));
        wrapper.like("request_path", request.getRequestPath(), StringUtils.isNotBlank(request.getRequestPath()));
        wrapper.eq("ip_address", request.getIpAddress(), StringUtils.isNotBlank(request.getIpAddress()));
        wrapper.like("account_identifier", request.getAccountIdentifier(), StringUtils.isNotBlank(request.getAccountIdentifier()));
        wrapper.ge("event_time", request.getStartTime(), request.getStartTime() != null);
        wrapper.le("event_time", request.getEndTime(), request.getEndTime() != null);
        return wrapper;
    }

    /**
     * 判断是否为慢接口。
     */
    private boolean isSlowRequest(RequestObservationCommand command) {
        return command.getDurationMillis() != null
                && command.getDurationMillis() >= properties.getSlowApiThresholdMillis();
    }

    /**
     * 判断是否为错误请求。
     */
    private boolean isErrorRequest(RequestObservationCommand command) {
        return command.getThrowable() != null
                || command.getStatusCode() != null && command.getStatusCode() >= 400;
    }

    /**
     * 构造请求事件。
     */
    private ObservabilityEvent buildRequestEvent(RequestObservationCommand command, ObservabilityEventTypeEnum type) {
        ObservabilityEvent event = new ObservabilityEvent();
        ObservabilityEventTypeEnum resolvedType = resolveRequestEventType(command, type);
        event.setEventType(resolvedType.getValue());
        event.setEventLevel(ObservabilityEventTypeEnum.API_ERROR == resolvedType ? "error" : "warning");
        event.setTitle(resolveRequestEventTitle(resolvedType));
        event.setSubject(command.getRequestPath());
        event.setRequestPath(command.getRequestPath());
        event.setHttpMethod(command.getHttpMethod());
        event.setStatusCode(command.getStatusCode());
        event.setDurationMillis(command.getDurationMillis());
        event.setUserId(command.getUserId());
        event.setAccountIdentifier(command.getAccountIdentifier());
        event.setIpAddress(command.getIpAddress());
        event.setExceptionSummary(buildExceptionSummary(command.getThrowable()));
        event.setEventTime(command.getEventTime() == null ? new Date() : command.getEventTime());
        return event;
    }

    /**
     * 强制下线接口单独归类为安全事件。
     */
    private ObservabilityEventTypeEnum resolveRequestEventType(RequestObservationCommand command,
            ObservabilityEventTypeEnum type) {
        if (StringUtils.contains(command.getRequestPath(), "/online/user/force-logout")) {
            return ObservabilityEventTypeEnum.FORCE_LOGOUT;
        }
        return type;
    }

    /**
     * 获取请求事件标题。
     */
    private String resolveRequestEventTitle(ObservabilityEventTypeEnum type) {
        if (ObservabilityEventTypeEnum.API_ERROR == type) {
            return "接口错误";
        }
        if (ObservabilityEventTypeEnum.FORCE_LOGOUT == type) {
            return "强制下线";
        }
        return "慢接口";
    }

    /**
     * 异步安全写入事件。
     */
    private void saveSafely(ObservabilityEvent event) {
        eventExecutor.execute(() -> {
            try {
                saveEvent(event);
            } catch (Exception e) {
                log.error("save observability event failed", e);
            }
        });
    }

    /**
     * 构造异常摘要，避免保存完整堆栈。
     */
    private String buildExceptionSummary(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        return sanitizer.sanitize(throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
    }
}

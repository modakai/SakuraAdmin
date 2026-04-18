package com.sakura.boot_init.notification.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.sakura.boot_init.notification.model.dto.NotificationTemplateAddRequest;
import com.sakura.boot_init.notification.model.dto.NotificationTemplateQueryRequest;
import com.sakura.boot_init.notification.model.dto.NotificationTemplateUpdateRequest;
import com.sakura.boot_init.notification.model.entity.NotificationTemplate;
import com.sakura.boot_init.notification.model.vo.NotificationTemplateVO;
import com.sakura.boot_init.notification.repository.NotificationTemplateMapper;
import com.sakura.boot_init.notification.service.NotificationTemplateService;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.exception.BusinessException;
import com.sakura.boot_init.support.exception.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息通知模板服务实现。
 *
 * @author Sakura
 */
@Service
public class NotificationTemplateServiceImpl extends ServiceImpl<NotificationTemplateMapper, NotificationTemplate>
        implements NotificationTemplateService {

    @Resource
    private NotificationTemplateRenderer templateRenderer;

    @Override
    public Long addTemplate(NotificationTemplateAddRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        NotificationTemplate template = new NotificationTemplate();
        BeanUtils.copyProperties(request, template);
        validTemplate(template);
        if (template.getEnabled() == null) {
            template.setEnabled(0);
        }
        boolean result = this.save(template);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return template.getId();
    }

    @Override
    public boolean updateTemplate(NotificationTemplateUpdateRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null || request.getId() <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(this.getById(request.getId()) == null, ErrorCode.NOT_FOUND_ERROR);
        NotificationTemplate template = new NotificationTemplate();
        BeanUtils.copyProperties(request, template);
        validTemplate(template);
        return this.updateById(template);
    }

    @Override
    public boolean enableTemplate(Long id) {
        return updateEnabled(id, 1);
    }

    @Override
    public boolean disableTemplate(Long id) {
        return updateEnabled(id, 0);
    }

    @Override
    public NotificationTemplate getEnabledByEventType(String eventType) {
        if (StringUtils.isBlank(eventType)) {
            return null;
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("event_type", eventType)
                .eq("enabled", 1)
                .orderBy("id", false);
        return this.getOne(queryWrapper);
    }

    @Override
    public QueryWrapper getQueryWrapper(NotificationTemplateQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.like("template_code", request.getTemplateCode(), StringUtils.isNotBlank(request.getTemplateCode()));
        queryWrapper.eq("event_type", request.getEventType(), StringUtils.isNotBlank(request.getEventType()));
        queryWrapper.eq("enabled", request.getEnabled(), request.getEnabled() != null);
        queryWrapper.orderBy("id", false);
        return queryWrapper;
    }

    @Override
    public NotificationTemplateVO getTemplateVO(NotificationTemplate template) {
        if (template == null) {
            return null;
        }
        NotificationTemplateVO vo = new NotificationTemplateVO();
        BeanUtils.copyProperties(template, vo);
        return vo;
    }

    @Override
    public List<NotificationTemplateVO> getTemplateVO(List<NotificationTemplate> templateList) {
        if (CollUtil.isEmpty(templateList)) {
            return new ArrayList<>();
        }
        return templateList.stream().map(this::getTemplateVO).collect(Collectors.toList());
    }

    /**
     * 更新模板启用状态。
     */
    private boolean updateEnabled(Long id, Integer enabled) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(this.getById(id) == null, ErrorCode.NOT_FOUND_ERROR);
        NotificationTemplate template = new NotificationTemplate();
        template.setId(id);
        template.setEnabled(enabled);
        return this.updateById(template);
    }

    /**
     * 校验模板参数。
     */
    private void validTemplate(NotificationTemplate template) {
        if (StringUtils.isAnyBlank(template.getTemplateCode(), template.getEventType(),
                template.getTitleTemplate(), template.getContentTemplate(), template.getReceiverType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        templateRenderer.validateTemplateVariables(template.getTitleTemplate(), template.getVariableSchema());
        templateRenderer.validateTemplateVariables(template.getContentTemplate(), template.getVariableSchema());
    }
}

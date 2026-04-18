package com.sakura.boot_init.notification.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.sakura.boot_init.notification.enums.NotificationReceiverTypeEnum;
import com.sakura.boot_init.notification.enums.NotificationStatusEnum;
import com.sakura.boot_init.notification.enums.NotificationTargetTypeEnum;
import com.sakura.boot_init.notification.enums.NotificationTypeEnum;
import com.sakura.boot_init.notification.model.dto.NotificationAddRequest;
import com.sakura.boot_init.notification.model.dto.NotificationAutoSendRequest;
import com.sakura.boot_init.notification.model.dto.NotificationQueryRequest;
import com.sakura.boot_init.notification.model.dto.NotificationTargetContext;
import com.sakura.boot_init.notification.model.dto.NotificationUpdateRequest;
import com.sakura.boot_init.notification.model.entity.Notification;
import com.sakura.boot_init.notification.model.entity.NotificationTemplate;
import com.sakura.boot_init.notification.model.vo.NotificationVO;
import com.sakura.boot_init.notification.repository.NotificationMapper;
import com.sakura.boot_init.notification.service.NotificationReadService;
import com.sakura.boot_init.notification.service.NotificationService;
import com.sakura.boot_init.notification.service.NotificationTargetService;
import com.sakura.boot_init.notification.service.NotificationTemplateService;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.constant.CommonConstant;
import com.sakura.boot_init.support.exception.BusinessException;
import com.sakura.boot_init.support.exception.ThrowUtils;
import com.sakura.boot_init.support.util.SqlUtils;
import com.sakura.boot_init.user.model.entity.User;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知公告服务实现。
 *
 * @author Sakura
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Resource
    private NotificationTargetService notificationTargetService;

    @Resource
    private NotificationReadService notificationReadService;

    @Resource
    private NotificationTemplateService notificationTemplateService;

    @Resource
    private NotificationTemplateRenderer templateRenderer;

    @Resource
    private NotificationDomainServiceImpl domainService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addNotification(NotificationAddRequest request, User operator) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Notification notification = new Notification();
        BeanUtils.copyProperties(request, notification);
        notification.setStatus(NotificationStatusEnum.DRAFT.getValue());
        if (operator != null) {
            notification.setCreateUserId(operator.getId());
            notification.setUpdateUserId(operator.getId());
        }
        fillDefaultDisplay(notification);
        validNotification(notification);
        boolean result = this.save(notification);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        notificationTargetService.replaceTargets(notification.getId(), notification.getTargetType(),
                request.getTargetRoles(), request.getTargetUserIds());
        return notification.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNotification(NotificationUpdateRequest request, User operator) {
        ThrowUtils.throwIf(request == null || request.getId() == null || request.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Notification oldNotification = this.getById(request.getId());
        ThrowUtils.throwIf(oldNotification == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!NotificationStatusEnum.DRAFT.getValue().equals(oldNotification.getStatus()),
                ErrorCode.OPERATION_ERROR, "notification.only_draft_can_update");
        Notification notification = new Notification();
        BeanUtils.copyProperties(request, notification);
        if (operator != null) {
            notification.setUpdateUserId(operator.getId());
        }
        fillDefaultDisplay(notification);
        validNotification(notification);
        boolean result = this.updateById(notification);
        notificationTargetService.replaceTargets(notification.getId(), notification.getTargetType(),
                request.getTargetRoles(), request.getTargetUserIds());
        return result;
    }

    @Override
    public boolean publishNotification(Long id, User operator) {
        Notification notification = assertNotificationExists(id);
        domainService.assertCanPublish(notification);
        Notification update = new Notification();
        update.setId(id);
        update.setStatus(NotificationStatusEnum.PUBLISHED.getValue());
        update.setPublishTime(new Date());
        update.setPublisherId(operator == null ? null : operator.getId());
        update.setUpdateUserId(operator == null ? null : operator.getId());
        return this.updateById(update);
    }

    @Override
    public boolean revokeNotification(Long id, User operator) {
        Notification notification = assertNotificationExists(id);
        ThrowUtils.throwIf(!NotificationStatusEnum.PUBLISHED.getValue().equals(notification.getStatus()),
                ErrorCode.OPERATION_ERROR, "notification.only_published_can_revoke");
        Notification update = new Notification();
        update.setId(id);
        update.setStatus(NotificationStatusEnum.REVOKED.getValue());
        update.setUpdateUserId(operator == null ? null : operator.getId());
        return this.updateById(update);
    }

    @Override
    public boolean archiveNotification(Long id, User operator) {
        assertNotificationExists(id);
        Notification update = new Notification();
        update.setId(id);
        update.setStatus(NotificationStatusEnum.ARCHIVED.getValue());
        update.setUpdateUserId(operator == null ? null : operator.getId());
        return this.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendByTemplate(NotificationAutoSendRequest request) {
        ThrowUtils.throwIf(request == null || request.getTargetUserId() == null, ErrorCode.PARAMS_ERROR);
        NotificationTemplate template = notificationTemplateService.getEnabledByEventType(request.getEventType());
        if (template == null) {
            return null;
        }
        String title = templateRenderer.render(template.getTitleTemplate(), template.getVariableSchema(), request.getVariables());
        String content = templateRenderer.render(template.getContentTemplate(), template.getVariableSchema(), request.getVariables());
        NotificationAddRequest addRequest = new NotificationAddRequest();
        addRequest.setType(NotificationTypeEnum.MESSAGE.getValue());
        addRequest.setTitle(title);
        addRequest.setContent(content);
        addRequest.setLevel("info");
        addRequest.setReceiverType(StringUtils.defaultIfBlank(request.getReceiverType(), template.getReceiverType()));
        addRequest.setTargetType(NotificationTargetTypeEnum.USER.getValue());
        addRequest.setTargetUserIds(List.of(request.getTargetUserId()));
        Long notificationId = addNotification(addRequest, null);
        publishNotification(notificationId, null);
        return notificationId;
    }

    @Override
    public QueryWrapper getQueryWrapper(NotificationQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq("id", request.getId(), request.getId() != null);
        queryWrapper.eq("type", request.getType(), StringUtils.isNotBlank(request.getType()));
        queryWrapper.like("title", request.getTitle(), StringUtils.isNotBlank(request.getTitle()));
        queryWrapper.eq("status", request.getStatus(), StringUtils.isNotBlank(request.getStatus()));
        queryWrapper.eq("receiver_type", request.getReceiverType(), StringUtils.isNotBlank(request.getReceiverType()));
        queryWrapper.eq("target_type", request.getTargetType(), StringUtils.isNotBlank(request.getTargetType()));
        queryWrapper.ge("publish_time", request.getPublishStartTime(), request.getPublishStartTime() != null);
        queryWrapper.le("publish_time", request.getPublishEndTime(), request.getPublishEndTime() != null);
        if (SqlUtils.validSortField(request.getSortField())) {
            queryWrapper.orderBy(request.getSortField(), CommonConstant.SORT_ORDER_ASC.equals(request.getSortOrder()));
        } else {
            queryWrapper.orderBy("id", false);
        }
        return queryWrapper;
    }

    @Override
    public List<NotificationVO> listVisibleNotifications(String receiverType, User user, String type) {
        ThrowUtils.throwIf(user == null || user.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("status", NotificationStatusEnum.PUBLISHED.getValue())
                .eq("type", type, StringUtils.isNotBlank(type))
                .orderBy("pinned", false)
                .orderBy("publish_time", false)
                .orderBy("id", false);
        Date now = new Date();
        return this.list(queryWrapper).stream()
                .filter(notification -> NotificationReceiverTypeEnum.matches(notification.getReceiverType(), receiverType))
                .filter(notification -> notification.getEffectiveTime() == null || !notification.getEffectiveTime().after(now))
                .filter(notification -> notification.getExpireTime() == null || !notification.getExpireTime().before(now))
                .filter(notification -> isVisible(notification, receiverType, user))
                .filter(notification -> !NotificationTypeEnum.ANNOUNCEMENT.getValue().equals(notification.getType())
                        || !notificationReadService.isClosed(notification.getId(), receiverType, user.getId()))
                .map(notification -> {
                    NotificationVO vo = getNotificationVO(notification);
                    vo.setRead(notificationReadService.isRead(notification.getId(), receiverType, user.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public long countUnreadMessages(String receiverType, User user) {
        return listVisibleNotifications(receiverType, user, NotificationTypeEnum.MESSAGE.getValue()).stream()
                .filter(vo -> !Boolean.TRUE.equals(vo.getRead()))
                .count();
    }

    @Override
    public boolean markRead(Long notificationId, String receiverType, User user) {
        Notification notification = assertNotificationExists(notificationId);
        ThrowUtils.throwIf(!isVisible(notification, receiverType, user), ErrorCode.NO_AUTH_ERROR);
        notificationReadService.markRead(notificationId, receiverType, user.getId());
        return true;
    }

    @Override
    public boolean markAllRead(String receiverType, User user) {
        for (NotificationVO vo : listVisibleNotifications(receiverType, user, NotificationTypeEnum.MESSAGE.getValue())) {
            notificationReadService.markRead(vo.getId(), receiverType, user.getId());
        }
        return true;
    }

    @Override
    public boolean closeAnnouncement(Long notificationId, String receiverType, User user) {
        Notification notification = assertNotificationExists(notificationId);
        ThrowUtils.throwIf(!NotificationTypeEnum.ANNOUNCEMENT.getValue().equals(notification.getType()), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(!isVisible(notification, receiverType, user), ErrorCode.NO_AUTH_ERROR);
        notificationReadService.markClosed(notificationId, receiverType, user.getId());
        return true;
    }

    @Override
    public NotificationVO getNotificationVO(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationVO vo = new NotificationVO();
        BeanUtils.copyProperties(notification, vo);
        vo.setTargetRoles(notificationTargetService.listRoleTargets(notification.getId()));
        vo.setTargetUserIds(notificationTargetService.listUserTargets(notification.getId()));
        return vo;
    }

    @Override
    public List<NotificationVO> getNotificationVO(List<Notification> notificationList) {
        if (CollUtil.isEmpty(notificationList)) {
            return new ArrayList<>();
        }
        return notificationList.stream().map(this::getNotificationVO).collect(Collectors.toList());
    }

    /**
     * 判断通知是否对用户可见。
     */
    private boolean isVisible(Notification notification, String receiverType, User user) {
        return domainService.isVisibleTo(notification, new NotificationTargetContext(receiverType, user),
                notificationTargetService.listRoleTargets(notification.getId()),
                notificationTargetService.listUserTargets(notification.getId()));
    }

    /**
     * 断言通知存在。
     */
    private Notification assertNotificationExists(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        Notification notification = this.getById(id);
        ThrowUtils.throwIf(notification == null, ErrorCode.NOT_FOUND_ERROR);
        return notification;
    }

    /**
     * 填充展示配置默认值。
     */
    private void fillDefaultDisplay(Notification notification) {
        if (notification.getPinned() == null) {
            notification.setPinned(0);
        }
        if (notification.getPopup() == null) {
            notification.setPopup(0);
        }
        if (StringUtils.isBlank(notification.getLevel())) {
            notification.setLevel("info");
        }
    }

    /**
     * 校验通知公告参数。
     */
    private void validNotification(Notification notification) {
        if (StringUtils.isAnyBlank(notification.getType(), notification.getTitle(), notification.getContent(),
                notification.getReceiverType(), notification.getTargetType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }
}

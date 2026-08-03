package com.sakura.boot_init.rbac.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.sakura.boot_init.rbac.model.entity.SysPermission;
import com.sakura.boot_init.rbac.model.vo.PermissionNodeVO;
import com.sakura.boot_init.rbac.model.vo.UserPermission;
import com.sakura.boot_init.rbac.repository.SysPermissionMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sakura.boot_init.rbac.model.entity.table.SysPermissionTableDef.SYS_PERMISSION;

/**
 * 权限点树构建服务：根据用户权限快照组装菜单树（含按钮节点）。
 *
 * <p>超管返回全部启用的权限点；普通用户仅保留权限码在权限集合内的节点。
 * 无权限码的目录节点（如系统管理）仅当其下存在可见子节点时保留。
 *
 * @author sakura
 */
@Service
public class PermissionTreeService {

    /**
     * 根节点 parentId。
     */
    private static final long ROOT_PARENT_ID = 0L;

    private final SysPermissionMapper sysPermissionMapper;
    private final PermissionQueryService permissionQueryService;

    public PermissionTreeService(SysPermissionMapper sysPermissionMapper,
            PermissionQueryService permissionQueryService) {
        this.sysPermissionMapper = sysPermissionMapper;
        this.permissionQueryService = permissionQueryService;
    }

    /**
     * 构建当前用户的权限点树。
     *
     * @param userId 用户 id
     * @return 权限点树
     */
    public List<PermissionNodeVO> buildTreeForUser(Long userId) {
        return buildTreeForPermission(permissionQueryService.loadUserPermission(userId));
    }

    /**
     * 基于已加载的用户权限快照构建权限点树，避免重复查询。
     *
     * @param permission 用户权限快照
     * @return 权限点树
     */
    public List<PermissionNodeVO> buildTreeForPermission(UserPermission permission) {
        return buildTree(permission, selectAllEnabled());
    }

    /**
     * 构建全部启用的权限点树（管理端分配权限使用）。
     *
     * @return 权限点树
     */
    public List<PermissionNodeVO> buildFullTree() {
        return buildTree(new UserPermission(List.of(), Set.of(), true), selectAllEnabled());
    }

    /**
     * 查询全部启用的权限点。
     *
     * @return 权限点列表
     */
    private List<SysPermission> selectAllEnabled() {
        return sysPermissionMapper.selectListByQuery(
                QueryWrapper.create().where(SYS_PERMISSION.STATUS.eq(1)).and(SYS_PERMISSION.IS_DELETE.eq(0)));
    }

    /**
     * 纯逻辑构建权限点树（可按权限过滤、剔除空目录、按排序组装）。
     *
     * @param permission 用户权限快照
     * @param allEnabled 全部启用的权限点
     * @return 权限点树
     */
    public List<PermissionNodeVO> buildTree(UserPermission permission, List<SysPermission> allEnabled) {
        boolean superadmin = permission.superadmin();
        List<SysPermission> visible = allEnabled.stream()
                .filter(node -> superadmin
                        || StringUtils.isBlank(node.getPermissionCode())
                        || permission.permissions().contains(node.getPermissionCode()))
                .collect(Collectors.toList());

        Map<Long, List<SysPermission>> byParent = visible.stream()
                .collect(Collectors.groupingBy(node -> node.getParentId() == null ? ROOT_PARENT_ID : node.getParentId()));

        List<PermissionNodeVO> tree = buildChildren(ROOT_PARENT_ID, byParent);
        return pruneEmptyDirectories(tree);
    }

    /**
     * 递归组装某父节点下的子节点，按排序值升序。
     *
     * @param parentId 父节点 id
     * @param byParent 按父节点分组的权限点
     * @return 子节点列表
     */
    private List<PermissionNodeVO> buildChildren(Long parentId, Map<Long, List<SysPermission>> byParent) {
        List<SysPermission> children = byParent.getOrDefault(parentId, List.of());
        return children.stream()
                .sorted(Comparator.comparing(node -> node.getSortOrder() == null ? 0 : node.getSortOrder()))
                .map(node -> {
                    PermissionNodeVO vo = new PermissionNodeVO();
                    vo.setId(node.getId());
                    vo.setParentId(node.getParentId());
                    vo.setType(node.getType());
                    vo.setTitle(node.getTitle());
                    vo.setPermissionCode(node.getPermissionCode());
                    vo.setPath(node.getPath());
                    vo.setComponent(node.getComponent());
                    vo.setIcon(node.getIcon());
                    vo.setSortOrder(node.getSortOrder());
                    vo.setChildren(buildChildren(node.getId(), byParent));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 剔除无可见子节点的目录节点（menu 类型且无权限码）。
     *
     * @param nodes 待处理的节点列表
     * @return 裁剪后的节点列表
     */
    private List<PermissionNodeVO> pruneEmptyDirectories(List<PermissionNodeVO> nodes) {
        List<PermissionNodeVO> pruned = new ArrayList<>();
        for (PermissionNodeVO node : nodes) {
            node.setChildren(pruneEmptyDirectories(node.getChildren()));
            boolean isDirectory = "menu".equals(node.getType()) && StringUtils.isBlank(node.getPermissionCode());
            if (isDirectory && node.getChildren().isEmpty()) {
                continue;
            }
            pruned.add(node);
        }
        return pruned;
    }
}

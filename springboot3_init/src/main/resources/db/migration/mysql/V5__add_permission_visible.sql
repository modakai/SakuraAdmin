-- 权限点新增「是否显示」维度：与 status（启用/禁用）正交。隐藏菜单仍在树中，由前端侧边栏渲染时裁剪。
alter table `sys_permission`
    add column `visible` tinyint default 1 not null comment '是否显示：1显示 0隐藏（仅menu生效）' after `status`;

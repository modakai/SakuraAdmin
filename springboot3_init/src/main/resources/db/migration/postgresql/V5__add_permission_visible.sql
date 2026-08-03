-- 权限点新增「是否显示」维度：与 status（启用/禁用）正交。隐藏菜单仍在树中，由前端侧边栏渲染时裁剪。
alter table public.sys_permission
    add column visible smallint not null default 1;

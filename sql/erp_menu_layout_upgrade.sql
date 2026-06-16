-- ERP菜单分组优化：基础资料、订单管理、库存管理、生产管理
-- 适用于已经导入旧版ERP菜单的数据库；不会修改业务按钮权限，仅调整菜单层级。

insert into sys_menu
select 3020, '基础资料', 3000, 1, 'basic', null, '', '', 1, 0, 'M', '0', '0', '', 'dict', 'admin', sysdate(), '', null, 'ERP基础资料目录'
where not exists (select 1 from sys_menu where menu_id = 3020);

insert into sys_menu
select 3021, '订单管理', 3000, 2, 'order', null, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '', null, 'ERP订单管理目录'
where not exists (select 1 from sys_menu where menu_id = 3021);

insert into sys_menu
select 3022, '库存管理', 3000, 3, 'stock', null, '', '', 1, 0, 'M', '0', '0', '', 'warehouse', 'admin', sysdate(), '', null, 'ERP库存管理目录'
where not exists (select 1 from sys_menu where menu_id = 3022);

insert into sys_menu
select 3023, '生产管理', 3000, 4, 'manufacture', null, '', '', 1, 0, 'M', '0', '0', '', 'job', 'admin', sysdate(), '', null, 'ERP生产管理目录'
where not exists (select 1 from sys_menu where menu_id = 3023);

update sys_menu set parent_id = 3020, order_num = 1, icon = 'peoples' where menu_id = 3001;
update sys_menu set parent_id = 3020, order_num = 2, icon = 'lock' where menu_id = 3007;
update sys_menu set parent_id = 3020, order_num = 3, icon = 'component' where menu_id = 3010;
update sys_menu set parent_id = 3020, order_num = 4, icon = 'build' where menu_id = 3030;

update sys_menu set parent_id = 3021, order_num = 1, icon = 'shopping' where menu_id = 3090;
update sys_menu set parent_id = 3021, order_num = 2, icon = 'upload' where menu_id = 3110;

update sys_menu set parent_id = 3022, order_num = 1, icon = 'table' where menu_id = 3040;
update sys_menu set parent_id = 3022, order_num = 2, icon = 'log' where menu_id = 3050;
update sys_menu set parent_id = 3022, order_num = 3, icon = 'enter' where menu_id = 3060;
update sys_menu set parent_id = 3022, order_num = 4, icon = 'upload' where menu_id = 3070;

update sys_menu set parent_id = 3023, order_num = 1, icon = 'job' where menu_id = 3080;
update sys_menu set parent_id = 3023, order_num = 2, icon = 'skill' where menu_id = 3100;

-- 给已有ERP角色补齐新增父目录授权，避免普通角色只拥有子菜单时前端菜单树断层。
insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, 3020
from sys_role_menu rm
where rm.menu_id in (3001, 3007, 3010, 3030)
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = 3020);

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, 3021
from sys_role_menu rm
where rm.menu_id in (3090, 3110)
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = 3021);

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, 3022
from sys_role_menu rm
where rm.menu_id in (3040, 3050, 3060, 3070)
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = 3022);

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, 3023
from sys_role_menu rm
where rm.menu_id in (3080, 3100)
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = 3023);

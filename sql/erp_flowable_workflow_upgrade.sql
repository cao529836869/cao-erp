-- Flowable integration for production -> picking -> cutting.
-- Run this after the base RuoYi menu data is initialized.

SET @workflow_dir_id := (SELECT menu_id FROM sys_menu WHERE menu_name = '生产管理' AND menu_type = 'M' LIMIT 1);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '生产流程待办', IFNULL(@workflow_dir_id, 0), 90, 'productionWorkflow', 'workflow/production/index', '', 1, 0, 'C', '0', '0', 'workflow:production:todo', 'list', 'admin', sysdate(), '生产领料裁剪Flowable待办'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE component = 'workflow/production/index');

SET @parent_id := (SELECT menu_id FROM sys_menu WHERE component = 'workflow/production/index' LIMIT 1);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '发起生产流程', @parent_id, 10, '#', '', '', 1, 0, 'F', '0', '0', 'workflow:production:start', '#', 'admin', sysdate(), ''
WHERE @parent_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'workflow:production:start');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '生产流程查询', @parent_id, 12, '#', '', '', 1, 0, 'F', '0', '0', 'workflow:production:query', '#', 'admin', sysdate(), ''
WHERE @parent_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'workflow:production:query');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '领料过账待办', @parent_id, 13, '#', '', '', 1, 0, 'F', '0', '0', 'workflow:production:picking', '#', 'admin', sysdate(), ''
WHERE @parent_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'workflow:production:picking');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '裁剪完成待办', @parent_id, 14, '#', '', '', 1, 0, 'F', '0', '0', 'workflow:production:cut', '#', 'admin', sysdate(), ''
WHERE @parent_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'workflow:production:cut');

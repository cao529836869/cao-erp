-- 销售订单、裁剪单、发货单升级脚本

create table if not exists erp_sales_order (
  sales_order_id   bigint(20)      not null auto_increment comment '销售订单ID',
  sales_order_no   varchar(40)     not null                comment '销售订单编号',
  customer_id      bigint(20)      default null            comment '客户ID',
  customer_name    varchar(100)    default null            comment '客户名称',
  order_date       date            default null            comment '订单日期',
  delivery_date    date            default null            comment '交货日期',
  total_qty        decimal(14,3)   default 0.000           comment '总数量',
  total_amount     decimal(14,2)   default 0.00            comment '总金额',
  order_status     varchar(20)     default '草稿'          comment '订单状态',
  del_flag         char(1)         default '0'             comment '删除标志',
  create_by        varchar(64)     default ''              comment '创建者',
  create_time      datetime        default null            comment '创建时间',
  update_by        varchar(64)     default ''              comment '更新者',
  update_time      datetime        default null            comment '更新时间',
  remark           varchar(500)    default null            comment '备注',
  primary key (sales_order_id),
  unique key uk_erp_sales_order_no (sales_order_no)
) engine=InnoDB default charset=utf8mb4 comment='销售订单主表';

create table if not exists erp_sales_order_detail (
  sales_order_detail_id bigint(20) not null auto_increment comment '销售订单明细ID',
  sales_order_id   bigint(20)      not null                comment '销售订单ID',
  sales_order_no   varchar(40)     not null                comment '销售订单编号',
  style_id         bigint(20)      default null            comment '款式ID',
  sku_id           bigint(20)      default null            comment '款式SKU ID',
  sku_code         varchar(80)     default null            comment 'SKU编码',
  style_no         varchar(60)     default null            comment '款号',
  style_name       varchar(100)    default null            comment '款式名称',
  color_name       varchar(50)     default null            comment '颜色',
  size_name        varchar(50)     default null            comment '尺码',
  order_qty        decimal(14,3)   default 0.000           comment '订单数量',
  unit_price       decimal(14,4)   default 0.0000          comment '单价',
  amount           decimal(14,2)   default 0.00            comment '金额',
  create_by        varchar(64)     default ''              comment '创建者',
  create_time      datetime        default null            comment '创建时间',
  update_by        varchar(64)     default ''              comment '更新者',
  update_time      datetime        default null            comment '更新时间',
  remark           varchar(500)    default null            comment '备注',
  primary key (sales_order_detail_id),
  key idx_erp_sales_detail_order (sales_order_id),
  key idx_erp_sales_detail_sku (sku_id)
) engine=InnoDB default charset=utf8mb4 comment='销售订单明细表';

create table if not exists erp_cut_order (
  cut_order_id       bigint(20)      not null auto_increment comment '裁剪单ID',
  cut_order_no       varchar(40)     not null                comment '裁剪单编号',
  production_order_id bigint(20)     not null                comment '生产订单ID',
  production_order_no varchar(40)    not null                comment '生产订单编号',
  sales_order_id     bigint(20)      default null            comment '销售订单ID',
  sales_order_no     varchar(40)     default null            comment '销售订单编号',
  customer_id        bigint(20)      default null            comment '客户ID',
  customer_name      varchar(100)    default null            comment '客户名称',
  plan_finish_time   datetime        not null                comment '计划完成时间',
  finish_time        datetime        default null            comment '实际完成时间',
  total_qty          decimal(14,3)   default 0.000           comment '裁剪计划数量',
  cut_status         int(2)          default 0               comment '裁剪状态：0待裁剪 1裁剪中 2已完成 3已取消',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime        default null            comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime        default null            comment '更新时间',
  remark             varchar(500)    default null            comment '备注',
  primary key (cut_order_id),
  unique key uk_erp_cut_order_no (cut_order_no),
  unique key uk_erp_cut_production (production_order_id),
  key idx_erp_cut_timeout (cut_status, plan_finish_time)
) engine=InnoDB default charset=utf8mb4 comment='裁剪单主表';

create table if not exists erp_cut_order_detail (
  cut_detail_id      bigint(20)      not null auto_increment comment '裁剪单明细ID',
  cut_order_id       bigint(20)      not null                comment '裁剪单ID',
  cut_order_no       varchar(40)     not null                comment '裁剪单编号',
  production_order_detail_id bigint(20) default null         comment '生产订单明细ID',
  style_id           bigint(20)      default null            comment '款式ID',
  sku_id             bigint(20)      default null            comment '款式SKU ID',
  style_no           varchar(60)     default null            comment '款号',
  style_name         varchar(100)    default null            comment '款式名称',
  color_name         varchar(50)     default null            comment '颜色',
  size_name          varchar(50)     default null            comment '尺码',
  plan_qty           decimal(14,3)   default 0.000           comment '计划裁剪数量',
  cut_qty            decimal(14,3)   default 0.000           comment '已裁剪数量',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime        default null            comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime        default null            comment '更新时间',
  remark             varchar(500)    default null            comment '备注',
  primary key (cut_detail_id),
  key idx_erp_cut_detail_order (cut_order_id),
  key idx_erp_cut_detail_sku (sku_id)
) engine=InnoDB default charset=utf8mb4 comment='裁剪单明细表';

create table if not exists erp_delivery_order (
  delivery_order_id bigint(20)      not null auto_increment comment '发货单ID',
  delivery_order_no varchar(40)     not null                comment '发货单编号',
  sales_order_id    bigint(20)      default null            comment '销售订单ID',
  sales_order_no    varchar(40)     default null            comment '销售订单编号',
  customer_id       bigint(20)      default null            comment '客户ID',
  customer_name     varchar(100)    default null            comment '客户名称',
  warehouse_id      bigint(20)      default null            comment '仓库ID',
  warehouse_name    varchar(100)    default null            comment '仓库名称',
  delivery_date     date            default null            comment '发货日期',
  total_qty         decimal(14,3)   default 0.000           comment '发货数量',
  delivery_status   varchar(20)     default '草稿'          comment '发货状态',
  logistics_company varchar(100)    default null            comment '物流公司',
  tracking_no       varchar(100)    default null            comment '物流单号',
  outbound_order_id bigint(20)      default null            comment '出库单ID',
  outbound_order_no varchar(40)     default null            comment '出库单编号',
  create_by         varchar(64)     default ''              comment '创建者',
  create_time       datetime        default null            comment '创建时间',
  update_by         varchar(64)     default ''              comment '更新者',
  update_time       datetime        default null            comment '更新时间',
  remark            varchar(500)    default null            comment '备注',
  primary key (delivery_order_id),
  unique key uk_erp_delivery_order_no (delivery_order_no)
) engine=InnoDB default charset=utf8mb4 comment='发货单主表';

create table if not exists erp_delivery_order_detail (
  delivery_detail_id bigint(20)     not null auto_increment comment '发货单明细ID',
  delivery_order_id  bigint(20)     not null                comment '发货单ID',
  delivery_order_no  varchar(40)    not null                comment '发货单编号',
  sales_detail_id    bigint(20)     default null            comment '销售订单明细ID',
  inventory_id       bigint(20)     not null                comment '库存ID',
  sku_id             bigint(20)     default null            comment '成衣SKU ID',
  sku_code           varchar(80)    default null            comment 'SKU编码',
  style_no           varchar(60)    default null            comment '款号',
  style_name         varchar(100)   default null            comment '款式名称',
  color_name         varchar(50)    default null            comment '颜色',
  size_name          varchar(50)    default null            comment '尺码',
  batch_no           varchar(80)    default ''              comment '批次号',
  stock_qty          decimal(14,3)  default 0.000           comment '库存数量快照',
  locked_qty         decimal(14,3)  default 0.000           comment '锁定数量',
  available_qty      decimal(14,3)  default 0.000           comment '可用数量快照',
  delivery_qty       decimal(14,3)  default 0.000           comment '发货数量',
  unit_name          varchar(20)    default null            comment '单位',
  unit_price         decimal(14,4)  default 0.0000          comment '单价',
  create_by          varchar(64)    default ''              comment '创建者',
  create_time        datetime       default null            comment '创建时间',
  update_by          varchar(64)    default ''              comment '更新者',
  update_time        datetime       default null            comment '更新时间',
  remark             varchar(500)   default null            comment '备注',
  primary key (delivery_detail_id),
  key idx_erp_delivery_detail_order (delivery_order_id),
  key idx_erp_delivery_detail_inventory (inventory_id),
  key idx_erp_delivery_detail_sales (sales_detail_id)
) engine=InnoDB default charset=utf8mb4 comment='发货单明细表';

-- Compatibility patch for databases where these tables were created by an earlier draft.
-- MySQL does not change an existing table when "create table if not exists" is re-run.
set @schema_name := database();

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_sales_order_detail' and column_name = 'sku_code') = 0,
  'alter table erp_sales_order_detail add column sku_code varchar(80) default null comment ''SKU编码'' after sku_id',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_sales_order_detail' and column_name = 'style_no') = 0,
  'alter table erp_sales_order_detail add column style_no varchar(60) default null comment ''款号'' after sku_code',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_sales_order_detail' and column_name = 'style_name') = 0,
  'alter table erp_sales_order_detail add column style_name varchar(100) default null comment ''款式名称'' after style_no',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_sales_order_detail' and column_name = 'color_name') = 0,
  'alter table erp_sales_order_detail add column color_name varchar(50) default null comment ''颜色'' after style_name',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_sales_order_detail' and column_name = 'size_name') = 0,
  'alter table erp_sales_order_detail add column size_name varchar(50) default null comment ''尺码'' after color_name',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_sales_order_detail' and column_name = 'order_qty') = 0,
  'alter table erp_sales_order_detail add column order_qty decimal(14,3) default 0.000 comment ''订单数量'' after size_name',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_sales_order_detail' and column_name = 'unit_price') = 0,
  'alter table erp_sales_order_detail add column unit_price decimal(14,4) default 0.0000 comment ''单价'' after order_qty',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_sales_order_detail' and column_name = 'amount') = 0,
  'alter table erp_sales_order_detail add column amount decimal(14,2) default 0.00 comment ''金额'' after unit_price',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_delivery_order' and column_name = 'outbound_order_id') = 0,
  'alter table erp_delivery_order add column outbound_order_id bigint(20) default null comment ''出库单ID'' after delivery_status',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_delivery_order' and column_name = 'outbound_order_no') = 0,
  'alter table erp_delivery_order add column outbound_order_no varchar(40) default null comment ''出库单编号'' after outbound_order_id',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_delivery_order' and column_name = 'logistics_company') = 0,
  'alter table erp_delivery_order add column logistics_company varchar(100) default null comment ''物流公司'' after delivery_status',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_delivery_order' and column_name = 'tracking_no') = 0,
  'alter table erp_delivery_order add column tracking_no varchar(100) default null comment ''物流单号'' after logistics_company',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_delivery_order' and column_name = 'warehouse_id') = 0,
  'alter table erp_delivery_order add column warehouse_id bigint(20) default null comment ''仓库ID'' after customer_name',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_delivery_order' and column_name = 'warehouse_name') = 0,
  'alter table erp_delivery_order add column warehouse_name varchar(100) default null comment ''仓库名称'' after warehouse_id',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_delivery_order' and column_name = 'total_qty') = 0,
  'alter table erp_delivery_order add column total_qty decimal(14,3) default 0.000 comment ''发货数量'' after delivery_date',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_delivery_order' and column_name = 'delivery_qty') > 0,
  'update erp_delivery_order set total_qty = ifnull(total_qty, delivery_qty)',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

insert into sys_menu
select 3021, '订单管理', 3000, 2, 'order', null, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '', null, 'ERP订单管理目录'
where not exists (select 1 from sys_menu where menu_id = 3021);
insert into sys_menu
select 3023, '生产管理', 3000, 4, 'manufacture', null, '', '', 1, 0, 'M', '0', '0', '', 'job', 'admin', sysdate(), '', null, 'ERP生产管理目录'
where not exists (select 1 from sys_menu where menu_id = 3023);

insert ignore into sys_menu values(3090, '销售订单', '3021', '1', 'sales', 'erp/sales/index', '', '', 1, 0, 'C', '0', '0', 'erp:sales:list', 'shopping', 'admin', sysdate(), '', null, '销售订单菜单');
update sys_menu set parent_id = 3021, order_num = 1, icon = 'shopping' where menu_id = 3090;
insert ignore into sys_menu values(3091, '销售查询', '3090', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:sales:query', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3092, '销售新增', '3090', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:sales:add', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3093, '销售修改', '3090', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:sales:edit', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3094, '销售删除', '3090', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:sales:remove', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3095, '销售导出', '3090', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:sales:export', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3096, '销售导入', '3090', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:sales:import', '#', 'admin', sysdate(), '', null, '');

insert ignore into sys_menu values(3100, '裁剪单', '3023', '2', 'cut', 'erp/cut/index', '', '', 1, 0, 'C', '0', '0', 'erp:cut:list', 'skill', 'admin', sysdate(), '', null, '裁剪单菜单');
update sys_menu set parent_id = 3023, order_num = 2, icon = 'skill' where menu_id = 3100;
insert ignore into sys_menu values(3101, '裁剪查询', '3100', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:cut:query', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3102, '裁剪新增', '3100', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:cut:add', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3103, '裁剪修改', '3100', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:cut:edit', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3104, '裁剪删除', '3100', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:cut:remove', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3105, '裁剪导出', '3100', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:cut:export', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3106, '裁剪完成', '3100', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:cut:finish', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3107, '裁剪取消', '3100', '7', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:cut:cancel', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3108, '裁剪延期', '3100', '8', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:cut:delay', '#', 'admin', sysdate(), '', null, '');

insert ignore into sys_menu values(3110, '发货单', '3021', '2', 'delivery', 'erp/delivery/index', '', '', 1, 0, 'C', '0', '0', 'erp:delivery:list', 'upload', 'admin', sysdate(), '', null, '发货单菜单');
update sys_menu set parent_id = 3021, order_num = 2, icon = 'upload' where menu_id = 3110;
insert ignore into sys_menu values(3111, '发货查询', '3110', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:delivery:query', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3112, '发货新增', '3110', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:delivery:add', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3113, '发货修改', '3110', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:delivery:edit', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3114, '发货删除', '3110', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:delivery:remove', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3115, '发货导出', '3110', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:delivery:export', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3116, '发货确认', '3110', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:delivery:confirm', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3117, '发货取消', '3110', '7', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:delivery:cancel', '#', 'admin', sysdate(), '', null, '');
insert ignore into sys_menu values(3118, '物流维护', '3110', '8', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:delivery:logistics', '#', 'admin', sysdate(), '', null, '');

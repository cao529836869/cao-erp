-- 库存出入库单据升级脚本
-- 已安装旧库时执行；新库可直接使用 yiyayi_kids_erp_core.sql

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS erp_warehouse (
  warehouse_id      BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  warehouse_code    VARCHAR(40)     NOT NULL                COMMENT '仓库编码',
  warehouse_name    VARCHAR(80)     NOT NULL                COMMENT '仓库名称',
  warehouse_type    VARCHAR(30)     DEFAULT '物料仓'        COMMENT '仓库类型，如物料仓、成衣仓、次品仓、样品仓',
  manager_name      VARCHAR(64)     DEFAULT NULL            COMMENT '仓库负责人',
  contact_phone     VARCHAR(30)     DEFAULT NULL            COMMENT '联系电话',
  province          VARCHAR(50)     DEFAULT NULL            COMMENT '省份',
  city              VARCHAR(50)     DEFAULT NULL            COMMENT '城市',
  county            VARCHAR(50)     DEFAULT NULL            COMMENT '区县',
  address           VARCHAR(255)    DEFAULT NULL            COMMENT '仓库地址',
  status            CHAR(1)         DEFAULT '0'             COMMENT '状态（0正常 1停用）',
  del_flag          CHAR(1)         DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (warehouse_id),
  UNIQUE KEY uk_erp_warehouse_code (warehouse_code),
  KEY idx_erp_warehouse_name (warehouse_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库档案表';

CREATE TABLE IF NOT EXISTS erp_inbound_order (
  inbound_order_id  BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '入库单ID',
  inbound_order_no  VARCHAR(40)     NOT NULL                COMMENT '入库单编号',
  inbound_type      VARCHAR(30)     NOT NULL                COMMENT '入库类型，如采购入库、生产入库、销售退货、盘盈入库、其他入库',
  source_type       VARCHAR(30)     DEFAULT NULL            COMMENT '来源类型，如采购订单、生产订单、销售退货单、手工创建',
  source_no         VARCHAR(40)     DEFAULT NULL            COMMENT '来源单号',
  supplier_id       BIGINT(20)      DEFAULT NULL            COMMENT '供应商ID，采购入库使用',
  supplier_name     VARCHAR(100)    DEFAULT NULL            COMMENT '供应商名称',
  customer_id       BIGINT(20)      DEFAULT NULL            COMMENT '客户ID，销售退货使用',
  customer_name     VARCHAR(100)    DEFAULT NULL            COMMENT '客户名称',
  warehouse_id      BIGINT(20)      NOT NULL DEFAULT 0      COMMENT '入库仓库ID',
  warehouse_name    VARCHAR(80)     NOT NULL                COMMENT '入库仓库名称',
  inbound_date      DATE            NOT NULL                COMMENT '入库日期',
  total_qty         DECIMAL(14,3)   DEFAULT 0.000           COMMENT '入库总数量',
  total_amount      DECIMAL(14,2)   DEFAULT 0.00            COMMENT '入库总金额',
  order_status      VARCHAR(20)     DEFAULT '草稿'          COMMENT '单据状态，如草稿、已审核、已入库、已作废',
  audit_by          VARCHAR(64)     DEFAULT NULL            COMMENT '审核人',
  audit_time        DATETIME        DEFAULT NULL            COMMENT '审核时间',
  post_by           VARCHAR(64)     DEFAULT NULL            COMMENT '过账人',
  post_time         DATETIME        DEFAULT NULL            COMMENT '过账时间',
  cancel_by         VARCHAR(64)     DEFAULT NULL            COMMENT '作废人',
  cancel_time       DATETIME        DEFAULT NULL            COMMENT '作废时间',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (inbound_order_id),
  UNIQUE KEY uk_erp_inbound_order_no (inbound_order_no),
  KEY idx_erp_inbound_order_type (inbound_type),
  KEY idx_erp_inbound_order_source (source_type, source_no),
  KEY idx_erp_inbound_order_warehouse (warehouse_id),
  KEY idx_erp_inbound_order_date (inbound_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单主表';

CREATE TABLE IF NOT EXISTS erp_inbound_order_detail (
  inbound_detail_id BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '入库单明细ID',
  inbound_order_id  BIGINT(20)      NOT NULL                COMMENT '入库单ID',
  inbound_order_no  VARCHAR(40)     NOT NULL                COMMENT '入库单编号',
  source_detail_id  BIGINT(20)      DEFAULT NULL            COMMENT '来源单据明细ID',
  item_type         VARCHAR(20)     NOT NULL                COMMENT '库存对象类型，如成衣、物料',
  item_id           BIGINT(20)      NOT NULL                COMMENT '库存对象ID，成衣对应款式SKU ID，物料对应物料SKU ID',
  item_code         VARCHAR(80)     NOT NULL                COMMENT '库存对象编码',
  item_name         VARCHAR(100)    NOT NULL                COMMENT '库存对象名称',
  color_name        VARCHAR(50)     DEFAULT NULL            COMMENT '颜色名称',
  size_name         VARCHAR(50)     DEFAULT NULL            COMMENT '尺码名称，成衣使用',
  spec_name         VARCHAR(100)    DEFAULT NULL            COMMENT '规格名称，物料使用',
  batch_no          VARCHAR(60)     DEFAULT ''              COMMENT '批次号',
  location_code     VARCHAR(40)     DEFAULT NULL            COMMENT '库位编码',
  plan_qty          DECIMAL(14,3)   DEFAULT 0.000           COMMENT '计划入库数量',
  inbound_qty       DECIMAL(14,3)   NOT NULL DEFAULT 0.000  COMMENT '实际入库数量',
  unit_name         VARCHAR(20)     NOT NULL                COMMENT '单位',
  unit_price        DECIMAL(14,4)   DEFAULT 0.0000          COMMENT '入库单价',
  amount            DECIMAL(14,2)   DEFAULT 0.00            COMMENT '入库金额',
  production_date   DATE            DEFAULT NULL            COMMENT '生产日期',
  expire_date       DATE            DEFAULT NULL            COMMENT '失效日期',
  quality_status    VARCHAR(20)     DEFAULT '待检'          COMMENT '质检状态，如待检、合格、不合格、让步接收',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (inbound_detail_id),
  KEY idx_erp_inbound_detail_order_id (inbound_order_id),
  KEY idx_erp_inbound_detail_item (item_type, item_id),
  KEY idx_erp_inbound_detail_batch (batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单明细表';

CREATE TABLE IF NOT EXISTS erp_outbound_order (
  outbound_order_id BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '出库单ID',
  outbound_order_no VARCHAR(40)     NOT NULL                COMMENT '出库单编号',
  outbound_type     VARCHAR(30)     NOT NULL                COMMENT '出库类型，如销售出库、生产领料、采购退货、盘亏出库、其他出库',
  source_type       VARCHAR(30)     DEFAULT NULL            COMMENT '来源类型，如销售订单、生产订单、采购退货单、手工创建',
  source_no         VARCHAR(40)     DEFAULT NULL            COMMENT '来源单号',
  customer_id       BIGINT(20)      DEFAULT NULL            COMMENT '客户ID，销售出库使用',
  customer_name     VARCHAR(100)    DEFAULT NULL            COMMENT '客户名称',
  supplier_id       BIGINT(20)      DEFAULT NULL            COMMENT '供应商ID，采购退货使用',
  supplier_name     VARCHAR(100)    DEFAULT NULL            COMMENT '供应商名称',
  warehouse_id      BIGINT(20)      NOT NULL DEFAULT 0      COMMENT '出库仓库ID',
  warehouse_name    VARCHAR(80)     NOT NULL                COMMENT '出库仓库名称',
  outbound_date     DATE            NOT NULL                COMMENT '出库日期',
  total_qty         DECIMAL(14,3)   DEFAULT 0.000           COMMENT '出库总数量',
  total_amount      DECIMAL(14,2)   DEFAULT 0.00            COMMENT '出库总金额',
  order_status      VARCHAR(20)     DEFAULT '草稿'          COMMENT '单据状态，如草稿、已审核、已拣货、已出库、已作废',
  audit_by          VARCHAR(64)     DEFAULT NULL            COMMENT '审核人',
  audit_time        DATETIME        DEFAULT NULL            COMMENT '审核时间',
  pick_by           VARCHAR(64)     DEFAULT NULL            COMMENT '拣货人',
  pick_time         DATETIME        DEFAULT NULL            COMMENT '拣货时间',
  post_by           VARCHAR(64)     DEFAULT NULL            COMMENT '过账人',
  post_time         DATETIME        DEFAULT NULL            COMMENT '过账时间',
  cancel_by         VARCHAR(64)     DEFAULT NULL            COMMENT '作废人',
  cancel_time       DATETIME        DEFAULT NULL            COMMENT '作废时间',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (outbound_order_id),
  UNIQUE KEY uk_erp_outbound_order_no (outbound_order_no),
  KEY idx_erp_outbound_order_type (outbound_type),
  KEY idx_erp_outbound_order_source (source_type, source_no),
  KEY idx_erp_outbound_order_warehouse (warehouse_id),
  KEY idx_erp_outbound_order_date (outbound_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单主表';

CREATE TABLE IF NOT EXISTS erp_outbound_order_detail (
  outbound_detail_id BIGINT(20)     NOT NULL AUTO_INCREMENT COMMENT '出库单明细ID',
  outbound_order_id BIGINT(20)      NOT NULL                COMMENT '出库单ID',
  outbound_order_no VARCHAR(40)     NOT NULL                COMMENT '出库单编号',
  source_detail_id  BIGINT(20)      DEFAULT NULL            COMMENT '来源单据明细ID',
  item_type         VARCHAR(20)     NOT NULL                COMMENT '库存对象类型，如成衣、物料',
  item_id           BIGINT(20)      NOT NULL                COMMENT '库存对象ID，成衣对应款式SKU ID，物料对应物料SKU ID',
  item_code         VARCHAR(80)     NOT NULL                COMMENT '库存对象编码',
  item_name         VARCHAR(100)    NOT NULL                COMMENT '库存对象名称',
  color_name        VARCHAR(50)     DEFAULT NULL            COMMENT '颜色名称',
  size_name         VARCHAR(50)     DEFAULT NULL            COMMENT '尺码名称，成衣使用',
  spec_name         VARCHAR(100)    DEFAULT NULL            COMMENT '规格名称，物料使用',
  batch_no          VARCHAR(60)     DEFAULT ''              COMMENT '批次号',
  location_code     VARCHAR(40)     DEFAULT NULL            COMMENT '库位编码',
  plan_qty          DECIMAL(14,3)   DEFAULT 0.000           COMMENT '计划出库数量',
  locked_qty        DECIMAL(14,3)   DEFAULT 0.000           COMMENT '锁定数量',
  outbound_qty      DECIMAL(14,3)   NOT NULL DEFAULT 0.000  COMMENT '实际出库数量',
  unit_name         VARCHAR(20)     NOT NULL                COMMENT '单位',
  unit_price        DECIMAL(14,4)   DEFAULT 0.0000          COMMENT '出库单价',
  amount            DECIMAL(14,2)   DEFAULT 0.00            COMMENT '出库金额',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (outbound_detail_id),
  KEY idx_erp_outbound_detail_order_id (outbound_order_id),
  KEY idx_erp_outbound_detail_item (item_type, item_id),
  KEY idx_erp_outbound_detail_batch (batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单明细表';

-- 库存模块菜单与权限按钮
insert into sys_menu
select 3020, '基础资料', 3000, 1, 'basic', null, '', '', 1, 0, 'M', '0', '0', '', 'dict', 'admin', sysdate(), '', null, 'ERP基础资料目录'
where not exists (select 1 from sys_menu where menu_id = 3020);
insert into sys_menu
select 3022, '库存管理', 3000, 3, 'stock', null, '', '', 1, 0, 'M', '0', '0', '', 'warehouse', 'admin', sysdate(), '', null, 'ERP库存管理目录'
where not exists (select 1 from sys_menu where menu_id = 3022);

delete from sys_menu where menu_id between 3030 and 3099;
insert into sys_menu values(3030, '仓库档案', '3020', '4', 'warehouse', 'erp/warehouse/index', '', '', 1, 0, 'C', '0', '0', 'erp:warehouse:list', 'build', 'admin', sysdate(), '', null, '仓库档案菜单');
insert into sys_menu values(3031, '仓库查询', '3030', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:warehouse:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3032, '仓库新增', '3030', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:warehouse:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3033, '仓库修改', '3030', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:warehouse:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3034, '仓库删除', '3030', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:warehouse:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3035, '仓库导出', '3030', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:warehouse:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3040, '库存汇总', '3022', '1', 'inventory', 'erp/inventory/index', '', '', 1, 0, 'C', '0', '0', 'erp:inventory:list', 'table', 'admin', sysdate(), '', null, '库存汇总菜单');
insert into sys_menu values(3041, '库存查询', '3040', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:inventory:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3042, '库存导出', '3040', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:inventory:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3050, '库存流水', '3022', '2', 'transaction', 'erp/transaction/index', '', '', 1, 0, 'C', '0', '0', 'erp:transaction:list', 'log', 'admin', sysdate(), '', null, '库存流水菜单');
insert into sys_menu values(3051, '流水查询', '3050', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:transaction:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3052, '流水导出', '3050', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:transaction:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3060, '入库单', '3022', '3', 'inbound', 'erp/inbound/index', '', '', 1, 0, 'C', '0', '0', 'erp:inbound:list', 'enter', 'admin', sysdate(), '', null, '入库单菜单');
insert into sys_menu values(3061, '入库查询', '3060', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:inbound:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3062, '入库新增', '3060', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:inbound:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3063, '入库修改', '3060', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:inbound:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3064, '入库删除', '3060', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:inbound:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3065, '入库导出', '3060', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:inbound:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3066, '入库过账', '3060', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:inbound:post', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3067, '入库取消过账', '3060', '7', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:inbound:cancelPost', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3070, '出库单', '3022', '4', 'outbound', 'erp/outbound/index', '', '', 1, 0, 'C', '0', '0', 'erp:outbound:list', 'upload', 'admin', sysdate(), '', null, '出库单菜单');
insert into sys_menu values(3071, '出库查询', '3070', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:outbound:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3072, '出库新增', '3070', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:outbound:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3073, '出库修改', '3070', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:outbound:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3074, '出库删除', '3070', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:outbound:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3075, '出库导出', '3070', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:outbound:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3076, '出库过账', '3070', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:outbound:post', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3077, '出库取消过账', '3070', '7', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:outbound:cancelPost', '#', 'admin', sysdate(), '', null, '');

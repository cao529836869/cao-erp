-- 物料主表、物料SKU与菜单权限升级脚本
-- 已执行 sql/yiyayi_kids_erp_core.sql 全量脚本的环境无需重复创建表结构。

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS erp_material (
  material_id       BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '物料ID',
  material_code     VARCHAR(40)     NOT NULL                COMMENT '物料编码',
  material_name     VARCHAR(100)    NOT NULL                COMMENT '物料名称',
  material_type     VARCHAR(30)     NOT NULL                COMMENT '物料类型，如面料、辅料、包装材料',
  category_name     VARCHAR(50)     DEFAULT NULL            COMMENT '物料分类',
  unit_name         VARCHAR(20)     NOT NULL                COMMENT '库存单位',
  default_supplier_id BIGINT(20)    DEFAULT NULL            COMMENT '默认供应商ID',
  default_supplier_name VARCHAR(100) DEFAULT NULL           COMMENT '默认供应商名称',
  safe_stock_qty    DECIMAL(14,3)   DEFAULT 0.000           COMMENT '安全库存数量',
  status            CHAR(1)         DEFAULT '0'             COMMENT '状态（0正常 1停用）',
  del_flag          CHAR(1)         DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (material_id),
  UNIQUE KEY uk_erp_material_code (material_code),
  KEY idx_erp_material_name (material_name),
  KEY idx_erp_material_type (material_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料主表';

CREATE TABLE IF NOT EXISTS erp_material_sku (
  material_sku_id   BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '物料SKU ID',
  material_id       BIGINT(20)      NOT NULL                COMMENT '物料ID',
  material_code     VARCHAR(40)     NOT NULL                COMMENT '物料编码',
  material_sku_code VARCHAR(80)     NOT NULL                COMMENT '物料SKU编码',
  color_code        VARCHAR(30)     DEFAULT NULL            COMMENT '颜色编码',
  color_name        VARCHAR(50)     DEFAULT NULL            COMMENT '颜色名称',
  spec_name         VARCHAR(100)    DEFAULT NULL            COMMENT '规格名称，如幅宽、克重、型号',
  width_value       DECIMAL(10,2)   DEFAULT NULL            COMMENT '幅宽',
  gram_weight       DECIMAL(10,2)   DEFAULT NULL            COMMENT '克重',
  unit_name         VARCHAR(20)     NOT NULL                COMMENT '单位',
  barcode           VARCHAR(80)     DEFAULT NULL            COMMENT '条码',
  status            CHAR(1)         DEFAULT '0'             COMMENT '状态（0正常 1停用）',
  del_flag          CHAR(1)         DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (material_sku_id),
  UNIQUE KEY uk_erp_material_sku_code (material_sku_code),
  KEY idx_erp_material_sku_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料SKU表';

insert into sys_menu
select 3000, 'ERP管理', '0', '5', 'erp', null, '', '', 1, 0, 'M', '0', '0', '', 'tree', 'admin', sysdate(), '', null, 'ERP业务管理目录'
where not exists (select 1 from sys_menu where menu_id = 3000);
insert into sys_menu
select 3020, '基础资料', 3000, 1, 'basic', null, '', '', 1, 0, 'M', '0', '0', '', 'dict', 'admin', sysdate(), '', null, 'ERP基础资料目录'
where not exists (select 1 from sys_menu where menu_id = 3020);

delete from sys_menu where menu_id in (3010, 3011, 3012, 3013, 3014, 3015, 3016);
insert into sys_menu values(3010, '物料档案', '3020', '3', 'material', 'erp/material/index', '', '', 1, 0, 'C', '0', '0', 'erp:material:list', 'component', 'admin', sysdate(), '', null, '物料档案菜单');
insert into sys_menu values(3011, '物料查询', '3010', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:material:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3012, '物料新增', '3010', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:material:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3013, '物料修改', '3010', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:material:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3014, '物料删除', '3010', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:material:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3015, '物料导出', '3010', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:material:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3016, '物料编码生成', '3010', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:material:code', '#', 'admin', sysdate(), '', null, '');

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 一丫一童装工厂 ERP 核心业务表
-- MySQL 5.7 compatible
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS erp_delivery_order;
DROP TABLE IF EXISTS erp_quality_check;
DROP TABLE IF EXISTS erp_sewing_report;
DROP TABLE IF EXISTS erp_cutting_order;
DROP TABLE IF EXISTS erp_production_order_detail;
DROP TABLE IF EXISTS erp_production_order;
DROP TABLE IF EXISTS erp_inventory_transaction;
DROP TABLE IF EXISTS erp_outbound_order_detail;
DROP TABLE IF EXISTS erp_outbound_order;
DROP TABLE IF EXISTS erp_inbound_order_detail;
DROP TABLE IF EXISTS erp_inbound_order;
DROP TABLE IF EXISTS erp_inventory;
DROP TABLE IF EXISTS erp_warehouse;
DROP TABLE IF EXISTS erp_purchase_order_detail;
DROP TABLE IF EXISTS erp_purchase_order;
DROP TABLE IF EXISTS erp_sales_order_detail;
DROP TABLE IF EXISTS erp_sales_order;
DROP TABLE IF EXISTS erp_style_bom_detail;
DROP TABLE IF EXISTS erp_style_bom;
DROP TABLE IF EXISTS erp_material_sku;
DROP TABLE IF EXISTS erp_material;
DROP TABLE IF EXISTS erp_style_sku;
DROP TABLE IF EXISTS erp_style;
DROP TABLE IF EXISTS erp_supplier;
DROP TABLE IF EXISTS erp_customer;

-- ----------------------------
-- 1、客户档案表
-- ----------------------------
CREATE TABLE erp_customer (
  customer_id       BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  customer_code     VARCHAR(40)     NOT NULL                COMMENT '客户编码',
  customer_name     VARCHAR(100)    NOT NULL                COMMENT '客户名称',
  customer_type     VARCHAR(20)     DEFAULT '品牌客户'       COMMENT '客户类型，如品牌客户、批发客户、门店客户、电商客户',
  contact_name      VARCHAR(50)     DEFAULT NULL            COMMENT '联系人',
  contact_phone     VARCHAR(30)     DEFAULT NULL            COMMENT '联系电话',
  contact_email     VARCHAR(100)    DEFAULT NULL            COMMENT '联系邮箱',
  province          VARCHAR(50)     DEFAULT NULL            COMMENT '省份',
  city              VARCHAR(50)     DEFAULT NULL            COMMENT '城市',
  county            VARCHAR(50)     DEFAULT NULL            COMMENT '区县',
  address           VARCHAR(255)    DEFAULT NULL            COMMENT '详细地址',
  credit_limit      DECIMAL(14,2)   DEFAULT 0.00            COMMENT '信用额度',
  settlement_type   VARCHAR(30)     DEFAULT NULL            COMMENT '结算方式，如月结、预付款、货到付款',
  status            CHAR(1)         DEFAULT '0'             COMMENT '状态（0正常 1停用）',
  del_flag          CHAR(1)         DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (customer_id),
  UNIQUE KEY uk_erp_customer_code (customer_code),
  KEY idx_erp_customer_name (customer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户档案表';

-- ----------------------------
-- 2、供应商档案表
-- ----------------------------
CREATE TABLE erp_supplier (
  supplier_id       BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  supplier_code     VARCHAR(40)     NOT NULL                COMMENT '供应商编码',
  supplier_name     VARCHAR(100)    NOT NULL                COMMENT '供应商名称',
  supplier_type     VARCHAR(30)     DEFAULT NULL            COMMENT '供应商类型，如面料、辅料、包装、外协加工',
  contact_name      VARCHAR(50)     DEFAULT NULL            COMMENT '联系人',
  contact_phone     VARCHAR(30)     DEFAULT NULL            COMMENT '联系电话',
  contact_email     VARCHAR(100)    DEFAULT NULL            COMMENT '联系邮箱',
  province          VARCHAR(50)     DEFAULT NULL            COMMENT '省份',
  city              VARCHAR(50)     DEFAULT NULL            COMMENT '城市',
  address           VARCHAR(255)    DEFAULT NULL            COMMENT '详细地址',
  lead_time_days    INT(11)         DEFAULT 0               COMMENT '常规交期天数',
  settlement_type   VARCHAR(30)     DEFAULT NULL            COMMENT '结算方式',
  status            CHAR(1)         DEFAULT '0'             COMMENT '状态（0正常 1停用）',
  del_flag          CHAR(1)         DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (supplier_id),
  UNIQUE KEY uk_erp_supplier_code (supplier_code),
  KEY idx_erp_supplier_name (supplier_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商档案表';

-- ----------------------------
-- 3、童装款式主表
-- ----------------------------
CREATE TABLE erp_style (
  style_id          BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '款式ID',
  style_no          VARCHAR(40)     NOT NULL                COMMENT '款号',
  style_name        VARCHAR(100)    NOT NULL                COMMENT '款式名称',
  category_name     VARCHAR(50)     DEFAULT NULL            COMMENT '品类，如T恤、连衣裙、家居服、卫衣、套装',
  brand_name        VARCHAR(60)     DEFAULT NULL            COMMENT '品牌名称',
  season_name       VARCHAR(30)     DEFAULT NULL            COMMENT '季节，如春夏、秋冬',
  year_name         VARCHAR(10)     DEFAULT NULL            COMMENT '年份',
  gender_type       VARCHAR(20)     DEFAULT NULL            COMMENT '童装性别类型，如男童、女童、中性',
  designer          VARCHAR(50)     DEFAULT NULL            COMMENT '设计师',
  sample_status     CHAR(1)         DEFAULT '0'             COMMENT '样衣状态（0待打样 1打样中 2已确认）',
  production_status CHAR(1)         DEFAULT '0'             COMMENT '生产状态（0未投产 1生产中 2已完结）',
  image_url         VARCHAR(1000)   DEFAULT NULL            COMMENT '主图地址，多个地址用英文逗号分隔',
  status            CHAR(1)         DEFAULT '0'             COMMENT '状态（0正常 1停用）',
  del_flag          CHAR(1)         DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (style_id),
  UNIQUE KEY uk_erp_style_no (style_no),
  KEY idx_erp_style_category (category_name),
  KEY idx_erp_style_season (season_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='童装款式主表';

-- ----------------------------
-- 4、童装款式SKU表
-- ----------------------------
CREATE TABLE erp_style_sku (
  sku_id            BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '款式SKU ID',
  style_id          BIGINT(20)      NOT NULL                COMMENT '款式ID',
  style_no          VARCHAR(40)     NOT NULL                COMMENT '款号',
  sku_code          VARCHAR(80)     NOT NULL                COMMENT 'SKU编码，通常由款号、颜色、尺码组成',
  color_code        VARCHAR(30)     DEFAULT NULL            COMMENT '颜色编码',
  color_name        VARCHAR(50)     NOT NULL                COMMENT '颜色名称',
  size_code         VARCHAR(30)     NOT NULL                COMMENT '尺码编码，如80、90、100、110、120、130、140',
  size_name         VARCHAR(50)     NOT NULL                COMMENT '尺码名称',
  barcode           VARCHAR(80)     DEFAULT NULL            COMMENT '条码',
  retail_price      DECIMAL(14,2)   DEFAULT 0.00            COMMENT '建议零售价',
  status            CHAR(1)         DEFAULT '0'             COMMENT '状态（0正常 1停用）',
  del_flag          CHAR(1)         DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (sku_id),
  UNIQUE KEY uk_erp_style_sku_code (sku_code),
  KEY idx_erp_style_sku_style_id (style_id),
  KEY idx_erp_style_sku_style_no (style_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='童装款式SKU表';

-- ----------------------------
-- 5、物料主表
-- ----------------------------
CREATE TABLE erp_material (
  material_id       BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '物料ID',
  material_code     VARCHAR(40)     NOT NULL                COMMENT '物料编码',
  material_name     VARCHAR(100)    NOT NULL                COMMENT '物料名称',
  material_type     VARCHAR(30)     NOT NULL                COMMENT '物料类型，如面料、辅料、包装材料',
  category_name     VARCHAR(50)     DEFAULT NULL            COMMENT '物料分类，如棉毛布、罗纹、纽扣、拉链、吊牌、包装袋',
  unit_name         VARCHAR(20)     NOT NULL                COMMENT '库存单位，如米、公斤、个、张、件',
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

-- ----------------------------
-- 6、物料SKU表
-- ----------------------------
CREATE TABLE erp_material_sku (
  material_sku_id   BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '物料SKU ID',
  material_id       BIGINT(20)      NOT NULL                COMMENT '物料ID',
  material_code     VARCHAR(40)     NOT NULL                COMMENT '物料编码',
  material_sku_code VARCHAR(80)     NOT NULL                COMMENT '物料SKU编码',
  color_code        VARCHAR(30)     DEFAULT NULL            COMMENT '颜色编码',
  color_name        VARCHAR(50)     DEFAULT NULL            COMMENT '颜色名称',
  spec_name         VARCHAR(100)    DEFAULT NULL            COMMENT '规格名称，如幅宽、克重、型号',
  width_value       DECIMAL(10,2)   DEFAULT NULL            COMMENT '幅宽数值，面料使用',
  gram_weight       DECIMAL(10,2)   DEFAULT NULL            COMMENT '克重，面料使用',
  unit_name         VARCHAR(20)     NOT NULL                COMMENT '库存单位',
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

-- ----------------------------
-- 7、款式BOM主表
-- ----------------------------
CREATE TABLE erp_style_bom (
  bom_id            BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT 'BOM ID',
  bom_no            VARCHAR(40)     NOT NULL                COMMENT 'BOM编号',
  style_id          BIGINT(20)      NOT NULL                COMMENT '款式ID',
  style_no          VARCHAR(40)     NOT NULL                COMMENT '款号',
  version_no        VARCHAR(20)     NOT NULL                COMMENT 'BOM版本号',
  bom_status        CHAR(1)         DEFAULT '0'             COMMENT 'BOM状态（0草稿 1已审核 2已停用）',
  effective_date    DATE            DEFAULT NULL            COMMENT '生效日期',
  audit_by          VARCHAR(64)     DEFAULT NULL            COMMENT '审核人',
  audit_time        DATETIME        DEFAULT NULL            COMMENT '审核时间',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (bom_id),
  UNIQUE KEY uk_erp_style_bom_no (bom_no),
  KEY idx_erp_style_bom_style_id (style_id),
  KEY idx_erp_style_bom_style_no (style_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='款式BOM主表';

-- ----------------------------
-- 8、款式BOM明细表
-- ----------------------------
CREATE TABLE erp_style_bom_detail (
  bom_detail_id     BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT 'BOM明细ID',
  bom_id            BIGINT(20)      NOT NULL                COMMENT 'BOM ID',
  material_id       BIGINT(20)      NOT NULL                COMMENT '物料ID',
  material_sku_id   BIGINT(20)      DEFAULT NULL            COMMENT '物料SKU ID',
  material_code     VARCHAR(40)     NOT NULL                COMMENT '物料编码',
  material_name     VARCHAR(100)    NOT NULL                COMMENT '物料名称',
  material_type     VARCHAR(30)     NOT NULL                COMMENT '物料类型',
  color_name        VARCHAR(50)     DEFAULT NULL            COMMENT '物料颜色',
  spec_name         VARCHAR(100)    DEFAULT NULL            COMMENT '物料规格',
  usage_qty         DECIMAL(14,4)   NOT NULL DEFAULT 0.0000 COMMENT '单件标准用量',
  loss_rate         DECIMAL(8,4)    DEFAULT 0.0000          COMMENT '损耗率，例如0.0500表示5%',
  unit_name         VARCHAR(20)     NOT NULL                COMMENT '用量单位',
  position_name     VARCHAR(60)     DEFAULT NULL            COMMENT '使用部位，如大身、袖口、领口、包装',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (bom_detail_id),
  KEY idx_erp_bom_detail_bom_id (bom_id),
  KEY idx_erp_bom_detail_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='款式BOM明细表';

-- ----------------------------
-- 9、销售订单主表
-- ----------------------------
CREATE TABLE erp_sales_order (
  sales_order_id    BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '销售订单ID',
  sales_order_no    VARCHAR(40)     NOT NULL                COMMENT '销售订单编号',
  customer_id       BIGINT(20)      NOT NULL                COMMENT '客户ID',
  customer_name     VARCHAR(100)    NOT NULL                COMMENT '客户名称',
  order_date        DATE            NOT NULL                COMMENT '下单日期',
  delivery_date     DATE            DEFAULT NULL            COMMENT '要求交货日期',
  order_source      VARCHAR(30)     DEFAULT NULL            COMMENT '订单来源，如订货会、补单、电商、线下门店',
  currency_code     VARCHAR(10)     DEFAULT 'CNY'           COMMENT '币种',
  total_qty         DECIMAL(14,3)   DEFAULT 0.000           COMMENT '订单总件数',
  total_amount      DECIMAL(14,2)   DEFAULT 0.00            COMMENT '订单总金额',
  order_status      VARCHAR(20)     DEFAULT '草稿'          COMMENT '订单状态，如草稿、已审核、生产中、已出货、已关闭',
  audit_by          VARCHAR(64)     DEFAULT NULL            COMMENT '审核人',
  audit_time        DATETIME        DEFAULT NULL            COMMENT '审核时间',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (sales_order_id),
  UNIQUE KEY uk_erp_sales_order_no (sales_order_no),
  KEY idx_erp_sales_order_customer_id (customer_id),
  KEY idx_erp_sales_order_delivery_date (delivery_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单主表';

-- ----------------------------
-- 10、销售订单明细表
-- ----------------------------
CREATE TABLE erp_sales_order_detail (
  sales_order_detail_id BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT '销售订单明细ID',
  sales_order_id    BIGINT(20)      NOT NULL                COMMENT '销售订单ID',
  sales_order_no    VARCHAR(40)     NOT NULL                COMMENT '销售订单编号',
  style_id          BIGINT(20)      NOT NULL                COMMENT '款式ID',
  sku_id            BIGINT(20)      NOT NULL                COMMENT '款式SKU ID',
  style_no          VARCHAR(40)     NOT NULL                COMMENT '款号',
  style_name        VARCHAR(100)    NOT NULL                COMMENT '款式名称',
  color_name        VARCHAR(50)     NOT NULL                COMMENT '颜色名称',
  size_name         VARCHAR(50)     NOT NULL                COMMENT '尺码名称',
  order_qty         DECIMAL(14,3)   NOT NULL DEFAULT 0.000  COMMENT '订单数量',
  shipped_qty       DECIMAL(14,3)   DEFAULT 0.000           COMMENT '已发货数量',
  unit_price        DECIMAL(14,2)   DEFAULT 0.00            COMMENT '销售单价',
  amount            DECIMAL(14,2)   DEFAULT 0.00            COMMENT '明细金额',
  delivery_date     DATE            DEFAULT NULL            COMMENT '明细要求交货日期',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (sales_order_detail_id),
  KEY idx_erp_sales_detail_order_id (sales_order_id),
  KEY idx_erp_sales_detail_style_id (style_id),
  KEY idx_erp_sales_detail_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单明细表';

-- ----------------------------
-- 11、采购订单主表
-- ----------------------------
CREATE TABLE erp_purchase_order (
  purchase_order_id BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '采购订单ID',
  purchase_order_no VARCHAR(40)     NOT NULL                COMMENT '采购订单编号',
  supplier_id       BIGINT(20)      NOT NULL                COMMENT '供应商ID',
  supplier_name     VARCHAR(100)    NOT NULL                COMMENT '供应商名称',
  order_date        DATE            NOT NULL                COMMENT '采购日期',
  expected_date     DATE            DEFAULT NULL            COMMENT '预计到货日期',
  source_type       VARCHAR(30)     DEFAULT NULL            COMMENT '来源类型，如生产备料、库存补货、样衣采购',
  source_no         VARCHAR(40)     DEFAULT NULL            COMMENT '来源单号',
  total_qty         DECIMAL(14,3)   DEFAULT 0.000           COMMENT '采购总数量',
  total_amount      DECIMAL(14,2)   DEFAULT 0.00            COMMENT '采购总金额',
  order_status      VARCHAR(20)     DEFAULT '草稿'          COMMENT '订单状态，如草稿、已审核、部分入库、已完成、已关闭',
  audit_by          VARCHAR(64)     DEFAULT NULL            COMMENT '审核人',
  audit_time        DATETIME        DEFAULT NULL            COMMENT '审核时间',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (purchase_order_id),
  UNIQUE KEY uk_erp_purchase_order_no (purchase_order_no),
  KEY idx_erp_purchase_order_supplier_id (supplier_id),
  KEY idx_erp_purchase_order_expected_date (expected_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单主表';

-- ----------------------------
-- 12、采购订单明细表
-- ----------------------------
CREATE TABLE erp_purchase_order_detail (
  purchase_order_detail_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '采购订单明细ID',
  purchase_order_id BIGINT(20)      NOT NULL                COMMENT '采购订单ID',
  purchase_order_no VARCHAR(40)     NOT NULL                COMMENT '采购订单编号',
  material_id       BIGINT(20)      NOT NULL                COMMENT '物料ID',
  material_sku_id   BIGINT(20)      DEFAULT NULL            COMMENT '物料SKU ID',
  material_code     VARCHAR(40)     NOT NULL                COMMENT '物料编码',
  material_name     VARCHAR(100)    NOT NULL                COMMENT '物料名称',
  color_name        VARCHAR(50)     DEFAULT NULL            COMMENT '颜色名称',
  spec_name         VARCHAR(100)    DEFAULT NULL            COMMENT '规格名称',
  order_qty         DECIMAL(14,3)   NOT NULL DEFAULT 0.000  COMMENT '采购数量',
  received_qty      DECIMAL(14,3)   DEFAULT 0.000           COMMENT '已入库数量',
  unit_name         VARCHAR(20)     NOT NULL                COMMENT '采购单位',
  unit_price        DECIMAL(14,4)   DEFAULT 0.0000          COMMENT '采购单价',
  amount            DECIMAL(14,2)   DEFAULT 0.00            COMMENT '明细金额',
  expected_date     DATE            DEFAULT NULL            COMMENT '预计到货日期',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (purchase_order_detail_id),
  KEY idx_erp_purchase_detail_order_id (purchase_order_id),
  KEY idx_erp_purchase_detail_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表';

-- ----------------------------
-- 13、仓库档案表
-- ----------------------------
CREATE TABLE erp_warehouse (
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

-- ----------------------------
-- 14、入库单主表
-- ----------------------------
CREATE TABLE erp_inbound_order (
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

-- ----------------------------
-- 15、入库单明细表
-- ----------------------------
CREATE TABLE erp_inbound_order_detail (
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

-- ----------------------------
-- 16、出库单主表
-- ----------------------------
CREATE TABLE erp_outbound_order (
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

-- ----------------------------
-- 17、出库单明细表
-- ----------------------------
CREATE TABLE erp_outbound_order_detail (
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

-- ----------------------------
-- 18、库存汇总表
-- ----------------------------
CREATE TABLE erp_inventory (
  inventory_id      BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  warehouse_id      BIGINT(20)      NOT NULL DEFAULT 0      COMMENT '仓库ID，未建仓库档案时可填0',
  warehouse_name    VARCHAR(80)     NOT NULL                COMMENT '仓库名称',
  item_type         VARCHAR(20)     NOT NULL                COMMENT '库存对象类型，如成衣、物料',
  item_id           BIGINT(20)      NOT NULL                COMMENT '库存对象ID，成衣对应款式SKU ID，物料对应物料SKU ID',
  item_code         VARCHAR(80)     NOT NULL                COMMENT '库存对象编码',
  item_name         VARCHAR(100)    NOT NULL                COMMENT '库存对象名称',
  color_name        VARCHAR(50)     DEFAULT NULL            COMMENT '颜色名称',
  size_name         VARCHAR(50)     DEFAULT NULL            COMMENT '尺码名称，成衣使用',
  spec_name         VARCHAR(100)    DEFAULT NULL            COMMENT '规格名称，物料使用',
  batch_no          VARCHAR(60)     DEFAULT ''              COMMENT '批次号',
  available_qty     DECIMAL(14,3)   DEFAULT 0.000           COMMENT '可用库存数量',
  locked_qty        DECIMAL(14,3)   DEFAULT 0.000           COMMENT '锁定库存数量',
  unit_price        DECIMAL(14,4)   DEFAULT 0.0000          COMMENT '库存单价',
  unit_name         VARCHAR(20)     NOT NULL                COMMENT '库存单位',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (inventory_id),
  UNIQUE KEY uk_erp_inventory_item_batch (warehouse_id, item_type, item_id, batch_no),
  KEY idx_erp_inventory_item_code (item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存汇总表';

-- ----------------------------
-- 19、库存流水表
-- ----------------------------
CREATE TABLE erp_inventory_transaction (
  transaction_id    BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '库存流水ID',
  transaction_no    VARCHAR(40)     NOT NULL                COMMENT '库存流水编号',
  transaction_type  VARCHAR(30)     NOT NULL                COMMENT '流水类型，如采购入库、生产领料、成衣入库、销售出库、盘点调整',
  business_no       VARCHAR(40)     DEFAULT NULL            COMMENT '业务单号',
  warehouse_id      BIGINT(20)      NOT NULL DEFAULT 0      COMMENT '仓库ID',
  warehouse_name    VARCHAR(80)     NOT NULL                COMMENT '仓库名称',
  item_type         VARCHAR(20)     NOT NULL                COMMENT '库存对象类型，如成衣、物料',
  item_id           BIGINT(20)      NOT NULL                COMMENT '库存对象ID',
  item_code         VARCHAR(80)     NOT NULL                COMMENT '库存对象编码',
  item_name         VARCHAR(100)    NOT NULL                COMMENT '库存对象名称',
  batch_no          VARCHAR(60)     DEFAULT ''              COMMENT '批次号',
  in_qty            DECIMAL(14,3)   DEFAULT 0.000           COMMENT '入库数量',
  out_qty           DECIMAL(14,3)   DEFAULT 0.000           COMMENT '出库数量',
  balance_qty       DECIMAL(14,3)   DEFAULT 0.000           COMMENT '流水后结存数量',
  unit_name         VARCHAR(20)     NOT NULL                COMMENT '单位',
  operator_name     VARCHAR(64)     DEFAULT NULL            COMMENT '操作人',
  transaction_time  DATETIME        DEFAULT NULL            COMMENT '流水发生时间',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (transaction_id),
  UNIQUE KEY uk_erp_inventory_transaction_no (transaction_no),
  KEY idx_erp_inventory_transaction_business_no (business_no),
  KEY idx_erp_inventory_transaction_item (item_type, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- ----------------------------
-- 15、生产订单主表
-- ----------------------------
CREATE TABLE erp_production_order (
  production_order_id BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '生产订单ID',
  production_order_no VARCHAR(40)  NOT NULL                COMMENT '生产订单编号',
  sales_order_id    BIGINT(20)     DEFAULT NULL            COMMENT '来源销售订单ID',
  sales_order_no    VARCHAR(40)    DEFAULT NULL            COMMENT '来源销售订单编号',
  customer_id       BIGINT(20)     DEFAULT NULL            COMMENT '客户ID',
  customer_name     VARCHAR(100)   DEFAULT NULL            COMMENT '客户名称',
  plan_start_date   DATE           DEFAULT NULL            COMMENT '计划开工日期',
  plan_finish_date  DATE           DEFAULT NULL            COMMENT '计划完工日期',
  total_qty         DECIMAL(14,3)  DEFAULT 0.000           COMMENT '生产总件数',
  completed_qty     DECIMAL(14,3)  DEFAULT 0.000           COMMENT '已完成件数',
  order_status      VARCHAR(20)    DEFAULT '草稿'          COMMENT '生产状态，如草稿、已下达、裁剪中、缝制中、后整中、已完工、已关闭',
  audit_by          VARCHAR(64)    DEFAULT NULL            COMMENT '审核人',
  audit_time        DATETIME       DEFAULT NULL            COMMENT '审核时间',
  create_by         VARCHAR(64)    DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME       DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)    DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME       DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)   DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (production_order_id),
  UNIQUE KEY uk_erp_production_order_no (production_order_no),
  KEY idx_erp_production_order_sales_no (sales_order_no),
  KEY idx_erp_production_order_plan_finish (plan_finish_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产订单主表';

-- ----------------------------
-- 16、生产订单明细表
-- ----------------------------
CREATE TABLE erp_production_order_detail (
  production_order_detail_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '生产订单明细ID',
  production_order_id BIGINT(20)   NOT NULL                COMMENT '生产订单ID',
  production_order_no VARCHAR(40)  NOT NULL                COMMENT '生产订单编号',
  style_id          BIGINT(20)     NOT NULL                COMMENT '款式ID',
  sku_id            BIGINT(20)     NOT NULL                COMMENT '款式SKU ID',
  style_no          VARCHAR(40)    NOT NULL                COMMENT '款号',
  style_name        VARCHAR(100)   NOT NULL                COMMENT '款式名称',
  color_name        VARCHAR(50)    NOT NULL                COMMENT '颜色名称',
  size_name         VARCHAR(50)    NOT NULL                COMMENT '尺码名称',
  plan_qty          DECIMAL(14,3)  NOT NULL DEFAULT 0.000  COMMENT '计划生产数量',
  cut_qty           DECIMAL(14,3)  DEFAULT 0.000           COMMENT '已裁剪数量',
  sewn_qty          DECIMAL(14,3)  DEFAULT 0.000           COMMENT '已缝制数量',
  finished_qty      DECIMAL(14,3)  DEFAULT 0.000           COMMENT '已后整完成数量',
  qualified_qty     DECIMAL(14,3)  DEFAULT 0.000           COMMENT '质检合格数量',
  create_by         VARCHAR(64)    DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME       DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)    DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME       DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)   DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (production_order_detail_id),
  KEY idx_erp_production_detail_order_id (production_order_id),
  KEY idx_erp_production_detail_style_id (style_id),
  KEY idx_erp_production_detail_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产订单明细表';

-- ----------------------------
-- 17、裁剪单表
-- ----------------------------
CREATE TABLE erp_cutting_order (
  cutting_order_id  BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '裁剪单ID',
  cutting_order_no  VARCHAR(40)     NOT NULL                COMMENT '裁剪单编号',
  production_order_id BIGINT(20)    NOT NULL                COMMENT '生产订单ID',
  production_order_no VARCHAR(40)   NOT NULL                COMMENT '生产订单编号',
  style_id          BIGINT(20)      NOT NULL                COMMENT '款式ID',
  style_no          VARCHAR(40)     NOT NULL                COMMENT '款号',
  color_name        VARCHAR(50)     DEFAULT NULL            COMMENT '颜色名称',
  fabric_batch_no   VARCHAR(60)     DEFAULT NULL            COMMENT '面料批次号或缸号',
  cutting_bed_no    VARCHAR(40)     DEFAULT NULL            COMMENT '裁床号',
  layer_count       INT(11)         DEFAULT 0               COMMENT '铺布层数',
  plan_qty          DECIMAL(14,3)   DEFAULT 0.000           COMMENT '计划裁剪数量',
  cut_qty           DECIMAL(14,3)   DEFAULT 0.000           COMMENT '实际裁剪数量',
  loss_qty          DECIMAL(14,3)   DEFAULT 0.000           COMMENT '裁剪损耗数量',
  cutting_status    VARCHAR(20)     DEFAULT '待裁剪'        COMMENT '裁剪状态，如待裁剪、裁剪中、已完成',
  operator_name     VARCHAR(64)     DEFAULT NULL            COMMENT '裁剪负责人',
  start_time        DATETIME        DEFAULT NULL            COMMENT '开始时间',
  finish_time       DATETIME        DEFAULT NULL            COMMENT '完成时间',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (cutting_order_id),
  UNIQUE KEY uk_erp_cutting_order_no (cutting_order_no),
  KEY idx_erp_cutting_order_production_id (production_order_id),
  KEY idx_erp_cutting_order_style_id (style_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='裁剪单表';

-- ----------------------------
-- 18、缝制产量上报表
-- ----------------------------
CREATE TABLE erp_sewing_report (
  sewing_report_id  BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '缝制上报ID',
  report_no         VARCHAR(40)     NOT NULL                COMMENT '上报单号',
  production_order_id BIGINT(20)    NOT NULL                COMMENT '生产订单ID',
  production_order_no VARCHAR(40)   NOT NULL                COMMENT '生产订单编号',
  style_id          BIGINT(20)      NOT NULL                COMMENT '款式ID',
  sku_id            BIGINT(20)      DEFAULT NULL            COMMENT '款式SKU ID',
  style_no          VARCHAR(40)     NOT NULL                COMMENT '款号',
  color_name        VARCHAR(50)     DEFAULT NULL            COMMENT '颜色名称',
  size_name         VARCHAR(50)     DEFAULT NULL            COMMENT '尺码名称',
  workshop_name     VARCHAR(80)     DEFAULT NULL            COMMENT '车间名称',
  line_name         VARCHAR(80)     DEFAULT NULL            COMMENT '生产线或组别',
  worker_name       VARCHAR(64)     DEFAULT NULL            COMMENT '员工姓名',
  process_name      VARCHAR(80)     DEFAULT NULL            COMMENT '工序名称',
  report_qty        DECIMAL(14,3)   NOT NULL DEFAULT 0.000  COMMENT '上报数量',
  qualified_qty     DECIMAL(14,3)   DEFAULT 0.000           COMMENT '合格数量',
  defective_qty     DECIMAL(14,3)   DEFAULT 0.000           COMMENT '不良数量',
  report_date       DATE            NOT NULL                COMMENT '上报日期',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (sewing_report_id),
  UNIQUE KEY uk_erp_sewing_report_no (report_no),
  KEY idx_erp_sewing_report_production_id (production_order_id),
  KEY idx_erp_sewing_report_date (report_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缝制产量上报表';

-- ----------------------------
-- 19、质量检验表
-- ----------------------------
CREATE TABLE erp_quality_check (
  quality_check_id  BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '质检ID',
  quality_check_no  VARCHAR(40)     NOT NULL                COMMENT '质检单号',
  production_order_id BIGINT(20)    DEFAULT NULL            COMMENT '生产订单ID',
  production_order_no VARCHAR(40)   DEFAULT NULL            COMMENT '生产订单编号',
  style_id          BIGINT(20)      NOT NULL                COMMENT '款式ID',
  sku_id            BIGINT(20)      DEFAULT NULL            COMMENT '款式SKU ID',
  style_no          VARCHAR(40)     NOT NULL                COMMENT '款号',
  color_name        VARCHAR(50)     DEFAULT NULL            COMMENT '颜色名称',
  size_name         VARCHAR(50)     DEFAULT NULL            COMMENT '尺码名称',
  check_type        VARCHAR(30)     NOT NULL                COMMENT '质检类型，如首检、巡检、成品质检、检针、安全检查',
  check_qty         DECIMAL(14,3)   NOT NULL DEFAULT 0.000  COMMENT '检验数量',
  qualified_qty     DECIMAL(14,3)   DEFAULT 0.000           COMMENT '合格数量',
  defective_qty     DECIMAL(14,3)   DEFAULT 0.000           COMMENT '不良数量',
  defect_desc       VARCHAR(500)    DEFAULT NULL            COMMENT '不良描述，如跳线、污渍、色差、尺寸偏差、纽扣松动',
  check_result      VARCHAR(20)     DEFAULT '待判定'        COMMENT '检验结果，如合格、不合格、待返工、待判定',
  inspector_name    VARCHAR(64)     DEFAULT NULL            COMMENT '质检员',
  check_time        DATETIME        DEFAULT NULL            COMMENT '检验时间',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (quality_check_id),
  UNIQUE KEY uk_erp_quality_check_no (quality_check_no),
  KEY idx_erp_quality_check_production_id (production_order_id),
  KEY idx_erp_quality_check_style_id (style_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量检验表';

-- ----------------------------
-- 20、发货单表
-- ----------------------------
CREATE TABLE erp_delivery_order (
  delivery_order_id BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '发货单ID',
  delivery_order_no VARCHAR(40)     NOT NULL                COMMENT '发货单编号',
  sales_order_id    BIGINT(20)      DEFAULT NULL            COMMENT '销售订单ID',
  sales_order_no    VARCHAR(40)     DEFAULT NULL            COMMENT '销售订单编号',
  customer_id       BIGINT(20)      NOT NULL                COMMENT '客户ID',
  customer_name     VARCHAR(100)    NOT NULL                COMMENT '客户名称',
  delivery_date     DATE            NOT NULL                COMMENT '发货日期',
  delivery_qty      DECIMAL(14,3)   DEFAULT 0.000           COMMENT '发货总件数',
  carton_count      INT(11)         DEFAULT 0               COMMENT '箱数',
  logistics_company VARCHAR(100)    DEFAULT NULL            COMMENT '物流公司',
  tracking_no       VARCHAR(100)    DEFAULT NULL            COMMENT '物流单号',
  receiver_name     VARCHAR(50)     DEFAULT NULL            COMMENT '收货人',
  receiver_phone    VARCHAR(30)     DEFAULT NULL            COMMENT '收货电话',
  receiver_address  VARCHAR(255)    DEFAULT NULL            COMMENT '收货地址',
  delivery_status   VARCHAR(20)     DEFAULT '待发货'        COMMENT '发货状态，如待发货、已发货、已签收、已取消',
  create_by         VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME        DEFAULT NULL            COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME        DEFAULT NULL            COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (delivery_order_id),
  UNIQUE KEY uk_erp_delivery_order_no (delivery_order_no),
  KEY idx_erp_delivery_order_sales_no (sales_order_no),
  KEY idx_erp_delivery_order_customer_id (customer_id),
  KEY idx_erp_delivery_order_date (delivery_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发货单表';

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 客户档案菜单与权限按钮
-- 如已存在相同ID菜单，可先调整下面的menu_id后再执行
-- ----------------------------
delete from sys_menu where menu_id in (3000, 3001, 3002, 3003, 3004, 3005, 3006, 3020, 3021, 3022, 3023);
insert into sys_menu values(3000, 'ERP管理', '0', '5', 'erp', null, '', '', 1, 0, 'M', '0', '0', '', 'tree', 'admin', sysdate(), '', null, 'ERP业务管理目录');
insert into sys_menu values(3020, '基础资料', '3000', '1', 'basic', null, '', '', 1, 0, 'M', '0', '0', '', 'dict', 'admin', sysdate(), '', null, 'ERP基础资料目录');
insert into sys_menu values(3021, '订单管理', '3000', '2', 'order', null, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '', null, 'ERP订单管理目录');
insert into sys_menu values(3022, '库存管理', '3000', '3', 'stock', null, '', '', 1, 0, 'M', '0', '0', '', 'warehouse', 'admin', sysdate(), '', null, 'ERP库存管理目录');
insert into sys_menu values(3023, '生产管理', '3000', '4', 'manufacture', null, '', '', 1, 0, 'M', '0', '0', '', 'job', 'admin', sysdate(), '', null, 'ERP生产管理目录');
insert into sys_menu values(3001, '客户档案', '3020', '1', 'customer', 'erp/customer/index', '', '', 1, 0, 'C', '0', '0', 'erp:customer:list', 'peoples', 'admin', sysdate(), '', null, '客户档案菜单');
insert into sys_menu values(3002, '客户查询', '3001', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:customer:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3003, '客户新增', '3001', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:customer:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3004, '客户修改', '3001', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:customer:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3005, '客户删除', '3001', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:customer:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3006, '客户导出', '3001', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:customer:export', '#', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 物料档案菜单与权限按钮
-- 如已存在相同ID菜单，可先调整下面的menu_id后再执行
-- ----------------------------
delete from sys_menu where menu_id in (3010, 3011, 3012, 3013, 3014, 3015, 3016);
insert into sys_menu values(3010, '物料档案', '3020', '3', 'material', 'erp/material/index', '', '', 1, 0, 'C', '0', '0', 'erp:material:list', 'component', 'admin', sysdate(), '', null, '物料档案菜单');
insert into sys_menu values(3011, '物料查询', '3010', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:material:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3012, '物料新增', '3010', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:material:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3013, '物料修改', '3010', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:material:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3014, '物料删除', '3010', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:material:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3015, '物料导出', '3010', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:material:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3016, '物料编码生成', '3010', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:material:code', '#', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 库存模块菜单与权限按钮
-- ----------------------------
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
insert into sys_menu values(3080, '生产订单', '3023', '1', 'production', 'erp/production/index', '', '', 1, 0, 'C', '0', '0', 'erp:production:list', 'job', 'admin', sysdate(), '', null, '生产订单菜单');
insert into sys_menu values(3081, '生产查询', '3080', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:production:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3082, '生产新增', '3080', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:production:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3083, '生产修改', '3080', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:production:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3084, '生产删除', '3080', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:production:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3085, '生产导出', '3080', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:production:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3086, '生产下达', '3080', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:production:release', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3087, '生产关闭', '3080', '7', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:production:close', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(3088, '生成领料单', '3080', '8', '#', '', '', '', 1, 0, 'F', '0', '0', 'erp:production:picking', '#', 'admin', sysdate(), '', null, '');

-- 重置 ERP 业务数据并生成一套可联调测试的数据
-- 适用场景：已经建好 RuoYi 系统表和 ERP 表后，清空 ERP 业务数据，重新生成当前库存流程演示数据
-- 注意：本脚本不会删除 sys_menu、sys_user 等系统管理数据

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1、清空 ERP 业务数据
-- ----------------------------
DELETE FROM erp_delivery_order;
DELETE FROM erp_quality_check;
DELETE FROM erp_sewing_report;
DELETE FROM erp_cut_order_detail;
DELETE FROM erp_cut_order;
DELETE FROM erp_cutting_order;
DELETE FROM erp_production_order_detail;
DELETE FROM erp_production_order;
DELETE FROM erp_inventory_transaction;
DELETE FROM erp_outbound_order_detail;
DELETE FROM erp_outbound_order;
DELETE FROM erp_inbound_order_detail;
DELETE FROM erp_inbound_order;
DELETE FROM erp_inventory;
DELETE FROM erp_warehouse;
DELETE FROM erp_purchase_order_detail;
DELETE FROM erp_purchase_order;
DELETE FROM erp_sales_order_detail;
DELETE FROM erp_sales_order;
DELETE FROM erp_style_bom_detail;
DELETE FROM erp_style_bom;
DELETE FROM erp_material_sku;
DELETE FROM erp_material;
DELETE FROM erp_style_sku;
DELETE FROM erp_style;
DELETE FROM erp_supplier;
DELETE FROM erp_customer;

-- ----------------------------
-- 2、基础档案：客户、供应商、仓库
-- ----------------------------
INSERT INTO erp_customer
(customer_code, customer_name, customer_type, contact_name, contact_phone, contact_email, province, city, county, address,
 credit_limit, settlement_type, status, del_flag, create_by, create_time, remark)
VALUES
('CUS-FLOW-001', '杭州星芽童装品牌有限公司', '品牌客户', '陈晓云', '13857010001', 'buyer01@xingya-kids.com', '浙江省', '杭州市', '滨江区', '滨盛路童装产业园A座201', 300000.00, '月结30天', '0', '0', 'admin', sysdate(), '用于测试销售订单与成衣出库'),
('CUS-FLOW-002', '上海暖树儿童用品有限公司', '品牌客户', '刘安琪', '13916020002', 'liuanqi@warmtree.cn', '上海市', '上海市', '闵行区', '虹梅南路儿童生活馆5楼', 500000.00, '月结45天', '0', '0', 'admin', sysdate(), '用于测试大客户订单');

INSERT INTO erp_supplier
(supplier_code, supplier_name, supplier_type, contact_name, contact_phone, contact_email, province, city, address,
 lead_time_days, settlement_type, status, del_flag, create_by, create_time, remark)
VALUES
('SUP-FAB-001', '绍兴晨织纺织有限公司', '面料', '周晨', '13757570001', 'fabric@chenzhi.cn', '浙江省', '绍兴市', '柯桥轻纺城东区18号', 7, '月结30天', '0', '0', 'admin', sysdate(), '主供棉毛布、罗纹、卫衣布'),
('SUP-ACC-001', '义乌童辅辅料商行', '辅料', '何丽', '13657990002', 'acc@kids.cn', '浙江省', '义乌市', '国际商贸城三区', 5, '货到付款', '0', '0', 'admin', sysdate(), '主供纽扣、拉链、松紧带'),
('SUP-PKG-001', '杭州明印包装有限公司', '包装', '赵明', '13588880003', 'pack@mingyin.cn', '浙江省', '杭州市', '萧山区包装产业园', 4, '月结30天', '0', '0', 'admin', sysdate(), '主供吊牌、包装袋');

INSERT INTO erp_warehouse
(warehouse_code, warehouse_name, warehouse_type, manager_name, contact_phone, province, city, county, address,
 status, del_flag, create_by, create_time, remark)
VALUES
('WH-MAT-001', '一号物料仓', '物料仓', '王仓', '13800000001', '浙江省', '杭州市', '滨江区', '童装工厂一号仓', '0', '0', 'admin', sysdate(), '物料入库、生产领料主仓'),
('WH-FG-001', '一号成衣仓', '成衣仓', '李仓', '13800000002', '浙江省', '杭州市', '滨江区', '童装工厂二号仓', '0', '0', 'admin', sysdate(), '成衣入库、销售出库主仓'),
('WH-QC-001', '质检暂存仓', '次品仓', '赵质检', '13800000003', '浙江省', '杭州市', '滨江区', '质检暂存区', '0', '0', 'admin', sysdate(), '待检和不合格暂存');

SELECT @wh_mat := warehouse_id FROM erp_warehouse WHERE warehouse_code = 'WH-MAT-001';
SELECT @wh_fg := warehouse_id FROM erp_warehouse WHERE warehouse_code = 'WH-FG-001';
SELECT @sup_fab := supplier_id FROM erp_supplier WHERE supplier_code = 'SUP-FAB-001';
SELECT @sup_acc := supplier_id FROM erp_supplier WHERE supplier_code = 'SUP-ACC-001';
SELECT @sup_pkg := supplier_id FROM erp_supplier WHERE supplier_code = 'SUP-PKG-001';
SELECT @cus_001 := customer_id FROM erp_customer WHERE customer_code = 'CUS-FLOW-001';

-- ----------------------------
-- 3、物料档案与物料 SKU
-- ----------------------------
INSERT INTO erp_material
(material_code, material_name, material_type, category_name, unit_name, default_supplier_id, default_supplier_name,
 safe_stock_qty, status, del_flag, create_by, create_time, remark)
VALUES
('FAB-COT-0001', '32S精梳棉毛布', '面料', '棉毛布', '米', @sup_fab, '绍兴晨织纺织有限公司', 500.000, '0', '0', 'admin', sysdate(), 'T恤、家居服常用大身面料'),
('FAB-RIB-0002', '1x1弹力罗纹', '面料', '罗纹', '米', @sup_fab, '绍兴晨织纺织有限公司', 200.000, '0', '0', 'admin', sysdate(), '领口、袖口、脚口用料'),
('FAB-FLC-0003', '280g卫衣毛圈布', '面料', '卫衣布', '米', @sup_fab, '绍兴晨织纺织有限公司', 300.000, '0', '0', 'admin', sysdate(), '秋冬卫衣主料'),
('ACC-ZIP-0004', '5号尼龙闭尾拉链', '辅料', '拉链', '条', @sup_acc, '义乌童辅辅料商行', 500.000, '0', '0', 'admin', sysdate(), '连帽卫衣、外套用拉链'),
('ACC-BTN-0005', '11mm四眼树脂纽扣', '辅料', '纽扣', '个', @sup_acc, '义乌童辅辅料商行', 2000.000, '0', '0', 'admin', sysdate(), '裙装、衬衫类常用纽扣'),
('PKG-BAG-0006', '透明自封包装袋', '包装材料', '包装袋', '个', @sup_pkg, '杭州明印包装有限公司', 3000.000, '0', '0', 'admin', sysdate(), '成衣单件包装');

SELECT @mat_cot := material_id FROM erp_material WHERE material_code = 'FAB-COT-0001';
SELECT @mat_rib := material_id FROM erp_material WHERE material_code = 'FAB-RIB-0002';
SELECT @mat_flc := material_id FROM erp_material WHERE material_code = 'FAB-FLC-0003';
SELECT @mat_zip := material_id FROM erp_material WHERE material_code = 'ACC-ZIP-0004';
SELECT @mat_btn := material_id FROM erp_material WHERE material_code = 'ACC-BTN-0005';
SELECT @mat_bag := material_id FROM erp_material WHERE material_code = 'PKG-BAG-0006';

INSERT INTO erp_material_sku
(material_id, material_code, material_sku_code, color_code, color_name, spec_name, width_value, gram_weight, unit_name,
 barcode, status, del_flag, create_by, create_time, remark)
VALUES
(@mat_cot, 'FAB-COT-0001', 'FAB-COT-0001-IVORY-165-180', 'IVORY', '象牙白', '165cm/180g', 165.00, 180.00, '米', '6900001000011', '0', '0', 'admin', sysdate(), '可测试入库与生产领料'),
(@mat_cot, 'FAB-COT-0001', 'FAB-COT-0001-PINK-165-180', 'PINK', '蜜桃粉', '165cm/180g', 165.00, 180.00, '米', '6900001000012', '0', '0', 'admin', sysdate(), '可测试新增入库'),
(@mat_rib, 'FAB-RIB-0002', 'FAB-RIB-0002-IVORY-90-220', 'IVORY', '象牙白', '90cm/220g', 90.00, 220.00, '米', '6900001000021', '0', '0', 'admin', sysdate(), '可测试草稿入库过账'),
(@mat_flc, 'FAB-FLC-0003', 'FAB-FLC-0003-GRAY-175-280', 'GRAY', '浅花灰', '175cm/280g', 175.00, 280.00, '米', '6900001000031', '0', '0', 'admin', sysdate(), '卫衣主料'),
(@mat_zip, 'ACC-ZIP-0004', 'ACC-ZIP-0004-NAVY-35CM', 'NAVY', '藏青', '35cm', NULL, NULL, '条', '6900001000041', '0', '0', 'admin', sysdate(), '可测试库存不足出库'),
(@mat_btn, 'ACC-BTN-0005', 'ACC-BTN-0005-WHITE-11MM', 'WHITE', '奶白', '11mm', NULL, NULL, '个', '6900001000051', '0', '0', 'admin', sysdate(), '辅料库存'),
(@mat_bag, 'PKG-BAG-0006', 'PKG-BAG-0006-STD-28X38', NULL, '标准', '28x38cm', NULL, NULL, '个', '6900001000061', '0', '0', 'admin', sysdate(), '包装袋库存');

SELECT @sku_cot_ivory := material_sku_id FROM erp_material_sku WHERE material_sku_code = 'FAB-COT-0001-IVORY-165-180';
SELECT @sku_cot_pink := material_sku_id FROM erp_material_sku WHERE material_sku_code = 'FAB-COT-0001-PINK-165-180';
SELECT @sku_rib_ivory := material_sku_id FROM erp_material_sku WHERE material_sku_code = 'FAB-RIB-0002-IVORY-90-220';
SELECT @sku_flc_gray := material_sku_id FROM erp_material_sku WHERE material_sku_code = 'FAB-FLC-0003-GRAY-175-280';
SELECT @sku_zip_navy := material_sku_id FROM erp_material_sku WHERE material_sku_code = 'ACC-ZIP-0004-NAVY-35CM';
SELECT @sku_btn_white := material_sku_id FROM erp_material_sku WHERE material_sku_code = 'ACC-BTN-0005-WHITE-11MM';
SELECT @sku_bag_std := material_sku_id FROM erp_material_sku WHERE material_sku_code = 'PKG-BAG-0006-STD-28X38';

-- ----------------------------
-- 4、款式、成衣 SKU、BOM，用于款式/BOM查看
-- ----------------------------
INSERT INTO erp_style
(style_no, style_name, category_name, brand_name, season_name, year_name, gender_type, designer, sample_status,
 production_status, image_url, status, del_flag, create_by, create_time, remark)
VALUES
('KDS-FLOW-001', '云朵印花短袖T恤', 'T恤', '一丫一童装', '春夏', '2026', '中性', '林可', '2', '0',
 'https://dummyimage.com/360x360/91c9f7/ffffff&text=KDS-FLOW-001', '0', '0', 'admin', sysdate(), '用于测试BOM和物料领用'),
('KDS-FLOW-002', '拼色连帽卫衣', '卫衣', '一丫一童装', '秋冬', '2026', '中性', '周铠', '2', '0',
 'https://dummyimage.com/360x360/778beb/ffffff&text=KDS-FLOW-002', '0', '0', 'admin', sysdate(), '用于测试拉链和卫衣布BOM');

SELECT @style_ts := style_id FROM erp_style WHERE style_no = 'KDS-FLOW-001';
SELECT @style_hd := style_id FROM erp_style WHERE style_no = 'KDS-FLOW-002';

INSERT INTO erp_style_sku
(style_id, style_no, sku_code, color_code, color_name, size_code, size_name, barcode, retail_price, status, del_flag, create_by, create_time, remark)
VALUES
(@style_ts, 'KDS-FLOW-001', 'KDS-FLOW-001-IVORY-110', 'IVORY', '象牙白', '110', '110', '8800001000011', 69.00, '0', '0', 'admin', sysdate(), '测试SKU'),
(@style_ts, 'KDS-FLOW-001', 'KDS-FLOW-001-IVORY-120', 'IVORY', '象牙白', '120', '120', '8800001000012', 69.00, '0', '0', 'admin', sysdate(), '测试SKU'),
(@style_hd, 'KDS-FLOW-002', 'KDS-FLOW-002-GRAY-120', 'GRAY', '浅花灰', '120', '120', '8800001000021', 129.00, '0', '0', 'admin', sysdate(), '测试SKU');

SELECT @style_sku_ts_110 := sku_id FROM erp_style_sku WHERE sku_code = 'KDS-FLOW-001-IVORY-110';
SELECT @style_sku_ts_120 := sku_id FROM erp_style_sku WHERE sku_code = 'KDS-FLOW-001-IVORY-120';
SELECT @style_sku_hd_120 := sku_id FROM erp_style_sku WHERE sku_code = 'KDS-FLOW-002-GRAY-120';

INSERT INTO erp_style_bom
(bom_no, style_id, style_no, version_no, bom_status, effective_date, audit_by, audit_time, create_by, create_time, remark)
VALUES
('KDS-FLOW-001-BOM-V1', @style_ts, 'KDS-FLOW-001', 'V1', '1', curdate(), 'admin', sysdate(), 'admin', sysdate(), 'T恤标准BOM'),
('KDS-FLOW-002-BOM-V1', @style_hd, 'KDS-FLOW-002', 'V1', '1', curdate(), 'admin', sysdate(), 'admin', sysdate(), '卫衣标准BOM');

SELECT @bom_ts := bom_id FROM erp_style_bom WHERE bom_no = 'KDS-FLOW-001-BOM-V1';
SELECT @bom_hd := bom_id FROM erp_style_bom WHERE bom_no = 'KDS-FLOW-002-BOM-V1';

INSERT INTO erp_style_bom_detail
(bom_id, material_id, material_sku_id, material_code, material_name, material_type, color_name, spec_name,
 usage_qty, loss_rate, unit_name, position_name, create_by, create_time, remark)
VALUES
(@bom_ts, @mat_cot, @sku_cot_ivory, 'FAB-COT-0001', '32S精梳棉毛布', '面料', '象牙白', '165cm/180g', 0.6500, 0.0400, '米', '大身', 'admin', sysdate(), 'T恤主料'),
(@bom_ts, @mat_rib, @sku_rib_ivory, 'FAB-RIB-0002', '1x1弹力罗纹', '面料', '象牙白', '90cm/220g', 0.1200, 0.0500, '米', '领口袖口', 'admin', sysdate(), '罗纹口'),
(@bom_ts, @mat_bag, @sku_bag_std, 'PKG-BAG-0006', '透明自封包装袋', '包装材料', '标准', '28x38cm', 1.0000, 0.0000, '个', '包装', 'admin', sysdate(), '包装袋'),
(@bom_hd, @mat_flc, @sku_flc_gray, 'FAB-FLC-0003', '280g卫衣毛圈布', '面料', '浅花灰', '175cm/280g', 0.9200, 0.0500, '米', '大身', 'admin', sysdate(), '卫衣主料'),
(@bom_hd, @mat_zip, @sku_zip_navy, 'ACC-ZIP-0004', '5号尼龙闭尾拉链', '辅料', '藏青', '35cm', 1.0000, 0.0000, '条', '前中', 'admin', sysdate(), '前中拉链');

-- ----------------------------
-- 5、采购单、入库单、出库单、库存与流水
-- ----------------------------
INSERT INTO erp_purchase_order
(purchase_order_no, supplier_id, supplier_name, order_date, expected_date, source_type, source_no,
 total_qty, total_amount, order_status, audit_by, audit_time, create_by, create_time, remark)
VALUES
('PO-DEMO-001', @sup_fab, '绍兴晨织纺织有限公司', '2026-05-20', '2026-05-25', '库存补货', NULL, 1000.000, 18000.00, '已完成', 'admin', sysdate(), 'admin', sysdate(), '已通过入库单完成入库'),
('PO-DEMO-002', @sup_fab, '绍兴晨织纺织有限公司', '2026-05-31', '2026-06-03', '生产备料', 'MO-DEMO-001', 200.000, 2800.00, '已审核', 'admin', sysdate(), 'admin', sysdate(), '用于测试草稿入库单过账');

SELECT @po_001 := purchase_order_id FROM erp_purchase_order WHERE purchase_order_no = 'PO-DEMO-001';
SELECT @po_002 := purchase_order_id FROM erp_purchase_order WHERE purchase_order_no = 'PO-DEMO-002';

INSERT INTO erp_purchase_order_detail
(purchase_order_id, purchase_order_no, material_id, material_sku_id, material_code, material_name, color_name, spec_name,
 order_qty, received_qty, unit_name, unit_price, amount, expected_date, create_by, create_time, remark)
VALUES
(@po_001, 'PO-DEMO-001', @mat_cot, @sku_cot_ivory, 'FAB-COT-0001', '32S精梳棉毛布', '象牙白', '165cm/180g', 1000.000, 1000.000, '米', 18.0000, 18000.00, '2026-05-25', 'admin', sysdate(), '已入库'),
(@po_002, 'PO-DEMO-002', @mat_rib, @sku_rib_ivory, 'FAB-RIB-0002', '1x1弹力罗纹', '象牙白', '90cm/220g', 200.000, 0.000, '米', 14.0000, 2800.00, '2026-06-03', 'admin', sysdate(), '待入库');

-- 已过账入库单：验证库存汇总、库存流水、已入库状态
INSERT INTO erp_inbound_order
(inbound_order_no, inbound_type, source_type, source_no, supplier_id, supplier_name, warehouse_id, warehouse_name,
 inbound_date, total_qty, total_amount, order_status, audit_by, audit_time, post_by, post_time, create_by, create_time, remark)
VALUES
('RK-DEMO-POST-001', '采购入库', '采购订单', 'PO-DEMO-001', @sup_fab, '绍兴晨织纺织有限公司', @wh_mat, '一号物料仓',
 '2026-05-25', 1000.000, 18000.00, '已入库', 'admin', sysdate(), 'admin', sysdate(), 'admin', sysdate(), '已过账：用于验证库存和流水');

SELECT @rk_post := inbound_order_id FROM erp_inbound_order WHERE inbound_order_no = 'RK-DEMO-POST-001';

INSERT INTO erp_inbound_order_detail
(inbound_order_id, inbound_order_no, item_type, item_id, item_code, item_name, color_name, spec_name, batch_no,
 plan_qty, inbound_qty, unit_name, unit_price, amount, quality_status, create_by, create_time, remark)
VALUES
(@rk_post, 'RK-DEMO-POST-001', '物料', @sku_cot_ivory, 'FAB-COT-0001-IVORY-165-180', '32S精梳棉毛布', '象牙白', '165cm/180g', 'FAB-INIT-01',
 1000.000, 1000.000, '米', 18.0000, 18000.00, '合格', 'admin', sysdate(), '初始入库');

-- 草稿入库单：进入入库单页面后可直接点击过账，过账后库存会新增一行 RIB-TEST-01
INSERT INTO erp_inbound_order
(inbound_order_no, inbound_type, source_type, source_no, supplier_id, supplier_name, warehouse_id, warehouse_name,
 inbound_date, total_qty, total_amount, order_status, create_by, create_time, remark)
VALUES
('RK-DEMO-DRAFT-001', '采购入库', '采购订单', 'PO-DEMO-002', @sup_fab, '绍兴晨织纺织有限公司', @wh_mat, '一号物料仓',
 '2026-05-31', 200.000, 2800.00, '草稿', 'admin', sysdate(), '测试点：保存后点击过账，库存增加');

SELECT @rk_draft := inbound_order_id FROM erp_inbound_order WHERE inbound_order_no = 'RK-DEMO-DRAFT-001';

INSERT INTO erp_inbound_order_detail
(inbound_order_id, inbound_order_no, item_type, item_id, item_code, item_name, color_name, spec_name, batch_no,
 plan_qty, inbound_qty, unit_name, unit_price, amount, quality_status, create_by, create_time, remark)
VALUES
(@rk_draft, 'RK-DEMO-DRAFT-001', '物料', @sku_rib_ivory, 'FAB-RIB-0002-IVORY-90-220', '1x1弹力罗纹', '象牙白', '90cm/220g', 'RIB-TEST-01',
 200.000, 200.000, '米', 14.0000, 2800.00, '待检', 'admin', sysdate(), '待过账入库');

-- 生产订单与出库单来源
INSERT INTO erp_production_order
(production_order_no, sales_order_id, sales_order_no, customer_id, customer_name, plan_start_date, plan_finish_date,
 total_qty, completed_qty, order_status, audit_by, audit_time, create_by, create_time, remark)
VALUES
('MO-DEMO-001', NULL, NULL, @cus_001, '杭州星芽童装品牌有限公司', '2026-06-01', '2026-06-08',
 300.000, 0.000, '已下达', 'admin', sysdate(), 'admin', sysdate(), '用于测试生产领料出库');

SELECT @mo_001 := production_order_id FROM erp_production_order WHERE production_order_no = 'MO-DEMO-001';

INSERT INTO erp_production_order_detail
(production_order_id, production_order_no, style_id, sku_id, style_no, style_name, color_name, size_name,
 plan_qty, cut_qty, sewn_qty, finished_qty, qualified_qty, create_by, create_time, remark)
VALUES
(@mo_001, 'MO-DEMO-001', @style_ts, @style_sku_ts_110, 'KDS-FLOW-001', '云朵印花短袖T恤', '象牙白', '110',
 120.000, 0.000, 0.000, 0.000, 0.000, 'admin', sysdate(), '生产订单测试明细'),
(@mo_001, 'MO-DEMO-001', @style_ts, @style_sku_ts_120, 'KDS-FLOW-001', '云朵印花短袖T恤', '象牙白', '120',
 180.000, 0.000, 0.000, 0.000, 0.000, 'admin', sysdate(), '生产订单测试明细');

-- 已过账出库单：从 FAB-INIT-01 扣减 120 米，当前库存应为 880 米
INSERT INTO erp_outbound_order
(outbound_order_no, outbound_type, source_type, source_no, customer_id, customer_name, warehouse_id, warehouse_name,
 outbound_date, total_qty, total_amount, order_status, audit_by, audit_time, pick_by, pick_time, post_by, post_time,
 create_by, create_time, remark)
VALUES
('CK-DEMO-POST-001', '生产领料', '生产订单', 'MO-DEMO-001', @cus_001, '杭州星芽童装品牌有限公司', @wh_mat, '一号物料仓',
 '2026-05-30', 120.000, 2160.00, '已出库', 'admin', sysdate(), 'admin', sysdate(), 'admin', sysdate(),
 'admin', sysdate(), '已过账：验证库存扣减和流水');

SELECT @ck_post := outbound_order_id FROM erp_outbound_order WHERE outbound_order_no = 'CK-DEMO-POST-001';

INSERT INTO erp_outbound_order_detail
(outbound_order_id, outbound_order_no, item_type, item_id, item_code, item_name, color_name, spec_name, batch_no,
 plan_qty, locked_qty, outbound_qty, unit_name, unit_price, amount, create_by, create_time, remark)
VALUES
(@ck_post, 'CK-DEMO-POST-001', '物料', @sku_cot_ivory, 'FAB-COT-0001-IVORY-165-180', '32S精梳棉毛布', '象牙白', '165cm/180g', 'FAB-INIT-01',
 120.000, 0.000, 120.000, '米', 18.0000, 2160.00, 'admin', sysdate(), '生产领料已出库');

-- 草稿出库单：可直接过账，过账后 FAB-INIT-01 从 880 降到 820
INSERT INTO erp_outbound_order
(outbound_order_no, outbound_type, source_type, source_no, customer_id, customer_name, warehouse_id, warehouse_name,
 outbound_date, total_qty, total_amount, order_status, create_by, create_time, remark)
VALUES
('CK-DEMO-DRAFT-001', '生产领料', '生产订单', 'MO-DEMO-001', @cus_001, '杭州星芽童装品牌有限公司', @wh_mat, '一号物料仓',
 '2026-05-31', 60.000, 1080.00, '草稿', 'admin', sysdate(), '测试点：点击过账后库存减少');

SELECT @ck_draft := outbound_order_id FROM erp_outbound_order WHERE outbound_order_no = 'CK-DEMO-DRAFT-001';

INSERT INTO erp_outbound_order_detail
(outbound_order_id, outbound_order_no, item_type, item_id, item_code, item_name, color_name, spec_name, batch_no,
 plan_qty, locked_qty, outbound_qty, unit_name, unit_price, amount, create_by, create_time, remark)
VALUES
(@ck_draft, 'CK-DEMO-DRAFT-001', '物料', @sku_cot_ivory, 'FAB-COT-0001-IVORY-165-180', '32S精梳棉毛布', '象牙白', '165cm/180g', 'FAB-INIT-01',
 60.000, 0.000, 60.000, '米', 18.0000, 1080.00, 'admin', sysdate(), '待过账生产领料');

-- 库存不足测试单：当前拉链库存 300 条，本单要出 9999 条，过账应被拦截
INSERT INTO erp_outbound_order
(outbound_order_no, outbound_type, source_type, source_no, supplier_id, supplier_name, warehouse_id, warehouse_name,
 outbound_date, total_qty, total_amount, order_status, create_by, create_time, remark)
VALUES
('CK-DEMO-LOW-001', '生产领料', '生产订单', 'MO-DEMO-001', NULL, NULL, @wh_mat, '一号物料仓',
 '2026-05-31', 9999.000, 79992.00, '草稿', 'admin', sysdate(), '测试点：点击过账应提示库存不足');

SELECT @ck_low := outbound_order_id FROM erp_outbound_order WHERE outbound_order_no = 'CK-DEMO-LOW-001';

INSERT INTO erp_outbound_order_detail
(outbound_order_id, outbound_order_no, item_type, item_id, item_code, item_name, color_name, spec_name, batch_no,
 plan_qty, locked_qty, outbound_qty, unit_name, unit_price, amount, create_by, create_time, remark)
VALUES
(@ck_low, 'CK-DEMO-LOW-001', '物料', @sku_zip_navy, 'ACC-ZIP-0004-NAVY-35CM', '5号尼龙闭尾拉链', '藏青', '35cm', 'ACC-INIT-01',
 9999.000, 0.000, 9999.000, '条', 8.0000, 79992.00, 'admin', sysdate(), '库存不足测试');

-- 库存汇总：与已过账单据保持一致。草稿单据不影响库存。
INSERT INTO erp_inventory
(warehouse_id, warehouse_name, item_type, item_id, item_code, item_name, color_name, size_name, spec_name, batch_no,
 available_qty, locked_qty, unit_name, update_time, remark)
VALUES
(@wh_mat, '一号物料仓', '物料', @sku_cot_ivory, 'FAB-COT-0001-IVORY-165-180', '32S精梳棉毛布', '象牙白', NULL, '165cm/180g', 'FAB-INIT-01', 880.000, 0.000, '米', sysdate(), '1000入库 - 120出库 = 880'),
(@wh_mat, '一号物料仓', '物料', @sku_zip_navy, 'ACC-ZIP-0004-NAVY-35CM', '5号尼龙闭尾拉链', '藏青', NULL, '35cm', 'ACC-INIT-01', 300.000, 0.000, '条', sysdate(), '用于测试库存不足出库'),
(@wh_mat, '一号物料仓', '物料', @sku_btn_white, 'ACC-BTN-0005-WHITE-11MM', '11mm四眼树脂纽扣', '奶白', NULL, '11mm', 'ACC-INIT-02', 5000.000, 0.000, '个', sysdate(), '辅料现存库存'),
(@wh_mat, '一号物料仓', '物料', @sku_bag_std, 'PKG-BAG-0006-STD-28X38', '透明自封包装袋', '标准', NULL, '28x38cm', 'PKG-INIT-01', 8000.000, 0.000, '个', sysdate(), '包装材料现存库存');

INSERT INTO erp_inventory_transaction
(transaction_no, transaction_type, business_no, warehouse_id, warehouse_name, item_type, item_id, item_code, item_name,
 batch_no, in_qty, out_qty, balance_qty, unit_name, operator_name, transaction_time, create_by, create_time, remark)
VALUES
('LS-DEMO-0001', '采购入库', 'RK-DEMO-POST-001', @wh_mat, '一号物料仓', '物料', @sku_cot_ivory, 'FAB-COT-0001-IVORY-165-180', '32S精梳棉毛布', 'FAB-INIT-01', 1000.000, 0.000, 1000.000, '米', 'admin', '2026-05-25 09:00:00', 'admin', sysdate(), '采购入库过账'),
('LS-DEMO-0002', '生产领料', 'CK-DEMO-POST-001', @wh_mat, '一号物料仓', '物料', @sku_cot_ivory, 'FAB-COT-0001-IVORY-165-180', '32S精梳棉毛布', 'FAB-INIT-01', 0.000, 120.000, 880.000, '米', 'admin', '2026-05-30 10:00:00', 'admin', sysdate(), '生产领料过账');

SET FOREIGN_KEY_CHECKS = 1;

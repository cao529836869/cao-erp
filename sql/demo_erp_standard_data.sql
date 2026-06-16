-- 当前客户档案、童装款式、物料档案标准演示数据
-- 主档案共 25 条：客户 8 条、款式 7 条、物料 10 条。
-- 同时生成物料SKU、物料库存、款式SKU、款式BOM与BOM明细，用于验证模块联动效果。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清理旧演示数据，避免重复执行时报唯一键冲突。
delete from erp_inventory
where item_type = '物料'
  and (item_code like 'FAB-%-2605-%'
       or item_code like 'ACC-%-2605-%'
       or item_code like 'PKG-%-2605-%');

delete from erp_warehouse where warehouse_code in ('WH-MAT-DEMO', 'WH-FG-DEMO');

delete from erp_style_bom_detail
where bom_id in (select bom_id from erp_style_bom where bom_no like 'KDS26-%-BOM-V1');
delete from erp_style_bom where bom_no like 'KDS26-%-BOM-V1';
delete from erp_style_sku where style_no like 'KDS26-%';
delete from erp_style where style_no like 'KDS26-%';

delete from erp_material_sku
where material_code like 'FAB-%-2605-%'
   or material_code like 'ACC-%-2605-%'
   or material_code like 'PKG-%-2605-%';
delete from erp_material
where material_code like 'FAB-%-2605-%'
   or material_code like 'ACC-%-2605-%'
   or material_code like 'PKG-%-2605-%';

delete from erp_customer where customer_code like 'CUS-DEMO-%';

-- ----------------------------
-- 客户档案：8 条
-- ----------------------------
insert into erp_customer
(customer_code, customer_name, customer_type, contact_name, contact_phone, contact_email, province, city, county, address,
 credit_limit, settlement_type, status, del_flag, create_by, create_time, remark)
values
('CUS-DEMO-001', '杭州星芽童装品牌有限公司', '品牌客户', '陈晓雨', '13857010001', 'buyer01@xingya-kids.com', '浙江省', '杭州市', '滨江区', '滨盛路童装产业园A座1201', 300000.00, '月结30天', '0', '0', 'admin', sysdate(), '主做春夏T恤、连衣裙，重视面料手感和色牢度。'),
('CUS-DEMO-002', '上海暖树儿童用品有限公司', '品牌客户', '刘安琪', '13916020002', 'liuanqi@warmtree.cn', '上海市', '上海市', '闵行区', '虹梅南路儿童生活馆5楼', 500000.00, '月结45天', '0', '0', 'admin', sysdate(), '秋冬卫衣、家居服订单较多，要求尺码跳码稳定。'),
('CUS-DEMO-003', '南京小鹿集合店', '门店客户', '王文杰', '13602530003', 'store03@deer-kids.cn', '江苏省', '南京市', '建邺区', '江东中路万童广场B118', 80000.00, '货到付款', '0', '0', 'admin', sysdate(), '小批量多款，补单节奏快。'),
('CUS-DEMO-004', '成都云朵妈咪童装馆', '门店客户', '赵思琪', '13708040004', 'cloudmom@kids.cn', '四川省', '成都市', '锦江区', '春熙路儿童天地3层302', 60000.00, '预付款', '0', '0', 'admin', sysdate(), '偏好柔和色系和亲肤面料。'),
('CUS-DEMO-005', '广州贝壳电商供应链', '电商客户', '黄嘉明', '13502050005', 'ops@shellkids.com', '广东省', '广州市', '番禺区', '南村万博电商园8栋801', 200000.00, '月结30天', '0', '0', 'admin', sysdate(), '电商爆款客户，需要包装条码清晰。'),
('CUS-DEMO-006', '青岛海风童品批发部', '批发客户', '孙海宁', '13805320006', 'qdhf@wholesale.cn', '山东省', '青岛市', '市北区', '温州路童装批发市场2区16号', 120000.00, '货到付款', '0', '0', 'admin', sysdate(), '关注价格梯度和交期。'),
('CUS-DEMO-007', '郑州乐跑儿童运动馆', '门店客户', '李博远', '13603710007', 'run-kids@demo.cn', '河南省', '郑州市', '金水区', '花园路亲子商业街21号', 90000.00, '月结15天', '0', '0', 'admin', sysdate(), '运动套装、短裤套装需求稳定。'),
('CUS-DEMO-008', '北京童趣优选商贸有限公司', '品牌客户', '周雅婷', '13701080008', 'purchase@tongqu.cn', '北京市', '北京市', '朝阳区', '望京东路童趣中心9层', 450000.00, '月结45天', '0', '0', 'admin', sysdate(), '中高端童装品牌客户，关注质检记录和吊牌规范。');

-- ----------------------------
-- 物料档案：10 条
-- ----------------------------
insert into erp_material
(material_code, material_name, material_type, category_name, unit_name, default_supplier_id, default_supplier_name,
 safe_stock_qty, status, del_flag, create_by, create_time, remark)
values
('FAB-COT-2605-0001', '32S精梳棉毛布', '面料', '棉毛布', '米', null, '绍兴晨织纺织有限公司', 800.000, '0', '0', 'admin', sysdate(), 'T恤、家居服常用亲肤面料，建议缩水率按3%控制。'),
('FAB-RIB-2605-0002', '1x1弹力罗纹', '面料', '罗纹', '米', null, '绍兴晨织纺织有限公司', 300.000, '0', '0', 'admin', sysdate(), '用于领口、袖口、脚口，需与大身色差匹配。'),
('FAB-DEN-2605-0003', '6.5oz轻薄牛仔布', '面料', '牛仔布', '米', null, '广州蓝谷纺织有限公司', 500.000, '0', '0', 'admin', sysdate(), '适合短裤、裙装，水洗后手感柔软。'),
('FAB-FLC-2605-0004', '280g卫衣毛圈布', '面料', '毛圈布', '米', null, '湖州暖棉针织有限公司', 600.000, '0', '0', 'admin', sysdate(), '秋冬卫衣主料，需注意起毛起球测试。'),
('FAB-MSH-2605-0005', '透气小网眼布', '面料', '网眼布', '米', null, '福建森氧面料有限公司', 350.000, '0', '0', 'admin', sysdate(), '运动套装局部拼接，透气性好。'),
('ACC-BTN-2605-0006', '11mm四眼树脂纽扣', '辅料', '纽扣', '个', null, '义乌童辅辅料商行', 3000.000, '0', '0', 'admin', sysdate(), '用于裙装、衬衫类款式，注意过检针。'),
('ACC-ZIP-2605-0007', '5号尼龙闭尾拉链', '辅料', '拉链', '条', null, '义乌童辅辅料商行', 1200.000, '0', '0', 'admin', sysdate(), '用于卫衣、外套，拉头需圆润无锐边。'),
('ACC-ELT-2605-0008', '25mm亲肤松紧带', '辅料', '松紧带', '米', null, '东莞启弹织带有限公司', 1000.000, '0', '0', 'admin', sysdate(), '裤腰、裙腰常用，弹力回复率需抽检。'),
('PKG-TAG-2605-0009', '一丫一童装吊牌', '包装材料', '吊牌', '张', null, '杭州明印包装有限公司', 5000.000, '0', '0', 'admin', sysdate(), '标准品牌吊牌，按款式条码打印。'),
('PKG-BAG-2605-0010', '透明自封包装袋', '包装材料', '包装袋', '个', null, '杭州明印包装有限公司', 5000.000, '0', '0', 'admin', sysdate(), '单件成衣包装，按尺码区分袋型。');

select @mat_cot := material_id from erp_material where material_code = 'FAB-COT-2605-0001';
select @mat_rib := material_id from erp_material where material_code = 'FAB-RIB-2605-0002';
select @mat_den := material_id from erp_material where material_code = 'FAB-DEN-2605-0003';
select @mat_flc := material_id from erp_material where material_code = 'FAB-FLC-2605-0004';
select @mat_msh := material_id from erp_material where material_code = 'FAB-MSH-2605-0005';
select @mat_btn := material_id from erp_material where material_code = 'ACC-BTN-2605-0006';
select @mat_zip := material_id from erp_material where material_code = 'ACC-ZIP-2605-0007';
select @mat_elt := material_id from erp_material where material_code = 'ACC-ELT-2605-0008';
select @mat_tag := material_id from erp_material where material_code = 'PKG-TAG-2605-0009';
select @mat_bag := material_id from erp_material where material_code = 'PKG-BAG-2605-0010';

-- 物料SKU与库存汇总，用于物料模块“当前库存”展示。
insert into erp_material_sku
(material_id, material_code, material_sku_code, color_code, color_name, spec_name, width_value, gram_weight, unit_name,
 barcode, status, del_flag, create_by, create_time, remark)
values
(@mat_cot, 'FAB-COT-2605-0001', 'FAB-COT-2605-0001-IVORY-165-180', 'IVORY', '象牙白', '165cm/180g', 165.00, 180.00, '米', '6902605000101', '0', '0', 'admin', sysdate(), '基础白色大身面料'),
(@mat_cot, 'FAB-COT-2605-0001', 'FAB-COT-2605-0001-PINK-165-180', 'PINK', '蜜桃粉', '165cm/180g', 165.00, 180.00, '米', '6902605000102', '0', '0', 'admin', sysdate(), '女童款常用粉色大身面料'),
(@mat_rib, 'FAB-RIB-2605-0002', 'FAB-RIB-2605-0002-IVORY-90-220', 'IVORY', '象牙白', '90cm/220g', 90.00, 220.00, '米', '6902605000201', '0', '0', 'admin', sysdate(), '领口袖口罗纹'),
(@mat_den, 'FAB-DEN-2605-0003', 'FAB-DEN-2605-0003-BLUE-150-220', 'BLUE', '浅牛仔蓝', '150cm/6.5oz', 150.00, 220.00, '米', '6902605000301', '0', '0', 'admin', sysdate(), '短裤和半裙用牛仔布'),
(@mat_flc, 'FAB-FLC-2605-0004', 'FAB-FLC-2605-0004-GRAY-175-280', 'GRAY', '浅花灰', '175cm/280g', 175.00, 280.00, '米', '6902605000401', '0', '0', 'admin', sysdate(), '卫衣毛圈布'),
(@mat_msh, 'FAB-MSH-2605-0005', 'FAB-MSH-2605-0005-GREEN-160-150', 'GREEN', '薄荷绿', '160cm/150g', 160.00, 150.00, '米', '6902605000501', '0', '0', 'admin', sysdate(), '运动拼接网眼布'),
(@mat_btn, 'ACC-BTN-2605-0006', 'ACC-BTN-2605-0006-WHITE-11MM', 'WHITE', '奶白', '11mm', null, null, '个', '6902605000601', '0', '0', 'admin', sysdate(), '裙装扣'),
(@mat_zip, 'ACC-ZIP-2605-0007', 'ACC-ZIP-2605-0007-NAVY-35CM', 'NAVY', '藏青', '35cm', null, null, '条', '6902605000701', '0', '0', 'admin', sysdate(), '卫衣拉链'),
(@mat_elt, 'ACC-ELT-2605-0008', 'ACC-ELT-2605-0008-WHITE-25MM', 'WHITE', '白色', '25mm', null, null, '米', '6902605000801', '0', '0', 'admin', sysdate(), '裤腰松紧'),
(@mat_tag, 'PKG-TAG-2605-0009', 'PKG-TAG-2605-0009-STD', null, '标准', '70x110mm', null, null, '张', '6902605000901', '0', '0', 'admin', sysdate(), '通用吊牌'),
(@mat_bag, 'PKG-BAG-2605-0010', 'PKG-BAG-2605-0010-STD', null, '标准', '28x38cm', null, null, '个', '6902605001001', '0', '0', 'admin', sysdate(), '通用包装袋');

select @sku_cot_ivory := material_sku_id from erp_material_sku where material_sku_code = 'FAB-COT-2605-0001-IVORY-165-180';
select @sku_cot_pink := material_sku_id from erp_material_sku where material_sku_code = 'FAB-COT-2605-0001-PINK-165-180';
select @sku_rib_ivory := material_sku_id from erp_material_sku where material_sku_code = 'FAB-RIB-2605-0002-IVORY-90-220';
select @sku_den_blue := material_sku_id from erp_material_sku where material_sku_code = 'FAB-DEN-2605-0003-BLUE-150-220';
select @sku_flc_gray := material_sku_id from erp_material_sku where material_sku_code = 'FAB-FLC-2605-0004-GRAY-175-280';
select @sku_msh_green := material_sku_id from erp_material_sku where material_sku_code = 'FAB-MSH-2605-0005-GREEN-160-150';
select @sku_btn_white := material_sku_id from erp_material_sku where material_sku_code = 'ACC-BTN-2605-0006-WHITE-11MM';
select @sku_zip_navy := material_sku_id from erp_material_sku where material_sku_code = 'ACC-ZIP-2605-0007-NAVY-35CM';
select @sku_elt_white := material_sku_id from erp_material_sku where material_sku_code = 'ACC-ELT-2605-0008-WHITE-25MM';
select @sku_tag_std := material_sku_id from erp_material_sku where material_sku_code = 'PKG-TAG-2605-0009-STD';
select @sku_bag_std := material_sku_id from erp_material_sku where material_sku_code = 'PKG-BAG-2605-0010-STD';

insert into erp_warehouse
(warehouse_code, warehouse_name, warehouse_type, manager_name, contact_phone, province, city, county, address,
 status, del_flag, create_by, create_time, remark)
values
('WH-MAT-DEMO', '默认物料仓', '物料仓', '仓库主管', '13800000001', '浙江省', '杭州市', '滨江区', '童装工厂一号仓',
 '0', '0', 'admin', sysdate(), '演示用物料仓库'),
('WH-FG-DEMO', '默认成衣仓', '成衣仓', '仓库主管', '13800000002', '浙江省', '杭州市', '滨江区', '童装工厂二号仓',
 '0', '0', 'admin', sysdate(), '演示用成衣仓库');

select @wh_material := warehouse_id from erp_warehouse where warehouse_code = 'WH-MAT-DEMO';

insert into erp_inventory
(warehouse_id, warehouse_name, item_type, item_id, item_code, item_name, color_name, size_name, spec_name, batch_no,
 available_qty, locked_qty, unit_name, update_time, remark)
values
(@wh_material, '默认物料仓', '物料', @sku_cot_ivory, 'FAB-COT-2605-0001-IVORY-165-180', '32S精梳棉毛布', '象牙白', null, '165cm/180g', 'B260501', 1280.000, 120.000, '米', sysdate(), '演示库存'),
(@wh_material, '默认物料仓', '物料', @sku_cot_pink, 'FAB-COT-2605-0001-PINK-165-180', '32S精梳棉毛布', '蜜桃粉', null, '165cm/180g', 'B260502', 860.000, 80.000, '米', sysdate(), '演示库存'),
(@wh_material, '默认物料仓', '物料', @sku_rib_ivory, 'FAB-RIB-2605-0002-IVORY-90-220', '1x1弹力罗纹', '象牙白', null, '90cm/220g', 'B260503', 420.000, 35.000, '米', sysdate(), '演示库存'),
(@wh_material, '默认物料仓', '物料', @sku_den_blue, 'FAB-DEN-2605-0003-BLUE-150-220', '6.5oz轻薄牛仔布', '浅牛仔蓝', null, '150cm/6.5oz', 'B260504', 690.000, 60.000, '米', sysdate(), '演示库存'),
(@wh_material, '默认物料仓', '物料', @sku_flc_gray, 'FAB-FLC-2605-0004-GRAY-175-280', '280g卫衣毛圈布', '浅花灰', null, '175cm/280g', 'B260505', 760.000, 90.000, '米', sysdate(), '演示库存'),
(@wh_material, '默认物料仓', '物料', @sku_msh_green, 'FAB-MSH-2605-0005-GREEN-160-150', '透气小网眼布', '薄荷绿', null, '160cm/150g', 'B260506', 380.000, 40.000, '米', sysdate(), '演示库存'),
(@wh_material, '默认物料仓', '物料', @sku_btn_white, 'ACC-BTN-2605-0006-WHITE-11MM', '11mm四眼树脂纽扣', '奶白', null, '11mm', 'B260507', 8800.000, 600.000, '个', sysdate(), '演示库存'),
(@wh_material, '默认物料仓', '物料', @sku_zip_navy, 'ACC-ZIP-2605-0007-NAVY-35CM', '5号尼龙闭尾拉链', '藏青', null, '35cm', 'B260508', 1800.000, 120.000, '条', sysdate(), '演示库存'),
(@wh_material, '默认物料仓', '物料', @sku_elt_white, 'ACC-ELT-2605-0008-WHITE-25MM', '25mm亲肤松紧带', '白色', null, '25mm', 'B260509', 1560.000, 110.000, '米', sysdate(), '演示库存'),
(@wh_material, '默认物料仓', '物料', @sku_tag_std, 'PKG-TAG-2605-0009-STD', '一丫一童装吊牌', '标准', null, '70x110mm', 'B260510', 12000.000, 850.000, '张', sysdate(), '演示库存'),
(@wh_material, '默认物料仓', '物料', @sku_bag_std, 'PKG-BAG-2605-0010-STD', '透明自封包装袋', '标准', null, '28x38cm', 'B260511', 9500.000, 700.000, '个', sysdate(), '演示库存');

-- ----------------------------
-- 童装款式：7 条
-- ----------------------------
insert into erp_style
(style_no, style_name, category_name, brand_name, season_name, year_name, gender_type, designer, sample_status,
 production_status, image_url, status, del_flag, create_by, create_time, remark)
values
('KDS26-TS001', '云朵印花短袖T恤', 'T恤', '一丫一童装', '春夏', '2026', '中性', '林可', '2', '0',
 'https://dummyimage.com/360x360/91c9f7/ffffff&text=KDS26-TS001-1,https://dummyimage.com/360x360/f7d794/ffffff&text=KDS26-TS001-2',
 '0', '0', 'admin', sysdate(), '基础圆领T恤，适合电商和门店铺货。'),
('KDS26-DR002', '花园荷叶边连衣裙', '连衣裙', '一丫一童装', '春夏', '2026', '女童', '林可', '2', '1',
 'https://dummyimage.com/360x360/f8a5c2/ffffff&text=KDS26-DR002-1,https://dummyimage.com/360x360/f78fb3/ffffff&text=KDS26-DR002-2',
 '0', '0', 'admin', sysdate(), '荷叶边装饰，门襟配树脂纽扣。'),
('KDS26-ST003', '恐龙探险短裤套装', '套装', '一丫一童装', '春夏', '2026', '男童', '周铭', '1', '0',
 'https://dummyimage.com/360x360/78e08f/ffffff&text=KDS26-ST003-1',
 '0', '0', 'admin', sysdate(), '运动风短裤套装，局部网眼布拼接。'),
('KDS26-HD004', '拼色连帽卫衣', '卫衣', '一丫一童装', '秋冬', '2026', '中性', '周铭', '2', '1',
 'https://dummyimage.com/360x360/778beb/ffffff&text=KDS26-HD004-1,https://dummyimage.com/360x360/546de5/ffffff&text=KDS26-HD004-2',
 '0', '0', 'admin', sysdate(), '秋冬连帽卫衣，前中拉链款。'),
('KDS26-PA005', '舒适束脚休闲裤', '休闲裤', '一丫一童装', '春秋', '2026', '男童', '江然', '0', '0',
 'https://dummyimage.com/360x360/596275/ffffff&text=KDS26-PA005',
 '0', '0', 'admin', sysdate(), '束脚休闲裤，腰头松紧带结构。'),
('KDS26-SK006', '甜莓百褶半身裙', '半身裙', '一丫一童装', '春夏', '2026', '女童', '江然', '1', '0',
 'https://dummyimage.com/360x360/f3a683/ffffff&text=KDS26-SK006',
 '0', '0', 'admin', sysdate(), '轻薄牛仔布半裙，腰头松紧。'),
('KDS26-HM007', '小熊家居服套装', '家居服', '一丫一童装', '秋冬', '2026', '中性', '林可', '2', '0',
 'https://dummyimage.com/360x360/e15f41/ffffff&text=KDS26-HM007-1',
 '0', '0', 'admin', sysdate(), '亲肤棉毛布家居套装，领口罗纹。');

select @style_ts := style_id from erp_style where style_no = 'KDS26-TS001';
select @style_dr := style_id from erp_style where style_no = 'KDS26-DR002';
select @style_st := style_id from erp_style where style_no = 'KDS26-ST003';
select @style_hd := style_id from erp_style where style_no = 'KDS26-HD004';
select @style_pa := style_id from erp_style where style_no = 'KDS26-PA005';
select @style_sk := style_id from erp_style where style_no = 'KDS26-SK006';
select @style_hm := style_id from erp_style where style_no = 'KDS26-HM007';

insert into erp_style_sku
(style_id, style_no, sku_code, color_code, color_name, size_code, size_name, barcode, retail_price,
 status, del_flag, create_by, create_time, remark)
values
(@style_ts, 'KDS26-TS001', 'KDS26-TS001-IVORY-090', 'IVORY', '象牙白', '090', '90cm', '6972605010011', 79.00, '0', '0', 'admin', sysdate(), '演示款式SKU'),
(@style_ts, 'KDS26-TS001', 'KDS26-TS001-PINK-110', 'PINK', '蜜桃粉', '110', '110cm', '6972605010012', 79.00, '0', '0', 'admin', sysdate(), '演示款式SKU'),
(@style_dr, 'KDS26-DR002', 'KDS26-DR002-PINK-100', 'PINK', '蜜桃粉', '100', '100cm', '6972605010021', 149.00, '0', '0', 'admin', sysdate(), '演示款式SKU'),
(@style_dr, 'KDS26-DR002', 'KDS26-DR002-IVORY-120', 'IVORY', '象牙白', '120', '120cm', '6972605010022', 149.00, '0', '0', 'admin', sysdate(), '演示款式SKU'),
(@style_st, 'KDS26-ST003', 'KDS26-ST003-GREEN-100', 'GREEN', '薄荷绿', '100', '100cm', '6972605010031', 139.00, '0', '0', 'admin', sysdate(), '演示款式SKU'),
(@style_st, 'KDS26-ST003', 'KDS26-ST003-BLUE-120', 'BLUE', '浅牛仔蓝', '120', '120cm', '6972605010032', 139.00, '0', '0', 'admin', sysdate(), '演示款式SKU'),
(@style_hd, 'KDS26-HD004', 'KDS26-HD004-GRAY-100', 'GRAY', '浅花灰', '100', '100cm', '6972605010041', 169.00, '0', '0', 'admin', sysdate(), '演示款式SKU'),
(@style_hd, 'KDS26-HD004', 'KDS26-HD004-NAVY-130', 'NAVY', '藏青', '130', '130cm', '6972605010042', 169.00, '0', '0', 'admin', sysdate(), '演示款式SKU'),
(@style_pa, 'KDS26-PA005', 'KDS26-PA005-GRAY-110', 'GRAY', '浅花灰', '110', '110cm', '6972605010051', 119.00, '0', '0', 'admin', sysdate(), '演示款式SKU'),
(@style_pa, 'KDS26-PA005', 'KDS26-PA005-NAVY-130', 'NAVY', '藏青', '130', '130cm', '6972605010052', 119.00, '0', '0', 'admin', sysdate(), '演示款式SKU'),
(@style_sk, 'KDS26-SK006', 'KDS26-SK006-BLUE-100', 'BLUE', '浅牛仔蓝', '100', '100cm', '6972605010061', 129.00, '0', '0', 'admin', sysdate(), '演示款式SKU'),
(@style_sk, 'KDS26-SK006', 'KDS26-SK006-BLUE-120', 'BLUE', '浅牛仔蓝', '120', '120cm', '6972605010062', 129.00, '0', '0', 'admin', sysdate(), '演示款式SKU'),
(@style_hm, 'KDS26-HM007', 'KDS26-HM007-IVORY-100', 'IVORY', '象牙白', '100', '100cm', '6972605010071', 159.00, '0', '0', 'admin', sysdate(), '演示款式SKU'),
(@style_hm, 'KDS26-HM007', 'KDS26-HM007-PINK-120', 'PINK', '蜜桃粉', '120', '120cm', '6972605010072', 159.00, '0', '0', 'admin', sysdate(), '演示款式SKU');

insert into erp_style_bom
(bom_no, style_id, style_no, version_no, bom_status, effective_date, audit_by, audit_time, create_by, create_time, remark)
values
('KDS26-TS001-BOM-V1', @style_ts, 'KDS26-TS001', 'V1', '1', '2026-06-01', 'admin', sysdate(), 'admin', sysdate(), 'T恤基础BOM'),
('KDS26-DR002-BOM-V1', @style_dr, 'KDS26-DR002', 'V1', '1', '2026-06-01', 'admin', sysdate(), 'admin', sysdate(), '连衣裙BOM'),
('KDS26-ST003-BOM-V1', @style_st, 'KDS26-ST003', 'V1', '0', '2026-06-05', null, null, 'admin', sysdate(), '套装开发BOM'),
('KDS26-HD004-BOM-V1', @style_hd, 'KDS26-HD004', 'V1', '1', '2026-08-01', 'admin', sysdate(), 'admin', sysdate(), '卫衣BOM'),
('KDS26-PA005-BOM-V1', @style_pa, 'KDS26-PA005', 'V1', '0', '2026-07-15', null, null, 'admin', sysdate(), '休闲裤开发BOM'),
('KDS26-SK006-BOM-V1', @style_sk, 'KDS26-SK006', 'V1', '0', '2026-06-10', null, null, 'admin', sysdate(), '半身裙开发BOM'),
('KDS26-HM007-BOM-V1', @style_hm, 'KDS26-HM007', 'V1', '1', '2026-08-20', 'admin', sysdate(), 'admin', sysdate(), '家居服BOM');

select @bom_ts := bom_id from erp_style_bom where bom_no = 'KDS26-TS001-BOM-V1';
select @bom_dr := bom_id from erp_style_bom where bom_no = 'KDS26-DR002-BOM-V1';
select @bom_st := bom_id from erp_style_bom where bom_no = 'KDS26-ST003-BOM-V1';
select @bom_hd := bom_id from erp_style_bom where bom_no = 'KDS26-HD004-BOM-V1';
select @bom_pa := bom_id from erp_style_bom where bom_no = 'KDS26-PA005-BOM-V1';
select @bom_sk := bom_id from erp_style_bom where bom_no = 'KDS26-SK006-BOM-V1';
select @bom_hm := bom_id from erp_style_bom where bom_no = 'KDS26-HM007-BOM-V1';

insert into erp_style_bom_detail
(bom_id, material_id, material_sku_id, material_code, material_name, material_type, color_name, spec_name,
 usage_qty, loss_rate, unit_name, position_name, create_by, create_time, remark)
values
(@bom_ts, @mat_cot, @sku_cot_ivory, 'FAB-COT-2605-0001', '32S精梳棉毛布', '面料', '象牙白', '165cm/180g', 0.5200, 0.0300, '米', '大身', 'admin', sysdate(), 'T恤主料'),
(@bom_ts, @mat_rib, @sku_rib_ivory, 'FAB-RIB-2605-0002', '1x1弹力罗纹', '面料', '象牙白', '90cm/220g', 0.0700, 0.0500, '米', '领口', 'admin', sysdate(), '领口罗纹'),
(@bom_ts, @mat_tag, @sku_tag_std, 'PKG-TAG-2605-0009', '一丫一童装吊牌', '包装材料', '标准', '70x110mm', 1.0000, 0.0000, '张', '包装', 'admin', sysdate(), '吊牌'),
(@bom_dr, @mat_cot, @sku_cot_pink, 'FAB-COT-2605-0001', '32S精梳棉毛布', '面料', '蜜桃粉', '165cm/180g', 0.8600, 0.0400, '米', '裙身', 'admin', sysdate(), '连衣裙主料'),
(@bom_dr, @mat_btn, @sku_btn_white, 'ACC-BTN-2605-0006', '11mm四眼树脂纽扣', '辅料', '奶白', '11mm', 4.0000, 0.0200, '个', '门襟', 'admin', sysdate(), '门襟纽扣'),
(@bom_dr, @mat_bag, @sku_bag_std, 'PKG-BAG-2605-0010', '透明自封包装袋', '包装材料', '标准', '28x38cm', 1.0000, 0.0000, '个', '包装', 'admin', sysdate(), '包装袋'),
(@bom_st, @mat_msh, @sku_msh_green, 'FAB-MSH-2605-0005', '透气小网眼布', '面料', '薄荷绿', '160cm/150g', 0.4200, 0.0400, '米', '上衣拼接', 'admin', sysdate(), '运动拼接'),
(@bom_st, @mat_den, @sku_den_blue, 'FAB-DEN-2605-0003', '6.5oz轻薄牛仔布', '面料', '浅牛仔蓝', '150cm/6.5oz', 0.5800, 0.0500, '米', '短裤', 'admin', sysdate(), '短裤主料'),
(@bom_st, @mat_elt, @sku_elt_white, 'ACC-ELT-2605-0008', '25mm亲肤松紧带', '辅料', '白色', '25mm', 0.4600, 0.0300, '米', '裤腰', 'admin', sysdate(), '裤腰松紧'),
(@bom_hd, @mat_flc, @sku_flc_gray, 'FAB-FLC-2605-0004', '280g卫衣毛圈布', '面料', '浅花灰', '175cm/280g', 0.9200, 0.0500, '米', '大身', 'admin', sysdate(), '卫衣主料'),
(@bom_hd, @mat_zip, @sku_zip_navy, 'ACC-ZIP-2605-0007', '5号尼龙闭尾拉链', '辅料', '藏青', '35cm', 1.0000, 0.0000, '条', '前中', 'admin', sysdate(), '前中拉链'),
(@bom_hd, @mat_rib, @sku_rib_ivory, 'FAB-RIB-2605-0002', '1x1弹力罗纹', '面料', '象牙白', '90cm/220g', 0.1800, 0.0500, '米', '袖口下摆', 'admin', sysdate(), '袖口下摆罗纹'),
(@bom_pa, @mat_flc, @sku_flc_gray, 'FAB-FLC-2605-0004', '280g卫衣毛圈布', '面料', '浅花灰', '175cm/280g', 0.7400, 0.0500, '米', '裤身', 'admin', sysdate(), '休闲裤主料'),
(@bom_pa, @mat_elt, @sku_elt_white, 'ACC-ELT-2605-0008', '25mm亲肤松紧带', '辅料', '白色', '25mm', 0.5200, 0.0300, '米', '腰头', 'admin', sysdate(), '腰头松紧'),
(@bom_pa, @mat_tag, @sku_tag_std, 'PKG-TAG-2605-0009', '一丫一童装吊牌', '包装材料', '标准', '70x110mm', 1.0000, 0.0000, '张', '包装', 'admin', sysdate(), '吊牌'),
(@bom_sk, @mat_den, @sku_den_blue, 'FAB-DEN-2605-0003', '6.5oz轻薄牛仔布', '面料', '浅牛仔蓝', '150cm/6.5oz', 0.6500, 0.0500, '米', '裙身', 'admin', sysdate(), '半身裙主料'),
(@bom_sk, @mat_btn, @sku_btn_white, 'ACC-BTN-2605-0006', '11mm四眼树脂纽扣', '辅料', '奶白', '11mm', 2.0000, 0.0200, '个', '装饰', 'admin', sysdate(), '装饰扣'),
(@bom_sk, @mat_elt, @sku_elt_white, 'ACC-ELT-2605-0008', '25mm亲肤松紧带', '辅料', '白色', '25mm', 0.4200, 0.0300, '米', '腰头', 'admin', sysdate(), '腰头松紧'),
(@bom_hm, @mat_cot, @sku_cot_ivory, 'FAB-COT-2605-0001', '32S精梳棉毛布', '面料', '象牙白', '165cm/180g', 1.1200, 0.0400, '米', '上衣裤身', 'admin', sysdate(), '家居服主料'),
(@bom_hm, @mat_rib, @sku_rib_ivory, 'FAB-RIB-2605-0002', '1x1弹力罗纹', '面料', '象牙白', '90cm/220g', 0.1600, 0.0500, '米', '领口袖口', 'admin', sysdate(), '罗纹口'),
(@bom_hm, @mat_bag, @sku_bag_std, 'PKG-BAG-2605-0010', '透明自封包装袋', '包装材料', '标准', '28x38cm', 1.0000, 0.0000, '个', '包装', 'admin', sysdate(), '包装袋');

SET FOREIGN_KEY_CHECKS = 1;

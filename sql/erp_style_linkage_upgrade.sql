-- 款式主表与 erp_style_* 子表联动升级脚本
-- 已安装旧库时执行；新库可直接使用 yiyayi_kids_erp_core.sql

UPDATE erp_style
SET sample_status = CASE sample_status
  WHEN '待打样' THEN '0'
  WHEN '打样中' THEN '1'
  WHEN '已确认' THEN '2'
  ELSE IFNULL(sample_status, '0')
END;

UPDATE erp_style
SET production_status = CASE production_status
  WHEN '未投产' THEN '0'
  WHEN '生产中' THEN '1'
  WHEN '已完结' THEN '2'
  ELSE IFNULL(production_status, '0')
END;

UPDATE erp_style_bom
SET bom_status = CASE bom_status
  WHEN '草稿' THEN '0'
  WHEN '已审核' THEN '1'
  WHEN '已停用' THEN '2'
  ELSE IFNULL(bom_status, '0')
END;

ALTER TABLE erp_style
  MODIFY sample_status CHAR(1) DEFAULT '0' COMMENT '样衣状态（0待打样 1打样中 2已确认）',
  MODIFY production_status CHAR(1) DEFAULT '0' COMMENT '生产状态（0未投产 1生产中 2已完结）',
  MODIFY image_url VARCHAR(1000) DEFAULT NULL COMMENT '主图地址，多个地址用英文逗号分隔';

ALTER TABLE erp_style_bom
  MODIFY bom_status CHAR(1) DEFAULT '0' COMMENT 'BOM状态（0草稿 1已审核 2已停用）';

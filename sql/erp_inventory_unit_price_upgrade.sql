-- 库存汇总表增加库存单价字段
-- 执行说明：请在业务库中手动执行本文件，不要重复执行。

ALTER TABLE erp_inventory
  ADD COLUMN unit_price DECIMAL(14,4) DEFAULT 0.0000 COMMENT '库存单价' AFTER locked_qty;

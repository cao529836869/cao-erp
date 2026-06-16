-- Fix for an existing erp_sales_order_detail table created before sku_code and related columns were added.
-- Run this in the ry-vue database if /erp/sales/list reports missing sales detail columns.

set @schema_name := database();

set @sql := if((select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_sales_order_detail' and column_name = 'sales_order_detail_id') = 0
    and (select count(1) from information_schema.columns where table_schema = @schema_name and table_name = 'erp_sales_order_detail' and column_name = 'sales_detail_id') > 0,
  'alter table erp_sales_order_detail change column sales_detail_id sales_order_detail_id bigint(20) not null auto_increment comment ''销售订单明细ID''',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

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

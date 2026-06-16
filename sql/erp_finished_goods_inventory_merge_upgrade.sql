-- Merge finished goods inventory rows by warehouse + item type + SKU.
-- Finished goods should not create a new inventory row for every cut order batch.

CREATE TEMPORARY TABLE tmp_finished_goods_inventory AS
SELECT
    min(inventory_id) AS keep_inventory_id,
    warehouse_id,
    max(warehouse_name) AS warehouse_name,
    item_type,
    item_id,
    max(item_code) AS item_code,
    max(item_name) AS item_name,
    max(color_name) AS color_name,
    max(size_name) AS size_name,
    max(spec_name) AS spec_name,
    sum(ifnull(available_qty, 0)) AS available_qty,
    sum(ifnull(locked_qty, 0)) AS locked_qty,
    max(unit_price) AS unit_price,
    max(unit_name) AS unit_name,
    max(update_time) AS update_time,
    max(remark) AS remark
FROM erp_inventory
WHERE item_type = '成衣'
GROUP BY warehouse_id, item_type, item_id;

DELETE FROM erp_inventory
WHERE item_type = '成衣';

INSERT INTO erp_inventory (
    warehouse_id, warehouse_name, item_type, item_id, item_code, item_name,
    color_name, size_name, spec_name, batch_no, available_qty, locked_qty,
    unit_price, unit_name, update_time, remark
)
SELECT
    warehouse_id, warehouse_name, item_type, item_id, item_code, item_name,
    color_name, size_name, spec_name, '', available_qty, locked_qty,
    unit_price, unit_name, update_time, remark
FROM tmp_finished_goods_inventory;

DROP TEMPORARY TABLE tmp_finished_goods_inventory;

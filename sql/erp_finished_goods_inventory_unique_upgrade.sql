-- Make finished goods unique by warehouse + SKU while keeping the original batch_no value.
-- Materials still stay unique by warehouse + item type + SKU + batch_no.

ALTER TABLE erp_inventory
    ADD COLUMN inventory_batch_key VARCHAR(80)
        GENERATED ALWAYS AS (
            CASE WHEN item_type = '成衣' THEN '' ELSE ifnull(batch_no, '') END
        ) STORED;

ALTER TABLE erp_inventory
    DROP INDEX uk_erp_inventory_item_batch;

ALTER TABLE erp_inventory
    ADD UNIQUE KEY uk_erp_inventory_item_batch (warehouse_id, item_type, item_id, inventory_batch_key);

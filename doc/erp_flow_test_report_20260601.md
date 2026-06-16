# ERP Flow Test Report - 2026-06-01

## Scope

- Database: `ry-vue` on local MySQL.
- Reset script: `sql/reset_erp_business_demo_data.sql`.
- Backend: `http://localhost:8080`.
- Login user: `admin`.

## Actions

- Backed up the full database before reset.
- Fixed the reset script so it also clears `erp_cut_order_detail` and `erp_cut_order`.
- Re-ran ERP business demo data reset.
- Verified list APIs for customer, material, style, inbound, outbound, production, and inventory.
- Posted the draft inbound order `RK-DEMO-DRAFT-001`.
- Posted the draft outbound order `CK-DEMO-DRAFT-001`.
- Verified low-stock blocking with `CK-DEMO-LOW-001`.
- Generated a cut order from `MO-DEMO-001`.
- Created `MO-API-FLOW-001`, released it, generated a picking outbound order, and verified its post failure caused by empty batch numbers.

## Final Demo Data Snapshot

- Customers: 2
- Suppliers: 3
- Warehouses: 3
- Materials: 6
- Material SKUs: 7
- Styles: 2
- Style SKUs: 3
- BOMs: 2
- BOM details: 5
- Inbound orders: 2
- Outbound orders: 4
- Production orders: 2
- Cut orders: 1
- Inventory rows: 5
- Inventory transactions: 4

## Findings

1. Reset script missed current cut-order tables.
   - `erp_cut_order` and `erp_cut_order_detail` were not cleared, while the active controller/service use these tables.
   - Fixed in `sql/reset_erp_business_demo_data.sql`.

2. Production picking can be blocked by existing active picking outbound orders.
   - `MO-DEMO-001` cannot generate another picking order because `CK-DEMO-LOW-001` already exists as a draft.
   - This is valid duplicate prevention, but the demo data should make the intended test path clearer.

3. Auto-generated picking outbound details have empty batch numbers.
   - `MO-API-FLOW-001` generated `CK20260601154843`.
   - The outbound details calculated correct BOM quantities, but `batch_no` was empty.
   - Posting then failed with stock shortage for an empty batch.
   - Recommended improvement: allocate available batches automatically by FIFO/FEFO, or require batch assignment before allowing post.

4. Error responses return business `code: 500` in a normal HTTP response body.
   - API clients must inspect the JSON `code`, not just HTTP success.
   - Recommended improvement: standardize HTTP status codes or add a client interceptor convention for business errors.

## Generated Files

- Full DB backup before reset: `sql/backup_ry-vue_before_erp_reset_20260601_154451.sql`
- Final ERP demo data dump: `sql/display_erp_business_data_after_flow_test_20260601_154855.sql`

# ERP 业务模型图

本文档给出一套通用 ERP 业务模型，可用于后续模块拆分、数据库建模、菜单设计和接口规划。

## 1. ERP 总体业务模型

```mermaid
flowchart LR
    subgraph MD["基础资料"]
        MAT["物料/产品"]
        CUST["客户"]
        SUP["供应商"]
        WH["仓库/库位"]
        EMP["员工/组织"]
        ACCT["科目/税率/结算方式"]
    end

    subgraph SALE["销售管理"]
        SQ["销售报价"]
        SO["销售订单"]
        OUT["销售出库"]
        AR["应收账款"]
        SR["销售退货"]
    end

    subgraph PROC["采购管理"]
        PR["采购申请"]
        PO["采购订单"]
        IN["采购入库"]
        AP["应付账款"]
        RR["采购退货"]
    end

    subgraph INV["库存管理"]
        STOCK["库存台账"]
        TRANS["调拨"]
        CHECK["盘点"]
        WARN["库存预警"]
    end

    subgraph PROD["生产管理"]
        BOM["BOM"]
        MO["生产工单"]
        PICK["领料"]
        REPORT["报工"]
        FG["完工入库"]
    end

    subgraph FIN["财务管理"]
        VOUCHER["凭证"]
        CASH["收付款"]
        COST["成本核算"]
        PROFIT["利润分析"]
    end

    subgraph BI["经营分析"]
        DASH["经营看板"]
        SALE_RPT["销售分析"]
        INV_RPT["库存分析"]
        FIN_RPT["财务分析"]
    end

    MD --> SALE
    MD --> PROC
    MD --> INV
    MD --> PROD

    SQ --> SO --> OUT --> AR --> CASH
    SO --> WARN
    SR --> STOCK

    PR --> PO --> IN --> AP --> CASH
    RR --> STOCK

    BOM --> MO --> PICK --> REPORT --> FG
    PICK --> STOCK
    FG --> STOCK

    OUT --> STOCK
    IN --> STOCK
    TRANS --> STOCK
    CHECK --> STOCK

    AR --> VOUCHER
    AP --> VOUCHER
    CASH --> VOUCHER
    STOCK --> COST
    COST --> VOUCHER

    SALE --> BI
    PROC --> BI
    INV --> BI
    PROD --> BI
    FIN --> BI
```

## 2. 核心业务闭环

```mermaid
flowchart TD
    START["客户需求/销售机会"] --> QUOTE["销售报价"]
    QUOTE --> ORDER["销售订单"]
    ORDER --> CHECK_STOCK{"库存是否满足"}

    CHECK_STOCK -- "满足" --> SALE_OUT["销售出库"]
    CHECK_STOCK -- "不足" --> NEED{"补货方式"}

    NEED -- "采购补货" --> PURCHASE["采购订单"]
    PURCHASE --> PURCHASE_IN["采购入库"]
    PURCHASE_IN --> STOCK_UP["库存增加"]

    NEED -- "生产补货" --> WORK_ORDER["生产工单"]
    WORK_ORDER --> MATERIAL_PICK["生产领料"]
    MATERIAL_PICK --> PRODUCE["生产报工"]
    PRODUCE --> FINISH_IN["完工入库"]
    FINISH_IN --> STOCK_UP

    STOCK_UP --> SALE_OUT
    SALE_OUT --> INVOICE["销售开票/应收"]
    INVOICE --> RECEIVE["收款核销"]
    RECEIVE --> PROFIT["收入、成本、利润分析"]

    PURCHASE_IN --> PAYABLE["采购开票/应付"]
    PAYABLE --> PAY["付款核销"]

    SALE_OUT --> COST["出库成本"]
    COST --> PROFIT
    PAY --> CASH_FLOW["现金流"]
    RECEIVE --> CASH_FLOW
```

## 3. 核心实体关系模型

```mermaid
erDiagram
    SYS_DEPT ||--o{ SYS_USER : has
    SYS_USER ||--o{ ERP_AUDIT_LOG : operates

    ERP_CUSTOMER ||--o{ ERP_SALE_ORDER : places
    ERP_SUPPLIER ||--o{ ERP_PURCHASE_ORDER : receives

    ERP_MATERIAL ||--o{ ERP_SALE_ORDER_ITEM : sold_as
    ERP_MATERIAL ||--o{ ERP_PURCHASE_ORDER_ITEM : bought_as
    ERP_MATERIAL ||--o{ ERP_STOCK_BALANCE : stocked_as
    ERP_MATERIAL ||--o{ ERP_BOM_ITEM : component

    ERP_WAREHOUSE ||--o{ ERP_STOCK_BALANCE : owns
    ERP_WAREHOUSE ||--o{ ERP_STOCK_BILL : records

    ERP_SALE_ORDER ||--o{ ERP_SALE_ORDER_ITEM : contains
    ERP_SALE_ORDER ||--o{ ERP_STOCK_BILL : generates
    ERP_SALE_ORDER ||--o{ ERP_RECEIVABLE : creates

    ERP_PURCHASE_ORDER ||--o{ ERP_PURCHASE_ORDER_ITEM : contains
    ERP_PURCHASE_ORDER ||--o{ ERP_STOCK_BILL : generates
    ERP_PURCHASE_ORDER ||--o{ ERP_PAYABLE : creates

    ERP_BOM ||--o{ ERP_BOM_ITEM : contains
    ERP_BOM ||--o{ ERP_WORK_ORDER : used_by
    ERP_WORK_ORDER ||--o{ ERP_STOCK_BILL : material_issue
    ERP_WORK_ORDER ||--o{ ERP_STOCK_BILL : finished_goods_receipt

    ERP_STOCK_BILL ||--o{ ERP_STOCK_BILL_ITEM : contains
    ERP_STOCK_BILL_ITEM }o--|| ERP_MATERIAL : references

    ERP_RECEIVABLE ||--o{ ERP_RECEIVE_RECORD : settled_by
    ERP_PAYABLE ||--o{ ERP_PAYMENT_RECORD : settled_by

    ERP_RECEIVE_RECORD ||--o{ ERP_VOUCHER : posts
    ERP_PAYMENT_RECORD ||--o{ ERP_VOUCHER : posts
    ERP_STOCK_BILL ||--o{ ERP_VOUCHER : posts_cost

    ERP_CUSTOMER {
        bigint id
        string customer_code
        string customer_name
        string contact
        string phone
        string status
    }

    ERP_SUPPLIER {
        bigint id
        string supplier_code
        string supplier_name
        string contact
        string phone
        string status
    }

    ERP_MATERIAL {
        bigint id
        string material_code
        string material_name
        string material_type
        string unit
        decimal sale_price
        decimal purchase_price
        string status
    }

    ERP_WAREHOUSE {
        bigint id
        string warehouse_code
        string warehouse_name
        string address
        string status
    }

    ERP_STOCK_BALANCE {
        bigint id
        bigint warehouse_id
        bigint material_id
        decimal qty_available
        decimal qty_locked
        decimal qty_on_order
    }

    ERP_SALE_ORDER {
        bigint id
        string order_no
        bigint customer_id
        date order_date
        decimal total_amount
        string status
    }

    ERP_PURCHASE_ORDER {
        bigint id
        string order_no
        bigint supplier_id
        date order_date
        decimal total_amount
        string status
    }

    ERP_WORK_ORDER {
        bigint id
        string work_order_no
        bigint bom_id
        bigint material_id
        decimal plan_qty
        decimal finished_qty
        string status
    }
```

## 4. 建议菜单结构

```text
ERP 管理
├─ 基础资料
│  ├─ 客户管理
│  ├─ 供应商管理
│  ├─ 物料管理
│  ├─ 仓库管理
│  └─ 计量单位/税率/结算方式
├─ 销售管理
│  ├─ 销售报价
│  ├─ 销售订单
│  ├─ 销售出库
│  ├─ 销售退货
│  └─ 应收账款
├─ 采购管理
│  ├─ 采购申请
│  ├─ 采购订单
│  ├─ 采购入库
│  ├─ 采购退货
│  └─ 应付账款
├─ 库存管理
│  ├─ 库存台账
│  ├─ 库存流水
│  ├─ 调拨单
│  ├─ 盘点单
│  └─ 库存预警
├─ 生产管理
│  ├─ BOM 管理
│  ├─ 生产工单
│  ├─ 生产领料
│  ├─ 生产报工
│  └─ 完工入库
├─ 财务管理
│  ├─ 收款单
│  ├─ 付款单
│  ├─ 凭证管理
│  └─ 成本核算
└─ 经营分析
   ├─ 销售看板
   ├─ 库存看板
   ├─ 采购分析
   └─ 利润分析
```

## 5. 单据状态建议

| 业务对象 | 状态流转 |
| --- | --- |
| 销售订单 | 草稿 -> 待审核 -> 已审核 -> 部分出库 -> 已出库 -> 已完成 -> 已关闭 |
| 采购订单 | 草稿 -> 待审核 -> 已审核 -> 部分入库 -> 已入库 -> 已完成 -> 已关闭 |
| 生产工单 | 草稿 -> 已下达 -> 生产中 -> 已完工 -> 已关闭 |
| 出入库单 | 草稿 -> 待审核 -> 已审核 -> 已过账 -> 已作废 |
| 应收/应付 | 未结算 -> 部分结算 -> 已结算 -> 已冲销 |


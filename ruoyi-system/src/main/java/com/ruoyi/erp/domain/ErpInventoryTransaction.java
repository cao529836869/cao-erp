package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 库存流水对象 erp_inventory_transaction
 */
public class ErpInventoryTransaction extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long transactionId;
    @Excel(name = "流水编号")
    private String transactionNo;
    @Excel(name = "流水类型")
    private String transactionType;
    @Excel(name = "业务单号")
    private String businessNo;
    private Long warehouseId;
    @Excel(name = "仓库")
    private String warehouseName;
    @Excel(name = "库存类型")
    private String itemType;
    private Long itemId;
    @Excel(name = "物料/成衣编码")
    private String itemCode;
    /** 是否精确匹配编码，1表示精确匹配 */
    private String itemCodeExact;
    @Excel(name = "物料/成衣名称")
    private String itemName;
    @Excel(name = "批次号")
    private String batchNo;
    @Excel(name = "入库数量")
    private BigDecimal inQty;
    @Excel(name = "出库数量")
    private BigDecimal outQty;
    @Excel(name = "结存数量")
    private BigDecimal balanceQty;
    @Excel(name = "单位")
    private String unitName;
    @Excel(name = "操作人")
    private String operatorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发生时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date transactionTime;

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    public String getTransactionNo() { return transactionNo; }
    public void setTransactionNo(String transactionNo) { this.transactionNo = transactionNo; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public String getBusinessNo() { return businessNo; }
    public void setBusinessNo(String businessNo) { this.businessNo = businessNo; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemCodeExact() { return itemCodeExact; }
    public void setItemCodeExact(String itemCodeExact) { this.itemCodeExact = itemCodeExact; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public BigDecimal getInQty() { return inQty; }
    public void setInQty(BigDecimal inQty) { this.inQty = inQty; }
    public BigDecimal getOutQty() { return outQty; }
    public void setOutQty(BigDecimal outQty) { this.outQty = outQty; }
    public BigDecimal getBalanceQty() { return balanceQty; }
    public void setBalanceQty(BigDecimal balanceQty) { this.balanceQty = balanceQty; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public Date getTransactionTime() { return transactionTime; }
    public void setTransactionTime(Date transactionTime) { this.transactionTime = transactionTime; }
}

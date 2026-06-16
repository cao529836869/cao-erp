package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 出库单明细对象 erp_outbound_order_detail
 */
public class ErpOutboundOrderDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long outboundDetailId;
    private Long outboundOrderId;
    private String outboundOrderNo;
    private Long sourceDetailId;
    @Excel(name = "库存类型")
    private String itemType;
    private Long itemId;
    @Excel(name = "编码")
    private String itemCode;
    @Excel(name = "名称")
    private String itemName;
    @Excel(name = "颜色")
    private String colorName;
    @Excel(name = "尺码")
    private String sizeName;
    @Excel(name = "规格")
    private String specName;
    @Excel(name = "批次号")
    private String batchNo;
    private String locationCode;
    @Excel(name = "计划出库")
    private BigDecimal planQty;
    @Excel(name = "锁定数量")
    private BigDecimal lockedQty;
    @Excel(name = "实际出库")
    private BigDecimal outboundQty;
    @Excel(name = "单位")
    private String unitName;
    @Excel(name = "单价")
    private BigDecimal unitPrice;
    @Excel(name = "金额")
    private BigDecimal amount;

    public Long getOutboundDetailId() { return outboundDetailId; }
    public void setOutboundDetailId(Long outboundDetailId) { this.outboundDetailId = outboundDetailId; }
    public Long getOutboundOrderId() { return outboundOrderId; }
    public void setOutboundOrderId(Long outboundOrderId) { this.outboundOrderId = outboundOrderId; }
    public String getOutboundOrderNo() { return outboundOrderNo; }
    public void setOutboundOrderNo(String outboundOrderNo) { this.outboundOrderNo = outboundOrderNo; }
    public Long getSourceDetailId() { return sourceDetailId; }
    public void setSourceDetailId(Long sourceDetailId) { this.sourceDetailId = sourceDetailId; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getColorName() { return colorName; }
    public void setColorName(String colorName) { this.colorName = colorName; }
    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }
    public String getSpecName() { return specName; }
    public void setSpecName(String specName) { this.specName = specName; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
    public BigDecimal getPlanQty() { return planQty; }
    public void setPlanQty(BigDecimal planQty) { this.planQty = planQty; }
    public BigDecimal getLockedQty() { return lockedQty; }
    public void setLockedQty(BigDecimal lockedQty) { this.lockedQty = lockedQty; }
    public BigDecimal getOutboundQty() { return outboundQty; }
    public void setOutboundQty(BigDecimal outboundQty) { this.outboundQty = outboundQty; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

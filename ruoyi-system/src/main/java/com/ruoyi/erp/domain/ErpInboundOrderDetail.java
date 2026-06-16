package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 入库单明细对象 erp_inbound_order_detail
 */
public class ErpInboundOrderDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long inboundDetailId;
    private Long inboundOrderId;
    private String inboundOrderNo;
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
    @Excel(name = "计划入库")
    private BigDecimal planQty;
    @Excel(name = "实际入库")
    private BigDecimal inboundQty;
    @Excel(name = "单位")
    private String unitName;
    @Excel(name = "单价")
    private BigDecimal unitPrice;
    @Excel(name = "金额")
    private BigDecimal amount;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date productionDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expireDate;
    private String qualityStatus;

    public Long getInboundDetailId() { return inboundDetailId; }
    public void setInboundDetailId(Long inboundDetailId) { this.inboundDetailId = inboundDetailId; }
    public Long getInboundOrderId() { return inboundOrderId; }
    public void setInboundOrderId(Long inboundOrderId) { this.inboundOrderId = inboundOrderId; }
    public String getInboundOrderNo() { return inboundOrderNo; }
    public void setInboundOrderNo(String inboundOrderNo) { this.inboundOrderNo = inboundOrderNo; }
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
    public BigDecimal getInboundQty() { return inboundQty; }
    public void setInboundQty(BigDecimal inboundQty) { this.inboundQty = inboundQty; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Date getProductionDate() { return productionDate; }
    public void setProductionDate(Date productionDate) { this.productionDate = productionDate; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public String getQualityStatus() { return qualityStatus; }
    public void setQualityStatus(String qualityStatus) { this.qualityStatus = qualityStatus; }
}

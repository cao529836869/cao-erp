package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class ErpDeliveryOrderDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long deliveryDetailId;
    private Long deliveryOrderId;
    private String deliveryOrderNo;
    private Long salesDetailId;
    private Long inventoryId;
    private Long skuId;
    @Excel(name = "SKU编码")
    private String skuCode;
    @Excel(name = "款号")
    private String styleNo;
    @Excel(name = "款式名称")
    private String styleName;
    @Excel(name = "颜色")
    private String colorName;
    @Excel(name = "尺码")
    private String sizeName;
    @Excel(name = "批次号")
    private String batchNo;
    @Excel(name = "库存数量")
    private BigDecimal stockQty;
    @Excel(name = "锁定数量")
    private BigDecimal lockedQty;
    @Excel(name = "可用数量")
    private BigDecimal availableQty;
    @Excel(name = "发货数量")
    private BigDecimal deliveryQty;
    @Excel(name = "单位")
    private String unitName;
    @Excel(name = "单价")
    private BigDecimal unitPrice;

    public Long getDeliveryDetailId() { return deliveryDetailId; }
    public void setDeliveryDetailId(Long deliveryDetailId) { this.deliveryDetailId = deliveryDetailId; }
    public Long getDeliveryOrderId() { return deliveryOrderId; }
    public void setDeliveryOrderId(Long deliveryOrderId) { this.deliveryOrderId = deliveryOrderId; }
    public String getDeliveryOrderNo() { return deliveryOrderNo; }
    public void setDeliveryOrderNo(String deliveryOrderNo) { this.deliveryOrderNo = deliveryOrderNo; }
    public Long getSalesDetailId() { return salesDetailId; }
    public void setSalesDetailId(Long salesDetailId) { this.salesDetailId = salesDetailId; }
    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getStyleNo() { return styleNo; }
    public void setStyleNo(String styleNo) { this.styleNo = styleNo; }
    public String getStyleName() { return styleName; }
    public void setStyleName(String styleName) { this.styleName = styleName; }
    public String getColorName() { return colorName; }
    public void setColorName(String colorName) { this.colorName = colorName; }
    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public BigDecimal getStockQty() { return stockQty; }
    public void setStockQty(BigDecimal stockQty) { this.stockQty = stockQty; }
    public BigDecimal getLockedQty() { return lockedQty; }
    public void setLockedQty(BigDecimal lockedQty) { this.lockedQty = lockedQty; }
    public BigDecimal getAvailableQty() { return availableQty; }
    public void setAvailableQty(BigDecimal availableQty) { this.availableQty = availableQty; }
    public BigDecimal getDeliveryQty() { return deliveryQty; }
    public void setDeliveryQty(BigDecimal deliveryQty) { this.deliveryQty = deliveryQty; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}

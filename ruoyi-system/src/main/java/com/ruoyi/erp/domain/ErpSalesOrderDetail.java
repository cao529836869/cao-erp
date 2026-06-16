package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class ErpSalesOrderDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long salesDetailId;
    private Long salesOrderId;
    private String salesOrderNo;
    private Long styleId;
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
    @Excel(name = "数量")
    private BigDecimal orderQty;
    @Excel(name = "单价")
    private BigDecimal unitPrice;
    @Excel(name = "金额")
    private BigDecimal amount;

    public Long getSalesDetailId() { return salesDetailId; }
    public void setSalesDetailId(Long salesDetailId) { this.salesDetailId = salesDetailId; }
    public Long getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(Long salesOrderId) { this.salesOrderId = salesOrderId; }
    public String getSalesOrderNo() { return salesOrderNo; }
    public void setSalesOrderNo(String salesOrderNo) { this.salesOrderNo = salesOrderNo; }
    public Long getStyleId() { return styleId; }
    public void setStyleId(Long styleId) { this.styleId = styleId; }
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
    public BigDecimal getOrderQty() { return orderQty; }
    public void setOrderQty(BigDecimal orderQty) { this.orderQty = orderQty; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

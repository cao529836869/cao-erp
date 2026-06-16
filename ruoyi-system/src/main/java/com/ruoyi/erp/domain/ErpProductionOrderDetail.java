package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 生产订单明细对象 erp_production_order_detail
 */
public class ErpProductionOrderDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long productionOrderDetailId;
    private Long productionOrderId;
    private String productionOrderNo;
    private Long styleId;
    private Long skuId;
    @Excel(name = "款号")
    private String styleNo;
    @Excel(name = "款式名称")
    private String styleName;
    @Excel(name = "颜色")
    private String colorName;
    @Excel(name = "尺码")
    private String sizeName;
    @Excel(name = "计划数量")
    private BigDecimal planQty;
    @Excel(name = "已裁剪")
    private BigDecimal cutQty;
    @Excel(name = "已缝制")
    private BigDecimal sewnQty;
    @Excel(name = "已后整")
    private BigDecimal finishedQty;
    @Excel(name = "合格数量")
    private BigDecimal qualifiedQty;

    public Long getProductionOrderDetailId() { return productionOrderDetailId; }
    public void setProductionOrderDetailId(Long productionOrderDetailId) { this.productionOrderDetailId = productionOrderDetailId; }
    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }
    public String getProductionOrderNo() { return productionOrderNo; }
    public void setProductionOrderNo(String productionOrderNo) { this.productionOrderNo = productionOrderNo; }
    public Long getStyleId() { return styleId; }
    public void setStyleId(Long styleId) { this.styleId = styleId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getStyleNo() { return styleNo; }
    public void setStyleNo(String styleNo) { this.styleNo = styleNo; }
    public String getStyleName() { return styleName; }
    public void setStyleName(String styleName) { this.styleName = styleName; }
    public String getColorName() { return colorName; }
    public void setColorName(String colorName) { this.colorName = colorName; }
    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }
    public BigDecimal getPlanQty() { return planQty; }
    public void setPlanQty(BigDecimal planQty) { this.planQty = planQty; }
    public BigDecimal getCutQty() { return cutQty; }
    public void setCutQty(BigDecimal cutQty) { this.cutQty = cutQty; }
    public BigDecimal getSewnQty() { return sewnQty; }
    public void setSewnQty(BigDecimal sewnQty) { this.sewnQty = sewnQty; }
    public BigDecimal getFinishedQty() { return finishedQty; }
    public void setFinishedQty(BigDecimal finishedQty) { this.finishedQty = finishedQty; }
    public BigDecimal getQualifiedQty() { return qualifiedQty; }
    public void setQualifiedQty(BigDecimal qualifiedQty) { this.qualifiedQty = qualifiedQty; }
}

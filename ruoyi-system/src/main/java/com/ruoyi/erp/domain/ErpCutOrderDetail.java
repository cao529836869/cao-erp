package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class ErpCutOrderDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long cutDetailId;
    private Long cutOrderId;
    private String cutOrderNo;
    private Long productionOrderDetailId;
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
    @Excel(name = "已裁数量")
    private BigDecimal cutQty;

    public Long getCutDetailId() { return cutDetailId; }
    public void setCutDetailId(Long cutDetailId) { this.cutDetailId = cutDetailId; }
    public Long getCutOrderId() { return cutOrderId; }
    public void setCutOrderId(Long cutOrderId) { this.cutOrderId = cutOrderId; }
    public String getCutOrderNo() { return cutOrderNo; }
    public void setCutOrderNo(String cutOrderNo) { this.cutOrderNo = cutOrderNo; }
    public Long getProductionOrderDetailId() { return productionOrderDetailId; }
    public void setProductionOrderDetailId(Long productionOrderDetailId) { this.productionOrderDetailId = productionOrderDetailId; }
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
}

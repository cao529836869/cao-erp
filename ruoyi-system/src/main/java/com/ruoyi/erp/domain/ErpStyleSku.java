package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 童装款式SKU对象 erp_style_sku
 *
 * @author ruoyi
 * @date 2026-05-31
 */
public class ErpStyleSku extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 款式SKU ID */
    private Long skuId;

    /** 款式ID */
    private Long styleId;

    /** 款式名称 */
    private String styleName;

    /** 款号 */
    @Excel(name = "款号")
    private String styleNo;

    /** SKU编码，通常由款号、颜色、尺码组成 */
    @Excel(name = "SKU编码")
    private String skuCode;

    /** 颜色编码 */
    @Excel(name = "颜色编码")
    private String colorCode;

    /** 颜色名称 */
    @Excel(name = "颜色名称")
    private String colorName;

    /** 尺码编码 */
    @Excel(name = "尺码编码")
    private String sizeCode;

    /** 尺码名称 */
    @Excel(name = "尺码名称")
    private String sizeName;

    /** 条码 */
    @Excel(name = "条码")
    private String barcode;

    /** 建议零售价 */
    @Excel(name = "建议零售价")
    private BigDecimal retailPrice;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    public Long getSkuId()
    {
        return skuId;
    }

    public void setSkuId(Long skuId)
    {
        this.skuId = skuId;
    }

    public Long getStyleId()
    {
        return styleId;
    }

    public void setStyleId(Long styleId)
    {
        this.styleId = styleId;
    }

    public String getStyleName()
    {
        return styleName;
    }

    public void setStyleName(String styleName)
    {
        this.styleName = styleName;
    }

    public String getStyleNo()
    {
        return styleNo;
    }

    public void setStyleNo(String styleNo)
    {
        this.styleNo = styleNo;
    }

    public String getSkuCode()
    {
        return skuCode;
    }

    public void setSkuCode(String skuCode)
    {
        this.skuCode = skuCode;
    }

    public String getColorCode()
    {
        return colorCode;
    }

    public void setColorCode(String colorCode)
    {
        this.colorCode = colorCode;
    }

    public String getColorName()
    {
        return colorName;
    }

    public void setColorName(String colorName)
    {
        this.colorName = colorName;
    }

    public String getSizeCode()
    {
        return sizeCode;
    }

    public void setSizeCode(String sizeCode)
    {
        this.sizeCode = sizeCode;
    }

    public String getSizeName()
    {
        return sizeName;
    }

    public void setSizeName(String sizeName)
    {
        this.sizeName = sizeName;
    }

    public String getBarcode()
    {
        return barcode;
    }

    public void setBarcode(String barcode)
    {
        this.barcode = barcode;
    }

    public BigDecimal getRetailPrice()
    {
        return retailPrice;
    }

    public void setRetailPrice(BigDecimal retailPrice)
    {
        this.retailPrice = retailPrice;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("skuId", getSkuId())
                .append("styleId", getStyleId())
                .append("styleNo", getStyleNo())
                .append("skuCode", getSkuCode())
                .append("colorCode", getColorCode())
                .append("colorName", getColorName())
                .append("sizeCode", getSizeCode())
                .append("sizeName", getSizeName())
                .append("barcode", getBarcode())
                .append("retailPrice", getRetailPrice())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

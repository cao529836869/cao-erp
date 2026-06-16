package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 物料SKU对象 erp_material_sku
 *
 * @author ruoyi
 * @date 2026-05-31
 */
public class ErpMaterialSku extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 物料SKU ID */
    private Long materialSkuId;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 物料类型 */
    @Excel(name = "物料类型")
    private String materialType;

    /** 物料SKU编码 */
    @Excel(name = "物料SKU编码")
    private String materialSkuCode;

    /** 颜色编码 */
    @Excel(name = "颜色编码")
    private String colorCode;

    /** 颜色名称 */
    @Excel(name = "颜色名称")
    private String colorName;

    /** 规格名称 */
    @Excel(name = "规格名称")
    private String specName;

    /** 幅宽数值 */
    @Excel(name = "幅宽")
    private BigDecimal widthValue;

    /** 克重 */
    @Excel(name = "克重")
    private BigDecimal gramWeight;

    /** 库存单位 */
    @Excel(name = "库存单位")
    private String unitName;

    /** 条码 */
    @Excel(name = "条码")
    private String barcode;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    public Long getMaterialSkuId()
    {
        return materialSkuId;
    }

    public void setMaterialSkuId(Long materialSkuId)
    {
        this.materialSkuId = materialSkuId;
    }

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public String getMaterialCode()
    {
        return materialCode;
    }

    public void setMaterialCode(String materialCode)
    {
        this.materialCode = materialCode;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getMaterialType()
    {
        return materialType;
    }

    public void setMaterialType(String materialType)
    {
        this.materialType = materialType;
    }

    public String getMaterialSkuCode()
    {
        return materialSkuCode;
    }

    public void setMaterialSkuCode(String materialSkuCode)
    {
        this.materialSkuCode = materialSkuCode;
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

    public String getSpecName()
    {
        return specName;
    }

    public void setSpecName(String specName)
    {
        this.specName = specName;
    }

    public BigDecimal getWidthValue()
    {
        return widthValue;
    }

    public void setWidthValue(BigDecimal widthValue)
    {
        this.widthValue = widthValue;
    }

    public BigDecimal getGramWeight()
    {
        return gramWeight;
    }

    public void setGramWeight(BigDecimal gramWeight)
    {
        this.gramWeight = gramWeight;
    }

    public String getUnitName()
    {
        return unitName;
    }

    public void setUnitName(String unitName)
    {
        this.unitName = unitName;
    }

    public String getBarcode()
    {
        return barcode;
    }

    public void setBarcode(String barcode)
    {
        this.barcode = barcode;
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
                .append("materialSkuId", getMaterialSkuId())
                .append("materialId", getMaterialId())
                .append("materialCode", getMaterialCode())
                .append("materialName", getMaterialName())
                .append("materialType", getMaterialType())
                .append("materialSkuCode", getMaterialSkuCode())
                .append("colorCode", getColorCode())
                .append("colorName", getColorName())
                .append("specName", getSpecName())
                .append("widthValue", getWidthValue())
                .append("gramWeight", getGramWeight())
                .append("unitName", getUnitName())
                .append("barcode", getBarcode())
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

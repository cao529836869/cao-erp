package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 款式BOM明细对象 erp_style_bom_detail
 *
 * @author ruoyi
 * @date 2026-05-31
 */
public class ErpStyleBomDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** BOM明细ID */
    private Long bomDetailId;

    /** BOM ID */
    private Long bomId;

    /** 物料ID */
    private Long materialId;

    /** 物料SKU ID */
    private Long materialSkuId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 物料类型 */
    @Excel(name = "物料类型")
    private String materialType;

    /** 物料颜色 */
    @Excel(name = "物料颜色")
    private String colorName;

    /** 物料规格 */
    @Excel(name = "物料规格")
    private String specName;

    /** 单件标准用量 */
    @Excel(name = "单件标准用量")
    private BigDecimal usageQty;

    /** 损耗率 */
    @Excel(name = "损耗率")
    private BigDecimal lossRate;

    /** 用量单位 */
    @Excel(name = "用量单位")
    private String unitName;

    /** 使用部位 */
    @Excel(name = "使用部位")
    private String positionName;

    public Long getBomDetailId()
    {
        return bomDetailId;
    }

    public void setBomDetailId(Long bomDetailId)
    {
        this.bomDetailId = bomDetailId;
    }

    public Long getBomId()
    {
        return bomId;
    }

    public void setBomId(Long bomId)
    {
        this.bomId = bomId;
    }

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public Long getMaterialSkuId()
    {
        return materialSkuId;
    }

    public void setMaterialSkuId(Long materialSkuId)
    {
        this.materialSkuId = materialSkuId;
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

    public BigDecimal getUsageQty()
    {
        return usageQty;
    }

    public void setUsageQty(BigDecimal usageQty)
    {
        this.usageQty = usageQty;
    }

    public BigDecimal getLossRate()
    {
        return lossRate;
    }

    public void setLossRate(BigDecimal lossRate)
    {
        this.lossRate = lossRate;
    }

    public String getUnitName()
    {
        return unitName;
    }

    public void setUnitName(String unitName)
    {
        this.unitName = unitName;
    }

    public String getPositionName()
    {
        return positionName;
    }

    public void setPositionName(String positionName)
    {
        this.positionName = positionName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("bomDetailId", getBomDetailId())
                .append("bomId", getBomId())
                .append("materialId", getMaterialId())
                .append("materialSkuId", getMaterialSkuId())
                .append("materialCode", getMaterialCode())
                .append("materialName", getMaterialName())
                .append("materialType", getMaterialType())
                .append("colorName", getColorName())
                .append("specName", getSpecName())
                .append("usageQty", getUsageQty())
                .append("lossRate", getLossRate())
                .append("unitName", getUnitName())
                .append("positionName", getPositionName())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

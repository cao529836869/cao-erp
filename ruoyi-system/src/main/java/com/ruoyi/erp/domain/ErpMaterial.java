package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 物料主对象 erp_material
 *
 * @author ruoyi
 * @date 2026-05-31
 */
public class ErpMaterial extends BaseEntity
{
    private static final long serialVersionUID = 1L;

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

    /** 物料分类 */
    @Excel(name = "物料分类")
    private String categoryName;

    /** 库存单位 */
    @Excel(name = "库存单位")
    private String unitName;

    /** 默认供应商ID */
    private Long defaultSupplierId;

    /** 默认供应商名称 */
    @Excel(name = "默认供应商")
    private String defaultSupplierName;

    /** 安全库存数量 */
    @Excel(name = "安全库存")
    private BigDecimal safeStockQty;

    /** 当前库存数量 */
    @Excel(name = "当前库存")
    private BigDecimal currentStockQty;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    /** 物料SKU列表 */
    private List<ErpMaterialSku> skuList;

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    @Size(min = 0, max = 40, message = "物料编码长度不能超过40个字符")
    public String getMaterialCode()
    {
        return materialCode;
    }

    public void setMaterialCode(String materialCode)
    {
        this.materialCode = materialCode;
    }

    @NotBlank(message = "物料名称不能为空")
    @Size(min = 0, max = 100, message = "物料名称长度不能超过100个字符")
    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    @NotBlank(message = "物料类型不能为空")
    public String getMaterialType()
    {
        return materialType;
    }

    public void setMaterialType(String materialType)
    {
        this.materialType = materialType;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    @NotBlank(message = "库存单位不能为空")
    public String getUnitName()
    {
        return unitName;
    }

    public void setUnitName(String unitName)
    {
        this.unitName = unitName;
    }

    public Long getDefaultSupplierId()
    {
        return defaultSupplierId;
    }

    public void setDefaultSupplierId(Long defaultSupplierId)
    {
        this.defaultSupplierId = defaultSupplierId;
    }

    public String getDefaultSupplierName()
    {
        return defaultSupplierName;
    }

    public void setDefaultSupplierName(String defaultSupplierName)
    {
        this.defaultSupplierName = defaultSupplierName;
    }

    public BigDecimal getSafeStockQty()
    {
        return safeStockQty;
    }

    public void setSafeStockQty(BigDecimal safeStockQty)
    {
        this.safeStockQty = safeStockQty;
    }

    public BigDecimal getCurrentStockQty()
    {
        return currentStockQty;
    }

    public void setCurrentStockQty(BigDecimal currentStockQty)
    {
        this.currentStockQty = currentStockQty;
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

    public List<ErpMaterialSku> getSkuList()
    {
        return skuList;
    }

    public void setSkuList(List<ErpMaterialSku> skuList)
    {
        this.skuList = skuList;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("materialId", getMaterialId())
                .append("materialCode", getMaterialCode())
                .append("materialName", getMaterialName())
                .append("materialType", getMaterialType())
                .append("categoryName", getCategoryName())
                .append("unitName", getUnitName())
                .append("defaultSupplierId", getDefaultSupplierId())
                .append("defaultSupplierName", getDefaultSupplierName())
                .append("safeStockQty", getSafeStockQty())
                .append("currentStockQty", getCurrentStockQty())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("skuList", getSkuList())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

package com.ruoyi.erp.domain;

import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 童装款式主对象 erp_style
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
public class ErpStyle extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 款式ID */
    private Long styleId;

    /** 款号 */
    @Excel(name = "款号")
    private String styleNo;

    /** 款式名称 */
    @Excel(name = "款式名称")
    private String styleName;

    /** 品类 */
    @Excel(name = "品类")
    private String categoryName;

    /** 品牌名称 */
    @Excel(name = "品牌名称")
    private String brandName;

    /** 季节 */
    @Excel(name = "季节")
    private String seasonName;

    /** 年份 */
    @Excel(name = "年份")
    private String yearName;

    /** 童装性别类型 */
    @Excel(name = "性别类型")
    private String genderType;

    /** 设计师 */
    @Excel(name = "设计师")
    private String designer;

    /** 样衣状态 */
    @Excel(name = "样衣状态", readConverterExp = "0=待打样,1=打样中,2=已确认")
    private String sampleStatus;

    /** 生产状态 */
    @Excel(name = "生产状态", readConverterExp = "0=未投产,1=生产中,2=已完结")
    private String productionStatus;

    /** 主图地址 */
    @Excel(name = "主图地址")
    private String imageUrl;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    /** 款式SKU列表 */
    private List<ErpStyleSku> skuList;

    /** 款式BOM列表 */
    private List<ErpStyleBom> bomList;

    public void setStyleId(Long styleId) 
    {
        this.styleId = styleId;
    }

    public Long getStyleId() 
    {
        return styleId;
    }

    public void setStyleNo(String styleNo) 
    {
        this.styleNo = styleNo;
    }

    public String getStyleNo() 
    {
        return styleNo;
    }

    public void setStyleName(String styleName) 
    {
        this.styleName = styleName;
    }

    public String getStyleName() 
    {
        return styleName;
    }

    public void setCategoryName(String categoryName) 
    {
        this.categoryName = categoryName;
    }

    public String getCategoryName() 
    {
        return categoryName;
    }

    public void setBrandName(String brandName) 
    {
        this.brandName = brandName;
    }

    public String getBrandName() 
    {
        return brandName;
    }

    public void setSeasonName(String seasonName) 
    {
        this.seasonName = seasonName;
    }

    public String getSeasonName() 
    {
        return seasonName;
    }

    public void setYearName(String yearName) 
    {
        this.yearName = yearName;
    }

    public String getYearName() 
    {
        return yearName;
    }

    public void setGenderType(String genderType) 
    {
        this.genderType = genderType;
    }

    public String getGenderType() 
    {
        return genderType;
    }

    public void setDesigner(String designer) 
    {
        this.designer = designer;
    }

    public String getDesigner() 
    {
        return designer;
    }

    public void setSampleStatus(String sampleStatus) 
    {
        this.sampleStatus = sampleStatus;
    }

    public String getSampleStatus() 
    {
        return sampleStatus;
    }

    public void setProductionStatus(String productionStatus) 
    {
        this.productionStatus = productionStatus;
    }

    public String getProductionStatus() 
    {
        return productionStatus;
    }

    public void setImageUrl(String imageUrl) 
    {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() 
    {
        return imageUrl;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    public List<ErpStyleSku> getSkuList()
    {
        return skuList;
    }

    public void setSkuList(List<ErpStyleSku> skuList)
    {
        this.skuList = skuList;
    }

    public List<ErpStyleBom> getBomList()
    {
        return bomList;
    }

    public void setBomList(List<ErpStyleBom> bomList)
    {
        this.bomList = bomList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("styleId", getStyleId())
            .append("styleNo", getStyleNo())
            .append("styleName", getStyleName())
            .append("categoryName", getCategoryName())
            .append("brandName", getBrandName())
            .append("seasonName", getSeasonName())
            .append("yearName", getYearName())
            .append("genderType", getGenderType())
            .append("designer", getDesigner())
            .append("sampleStatus", getSampleStatus())
            .append("productionStatus", getProductionStatus())
            .append("imageUrl", getImageUrl())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("skuList", getSkuList())
            .append("bomList", getBomList())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

package com.ruoyi.erp.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 款式BOM主对象 erp_style_bom
 *
 * @author ruoyi
 * @date 2026-05-31
 */
public class ErpStyleBom extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** BOM ID */
    private Long bomId;

    /** BOM编号 */
    @Excel(name = "BOM编号")
    private String bomNo;

    /** 款式ID */
    private Long styleId;

    /** 款号 */
    @Excel(name = "款号")
    private String styleNo;

    /** BOM版本号 */
    @Excel(name = "BOM版本号")
    private String versionNo;

    /** BOM状态（0草稿 1已审核 2已停用） */
    @Excel(name = "BOM状态", readConverterExp = "0=草稿,1=已审核,2=已停用")
    private String bomStatus;

    /** 生效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date effectiveDate;

    /** 审核人 */
    @Excel(name = "审核人")
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** BOM明细 */
    private List<ErpStyleBomDetail> detailList;

    public Long getBomId()
    {
        return bomId;
    }

    public void setBomId(Long bomId)
    {
        this.bomId = bomId;
    }

    public String getBomNo()
    {
        return bomNo;
    }

    public void setBomNo(String bomNo)
    {
        this.bomNo = bomNo;
    }

    public Long getStyleId()
    {
        return styleId;
    }

    public void setStyleId(Long styleId)
    {
        this.styleId = styleId;
    }

    public String getStyleNo()
    {
        return styleNo;
    }

    public void setStyleNo(String styleNo)
    {
        this.styleNo = styleNo;
    }

    public String getVersionNo()
    {
        return versionNo;
    }

    public void setVersionNo(String versionNo)
    {
        this.versionNo = versionNo;
    }

    public String getBomStatus()
    {
        return bomStatus;
    }

    public void setBomStatus(String bomStatus)
    {
        this.bomStatus = bomStatus;
    }

    public Date getEffectiveDate()
    {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate)
    {
        this.effectiveDate = effectiveDate;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public Date getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    public List<ErpStyleBomDetail> getDetailList()
    {
        return detailList;
    }

    public void setDetailList(List<ErpStyleBomDetail> detailList)
    {
        this.detailList = detailList;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("bomId", getBomId())
                .append("bomNo", getBomNo())
                .append("styleId", getStyleId())
                .append("styleNo", getStyleNo())
                .append("versionNo", getVersionNo())
                .append("bomStatus", getBomStatus())
                .append("effectiveDate", getEffectiveDate())
                .append("auditBy", getAuditBy())
                .append("auditTime", getAuditTime())
                .append("detailList", getDetailList())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

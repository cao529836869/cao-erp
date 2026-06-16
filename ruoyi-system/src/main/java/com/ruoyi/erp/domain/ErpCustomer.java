package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 客户档案对象 erp_customer
 *
 * @author ruoyi
 */
public class ErpCustomer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 客户ID */
    private Long customerId;

    /** 客户编码 */
    @Excel(name = "客户编码")
    private String customerCode;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;

    /** 客户类型 */
    @Excel(name = "客户类型")
    private String customerType;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactName;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 联系邮箱 */
    @Excel(name = "联系邮箱")
    private String contactEmail;

    /** 省份 */
    @Excel(name = "省份")
    private String province;

    /** 城市 */
    @Excel(name = "城市")
    private String city;

    /** 区县 */
    @Excel(name = "区县")
    private String county;

    /** 详细地址 */
    @Excel(name = "详细地址")
    private String address;

    /** 信用额度 */
    @Excel(name = "信用额度")
    private BigDecimal creditLimit;

    /** 结算方式 */
    @Excel(name = "结算方式")
    private String settlementType;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    @NotBlank(message = "客户编码不能为空")
    @Size(min = 0, max = 40, message = "客户编码长度不能超过40个字符")
    public String getCustomerCode()
    {
        return customerCode;
    }

    public void setCustomerCode(String customerCode)
    {
        this.customerCode = customerCode;
    }

    @NotBlank(message = "客户名称不能为空")
    @Size(min = 0, max = 100, message = "客户名称长度不能超过100个字符")
    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public String getCustomerType()
    {
        return customerType;
    }

    public void setCustomerType(String customerType)
    {
        this.customerType = customerType;
    }

    public String getContactName()
    {
        return contactName;
    }

    public void setContactName(String contactName)
    {
        this.contactName = contactName;
    }

    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail()
    {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail)
    {
        this.contactEmail = contactEmail;
    }

    public String getProvince()
    {
        return province;
    }

    public void setProvince(String province)
    {
        this.province = province;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getCounty()
    {
        return county;
    }

    public void setCounty(String county)
    {
        this.county = county;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public BigDecimal getCreditLimit()
    {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit)
    {
        this.creditLimit = creditLimit;
    }

    public String getSettlementType()
    {
        return settlementType;
    }

    public void setSettlementType(String settlementType)
    {
        this.settlementType = settlementType;
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
            .append("customerId", getCustomerId())
            .append("customerCode", getCustomerCode())
            .append("customerName", getCustomerName())
            .append("customerType", getCustomerType())
            .append("contactName", getContactName())
            .append("contactPhone", getContactPhone())
            .append("contactEmail", getContactEmail())
            .append("province", getProvince())
            .append("city", getCity())
            .append("county", getCounty())
            .append("address", getAddress())
            .append("creditLimit", getCreditLimit())
            .append("settlementType", getSettlementType())
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

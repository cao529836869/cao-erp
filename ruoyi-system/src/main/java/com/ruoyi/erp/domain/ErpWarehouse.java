package com.ruoyi.erp.domain;

import jakarta.validation.constraints.NotBlank;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 仓库档案对象 erp_warehouse
 */
public class ErpWarehouse extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long warehouseId;

    @Excel(name = "仓库编码")
    private String warehouseCode;

    @Excel(name = "仓库名称")
    private String warehouseName;

    @Excel(name = "仓库类型")
    private String warehouseType;

    @Excel(name = "负责人")
    private String managerName;

    @Excel(name = "联系电话")
    private String contactPhone;

    @Excel(name = "省份")
    private String province;

    @Excel(name = "城市")
    private String city;

    @Excel(name = "区县")
    private String county;

    @Excel(name = "仓库地址")
    private String address;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    private String delFlag;

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    @NotBlank(message = "仓库编码不能为空")
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }

    @NotBlank(message = "仓库名称不能为空")
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getWarehouseType() { return warehouseType; }
    public void setWarehouseType(String warehouseType) { this.warehouseType = warehouseType; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}

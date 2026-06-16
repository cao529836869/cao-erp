package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class ErpDeliveryOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long deliveryOrderId;
    @Excel(name = "发货单号")
    private String deliveryOrderNo;
    private Long salesOrderId;
    @Excel(name = "销售单号")
    private String salesOrderNo;
    private Long customerId;
    @Excel(name = "客户")
    private String customerName;
    private Long warehouseId;
    @Excel(name = "仓库")
    private String warehouseName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发货日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date deliveryDate;
    @Excel(name = "发货数量")
    private BigDecimal totalQty;
    @Excel(name = "状态")
    private String deliveryStatus;

    public Long getDeliveryOrderId() { return deliveryOrderId; }
    public void setDeliveryOrderId(Long deliveryOrderId) { this.deliveryOrderId = deliveryOrderId; }
    public String getDeliveryOrderNo() { return deliveryOrderNo; }
    public void setDeliveryOrderNo(String deliveryOrderNo) { this.deliveryOrderNo = deliveryOrderNo; }
    public Long getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(Long salesOrderId) { this.salesOrderId = salesOrderId; }
    public String getSalesOrderNo() { return salesOrderNo; }
    public void setSalesOrderNo(String salesOrderNo) { this.salesOrderNo = salesOrderNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public Date getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(Date deliveryDate) { this.deliveryDate = deliveryDate; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
}

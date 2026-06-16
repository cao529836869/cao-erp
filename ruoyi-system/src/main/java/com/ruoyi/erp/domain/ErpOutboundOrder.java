package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 出库单对象 erp_outbound_order
 */
public class ErpOutboundOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long outboundOrderId;
    @Excel(name = "出库单号")
    private String outboundOrderNo;
    @Excel(name = "出库类型")
    private String outboundType;
    private String sourceType;
    private String sourceNo;
    private Long customerId;
    @Excel(name = "客户")
    private String customerName;
    private Long supplierId;
    private String supplierName;
    private Long warehouseId;
    @Excel(name = "仓库")
    private String warehouseName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "出库日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date outboundDate;
    @Excel(name = "总数量")
    private BigDecimal totalQty;
    @Excel(name = "总金额")
    private BigDecimal totalAmount;
    @Excel(name = "状态")
    private String orderStatus;
    @Excel(name = "物料SKU")
    private String itemSummary;
    private String auditBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;
    private String pickBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date pickTime;
    private String postBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date postTime;
    private String cancelBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cancelTime;
    private List<ErpOutboundOrderDetail> detailList;

    public Long getOutboundOrderId() { return outboundOrderId; }
    public void setOutboundOrderId(Long outboundOrderId) { this.outboundOrderId = outboundOrderId; }
    public String getOutboundOrderNo() { return outboundOrderNo; }
    public void setOutboundOrderNo(String outboundOrderNo) { this.outboundOrderNo = outboundOrderNo; }
    @NotBlank(message = "出库类型不能为空")
    public String getOutboundType() { return outboundType; }
    public void setOutboundType(String outboundType) { this.outboundType = outboundType; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceNo() { return sourceNo; }
    public void setSourceNo(String sourceNo) { this.sourceNo = sourceNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    @NotNull(message = "出库仓库不能为空")
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    @NotNull(message = "出库日期不能为空")
    public Date getOutboundDate() { return outboundDate; }
    public void setOutboundDate(Date outboundDate) { this.outboundDate = outboundDate; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getItemSummary() { return itemSummary; }
    public void setItemSummary(String itemSummary) { this.itemSummary = itemSummary; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getPickBy() { return pickBy; }
    public void setPickBy(String pickBy) { this.pickBy = pickBy; }
    public Date getPickTime() { return pickTime; }
    public void setPickTime(Date pickTime) { this.pickTime = pickTime; }
    public String getPostBy() { return postBy; }
    public void setPostBy(String postBy) { this.postBy = postBy; }
    public Date getPostTime() { return postTime; }
    public void setPostTime(Date postTime) { this.postTime = postTime; }
    public String getCancelBy() { return cancelBy; }
    public void setCancelBy(String cancelBy) { this.cancelBy = cancelBy; }
    public Date getCancelTime() { return cancelTime; }
    public void setCancelTime(Date cancelTime) { this.cancelTime = cancelTime; }
    public List<ErpOutboundOrderDetail> getDetailList() { return detailList; }
    public void setDetailList(List<ErpOutboundOrderDetail> detailList) { this.detailList = detailList; }
}

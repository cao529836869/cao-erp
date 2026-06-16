package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class ErpSalesOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long salesOrderId;
    @Excel(name = "销售单号")
    private String salesOrderNo;
    private Long customerId;
    @Excel(name = "客户")
    private String customerName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "订单日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date orderDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "交货日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date deliveryDate;
    @Excel(name = "总数量")
    private BigDecimal totalQty;
    @Excel(name = "总金额")
    private BigDecimal totalAmount;
    @Excel(name = "状态")
    private String orderStatus;
    @Excel(name = "款式SKU")
    private String itemSummary;
    private List<ErpSalesOrderDetail> detailList;

    public Long getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(Long salesOrderId) { this.salesOrderId = salesOrderId; }
    public String getSalesOrderNo() { return salesOrderNo; }
    public void setSalesOrderNo(String salesOrderNo) { this.salesOrderNo = salesOrderNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public Date getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(Date deliveryDate) { this.deliveryDate = deliveryDate; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getItemSummary() { return itemSummary; }
    public void setItemSummary(String itemSummary) { this.itemSummary = itemSummary; }
    public List<ErpSalesOrderDetail> getDetailList() { return detailList; }
    public void setDetailList(List<ErpSalesOrderDetail> detailList) { this.detailList = detailList; }
}

package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 生产订单对象 erp_production_order
 */
public class ErpProductionOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long productionOrderId;

    @Excel(name = "生产订单号")
    private String productionOrderNo;

    private Long salesOrderId;

    @Excel(name = "销售订单号")
    private String salesOrderNo;

    private Long customerId;

    @Excel(name = "客户")
    private String customerName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划开工日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date planStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划完工日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date planFinishDate;

    @Excel(name = "生产总数")
    private BigDecimal totalQty;

    @Excel(name = "已完成数")
    private BigDecimal completedQty;

    @Excel(name = "状态")
    private String orderStatus;

    @Excel(name = "款式SKU")
    private String itemSummary;

    private Integer pickingPostedFlag;

    private String auditBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    private List<ErpProductionOrderDetail> detailList;

    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }
    public String getProductionOrderNo() { return productionOrderNo; }
    public void setProductionOrderNo(String productionOrderNo) { this.productionOrderNo = productionOrderNo; }
    public Long getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(Long salesOrderId) { this.salesOrderId = salesOrderId; }
    public String getSalesOrderNo() { return salesOrderNo; }
    public void setSalesOrderNo(String salesOrderNo) { this.salesOrderNo = salesOrderNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    @NotNull(message = "计划开工日期不能为空")
    public Date getPlanStartDate() { return planStartDate; }
    public void setPlanStartDate(Date planStartDate) { this.planStartDate = planStartDate; }
    @NotNull(message = "计划完工日期不能为空")
    public Date getPlanFinishDate() { return planFinishDate; }
    public void setPlanFinishDate(Date planFinishDate) { this.planFinishDate = planFinishDate; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public BigDecimal getCompletedQty() { return completedQty; }
    public void setCompletedQty(BigDecimal completedQty) { this.completedQty = completedQty; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getItemSummary() { return itemSummary; }
    public void setItemSummary(String itemSummary) { this.itemSummary = itemSummary; }
    public Integer getPickingPostedFlag() { return pickingPostedFlag; }
    public void setPickingPostedFlag(Integer pickingPostedFlag) { this.pickingPostedFlag = pickingPostedFlag; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public List<ErpProductionOrderDetail> getDetailList() { return detailList; }
    public void setDetailList(List<ErpProductionOrderDetail> detailList) { this.detailList = detailList; }
}

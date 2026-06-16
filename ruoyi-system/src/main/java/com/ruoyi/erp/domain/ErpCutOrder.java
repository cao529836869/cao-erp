package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class ErpCutOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long cutOrderId;
    @Excel(name = "裁剪单号")
    private String cutOrderNo;
    private Long productionOrderId;
    @Excel(name = "生产单号")
    private String productionOrderNo;
    private Long salesOrderId;
    @Excel(name = "销售单号")
    private String salesOrderNo;
    private Long customerId;
    @Excel(name = "客户")
    private String customerName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "计划完成时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date planFinishTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "实际完成时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;
    @Excel(name = "计划数量")
    private BigDecimal totalQty;
    @Excel(name = "实际完成")
    private BigDecimal actualCutQty;
    @Excel(name = "裁剪状态", readConverterExp = "0=待裁剪,1=裁剪中,2=已完成,3=已取消")
    private Integer cutStatus;
    private String cutStatusName;
    private Integer timeoutFlag;
    @Excel(name = "款式SKU")
    private String itemSummary;
    private List<ErpCutOrderDetail> detailList;

    public Long getCutOrderId() { return cutOrderId; }
    public void setCutOrderId(Long cutOrderId) { this.cutOrderId = cutOrderId; }
    public String getCutOrderNo() { return cutOrderNo; }
    public void setCutOrderNo(String cutOrderNo) { this.cutOrderNo = cutOrderNo; }
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
    public Date getPlanFinishTime() { return planFinishTime; }
    public void setPlanFinishTime(Date planFinishTime) { this.planFinishTime = planFinishTime; }
    public Date getFinishTime() { return finishTime; }
    public void setFinishTime(Date finishTime) { this.finishTime = finishTime; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public Integer getCutStatus() { return cutStatus; }
    public void setCutStatus(Integer cutStatus) { this.cutStatus = cutStatus; }
    public String getCutStatusName() { return cutStatusName; }
    public void setCutStatusName(String cutStatusName) { this.cutStatusName = cutStatusName; }
    public Integer getTimeoutFlag() { return timeoutFlag; }
    public void setTimeoutFlag(Integer timeoutFlag) { this.timeoutFlag = timeoutFlag; }
    public String getItemSummary() { return itemSummary; }
    public void setItemSummary(String itemSummary) { this.itemSummary = itemSummary; }
    public BigDecimal getActualCutQty() { return actualCutQty; }
    public void setActualCutQty(BigDecimal actualCutQty) { this.actualCutQty = actualCutQty; }
    public List<ErpCutOrderDetail> getDetailList() { return detailList; }
    public void setDetailList(List<ErpCutOrderDetail> detailList) { this.detailList = detailList; }
}

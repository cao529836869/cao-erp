package com.ruoyi.workflow.domain;

import jakarta.validation.constraints.NotNull;

public class ProductionWorkflowStartRequest
{
    @NotNull(message = "生产单ID不能为空")
    private Long productionOrderId;

    private String pickingAssignee;

    private String cuttingAssignee;

    public Long getProductionOrderId()
    {
        return productionOrderId;
    }

    public void setProductionOrderId(Long productionOrderId)
    {
        this.productionOrderId = productionOrderId;
    }

    public String getPickingAssignee()
    {
        return pickingAssignee;
    }

    public void setPickingAssignee(String pickingAssignee)
    {
        this.pickingAssignee = pickingAssignee;
    }

    public String getCuttingAssignee()
    {
        return cuttingAssignee;
    }

    public void setCuttingAssignee(String cuttingAssignee)
    {
        this.cuttingAssignee = cuttingAssignee;
    }
}

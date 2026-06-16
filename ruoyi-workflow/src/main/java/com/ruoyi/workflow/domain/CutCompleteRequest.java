package com.ruoyi.workflow.domain;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class CutCompleteRequest
{
    private Long cutOrderId;

    @NotNull(message = "实际裁剪数不能为空")
    @DecimalMin(value = "0.001", message = "实际裁剪数必须大于0")
    private BigDecimal actualCutQty;

    public Long getCutOrderId()
    {
        return cutOrderId;
    }

    public void setCutOrderId(Long cutOrderId)
    {
        this.cutOrderId = cutOrderId;
    }

    public BigDecimal getActualCutQty()
    {
        return actualCutQty;
    }

    public void setActualCutQty(BigDecimal actualCutQty)
    {
        this.actualCutQty = actualCutQty;
    }
}

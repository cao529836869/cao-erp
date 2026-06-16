package com.ruoyi.workflow.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.erp.domain.ErpOutboundOrder;
import com.ruoyi.erp.service.IErpProductionOrderService;
import com.ruoyi.workflow.service.ProductionWorkflowBusinessService;

@Component("createPickingDelegate")
public class CreatePickingDelegate implements JavaDelegate
{
    @Autowired
    private IErpProductionOrderService productionOrderService;

    @Autowired
    private ProductionWorkflowBusinessService businessService;

    @Override
    public void execute(DelegateExecution execution)
    {
        Long productionOrderId = (Long) execution.getVariable("productionOrderId");
        productionOrderService.countBomThenBuildOutboundOrder(productionOrderId);
        ErpOutboundOrder pickingOrder = businessService.getPickingOrder(productionOrderId);
        execution.setVariable("outboundOrderId", pickingOrder.getOutboundOrderId());
        execution.setVariable("outboundOrderNo", pickingOrder.getOutboundOrderNo());
    }
}

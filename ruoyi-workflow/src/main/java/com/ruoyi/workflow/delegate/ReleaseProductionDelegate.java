package com.ruoyi.workflow.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.erp.domain.ErpProductionOrder;
import com.ruoyi.erp.service.IErpProductionOrderService;
import com.ruoyi.workflow.service.ProductionWorkflowBusinessService;

@Component("releaseProductionDelegate")
public class ReleaseProductionDelegate implements JavaDelegate
{
    @Autowired
    private IErpProductionOrderService productionOrderService;

    @Autowired
    private ProductionWorkflowBusinessService businessService;

    @Override
    public void execute(DelegateExecution execution)
    {
        Long productionOrderId = (Long) execution.getVariable("productionOrderId");
        String starter = (String) execution.getVariable("starter");
        productionOrderService.releaseProductionOrder(productionOrderId, starter);
        ErpProductionOrder order = businessService.getProductionOrder(productionOrderId);
        execution.setVariable("productionOrderNo", order.getProductionOrderNo());
    }
}

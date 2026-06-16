package com.ruoyi.workflow.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.erp.domain.ErpCutOrder;
import com.ruoyi.erp.service.IErpCutOrderService;
import com.ruoyi.workflow.service.ProductionWorkflowBusinessService;

@Component("createCutOrderDelegate")
public class CreateCutOrderDelegate implements JavaDelegate
{
    @Autowired
    private IErpCutOrderService cutOrderService;

    @Autowired
    private ProductionWorkflowBusinessService businessService;

    @Override
    public void execute(DelegateExecution execution)
    {
        Long productionOrderId = (Long) execution.getVariable("productionOrderId");
        String starter = (String) execution.getVariable("starter");
        cutOrderService.createFromProduction(productionOrderId, starter);
        ErpCutOrder cutOrder = businessService.getCutOrder(productionOrderId);
        execution.setVariable("cutOrderId", cutOrder.getCutOrderId());
        execution.setVariable("cutOrderNo", cutOrder.getCutOrderNo());
    }
}

package com.ruoyi.workflow.service;

import java.util.List;
import com.ruoyi.workflow.domain.CutCompleteRequest;
import com.ruoyi.workflow.domain.PickingCompleteRequest;
import com.ruoyi.workflow.domain.ProductionWorkflowInstanceVo;
import com.ruoyi.workflow.domain.ProductionWorkflowStartRequest;
import com.ruoyi.workflow.domain.ProductionWorkflowTaskVo;

public interface IProductionWorkflowService
{
    ProductionWorkflowInstanceVo start(ProductionWorkflowStartRequest request, String username);

    List<ProductionWorkflowTaskVo> selectTodoList(String username);

    ProductionWorkflowTaskVo selectTask(String taskId);

    void completePicking(String taskId, PickingCompleteRequest request, String username);

    void completeCutting(String taskId, CutCompleteRequest request, String username);
}

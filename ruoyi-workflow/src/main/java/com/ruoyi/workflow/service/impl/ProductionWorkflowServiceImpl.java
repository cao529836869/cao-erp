package com.ruoyi.workflow.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.erp.domain.ErpCutOrder;
import com.ruoyi.erp.domain.ErpOutboundOrder;
import com.ruoyi.erp.domain.ErpProductionOrder;
import com.ruoyi.erp.service.IErpCutOrderService;
import com.ruoyi.erp.service.IErpOutboundOrderService;
import com.ruoyi.workflow.domain.CutCompleteRequest;
import com.ruoyi.workflow.domain.PickingCompleteRequest;
import com.ruoyi.workflow.domain.ProductionWorkflowInstanceVo;
import com.ruoyi.workflow.domain.ProductionWorkflowStartRequest;
import com.ruoyi.workflow.domain.ProductionWorkflowTaskVo;
import com.ruoyi.workflow.service.IProductionWorkflowService;
import com.ruoyi.workflow.service.ProductionWorkflowBusinessService;

@Service
public class ProductionWorkflowServiceImpl implements IProductionWorkflowService
{
    private static final String PROCESS_KEY = "production_cut_flow";
    private static final String TASK_KEY_POST_PICKING = "postPickingUserTask";
    private static final String TASK_KEY_FINISH_CUT = "finishCutUserTask";

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IErpOutboundOrderService outboundOrderService;

    @Autowired
    private IErpCutOrderService cutOrderService;

    @Autowired
    private ProductionWorkflowBusinessService businessService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductionWorkflowInstanceVo start(ProductionWorkflowStartRequest request, String username)
    {
        ErpProductionOrder productionOrder = businessService.getProductionOrder(request.getProductionOrderId());
        String businessKey = "production:" + request.getProductionOrderId();
        ProcessInstance exists = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(PROCESS_KEY)
                .processInstanceBusinessKey(businessKey)
                .singleResult();
        if (exists != null)
        {
            throw new ServiceException("该生产单已存在运行中的流程");
        }

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("starter", username);
        variables.put("productionOrderId", request.getProductionOrderId());
        variables.put("productionOrderNo", productionOrder.getProductionOrderNo());
        variables.put("pickingAssignee", StringUtils.defaultIfBlank(request.getPickingAssignee(), username));
        variables.put("cuttingAssignee", StringUtils.defaultIfBlank(request.getCuttingAssignee(), username));

        ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS_KEY, businessKey, variables);
        return buildInstanceVo(instance);
    }

    @Override
    public List<ProductionWorkflowTaskVo> selectTodoList(String username)
    {
        return taskService.createTaskQuery()
                .processDefinitionKey(PROCESS_KEY)
                .taskAssignee(username)
                .includeProcessVariables()
                .orderByTaskCreateTime()
                .desc()
                .list()
                .stream()
                .map(this::buildTaskVo)
                .collect(Collectors.toList());
    }

    @Override
    public ProductionWorkflowTaskVo selectTask(String taskId)
    {
        Task task = getTask(taskId);
        return buildTaskVo(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completePicking(String taskId, PickingCompleteRequest request, String username)
    {
        Task task = getTask(taskId);
        assertTask(task, TASK_KEY_POST_PICKING, username);
        Long outboundOrderId = request == null ? null : request.getOutboundOrderId();
        if (outboundOrderId == null)
        {
            Long productionOrderId = (Long) taskService.getVariable(taskId, "productionOrderId");
            ErpOutboundOrder pickingOrder = businessService.getPickingOrder(productionOrderId);
            outboundOrderId = pickingOrder.getOutboundOrderId();
        }

        ErpOutboundOrder outboundOrder = outboundOrderService.selectOutboundOrderById(outboundOrderId);
        if (outboundOrder == null)
        {
            throw new ServiceException("领料单不存在");
        }
        if (!StringUtils.equals("已出库", outboundOrder.getOrderStatus())
                && !StringUtils.equals("已完成", outboundOrder.getOrderStatus()))
        {
            outboundOrderService.postOutboundOrder(outboundOrderId, username);
        }

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("outboundOrderId", outboundOrderId);
        taskService.complete(taskId, variables);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeCutting(String taskId, CutCompleteRequest request, String username)
    {
        Task task = getTask(taskId);
        assertTask(task, TASK_KEY_FINISH_CUT, username);
        if (request == null || request.getActualCutQty() == null)
        {
            throw new ServiceException("实际裁剪数不能为空");
        }

        Long cutOrderId = request.getCutOrderId();
        if (cutOrderId == null)
        {
            Long productionOrderId = (Long) taskService.getVariable(taskId, "productionOrderId");
            ErpCutOrder cutOrder = businessService.getCutOrder(productionOrderId);
            cutOrderId = cutOrder.getCutOrderId();
        }

        ErpCutOrder cutOrder = cutOrderService.selectCutOrderById(cutOrderId);
        if (cutOrder == null)
        {
            throw new ServiceException("裁剪单不存在");
        }
        if (cutOrder.getCutStatus() == null || cutOrder.getCutStatus() != 2)
        {
            cutOrderService.finishCutOrder(cutOrderId, request.getActualCutQty(), username);
        }

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("cutOrderId", cutOrderId);
        variables.put("actualCutQty", request.getActualCutQty());
        taskService.complete(taskId, variables);
    }

    private Task getTask(String taskId)
    {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .includeProcessVariables()
                .singleResult();
        if (task == null)
        {
            throw new ServiceException("流程任务不存在或已处理");
        }
        return task;
    }

    private void assertTask(Task task, String taskDefinitionKey, String username)
    {
        if (!StringUtils.equals(taskDefinitionKey, task.getTaskDefinitionKey()))
        {
            throw new ServiceException("当前任务节点不允许执行该操作");
        }
        if (StringUtils.isNotBlank(task.getAssignee()) && !StringUtils.equals(username, task.getAssignee()))
        {
            throw new ServiceException("当前任务不属于登录用户");
        }
    }

    private ProductionWorkflowTaskVo buildTaskVo(Task task)
    {
        ProductionWorkflowTaskVo vo = new ProductionWorkflowTaskVo();
        vo.setTaskId(task.getId());
        vo.setTaskName(task.getName());
        vo.setProcessInstanceId(task.getProcessInstanceId());
        vo.setCreateTime(task.getCreateTime());
        vo.setAssignee(task.getAssignee());
        vo.setVariables(task.getProcessVariables());
        return vo;
    }

    private ProductionWorkflowInstanceVo buildInstanceVo(ProcessInstance instance)
    {
        ProductionWorkflowInstanceVo vo = new ProductionWorkflowInstanceVo();
        vo.setProcessInstanceId(instance.getProcessInstanceId());
        vo.setBusinessKey(instance.getBusinessKey());
        vo.setVariables(runtimeService.getVariables(instance.getProcessInstanceId()));
        return vo;
    }
}

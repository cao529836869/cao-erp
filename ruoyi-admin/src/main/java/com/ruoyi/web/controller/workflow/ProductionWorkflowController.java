package com.ruoyi.web.controller.workflow;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.workflow.domain.CutCompleteRequest;
import com.ruoyi.workflow.domain.PickingCompleteRequest;
import com.ruoyi.workflow.domain.ProductionWorkflowStartRequest;
import com.ruoyi.workflow.domain.ProductionWorkflowTaskVo;
import com.ruoyi.workflow.service.IProductionWorkflowService;

@RestController
@RequestMapping("/workflow/production")
public class ProductionWorkflowController extends BaseController
{
    @Autowired
    private IProductionWorkflowService productionWorkflowService;

    @PreAuthorize("@ss.hasPermi('workflow:production:start')")
    @Log(title = "生产领料裁剪流程", businessType = BusinessType.INSERT)
    @PostMapping("/start")
    public AjaxResult start(@Validated @RequestBody ProductionWorkflowStartRequest request)
    {
        return success(productionWorkflowService.start(request, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('workflow:production:todo')")
    @GetMapping("/todo")
    public TableDataInfo todo()
    {
        List<ProductionWorkflowTaskVo> list = productionWorkflowService.selectTodoList(getUsername());
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(list.size());
        return rspData;
    }

    @PreAuthorize("@ss.hasPermi('workflow:production:query')")
    @GetMapping("/task/{taskId}")
    public AjaxResult task(@PathVariable String taskId)
    {
        return success(productionWorkflowService.selectTask(taskId));
    }

    @PreAuthorize("@ss.hasPermi('workflow:production:picking')")
    @Log(title = "生产领料出库过账", businessType = BusinessType.UPDATE)
    @PutMapping("/task/{taskId}/picking")
    public AjaxResult completePicking(@PathVariable String taskId, @RequestBody(required = false) PickingCompleteRequest request)
    {
        productionWorkflowService.completePicking(taskId, request, getUsername());
        return success();
    }

    @PreAuthorize("@ss.hasPermi('workflow:production:cut')")
    @Log(title = "生产裁剪完成", businessType = BusinessType.UPDATE)
    @PutMapping("/task/{taskId}/cut")
    public AjaxResult completeCutting(@PathVariable String taskId, @Validated @RequestBody CutCompleteRequest request)
    {
        productionWorkflowService.completeCutting(taskId, request, getUsername());
        return success();
    }
}

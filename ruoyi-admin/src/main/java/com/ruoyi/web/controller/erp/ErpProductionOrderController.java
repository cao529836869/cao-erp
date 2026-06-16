package com.ruoyi.web.controller.erp;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.erp.domain.ErpProductionOrder;
import com.ruoyi.erp.service.IErpCutOrderService;
import com.ruoyi.erp.service.IErpProductionOrderService;

@RestController
@RequestMapping("/erp/production")
public class ErpProductionOrderController extends BaseController
{
    @Autowired
    private IErpProductionOrderService productionOrderService;

    @Autowired
    private IErpCutOrderService cutOrderService;

    @PreAuthorize("@ss.hasPermi('erp:production:list')")
    @GetMapping("/list")
    public TableDataInfo list(ErpProductionOrder productionOrder)
    {
        startPage();
        return getDataTable(productionOrderService.selectProductionOrderList(productionOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:production:export')")
    @Log(title = "生产订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ErpProductionOrder productionOrder)
    {
        List<ErpProductionOrder> list = productionOrderService.selectProductionOrderList(productionOrder);
        ExcelUtil<ErpProductionOrder> util = new ExcelUtil<ErpProductionOrder>(ErpProductionOrder.class);
        util.exportExcel(response, list, "生产订单数据");
    }

    @PreAuthorize("@ss.hasPermi('erp:production:query')")
    @GetMapping("/{productionOrderId}")
    public AjaxResult getInfo(@PathVariable Long productionOrderId)
    {
        return success(productionOrderService.selectProductionOrderById(productionOrderId));
    }

    @PreAuthorize("@ss.hasPermi('erp:production:add')")
    @Log(title = "生产订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ErpProductionOrder productionOrder)
    {
        productionOrder.setCreateBy(getUsername());
        return toAjax(productionOrderService.insertProductionOrder(productionOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:production:edit')")
    @Log(title = "生产订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ErpProductionOrder productionOrder)
    {
        productionOrder.setUpdateBy(getUsername());
        return toAjax(productionOrderService.updateProductionOrder(productionOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:production:remove')")
    @Log(title = "生产订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{productionOrderIds}")
    public AjaxResult remove(@PathVariable Long[] productionOrderIds)
    {
        return toAjax(productionOrderService.deleteProductionOrderByIds(productionOrderIds));
    }

    @PreAuthorize("@ss.hasPermi('erp:production:release')")
    @Log(title = "生产订单下达", businessType = BusinessType.UPDATE)
    @PutMapping("/release/{productionOrderId}")
    public AjaxResult release(@PathVariable Long productionOrderId)
    {
        return toAjax(productionOrderService.releaseProductionOrder(productionOrderId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('erp:production:close')")
    @Log(title = "生产订单关闭", businessType = BusinessType.UPDATE)
    @PutMapping("/close/{productionOrderId}")
    public AjaxResult close(@PathVariable Long productionOrderId)
    {
        return toAjax(productionOrderService.closeProductionOrder(productionOrderId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('erp:production:picking')")
    @Log(title = "生产订单生成领料单", businessType = BusinessType.INSERT)
    @PutMapping("/picking/{productionOrderId}")
    public AjaxResult picking(@PathVariable Long productionOrderId)
    {
        return toAjax(productionOrderService.countBomThenBuildOutboundOrder(productionOrderId));
    }

    @PreAuthorize("@ss.hasPermi('erp:cut:add')")
    @Log(title = "生产单生成裁剪单", businessType = BusinessType.INSERT)
    @PutMapping("/cut/{productionOrderId}")
    public AjaxResult cut(@PathVariable Long productionOrderId)
    {
        return toAjax(cutOrderService.createFromProduction(productionOrderId, getUsername()));
    }
}

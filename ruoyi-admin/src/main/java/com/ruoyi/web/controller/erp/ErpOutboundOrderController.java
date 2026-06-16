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
import com.ruoyi.erp.domain.ErpInventory;
import com.ruoyi.erp.domain.ErpOutboundOrder;
import com.ruoyi.erp.service.IErpInventoryService;
import com.ruoyi.erp.service.IErpOutboundOrderService;

@RestController
@RequestMapping("/erp/outbound")
public class ErpOutboundOrderController extends BaseController
{
    @Autowired
    private IErpOutboundOrderService outboundOrderService;

    @Autowired
    private IErpInventoryService inventoryService;

    @PreAuthorize("@ss.hasPermi('erp:outbound:list')")
    @GetMapping("/list")
    public TableDataInfo list(ErpOutboundOrder outboundOrder)
    {
        startPage();
        return getDataTable(outboundOrderService.selectOutboundOrderList(outboundOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:outbound:export')")
    @Log(title = "出库单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ErpOutboundOrder outboundOrder)
    {
        List<ErpOutboundOrder> list = outboundOrderService.selectOutboundOrderList(outboundOrder);
        ExcelUtil<ErpOutboundOrder> util = new ExcelUtil<ErpOutboundOrder>(ErpOutboundOrder.class);
        util.exportExcel(response, list, "出库单数据");
    }

    @PreAuthorize("@ss.hasPermi('erp:outbound:query')")
    @GetMapping("/{outboundOrderId}")
    public AjaxResult getInfo(@PathVariable Long outboundOrderId)
    {
        return success(outboundOrderService.selectOutboundOrderById(outboundOrderId));
    }

    @PreAuthorize("@ss.hasAnyPermi('erp:outbound:add,erp:outbound:edit,erp:outbound:query,erp:inventory:list')")
    @GetMapping("/batches")
    public AjaxResult batches(ErpInventory inventory)
    {
        return success(inventoryService.selectInventoryList(inventory));
    }

    @PreAuthorize("@ss.hasPermi('erp:outbound:add')")
    @Log(title = "出库单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ErpOutboundOrder outboundOrder)
    {
        outboundOrder.setCreateBy(getUsername());
        return toAjax(outboundOrderService.insertOutboundOrder(outboundOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:outbound:edit')")
    @Log(title = "出库单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ErpOutboundOrder outboundOrder)
    {
        outboundOrder.setUpdateBy(getUsername());
        return toAjax(outboundOrderService.updateOutboundOrder(outboundOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:outbound:remove')")
    @Log(title = "出库单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{outboundOrderIds}")
    public AjaxResult remove(@PathVariable Long[] outboundOrderIds)
    {
        return toAjax(outboundOrderService.deleteOutboundOrderByIds(outboundOrderIds));
    }

    @PreAuthorize("@ss.hasPermi('erp:outbound:post')")
    @Log(title = "出库单过账", businessType = BusinessType.UPDATE)
    @PutMapping("/post/{outboundOrderId}")
    public AjaxResult post(@PathVariable Long outboundOrderId)
    {
        return toAjax(outboundOrderService.postOutboundOrder(outboundOrderId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('erp:outbound:cancelPost')")
    @Log(title = "出库单取消过账", businessType = BusinessType.UPDATE)
    @PutMapping("/cancelPost/{outboundOrderId}")
    public AjaxResult cancelPost(@PathVariable Long outboundOrderId)
    {
        return toAjax(outboundOrderService.cancelPostOutboundOrder(outboundOrderId, getUsername()));
    }
}

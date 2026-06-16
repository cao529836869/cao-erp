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
import com.ruoyi.erp.domain.ErpInboundOrder;
import com.ruoyi.erp.service.IErpInboundOrderService;

@RestController
@RequestMapping("/erp/inbound")
public class ErpInboundOrderController extends BaseController
{
    @Autowired
    private IErpInboundOrderService inboundOrderService;

    @PreAuthorize("@ss.hasPermi('erp:inbound:list')")
    @GetMapping("/list")
    public TableDataInfo list(ErpInboundOrder inboundOrder)
    {
        startPage();
        return getDataTable(inboundOrderService.selectInboundOrderList(inboundOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:inbound:export')")
    @Log(title = "入库单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ErpInboundOrder inboundOrder)
    {
        List<ErpInboundOrder> list = inboundOrderService.selectInboundOrderList(inboundOrder);
        ExcelUtil<ErpInboundOrder> util = new ExcelUtil<ErpInboundOrder>(ErpInboundOrder.class);
        util.exportExcel(response, list, "入库单数据");
    }

    @PreAuthorize("@ss.hasPermi('erp:inbound:query')")
    @GetMapping("/{inboundOrderId}")
    public AjaxResult getInfo(@PathVariable Long inboundOrderId)
    {
        return success(inboundOrderService.selectInboundOrderById(inboundOrderId));
    }

    @PreAuthorize("@ss.hasPermi('erp:inbound:add')")
    @Log(title = "入库单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ErpInboundOrder inboundOrder)
    {
        inboundOrder.setCreateBy(getUsername());
        return toAjax(inboundOrderService.insertInboundOrder(inboundOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:inbound:edit')")
    @Log(title = "入库单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ErpInboundOrder inboundOrder)
    {
        inboundOrder.setUpdateBy(getUsername());
        return toAjax(inboundOrderService.updateInboundOrder(inboundOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:inbound:remove')")
    @Log(title = "入库单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{inboundOrderIds}")
    public AjaxResult remove(@PathVariable Long[] inboundOrderIds)
    {
        return toAjax(inboundOrderService.deleteInboundOrderByIds(inboundOrderIds));
    }

    @PreAuthorize("@ss.hasPermi('erp:inbound:post')")
    @Log(title = "入库单过账", businessType = BusinessType.UPDATE)
    @PutMapping("/post/{inboundOrderId}")
    public AjaxResult post(@PathVariable Long inboundOrderId)
    {
        return toAjax(inboundOrderService.postInboundOrder(inboundOrderId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('erp:inbound:cancelPost')")
    @Log(title = "入库单取消过账", businessType = BusinessType.UPDATE)
    @PutMapping("/cancelPost/{inboundOrderId}")
    public AjaxResult cancelPost(@PathVariable Long inboundOrderId)
    {
        return toAjax(inboundOrderService.cancelPostInboundOrder(inboundOrderId, getUsername()));
    }
}

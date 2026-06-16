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
import com.ruoyi.erp.domain.ErpDeliveryOrder;
import com.ruoyi.erp.service.IErpDeliveryOrderService;

@RestController
@RequestMapping("/erp/delivery")
public class ErpDeliveryOrderController extends BaseController
{
    @Autowired
    private IErpDeliveryOrderService deliveryOrderService;

    @PreAuthorize("@ss.hasPermi('erp:delivery:list')")
    @GetMapping("/list")
    public TableDataInfo list(ErpDeliveryOrder deliveryOrder)
    {
        startPage();
        return getDataTable(deliveryOrderService.selectDeliveryOrderList(deliveryOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:delivery:export')")
    @Log(title = "发货单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ErpDeliveryOrder deliveryOrder)
    {
        List<ErpDeliveryOrder> list = deliveryOrderService.selectDeliveryOrderList(deliveryOrder);
        ExcelUtil<ErpDeliveryOrder> util = new ExcelUtil<ErpDeliveryOrder>(ErpDeliveryOrder.class);
        util.exportExcel(response, list, "发货单数据");
    }

    @PreAuthorize("@ss.hasPermi('erp:delivery:query')")
    @GetMapping("/{deliveryOrderId}")
    public AjaxResult getInfo(@PathVariable Long deliveryOrderId)
    {
        return success(deliveryOrderService.selectDeliveryOrderById(deliveryOrderId));
    }

    @PreAuthorize("@ss.hasPermi('erp:delivery:add')")
    @Log(title = "发货单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ErpDeliveryOrder deliveryOrder)
    {
        deliveryOrder.setCreateBy(getUsername());
        return toAjax(deliveryOrderService.insertDeliveryOrder(deliveryOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:delivery:edit')")
    @Log(title = "发货单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ErpDeliveryOrder deliveryOrder)
    {
        deliveryOrder.setUpdateBy(getUsername());
        return toAjax(deliveryOrderService.updateDeliveryOrder(deliveryOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:delivery:remove')")
    @Log(title = "发货单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deliveryOrderIds}")
    public AjaxResult remove(@PathVariable Long[] deliveryOrderIds)
    {
        return toAjax(deliveryOrderService.deleteDeliveryOrderByIds(deliveryOrderIds));
    }
}

package com.ruoyi.web.controller.erp;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.erp.domain.ErpInventory;
import com.ruoyi.erp.service.IErpInventoryService;

@RestController
@RequestMapping("/erp/inventory")
public class ErpInventoryController extends BaseController
{
    @Autowired
    private IErpInventoryService inventoryService;

    @PreAuthorize("@ss.hasPermi('erp:inventory:list')")
    @GetMapping("/list")
    public TableDataInfo list(ErpInventory inventory)
    {
        startPage();
        return getDataTable(inventoryService.selectInventoryList(inventory));
    }

    @PreAuthorize("@ss.hasPermi('erp:inventory:export')")
    @Log(title = "库存汇总", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ErpInventory inventory)
    {
        List<ErpInventory> list = inventoryService.selectInventoryList(inventory);
        ExcelUtil<ErpInventory> util = new ExcelUtil<ErpInventory>(ErpInventory.class);
        util.exportExcel(response, list, "库存汇总数据");
    }
}

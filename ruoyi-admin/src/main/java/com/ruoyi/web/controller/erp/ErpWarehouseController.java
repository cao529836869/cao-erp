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
import com.ruoyi.erp.domain.ErpWarehouse;
import com.ruoyi.erp.service.IErpWarehouseService;

@RestController
@RequestMapping("/erp/warehouse")
public class ErpWarehouseController extends BaseController
{
    @Autowired
    private IErpWarehouseService warehouseService;

    @PreAuthorize("@ss.hasPermi('erp:warehouse:list')")
    @GetMapping("/list")
    public TableDataInfo list(ErpWarehouse warehouse)
    {
        startPage();
        return getDataTable(warehouseService.selectWarehouseList(warehouse));
    }

    @PreAuthorize("@ss.hasPermi('erp:warehouse:export')")
    @Log(title = "仓库档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ErpWarehouse warehouse)
    {
        List<ErpWarehouse> list = warehouseService.selectWarehouseList(warehouse);
        ExcelUtil<ErpWarehouse> util = new ExcelUtil<ErpWarehouse>(ErpWarehouse.class);
        util.exportExcel(response, list, "仓库档案数据");
    }

    @PreAuthorize("@ss.hasPermi('erp:warehouse:query')")
    @GetMapping("/{warehouseId}")
    public AjaxResult getInfo(@PathVariable Long warehouseId)
    {
        return success(warehouseService.selectWarehouseById(warehouseId));
    }

    @PreAuthorize("@ss.hasPermi('erp:warehouse:add')")
    @Log(title = "仓库档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ErpWarehouse warehouse)
    {
        if (!warehouseService.checkWarehouseCodeUnique(warehouse))
        {
            return error("新增仓库'" + warehouse.getWarehouseName() + "'失败，仓库编码已存在");
        }
        warehouse.setCreateBy(getUsername());
        return toAjax(warehouseService.insertWarehouse(warehouse));
    }

    @PreAuthorize("@ss.hasPermi('erp:warehouse:edit')")
    @Log(title = "仓库档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ErpWarehouse warehouse)
    {
        if (!warehouseService.checkWarehouseCodeUnique(warehouse))
        {
            return error("修改仓库'" + warehouse.getWarehouseName() + "'失败，仓库编码已存在");
        }
        warehouse.setUpdateBy(getUsername());
        return toAjax(warehouseService.updateWarehouse(warehouse));
    }

    @PreAuthorize("@ss.hasPermi('erp:warehouse:remove')")
    @Log(title = "仓库档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{warehouseIds}")
    public AjaxResult remove(@PathVariable Long[] warehouseIds)
    {
        return toAjax(warehouseService.deleteWarehouseByIds(warehouseIds));
    }

    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        return success(warehouseService.selectWarehouseAll());
    }
}

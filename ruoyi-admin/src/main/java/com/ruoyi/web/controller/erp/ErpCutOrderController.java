package com.ruoyi.web.controller.erp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
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
import com.ruoyi.erp.domain.ErpCutOrder;
import com.ruoyi.erp.service.IErpCutOrderService;

@RestController
@RequestMapping("/erp/cut")
public class ErpCutOrderController extends BaseController
{
    @Autowired
    private IErpCutOrderService cutOrderService;

    @PreAuthorize("@ss.hasPermi('erp:cut:list')")
    @GetMapping("/list")
    public TableDataInfo list(ErpCutOrder cutOrder)
    {
        startPage();
        return getDataTable(cutOrderService.selectCutOrderList(cutOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:cut:export')")
    @Log(title = "裁剪单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ErpCutOrder cutOrder)
    {
        List<ErpCutOrder> list = cutOrderService.selectCutOrderList(cutOrder);
        ExcelUtil<ErpCutOrder> util = new ExcelUtil<ErpCutOrder>(ErpCutOrder.class);
        Map<String, CellStyle> actualQtyStyles = new HashMap<>();
        util.setCellStyleHandler((excel, row, order, field, column, cell) -> {
            if (!"actualCutQty".equals(field.getName()))
            {
                return;
            }
            CellStyle style = getActualQtyStyle(cell.getSheet().getWorkbook(), cell.getCellStyle(),
                    actualQtyStyles, compareCutQty(order));
            if (style != null)
            {
                cell.setCellStyle(style);
            }
        });
        util.exportExcel(response, list, "裁剪单数据");
    }

    @PreAuthorize("@ss.hasPermi('erp:cut:query')")
    @GetMapping("/{cutOrderId}")
    public AjaxResult getInfo(@PathVariable Long cutOrderId)
    {
        return success(cutOrderService.selectCutOrderById(cutOrderId));
    }

    @PreAuthorize("@ss.hasPermi('erp:cut:query')")
    @GetMapping("/production/{productionOrderId}")
    public AjaxResult getByProduction(@PathVariable Long productionOrderId)
    {
        return success(cutOrderService.selectCutOrderByProductionOrderId(productionOrderId));
    }

    @PreAuthorize("@ss.hasPermi('erp:cut:add')")
    @Log(title = "裁剪单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ErpCutOrder cutOrder)
    {
        cutOrder.setCreateBy(getUsername());
        return toAjax(cutOrderService.insertCutOrder(cutOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:cut:edit')")
    @Log(title = "裁剪单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ErpCutOrder cutOrder)
    {
        cutOrder.setUpdateBy(getUsername());
        return toAjax(cutOrderService.updateCutOrder(cutOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:cut:remove')")
    @Log(title = "裁剪单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{cutOrderIds}")
    public AjaxResult remove(@PathVariable Long[] cutOrderIds)
    {
        return toAjax(cutOrderService.deleteCutOrderByIds(cutOrderIds));
    }

    @PreAuthorize("@ss.hasPermi('erp:cut:finish')")
    @Log(title = "裁剪单完成", businessType = BusinessType.UPDATE)
    @PutMapping("/finish/{cutOrderId}")
    public AjaxResult finish(@PathVariable Long cutOrderId, @RequestBody ErpCutOrder cutOrder)
    {
        return toAjax(cutOrderService.finishCutOrder(cutOrderId, cutOrder.getActualCutQty(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('erp:cut:cancel')")
    @Log(title = "裁剪单取消", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{cutOrderId}")
    public AjaxResult cancel(@PathVariable Long cutOrderId)
    {
        return toAjax(cutOrderService.cancelCutOrder(cutOrderId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('erp:cut:delay')")
    @Log(title = "裁剪单延期", businessType = BusinessType.UPDATE)
    @PutMapping("/delay/{cutOrderId}")
    public AjaxResult delay(@PathVariable Long cutOrderId, @RequestBody ErpCutOrder cutOrder)
    {
        return toAjax(cutOrderService.delayCutOrder(cutOrderId, cutOrder.getPlanFinishTime(), cutOrder.getRemark(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('erp:cut:add')")
    @Log(title = "生产单生成裁剪单", businessType = BusinessType.INSERT)
    @PutMapping("/production/{productionOrderId}")
    public AjaxResult createFromProduction(@PathVariable Long productionOrderId)
    {
        return toAjax(cutOrderService.createFromProduction(productionOrderId, getUsername()));
    }

    private int compareCutQty(ErpCutOrder order)
    {
        if (order == null || order.getActualCutQty() == null || order.getTotalQty() == null)
        {
            return 0;
        }
        return order.getActualCutQty().compareTo(order.getTotalQty());
    }

    private CellStyle getActualQtyStyle(Workbook workbook, CellStyle baseStyle, Map<String, CellStyle> cache, int compareResult)
    {
        String key;
        short fontColor;
        if (compareResult > 0)
        {
            key = "over";
            fontColor = IndexedColors.BLUE.getIndex();
        }
        else if (compareResult == 0)
        {
            key = "equal";
            fontColor = IndexedColors.GREEN.getIndex();
        }
        else
        {
            key = "less";
            fontColor = IndexedColors.ORANGE.getIndex();
        }
        return cache.computeIfAbsent(key, item -> {
            CellStyle style = workbook.createCellStyle();
            style.cloneStyleFrom(baseStyle);
            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            font.setBold(true);
            font.setColor(fontColor);
            style.setFont(font);
            return style;
        });
    }
}

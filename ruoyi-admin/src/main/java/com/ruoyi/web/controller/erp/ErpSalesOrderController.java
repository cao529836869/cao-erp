package com.ruoyi.web.controller.erp;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.erp.domain.ErpSalesOrder;
import com.ruoyi.erp.domain.ErpSalesOrderImport;
import com.ruoyi.erp.service.IErpSalesOrderService;

@RestController
@RequestMapping("/erp/sales")
public class ErpSalesOrderController extends BaseController
{
    @Autowired
    private IErpSalesOrderService salesOrderService;

    @PreAuthorize("@ss.hasPermi('erp:sales:list')")
    @GetMapping("/list")
    public TableDataInfo list(ErpSalesOrder salesOrder)
    {
        startPage();
        return getDataTable(salesOrderService.selectSalesOrderList(salesOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:sales:export')")
    @Log(title = "销售订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ErpSalesOrder salesOrder)
    {
        List<ErpSalesOrder> list = salesOrderService.selectSalesOrderList(salesOrder);
        ExcelUtil<ErpSalesOrder> util = new ExcelUtil<ErpSalesOrder>(ErpSalesOrder.class);
        util.exportExcel(response, list, "销售订单数据");
    }

    @PreAuthorize("@ss.hasPermi('erp:sales:import')")
    @Log(title = "销售订单导入", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) throws Exception
    {
        ExcelUtil<ErpSalesOrderImport> util = new ExcelUtil<ErpSalesOrderImport>(ErpSalesOrderImport.class);
        List<ErpSalesOrderImport> list = util.importExcel(file.getInputStream());
        return success(salesOrderService.importSalesOrders(list, getUsername()));
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) throws Exception
    {
        String filePath = resolveTemplatePath();
        FileUtils.setAttachmentResponseHeader(response, "sales_order_import_template.xlsx");
        FileUtils.writeBytes(filePath, response.getOutputStream());
    }

    @PreAuthorize("@ss.hasPermi('erp:sales:query')")
    @GetMapping("/{salesOrderId}")
    public AjaxResult getInfo(@PathVariable Long salesOrderId)
    {
        return success(salesOrderService.selectSalesOrderById(salesOrderId));
    }

    @PreAuthorize("@ss.hasPermi('erp:sales:add')")
    @Log(title = "销售订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ErpSalesOrder salesOrder)
    {
        salesOrder.setCreateBy(getUsername());
        return toAjax(salesOrderService.insertSalesOrder(salesOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:sales:edit')")
    @Log(title = "销售订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ErpSalesOrder salesOrder)
    {
        salesOrder.setUpdateBy(getUsername());
        return toAjax(salesOrderService.updateSalesOrder(salesOrder));
    }

    @PreAuthorize("@ss.hasPermi('erp:sales:remove')")
    @Log(title = "销售订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{salesOrderIds}")
    public AjaxResult remove(@PathVariable Long[] salesOrderIds)
    {
        return toAjax(salesOrderService.deleteSalesOrderByIds(salesOrderIds));
    }

    @PreAuthorize("@ss.hasPermi('erp:production:add')")
    @Log(title = "销售订单生成生产订单", businessType = BusinessType.INSERT)
    @PostMapping("/production/{salesOrderId}")
    public AjaxResult generateProduction(@PathVariable Long salesOrderId)
    {
        return toAjax(salesOrderService.generateProductionOrder(salesOrderId, getUsername()));
    }

    private String resolveTemplatePath()
    {
        Path rootPath = Paths.get(System.getProperty("user.dir"), "doc", "sales_order_import_template.xlsx");
        if (Files.exists(rootPath))
        {
            return rootPath.toString();
        }
        return Paths.get(System.getProperty("user.dir"), "..", "doc", "sales_order_import_template.xlsx").normalize().toString();
    }
}

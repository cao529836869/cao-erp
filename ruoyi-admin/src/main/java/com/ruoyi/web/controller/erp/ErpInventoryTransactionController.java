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
import com.ruoyi.erp.domain.ErpInventoryTransaction;
import com.ruoyi.erp.service.IErpInventoryTransactionService;

@RestController
@RequestMapping("/erp/transaction")
public class ErpInventoryTransactionController extends BaseController
{
    @Autowired
    private IErpInventoryTransactionService transactionService;

    @PreAuthorize("@ss.hasPermi('erp:transaction:list')")
    @GetMapping("/list")
    public TableDataInfo list(ErpInventoryTransaction transaction)
    {
        startPage();
        return getDataTable(transactionService.selectTransactionList(transaction));
    }

    @PreAuthorize("@ss.hasPermi('erp:transaction:export')")
    @Log(title = "库存流水", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ErpInventoryTransaction transaction)
    {
        List<ErpInventoryTransaction> list = transactionService.selectTransactionList(transaction);
        ExcelUtil<ErpInventoryTransaction> util = new ExcelUtil<ErpInventoryTransaction>(ErpInventoryTransaction.class);
        util.exportExcel(response, list, "库存流水数据");
    }
}

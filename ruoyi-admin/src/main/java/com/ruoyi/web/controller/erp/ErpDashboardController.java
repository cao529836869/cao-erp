package com.ruoyi.web.controller.erp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.erp.service.IErpDashboardService;

@RestController
@RequestMapping("/erp/dashboard")
public class ErpDashboardController extends BaseController
{
    @Autowired
    private IErpDashboardService dashboardService;

    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return success(dashboardService.selectOverview());
    }
}

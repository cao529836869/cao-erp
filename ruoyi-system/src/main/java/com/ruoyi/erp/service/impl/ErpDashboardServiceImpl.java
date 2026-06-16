package com.ruoyi.erp.service.impl;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.erp.mapper.ErpDashboardMapper;
import com.ruoyi.erp.service.IErpDashboardService;

@Service
public class ErpDashboardServiceImpl implements IErpDashboardService
{
    @Autowired
    private ErpDashboardMapper dashboardMapper;

    @Override
    public Map<String, Object> selectOverview()
    {
        Map<String, Object> data = new HashMap<>();
        data.put("summary", dashboardMapper.selectSummary());
        data.put("weeklyFlow", dashboardMapper.selectWeeklyFlow());
        data.put("productionStatus", dashboardMapper.selectProductionStatus());
        data.put("materialAlerts", dashboardMapper.selectMaterialAlerts());
        data.put("recentSalesOrders", dashboardMapper.selectRecentSalesOrders());
        data.put("schedule", dashboardMapper.selectSchedule());
        data.put("warehouseStock", dashboardMapper.selectWarehouseStock());
        return data;
    }
}

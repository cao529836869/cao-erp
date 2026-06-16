package com.ruoyi.erp.mapper;

import java.util.List;
import java.util.Map;

public interface ErpDashboardMapper
{
    public Map<String, Object> selectSummary();

    public List<Map<String, Object>> selectWeeklyFlow();

    public List<Map<String, Object>> selectProductionStatus();

    public List<Map<String, Object>> selectMaterialAlerts();

    public List<Map<String, Object>> selectRecentSalesOrders();

    public List<Map<String, Object>> selectSchedule();

    public List<Map<String, Object>> selectWarehouseStock();
}

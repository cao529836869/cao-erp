package com.ruoyi.erp.service;

import java.util.List;
import com.ruoyi.erp.domain.ErpSalesOrder;
import com.ruoyi.erp.domain.ErpSalesOrderImport;

public interface IErpSalesOrderService
{
    public ErpSalesOrder selectSalesOrderById(Long salesOrderId);
    public List<ErpSalesOrder> selectSalesOrderList(ErpSalesOrder salesOrder);
    public int insertSalesOrder(ErpSalesOrder salesOrder);
    public int updateSalesOrder(ErpSalesOrder salesOrder);
    public int deleteSalesOrderByIds(Long[] salesOrderIds);
    public int generateProductionOrder(Long salesOrderId, String username);
    public String importSalesOrders(List<ErpSalesOrderImport> importList, String username);
}

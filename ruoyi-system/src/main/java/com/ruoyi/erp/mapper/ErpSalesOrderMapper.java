package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpSalesOrder;

public interface ErpSalesOrderMapper
{
    public ErpSalesOrder selectSalesOrderById(Long salesOrderId);
    public List<ErpSalesOrder> selectSalesOrderList(ErpSalesOrder salesOrder);
    public int insertSalesOrder(ErpSalesOrder salesOrder);
    public int updateSalesOrder(ErpSalesOrder salesOrder);
    public int deleteSalesOrderByIds(Long[] salesOrderIds);
}

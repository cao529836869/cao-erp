package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpSalesOrderDetail;

public interface ErpSalesOrderDetailMapper
{
    public List<ErpSalesOrderDetail> selectDetailByOrderId(Long salesOrderId);
    public int batchInsertDetail(List<ErpSalesOrderDetail> detailList);
    public int deleteDetailByOrderId(Long salesOrderId);
    public int deleteDetailByOrderIds(Long[] salesOrderIds);
}

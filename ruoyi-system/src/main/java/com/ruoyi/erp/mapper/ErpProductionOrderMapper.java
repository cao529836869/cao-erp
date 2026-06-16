package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpProductionOrder;

public interface ErpProductionOrderMapper
{
    public ErpProductionOrder selectProductionOrderById(Long productionOrderId);
    public List<ErpProductionOrder> selectProductionOrderList(ErpProductionOrder productionOrder);
    public ErpProductionOrder checkProductionOrderNoUnique(String productionOrderNo);
    public int insertProductionOrder(ErpProductionOrder productionOrder);
    public int updateProductionOrder(ErpProductionOrder productionOrder);
    public int updateProductionOrderRelease(ErpProductionOrder productionOrder);
    public int updateProductionOrderFinish(ErpProductionOrder productionOrder);
    public int updateProductionOrderClose(ErpProductionOrder productionOrder);
    public int deleteProductionOrderByIds(Long[] productionOrderIds);
}

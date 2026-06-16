package com.ruoyi.erp.service;

import java.util.List;
import com.ruoyi.erp.domain.ErpProductionOrder;

public interface IErpProductionOrderService
{
    public ErpProductionOrder selectProductionOrderById(Long productionOrderId);
    public List<ErpProductionOrder> selectProductionOrderList(ErpProductionOrder productionOrder);
    public boolean checkProductionOrderNoUnique(ErpProductionOrder productionOrder);
    public int insertProductionOrder(ErpProductionOrder productionOrder);
    public int updateProductionOrder(ErpProductionOrder productionOrder);
    public int deleteProductionOrderByIds(Long[] productionOrderIds);
    public int releaseProductionOrder(Long productionOrderId, String username);
    public int closeProductionOrder(Long productionOrderId, String username);
    public int countBomThenBuildOutboundOrder(Long productionOrderId);
}

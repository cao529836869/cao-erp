package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpCutOrder;

public interface ErpCutOrderMapper
{
    public ErpCutOrder selectCutOrderById(Long cutOrderId);
    public List<ErpCutOrder> selectCutOrderList(ErpCutOrder cutOrder);
    public ErpCutOrder selectCutOrderByProductionOrderId(Long productionOrderId);
    public int insertCutOrder(ErpCutOrder cutOrder);
    public int updateCutOrder(ErpCutOrder cutOrder);
    public int updateCutStatus(ErpCutOrder cutOrder);
    public int delayCutOrder(ErpCutOrder cutOrder);
    public int deleteCutOrderByIds(Long[] cutOrderIds);
}

package com.ruoyi.erp.service;

import java.util.Date;
import java.util.List;
import java.math.BigDecimal;
import com.ruoyi.erp.domain.ErpCutOrder;

public interface IErpCutOrderService
{
    public ErpCutOrder selectCutOrderById(Long cutOrderId);
    public ErpCutOrder selectCutOrderByProductionOrderId(Long productionOrderId);
    public List<ErpCutOrder> selectCutOrderList(ErpCutOrder cutOrder);
    public int insertCutOrder(ErpCutOrder cutOrder);
    public int updateCutOrder(ErpCutOrder cutOrder);
    public int deleteCutOrderByIds(Long[] cutOrderIds);
    public int finishCutOrder(Long cutOrderId, BigDecimal actualCutQty, String username);
    public int cancelCutOrder(Long cutOrderId, String username);
    public int delayCutOrder(Long cutOrderId, Date planFinishTime, String remark, String username);
    public int createFromProduction(Long productionOrderId, String username);
}

package com.ruoyi.erp.service;

import java.util.List;
import com.ruoyi.erp.domain.ErpInboundOrder;

public interface IErpInboundOrderService
{
    public ErpInboundOrder selectInboundOrderById(Long inboundOrderId);
    public List<ErpInboundOrder> selectInboundOrderList(ErpInboundOrder inboundOrder);
    public boolean checkInboundOrderNoUnique(ErpInboundOrder inboundOrder);
    public int insertInboundOrder(ErpInboundOrder inboundOrder);
    public int updateInboundOrder(ErpInboundOrder inboundOrder);
    public int deleteInboundOrderByIds(Long[] inboundOrderIds);
    public int postInboundOrder(Long inboundOrderId, String username);
    public int cancelPostInboundOrder(Long inboundOrderId, String username);
}

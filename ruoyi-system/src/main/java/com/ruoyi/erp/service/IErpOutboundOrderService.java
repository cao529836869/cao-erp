package com.ruoyi.erp.service;

import java.util.List;
import com.ruoyi.erp.domain.ErpOutboundOrder;

public interface IErpOutboundOrderService
{
    public ErpOutboundOrder selectOutboundOrderById(Long outboundOrderId);
    public List<ErpOutboundOrder> selectOutboundOrderList(ErpOutboundOrder outboundOrder);
    public boolean checkOutboundOrderNoUnique(ErpOutboundOrder outboundOrder);
    public int insertOutboundOrder(ErpOutboundOrder outboundOrder);
    public int updateOutboundOrder(ErpOutboundOrder outboundOrder);
    public int deleteOutboundOrderByIds(Long[] outboundOrderIds);
    public int postOutboundOrder(Long outboundOrderId, String username);
    public int cancelPostOutboundOrder(Long outboundOrderId, String username);
}

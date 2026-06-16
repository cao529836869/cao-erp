package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpOutboundOrder;

public interface ErpOutboundOrderMapper
{
    public ErpOutboundOrder selectOutboundOrderById(Long outboundOrderId);
    public ErpOutboundOrder selectOutboundOrderByIdForUpdate(Long outboundOrderId);
    public List<ErpOutboundOrder> selectOutboundOrderList(ErpOutboundOrder outboundOrder);
    public ErpOutboundOrder checkOutboundOrderNoUnique(String outboundOrderNo);
    public int insertOutboundOrder(ErpOutboundOrder outboundOrder);
    public int updateOutboundOrder(ErpOutboundOrder outboundOrder);
    public int updateOutboundOrderPost(ErpOutboundOrder outboundOrder);
    public int updateOutboundOrderCancel(ErpOutboundOrder outboundOrder);
    public int updateOutboundOrderCompleteBySource(ErpOutboundOrder outboundOrder);
    public int deleteOutboundOrderByIds(Long[] outboundOrderIds);
}

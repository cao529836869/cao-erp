package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpInboundOrder;

public interface ErpInboundOrderMapper
{
    public ErpInboundOrder selectInboundOrderById(Long inboundOrderId);
    public ErpInboundOrder selectInboundOrderByIdForUpdate(Long inboundOrderId);
    public List<ErpInboundOrder> selectInboundOrderList(ErpInboundOrder inboundOrder);
    public ErpInboundOrder checkInboundOrderNoUnique(String inboundOrderNo);
    public int insertInboundOrder(ErpInboundOrder inboundOrder);
    public int updateInboundOrder(ErpInboundOrder inboundOrder);
    public int updateInboundOrderPost(ErpInboundOrder inboundOrder);
    public int updateInboundOrderCancel(ErpInboundOrder inboundOrder);
    public int deleteInboundOrderByIds(Long[] inboundOrderIds);
}

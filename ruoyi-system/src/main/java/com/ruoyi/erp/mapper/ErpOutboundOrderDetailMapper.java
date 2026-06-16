package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpOutboundOrderDetail;

public interface ErpOutboundOrderDetailMapper
{
    public List<ErpOutboundOrderDetail> selectDetailByOrderId(Long outboundOrderId);
    public int batchInsertDetail(List<ErpOutboundOrderDetail> list);
    public int deleteDetailByOrderId(Long outboundOrderId);
    public int deleteDetailByOrderIds(Long[] outboundOrderIds);
}

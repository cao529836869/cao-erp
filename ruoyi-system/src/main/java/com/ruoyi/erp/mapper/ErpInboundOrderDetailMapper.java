package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpInboundOrderDetail;

public interface ErpInboundOrderDetailMapper
{
    public List<ErpInboundOrderDetail> selectDetailByOrderId(Long inboundOrderId);
    public int batchInsertDetail(List<ErpInboundOrderDetail> list);
    public int deleteDetailByOrderId(Long inboundOrderId);
    public int deleteDetailByOrderIds(Long[] inboundOrderIds);
}

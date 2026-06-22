package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpDeliveryOrderDetail;

public interface ErpDeliveryOrderDetailMapper
{
    public List<ErpDeliveryOrderDetail> selectDetailByOrderId(Long deliveryOrderId);
    public int updateLockInfo(ErpDeliveryOrderDetail detail);
    public int batchInsertDetail(List<ErpDeliveryOrderDetail> detailList);
    public int deleteDetailByOrderId(Long deliveryOrderId);
    public int deleteDetailByOrderIds(Long[] deliveryOrderIds);
}

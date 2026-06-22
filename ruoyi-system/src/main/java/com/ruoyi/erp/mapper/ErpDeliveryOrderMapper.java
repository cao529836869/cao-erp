package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpDeliveryOrder;

public interface ErpDeliveryOrderMapper
{
    public ErpDeliveryOrder selectDeliveryOrderById(Long deliveryOrderId);
    public ErpDeliveryOrder selectDeliveryOrderByIdForUpdate(Long deliveryOrderId);
    public List<ErpDeliveryOrder> selectDeliveryOrderList(ErpDeliveryOrder deliveryOrder);
    public int insertDeliveryOrder(ErpDeliveryOrder deliveryOrder);
    public int updateDeliveryOrder(ErpDeliveryOrder deliveryOrder);
    public int updateDeliveryLogistics(ErpDeliveryOrder deliveryOrder);
    public int updateDeliveryStatus(ErpDeliveryOrder deliveryOrder);
    public int updateDeliveryStatusByOutboundOrderId(ErpDeliveryOrder deliveryOrder);
    public int updateDeliveryOutbound(ErpDeliveryOrder deliveryOrder);
    public int deleteDeliveryOrderByIds(Long[] deliveryOrderIds);
}

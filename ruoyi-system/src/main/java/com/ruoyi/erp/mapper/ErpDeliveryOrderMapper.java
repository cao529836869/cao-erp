package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpDeliveryOrder;

public interface ErpDeliveryOrderMapper
{
    public ErpDeliveryOrder selectDeliveryOrderById(Long deliveryOrderId);
    public List<ErpDeliveryOrder> selectDeliveryOrderList(ErpDeliveryOrder deliveryOrder);
    public int insertDeliveryOrder(ErpDeliveryOrder deliveryOrder);
    public int updateDeliveryOrder(ErpDeliveryOrder deliveryOrder);
    public int deleteDeliveryOrderByIds(Long[] deliveryOrderIds);
}

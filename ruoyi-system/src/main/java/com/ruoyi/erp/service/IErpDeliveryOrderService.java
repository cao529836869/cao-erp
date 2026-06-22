package com.ruoyi.erp.service;

import java.util.List;
import com.ruoyi.erp.domain.ErpDeliveryOrder;
import com.ruoyi.erp.domain.ErpLogisticsQueryResult;

public interface IErpDeliveryOrderService
{
    public ErpDeliveryOrder selectDeliveryOrderById(Long deliveryOrderId);
    public List<ErpDeliveryOrder> selectDeliveryOrderList(ErpDeliveryOrder deliveryOrder);
    public int insertDeliveryOrder(ErpDeliveryOrder deliveryOrder);
    public int updateDeliveryOrder(ErpDeliveryOrder deliveryOrder);
    public int deleteDeliveryOrderByIds(Long[] deliveryOrderIds);
    public int confirmDeliveryOrder(Long deliveryOrderId, String username);
    public int cancelDeliveryOrder(Long deliveryOrderId, String username);
    public int updateDeliveryLogistics(ErpDeliveryOrder deliveryOrder, String username);
    public ErpLogisticsQueryResult queryDeliveryLogistics(Long deliveryOrderId);
}

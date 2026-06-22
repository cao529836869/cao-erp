package com.ruoyi.erp.service;

import com.ruoyi.erp.domain.ErpDeliveryOrder;
import com.ruoyi.erp.domain.ErpLogisticsQueryResult;

public interface IErpLogisticsQueryService
{
    public ErpLogisticsQueryResult query(ErpDeliveryOrder deliveryOrder);
}

package com.ruoyi.erp.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.erp.domain.ErpDeliveryOrder;
import com.ruoyi.erp.mapper.ErpDeliveryOrderMapper;
import com.ruoyi.erp.service.IErpDeliveryOrderService;

@Service
public class ErpDeliveryOrderServiceImpl implements IErpDeliveryOrderService
{
    @Autowired private ErpDeliveryOrderMapper deliveryOrderMapper;

    @Override
    public ErpDeliveryOrder selectDeliveryOrderById(Long deliveryOrderId)
    {
        return deliveryOrderMapper.selectDeliveryOrderById(deliveryOrderId);
    }

    @Override
    public List<ErpDeliveryOrder> selectDeliveryOrderList(ErpDeliveryOrder deliveryOrder)
    {
        return deliveryOrderMapper.selectDeliveryOrderList(deliveryOrder);
    }

    @Override
    public int insertDeliveryOrder(ErpDeliveryOrder deliveryOrder)
    {
        prepareOrder(deliveryOrder);
        return deliveryOrderMapper.insertDeliveryOrder(deliveryOrder);
    }

    @Override
    public int updateDeliveryOrder(ErpDeliveryOrder deliveryOrder)
    {
        prepareOrder(deliveryOrder);
        return deliveryOrderMapper.updateDeliveryOrder(deliveryOrder);
    }

    @Override
    public int deleteDeliveryOrderByIds(Long[] deliveryOrderIds)
    {
        return deliveryOrderMapper.deleteDeliveryOrderByIds(deliveryOrderIds);
    }

    private void prepareOrder(ErpDeliveryOrder order)
    {
        if (StringUtils.isBlank(order.getDeliveryOrderNo()))
        {
            order.setDeliveryOrderNo("FH" + DateUtils.dateTimeNow());
        }
        if (order.getDeliveryDate() == null)
        {
            order.setDeliveryDate(DateUtils.getNowDate());
        }
        if (StringUtils.isBlank(order.getDeliveryStatus()))
        {
            order.setDeliveryStatus("草稿");
        }
        if (order.getTotalQty() == null)
        {
            order.setTotalQty(BigDecimal.ZERO);
        }
    }
}

package com.ruoyi.workflow.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.erp.domain.ErpCutOrder;
import com.ruoyi.erp.domain.ErpOutboundOrder;
import com.ruoyi.erp.domain.ErpProductionOrder;
import com.ruoyi.erp.mapper.ErpCutOrderMapper;
import com.ruoyi.erp.mapper.ErpOutboundOrderMapper;
import com.ruoyi.erp.service.IErpProductionOrderService;

@Service
public class ProductionWorkflowBusinessService
{
    private static final String OUTBOUND_TYPE_PRODUCTION_PICKING = "生产领料";

    @Autowired
    private IErpProductionOrderService productionOrderService;

    @Autowired
    private ErpOutboundOrderMapper outboundOrderMapper;

    @Autowired
    private ErpCutOrderMapper cutOrderMapper;

    public ErpProductionOrder getProductionOrder(Long productionOrderId)
    {
        ErpProductionOrder order = productionOrderService.selectProductionOrderById(productionOrderId);
        if (order == null)
        {
            throw new ServiceException("生产单不存在");
        }
        return order;
    }

    public ErpOutboundOrder getPickingOrder(Long productionOrderId)
    {
        ErpProductionOrder productionOrder = getProductionOrder(productionOrderId);
        return getPickingOrder(productionOrder.getProductionOrderNo());
    }

    public ErpOutboundOrder getPickingOrder(String productionOrderNo)
    {
        ErpOutboundOrder query = new ErpOutboundOrder();
        query.setSourceNo(productionOrderNo);
        query.setOutboundType(OUTBOUND_TYPE_PRODUCTION_PICKING);
        List<ErpOutboundOrder> orders = outboundOrderMapper.selectOutboundOrderList(query);
        if (orders != null)
        {
            for (ErpOutboundOrder order : orders)
            {
                if (StringUtils.equals(productionOrderNo, order.getSourceNo())
                        && StringUtils.equals(OUTBOUND_TYPE_PRODUCTION_PICKING, order.getOutboundType())
                        && !StringUtils.equals("已取消", order.getOrderStatus()))
                {
                    return order;
                }
            }
        }
        throw new ServiceException("生产单未生成可用的领料单");
    }

    public ErpCutOrder getCutOrder(Long productionOrderId)
    {
        ErpCutOrder cutOrder = cutOrderMapper.selectCutOrderByProductionOrderId(productionOrderId);
        if (cutOrder == null)
        {
            throw new ServiceException("生产单未生成裁剪单");
        }
        return cutOrder;
    }
}

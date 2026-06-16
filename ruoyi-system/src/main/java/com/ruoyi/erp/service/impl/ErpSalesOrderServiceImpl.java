package com.ruoyi.erp.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.erp.domain.ErpSalesOrder;
import com.ruoyi.erp.domain.ErpSalesOrderDetail;
import com.ruoyi.erp.mapper.ErpSalesOrderDetailMapper;
import com.ruoyi.erp.mapper.ErpSalesOrderMapper;
import com.ruoyi.erp.service.IErpSalesOrderService;

@Service
public class ErpSalesOrderServiceImpl implements IErpSalesOrderService
{
    @Autowired private ErpSalesOrderMapper salesOrderMapper;
    @Autowired private ErpSalesOrderDetailMapper detailMapper;

    @Override
    public ErpSalesOrder selectSalesOrderById(Long salesOrderId)
    {
        ErpSalesOrder order = salesOrderMapper.selectSalesOrderById(salesOrderId);
        if (order != null)
        {
            order.setDetailList(detailMapper.selectDetailByOrderId(salesOrderId));
        }
        return order;
    }

    @Override
    public List<ErpSalesOrder> selectSalesOrderList(ErpSalesOrder salesOrder)
    {
        return salesOrderMapper.selectSalesOrderList(salesOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertSalesOrder(ErpSalesOrder salesOrder)
    {
        prepareOrder(salesOrder);
        int rows = salesOrderMapper.insertSalesOrder(salesOrder);
        insertDetails(salesOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSalesOrder(ErpSalesOrder salesOrder)
    {
        prepareOrder(salesOrder);
        int rows = salesOrderMapper.updateSalesOrder(salesOrder);
        detailMapper.deleteDetailByOrderId(salesOrder.getSalesOrderId());
        insertDetails(salesOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSalesOrderByIds(Long[] salesOrderIds)
    {
        detailMapper.deleteDetailByOrderIds(salesOrderIds);
        return salesOrderMapper.deleteSalesOrderByIds(salesOrderIds);
    }

    private void prepareOrder(ErpSalesOrder order)
    {
        if (StringUtils.isBlank(order.getSalesOrderNo()))
        {
            order.setSalesOrderNo("SO" + DateUtils.dateTimeNow());
        }
        if (order.getOrderDate() == null)
        {
            order.setOrderDate(DateUtils.getNowDate());
        }
        if (StringUtils.isBlank(order.getOrderStatus()))
        {
            order.setOrderStatus("草稿");
        }
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getDetailList() != null)
        {
            for (ErpSalesOrderDetail detail : order.getDetailList())
            {
                BigDecimal qty = nvl(detail.getOrderQty());
                BigDecimal price = nvl(detail.getUnitPrice());
                detail.setAmount(qty.multiply(price));
                totalQty = totalQty.add(qty);
                totalAmount = totalAmount.add(detail.getAmount());
            }
        }
        order.setTotalQty(totalQty);
        order.setTotalAmount(totalAmount);
    }

    private void insertDetails(ErpSalesOrder order)
    {
        if (order.getDetailList() == null || order.getDetailList().isEmpty())
        {
            throw new ServiceException("销售订单明细不能为空");
        }
        List<ErpSalesOrderDetail> insertList = new ArrayList<ErpSalesOrderDetail>();
        for (ErpSalesOrderDetail detail : order.getDetailList())
        {
            if (detail == null || detail.getSkuId() == null)
            {
                continue;
            }
            detail.setSalesOrderId(order.getSalesOrderId());
            detail.setSalesOrderNo(order.getSalesOrderNo());
            detail.setCreateBy(order.getCreateBy());
            insertList.add(detail);
        }
        if (insertList.isEmpty())
        {
            throw new ServiceException("销售订单明细不能为空");
        }
        detailMapper.batchInsertDetail(insertList);
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}

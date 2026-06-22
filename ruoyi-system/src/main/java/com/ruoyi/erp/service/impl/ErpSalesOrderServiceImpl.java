package com.ruoyi.erp.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.erp.domain.ErpCustomer;
import com.ruoyi.erp.domain.ErpProductionOrder;
import com.ruoyi.erp.domain.ErpProductionOrderDetail;
import com.ruoyi.erp.domain.ErpSalesOrder;
import com.ruoyi.erp.domain.ErpSalesOrderDetail;
import com.ruoyi.erp.domain.ErpSalesOrderImport;
import com.ruoyi.erp.domain.ErpStyleSku;
import com.ruoyi.erp.mapper.ErpCustomerMapper;
import com.ruoyi.erp.mapper.ErpProductionOrderMapper;
import com.ruoyi.erp.mapper.ErpSalesOrderDetailMapper;
import com.ruoyi.erp.mapper.ErpSalesOrderMapper;
import com.ruoyi.erp.mapper.ErpStyleSkuMapper;
import com.ruoyi.erp.service.IErpProductionOrderService;
import com.ruoyi.erp.service.IErpSalesOrderService;

@Service
public class ErpSalesOrderServiceImpl implements IErpSalesOrderService
{
    @Autowired private ErpSalesOrderMapper salesOrderMapper;
    @Autowired private ErpSalesOrderDetailMapper detailMapper;
    @Autowired private ErpCustomerMapper customerMapper;
    @Autowired private ErpStyleSkuMapper styleSkuMapper;
    @Autowired private ErpProductionOrderMapper productionOrderMapper;
    @Autowired private IErpProductionOrderService productionOrderService;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateProductionOrder(Long salesOrderId, String username)
    {
        ErpSalesOrder salesOrder = selectSalesOrderById(salesOrderId);
        if (salesOrder == null)
        {
            throw new ServiceException("销售订单不存在");
        }
        if (salesOrder.getDetailList() == null || salesOrder.getDetailList().isEmpty())
        {
            throw new ServiceException("销售订单明细不能为空");
        }
        if (productionOrderMapper.countProductionOrderBySalesOrderId(salesOrderId) > 0)
        {
            throw new ServiceException("该销售订单已生成生产订单，不能重复生成");
        }

        ErpProductionOrder productionOrder = new ErpProductionOrder();
        productionOrder.setSalesOrderId(salesOrder.getSalesOrderId());
        productionOrder.setSalesOrderNo(salesOrder.getSalesOrderNo());
        productionOrder.setCustomerId(salesOrder.getCustomerId());
        productionOrder.setCustomerName(salesOrder.getCustomerName());
        productionOrder.setPlanStartDate(DateUtils.getNowDate());
        productionOrder.setPlanFinishDate(salesOrder.getDeliveryDate() == null ? DateUtils.getNowDate() : salesOrder.getDeliveryDate());
        productionOrder.setOrderStatus("草稿");
        productionOrder.setCreateBy(username);
        productionOrder.setRemark("由销售订单 " + salesOrder.getSalesOrderNo() + " 生成");

        List<ErpProductionOrderDetail> productionDetails = new ArrayList<ErpProductionOrderDetail>();
        for (ErpSalesOrderDetail salesDetail : salesOrder.getDetailList())
        {
            ErpProductionOrderDetail detail = new ErpProductionOrderDetail();
            detail.setStyleId(salesDetail.getStyleId());
            detail.setSkuId(salesDetail.getSkuId());
            detail.setStyleNo(salesDetail.getStyleNo());
            detail.setStyleName(salesDetail.getStyleName());
            detail.setColorName(salesDetail.getColorName());
            detail.setSizeName(salesDetail.getSizeName());
            detail.setPlanQty(nvl(salesDetail.getOrderQty()));
            detail.setCutQty(BigDecimal.ZERO);
            detail.setSewnQty(BigDecimal.ZERO);
            detail.setFinishedQty(BigDecimal.ZERO);
            detail.setQualifiedQty(BigDecimal.ZERO);
            productionDetails.add(detail);
        }
        productionOrder.setDetailList(productionDetails);
        productionOrderService.insertProductionOrder(productionOrder);

        ErpSalesOrder statusOrder = new ErpSalesOrder();
        statusOrder.setSalesOrderId(salesOrderId);
        statusOrder.setOrderStatus("生产中");
        statusOrder.setUpdateBy(username);
        salesOrderMapper.updateSalesOrderStatus(statusOrder);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importSalesOrders(List<ErpSalesOrderImport> importList, String username)
    {
        if (importList == null || importList.isEmpty())
        {
            throw new ServiceException("导入销售订单数据不能为空");
        }

        Map<String, ErpSalesOrder> orderMap = new LinkedHashMap<String, ErpSalesOrder>();
        int rowNum = 1;
        for (ErpSalesOrderImport row : importList)
        {
            rowNum++;
            validateImportRow(row, rowNum);
            String salesOrderNo = row.getSalesOrderNo().trim();
            if (salesOrderMapper.selectSalesOrderByNo(salesOrderNo) != null)
            {
                throw new ServiceException("第" + rowNum + "行销售单号已存在：" + salesOrderNo);
            }
            ErpStyleSku sku = styleSkuMapper.selectErpStyleSkuBySkuCode(row.getSkuCode().trim());
            if (sku == null)
            {
                throw new ServiceException("第" + rowNum + "行SKU编码不存在或已停用：" + row.getSkuCode());
            }

            ErpSalesOrder order = orderMap.get(salesOrderNo);
            if (order == null)
            {
                order = buildImportOrder(row, username);
                orderMap.put(salesOrderNo, order);
            }
            else if (!StringUtils.equals(order.getCustomerName(), row.getCustomerName().trim()))
            {
                throw new ServiceException("第" + rowNum + "行同一销售单号的客户不一致：" + salesOrderNo);
            }
            order.getDetailList().add(buildImportDetail(row, sku));
        }

        int count = 0;
        for (ErpSalesOrder order : orderMap.values())
        {
            insertSalesOrder(order);
            count++;
        }
        return "导入成功，共导入 " + count + " 张销售订单，" + importList.size() + " 条明细";
    }

    private void prepareOrder(ErpSalesOrder order)
    {
        resolveCustomer(order);
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

    private void resolveCustomer(ErpSalesOrder order)
    {
        if (order.getCustomerId() != null)
        {
            ErpCustomer customer = customerMapper.selectCustomerById(order.getCustomerId());
            if (customer == null || !"0".equals(customer.getStatus()))
            {
                throw new ServiceException("客户档案不存在或已停用，请重新选择客户");
            }
            order.setCustomerName(customer.getCustomerName());
            return;
        }
        if (StringUtils.isBlank(order.getCustomerName()))
        {
            throw new ServiceException("客户不能为空");
        }
        ErpCustomer customer = customerMapper.selectCustomerByName(order.getCustomerName().trim());
        if (customer == null)
        {
            throw new ServiceException("客户档案不存在或已停用：" + order.getCustomerName() + "，请确认客户是否已维护并启用，名称大小写、全半角和前后空格是否一致");
        }
        order.setCustomerId(customer.getCustomerId());
        order.setCustomerName(customer.getCustomerName());
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

    private void validateImportRow(ErpSalesOrderImport row, int rowNum)
    {
        if (row == null)
        {
            throw new ServiceException("第" + rowNum + "行为空");
        }
        if (StringUtils.isBlank(row.getSalesOrderNo()))
        {
            throw new ServiceException("第" + rowNum + "行销售单号不能为空");
        }
        if (StringUtils.isBlank(row.getCustomerName()))
        {
            throw new ServiceException("第" + rowNum + "行客户不能为空");
        }
        if (StringUtils.isBlank(row.getSkuCode()))
        {
            throw new ServiceException("第" + rowNum + "行SKU编码不能为空");
        }
        if (nvl(row.getOrderQty()).compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("第" + rowNum + "行数量必须大于0");
        }
    }

    private ErpSalesOrder buildImportOrder(ErpSalesOrderImport row, String username)
    {
        ErpSalesOrder order = new ErpSalesOrder();
        order.setSalesOrderNo(row.getSalesOrderNo().trim());
        order.setCustomerName(row.getCustomerName().trim());
        order.setOrderDate(row.getOrderDate() == null ? DateUtils.getNowDate() : row.getOrderDate());
        order.setDeliveryDate(row.getDeliveryDate());
        order.setOrderStatus(StringUtils.defaultIfBlank(row.getOrderStatus(), "草稿"));
        order.setRemark(row.getRemark());
        order.setCreateBy(username);
        order.setDetailList(new ArrayList<ErpSalesOrderDetail>());
        return order;
    }

    private ErpSalesOrderDetail buildImportDetail(ErpSalesOrderImport row, ErpStyleSku sku)
    {
        ErpSalesOrderDetail detail = new ErpSalesOrderDetail();
        detail.setStyleId(sku.getStyleId());
        detail.setSkuId(sku.getSkuId());
        detail.setSkuCode(sku.getSkuCode());
        detail.setStyleNo(sku.getStyleNo());
        detail.setStyleName(sku.getStyleName());
        detail.setColorName(sku.getColorName());
        detail.setSizeName(sku.getSizeName());
        detail.setOrderQty(nvl(row.getOrderQty()));
        detail.setUnitPrice(nvl(row.getUnitPrice()));
        detail.setRemark(row.getRemark());
        return detail;
    }
}

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
import com.ruoyi.erp.domain.ErpDeliveryOrder;
import com.ruoyi.erp.domain.ErpDeliveryOrderDetail;
import com.ruoyi.erp.domain.ErpInventory;
import com.ruoyi.erp.domain.ErpLogisticsQueryResult;
import com.ruoyi.erp.domain.ErpOutboundOrder;
import com.ruoyi.erp.domain.ErpOutboundOrderDetail;
import com.ruoyi.erp.domain.ErpSalesOrder;
import com.ruoyi.erp.domain.ErpSalesOrderDetail;
import com.ruoyi.erp.domain.ErpWarehouse;
import com.ruoyi.erp.mapper.ErpDeliveryOrderDetailMapper;
import com.ruoyi.erp.mapper.ErpDeliveryOrderMapper;
import com.ruoyi.erp.mapper.ErpInventoryMapper;
import com.ruoyi.erp.mapper.ErpOutboundOrderDetailMapper;
import com.ruoyi.erp.mapper.ErpOutboundOrderMapper;
import com.ruoyi.erp.mapper.ErpSalesOrderDetailMapper;
import com.ruoyi.erp.mapper.ErpSalesOrderMapper;
import com.ruoyi.erp.mapper.ErpWarehouseMapper;
import com.ruoyi.erp.service.IErpDeliveryOrderService;
import com.ruoyi.erp.service.IErpLogisticsQueryService;

@Service
public class ErpDeliveryOrderServiceImpl implements IErpDeliveryOrderService
{
    private static final String STATUS_DRAFT = "草稿";
    private static final String STATUS_CONFIRMED = "已确认";
    private static final String STATUS_SHIPPED = "已发货";
    private static final String STATUS_CANCELED = "已取消";

    @Autowired private ErpDeliveryOrderMapper deliveryOrderMapper;
    @Autowired private ErpDeliveryOrderDetailMapper detailMapper;
    @Autowired private ErpSalesOrderMapper salesOrderMapper;
    @Autowired private ErpSalesOrderDetailMapper salesDetailMapper;
    @Autowired private ErpWarehouseMapper warehouseMapper;
    @Autowired private ErpInventoryMapper inventoryMapper;
    @Autowired private ErpOutboundOrderMapper outboundOrderMapper;
    @Autowired private ErpOutboundOrderDetailMapper outboundDetailMapper;
    @Autowired private IErpLogisticsQueryService logisticsQueryService;

    @Override
    public ErpDeliveryOrder selectDeliveryOrderById(Long deliveryOrderId)
    {
        ErpDeliveryOrder order = deliveryOrderMapper.selectDeliveryOrderById(deliveryOrderId);
        if (order != null)
        {
            order.setDetailList(detailMapper.selectDetailByOrderId(deliveryOrderId));
        }
        return order;
    }

    @Override
    public List<ErpDeliveryOrder> selectDeliveryOrderList(ErpDeliveryOrder deliveryOrder)
    {
        return deliveryOrderMapper.selectDeliveryOrderList(deliveryOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDeliveryOrder(ErpDeliveryOrder deliveryOrder)
    {
        prepareDraftOrder(deliveryOrder);
        int rows = deliveryOrderMapper.insertDeliveryOrder(deliveryOrder);
        insertDetails(deliveryOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDeliveryOrder(ErpDeliveryOrder deliveryOrder)
    {
        prepareDraftOrder(deliveryOrder);
        int rows = deliveryOrderMapper.updateDeliveryOrder(deliveryOrder);
        if (rows == 0)
        {
            throw new ServiceException("只有草稿发货单允许修改");
        }
        detailMapper.deleteDetailByOrderId(deliveryOrder.getDeliveryOrderId());
        insertDetails(deliveryOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDeliveryOrderByIds(Long[] deliveryOrderIds)
    {
        for (Long deliveryOrderId : deliveryOrderIds)
        {
            ErpDeliveryOrder order = deliveryOrderMapper.selectDeliveryOrderById(deliveryOrderId);
            if (order == null || !STATUS_DRAFT.equals(order.getDeliveryStatus()))
            {
                throw new ServiceException("只有草稿发货单允许删除");
            }
        }
        detailMapper.deleteDetailByOrderIds(deliveryOrderIds);
        return deliveryOrderMapper.deleteDeliveryOrderByIds(deliveryOrderIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmDeliveryOrder(Long deliveryOrderId, String username)
    {
        ErpDeliveryOrder order = selectDeliveryOrderByIdForUpdate(deliveryOrderId);
        if (order == null)
        {
            throw new ServiceException("发货单不存在");
        }
        if (!STATUS_DRAFT.equals(order.getDeliveryStatus()))
        {
            throw new ServiceException("只有草稿发货单允许确认发货");
        }
        if (order.getDetailList() == null || order.getDetailList().isEmpty())
        {
            throw new ServiceException("发货单明细不能为空");
        }
        normalizeSalesOrder(order);
        normalizeWarehouse(order);

        for (ErpDeliveryOrderDetail detail : order.getDetailList())
        {
            lockInventoryForDetail(order, detail);
            detail.setUpdateBy(username);
            detailMapper.updateLockInfo(detail);
        }

        ErpOutboundOrder outboundOrder = buildOutboundOrder(order, username);
        outboundOrderMapper.insertOutboundOrder(outboundOrder);
        List<ErpOutboundOrderDetail> outboundDetails = buildOutboundDetails(order, outboundOrder, username);
        outboundDetailMapper.batchInsertDetail(outboundDetails);

        ErpDeliveryOrder update = new ErpDeliveryOrder();
        update.setDeliveryOrderId(order.getDeliveryOrderId());
        update.setDeliveryStatus(STATUS_CONFIRMED);
        update.setOutboundOrderId(outboundOrder.getOutboundOrderId());
        update.setOutboundOrderNo(outboundOrder.getOutboundOrderNo());
        update.setUpdateBy(username);
        deliveryOrderMapper.updateDeliveryOutbound(update);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelDeliveryOrder(Long deliveryOrderId, String username)
    {
        ErpDeliveryOrder order = selectDeliveryOrderByIdForUpdate(deliveryOrderId);
        if (order == null)
        {
            throw new ServiceException("发货单不存在");
        }
        if (STATUS_SHIPPED.equals(order.getDeliveryStatus()))
        {
            throw new ServiceException("发货单已出库，不能取消");
        }
        if (STATUS_CANCELED.equals(order.getDeliveryStatus()))
        {
            throw new ServiceException("发货单已取消");
        }
        if (STATUS_CONFIRMED.equals(order.getDeliveryStatus()))
        {
            releaseDeliveryLocks(order);
            deleteDraftOutbound(order);
        }
        ErpDeliveryOrder update = new ErpDeliveryOrder();
        update.setDeliveryOrderId(order.getDeliveryOrderId());
        update.setDeliveryStatus(STATUS_CANCELED);
        update.setUpdateBy(username);
        deliveryOrderMapper.updateDeliveryStatus(update);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDeliveryLogistics(ErpDeliveryOrder deliveryOrder, String username)
    {
        if (deliveryOrder == null || deliveryOrder.getDeliveryOrderId() == null)
        {
            throw new ServiceException("发货单ID不能为空");
        }
        ErpDeliveryOrder oldOrder = deliveryOrderMapper.selectDeliveryOrderById(deliveryOrder.getDeliveryOrderId());
        if (oldOrder == null)
        {
            throw new ServiceException("发货单不存在");
        }
        if (!STATUS_SHIPPED.equals(oldOrder.getDeliveryStatus()))
        {
            throw new ServiceException("只有已发货的发货单允许填写物流单号");
        }
        if (StringUtils.isBlank(deliveryOrder.getTrackingNo()))
        {
            throw new ServiceException("物流单号不能为空");
        }
        deliveryOrder.setUpdateBy(username);
        int rows = deliveryOrderMapper.updateDeliveryLogistics(deliveryOrder);
        if (rows == 0)
        {
            throw new ServiceException("发货单状态已变化，请刷新后重试");
        }
        return rows;
    }

    @Override
    public ErpLogisticsQueryResult queryDeliveryLogistics(Long deliveryOrderId)
    {
        if (deliveryOrderId == null)
        {
            throw new ServiceException("发货单ID不能为空");
        }
        ErpDeliveryOrder order = deliveryOrderMapper.selectDeliveryOrderById(deliveryOrderId);
        if (order == null)
        {
            throw new ServiceException("发货单不存在");
        }
        if (StringUtils.isBlank(order.getTrackingNo()))
        {
            throw new ServiceException("物流单号不能为空");
        }
        return logisticsQueryService.query(order);
    }

    private void prepareDraftOrder(ErpDeliveryOrder order)
    {
        if (StringUtils.isBlank(order.getDeliveryOrderNo()))
        {
            order.setDeliveryOrderNo("FH" + DateUtils.dateTimeNow());
        }
        if (order.getDeliveryDate() == null)
        {
            order.setDeliveryDate(DateUtils.getNowDate());
        }
        order.setDeliveryStatus(STATUS_DRAFT);
        normalizeSalesOrder(order);
        normalizeWarehouse(order);
        BigDecimal totalQty = BigDecimal.ZERO;
        if (order.getDetailList() != null)
        {
            for (ErpDeliveryOrderDetail detail : order.getDetailList())
            {
                BigDecimal qty = nvl(detail.getDeliveryQty());
                if (qty.compareTo(BigDecimal.ZERO) <= 0)
                {
                    throw new ServiceException("发货数量必须大于0");
                }
                totalQty = totalQty.add(qty);
            }
        }
        order.setTotalQty(totalQty);
    }

    private void normalizeSalesOrder(ErpDeliveryOrder order)
    {
        if (order.getSalesOrderId() == null)
        {
            return;
        }
        ErpSalesOrder salesOrder = salesOrderMapper.selectSalesOrderById(order.getSalesOrderId());
        if (salesOrder == null)
        {
            throw new ServiceException("销售订单不存在");
        }
        order.setSalesOrderNo(salesOrder.getSalesOrderNo());
        order.setCustomerId(salesOrder.getCustomerId());
        order.setCustomerName(salesOrder.getCustomerName());
    }

    private void normalizeWarehouse(ErpDeliveryOrder order)
    {
        if (order.getWarehouseId() == null)
        {
            throw new ServiceException("发货仓库不能为空");
        }
        ErpWarehouse warehouse = warehouseMapper.selectWarehouseById(order.getWarehouseId());
        if (warehouse == null)
        {
            throw new ServiceException("发货仓库不存在");
        }
        order.setWarehouseName(warehouse.getWarehouseName());
    }

    private void insertDetails(ErpDeliveryOrder order)
    {
        List<ErpDeliveryOrderDetail> details = order.getDetailList();
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("发货单明细不能为空");
        }
        List<ErpDeliveryOrderDetail> insertList = new ArrayList<ErpDeliveryOrderDetail>();
        for (ErpDeliveryOrderDetail detail : details)
        {
            if (detail == null || detail.getInventoryId() == null)
            {
                continue;
            }
            detail.setDeliveryOrderId(order.getDeliveryOrderId());
            detail.setDeliveryOrderNo(order.getDeliveryOrderNo());
            detail.setCreateBy(order.getCreateBy());
            detail.setLockedQty(BigDecimal.ZERO);
            insertList.add(detail);
        }
        if (insertList.isEmpty())
        {
            throw new ServiceException("发货单明细不能为空");
        }
        detailMapper.batchInsertDetail(insertList);
    }

    private void lockInventoryForDetail(ErpDeliveryOrder order, ErpDeliveryOrderDetail detail)
    {
        BigDecimal qty = nvl(detail.getDeliveryQty());
        if (qty.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("发货数量必须大于0");
        }
        ErpInventory inventory = inventoryMapper.selectInventoryByIdForUpdate(detail.getInventoryId());
        if (inventory == null)
        {
            throw new ServiceException("库存不存在或已变化");
        }
        if (!order.getWarehouseId().equals(inventory.getWarehouseId()))
        {
            throw new ServiceException("库存不属于当前发货仓库：" + inventory.getItemCode());
        }
        if (!"成衣".equals(inventory.getItemType()))
        {
            throw new ServiceException("发货单只能选择成衣库存：" + inventory.getItemCode());
        }
        validateSalesDetail(order, detail, inventory);
        BigDecimal availableQty = nvl(inventory.getAvailableQty()).subtract(nvl(inventory.getLockedQty()));
        if (availableQty.compareTo(qty) < 0)
        {
            throw new ServiceException("库存不足：" + inventory.getItemCode() + " 批次 " + StringUtils.defaultString(inventory.getBatchNo()) + "，可用 " + availableQty);
        }
        int rows = inventoryMapper.increaseLockedQty(inventory.getInventoryId(), qty);
        if (rows == 0)
        {
            throw new ServiceException("库存已被其他单据锁定，请刷新后重试：" + inventory.getItemCode());
        }
        detail.setSkuId(inventory.getItemId());
        detail.setSkuCode(inventory.getItemCode());
        detail.setStyleNo(inventory.getItemCode());
        detail.setStyleName(inventory.getItemName());
        detail.setColorName(inventory.getColorName());
        detail.setSizeName(inventory.getSizeName());
        detail.setBatchNo(StringUtils.defaultString(inventory.getBatchNo()));
        detail.setStockQty(nvl(inventory.getAvailableQty()));
        detail.setLockedQty(qty);
        detail.setAvailableQty(availableQty.subtract(qty));
        detail.setUnitName(inventory.getUnitName());
        detail.setUnitPrice(nvl(inventory.getUnitPrice()));
    }

    private void validateSalesDetail(ErpDeliveryOrder order, ErpDeliveryOrderDetail detail, ErpInventory inventory)
    {
        if (detail.getSalesDetailId() == null)
        {
            return;
        }
        if (order.getSalesOrderId() == null)
        {
            throw new ServiceException("选择销售订单明细时，发货单必须关联销售订单");
        }
        ErpSalesOrderDetail salesDetail = salesDetailMapper.selectDetailById(detail.getSalesDetailId());
        if (salesDetail == null || !order.getSalesOrderId().equals(salesDetail.getSalesOrderId()))
        {
            throw new ServiceException("销售订单明细不属于当前销售订单");
        }
        if (!inventory.getItemId().equals(salesDetail.getSkuId()))
        {
            throw new ServiceException("发货库存SKU与销售订单明细SKU不一致：" + inventory.getItemCode());
        }
    }

    private ErpOutboundOrder buildOutboundOrder(ErpDeliveryOrder order, String username)
    {
        ErpOutboundOrder outboundOrder = new ErpOutboundOrder();
        outboundOrder.setOutboundOrderNo("CK" + DateUtils.dateTimeNow());
        outboundOrder.setOutboundType("销售出库");
        outboundOrder.setSourceType("发货单");
        outboundOrder.setSourceNo(order.getDeliveryOrderNo());
        outboundOrder.setCustomerId(order.getCustomerId());
        outboundOrder.setCustomerName(order.getCustomerName());
        outboundOrder.setWarehouseId(order.getWarehouseId());
        outboundOrder.setWarehouseName(order.getWarehouseName());
        outboundOrder.setOutboundDate(order.getDeliveryDate());
        outboundOrder.setTotalQty(order.getTotalQty());
        outboundOrder.setTotalAmount(BigDecimal.ZERO);
        outboundOrder.setOrderStatus("草稿");
        outboundOrder.setCreateBy(username);
        outboundOrder.setRemark("由发货单" + order.getDeliveryOrderNo() + "生成");
        return outboundOrder;
    }

    private List<ErpOutboundOrderDetail> buildOutboundDetails(ErpDeliveryOrder deliveryOrder, ErpOutboundOrder outboundOrder, String username)
    {
        List<ErpOutboundOrderDetail> list = new ArrayList<ErpOutboundOrderDetail>();
        for (ErpDeliveryOrderDetail deliveryDetail : deliveryOrder.getDetailList())
        {
            BigDecimal qty = nvl(deliveryDetail.getDeliveryQty());
            BigDecimal price = nvl(deliveryDetail.getUnitPrice());
            ErpOutboundOrderDetail detail = new ErpOutboundOrderDetail();
            detail.setOutboundOrderId(outboundOrder.getOutboundOrderId());
            detail.setOutboundOrderNo(outboundOrder.getOutboundOrderNo());
            detail.setSourceDetailId(deliveryDetail.getDeliveryDetailId());
            detail.setItemType("成衣");
            detail.setItemId(deliveryDetail.getSkuId());
            detail.setItemCode(deliveryDetail.getSkuCode());
            detail.setItemName(deliveryDetail.getStyleName());
            detail.setColorName(deliveryDetail.getColorName());
            detail.setSizeName(deliveryDetail.getSizeName());
            detail.setSpecName(deliveryDetail.getSizeName());
            detail.setBatchNo(StringUtils.defaultString(deliveryDetail.getBatchNo()));
            detail.setPlanQty(qty);
            detail.setLockedQty(qty);
            detail.setOutboundQty(qty);
            detail.setUnitName(deliveryDetail.getUnitName());
            detail.setUnitPrice(price);
            detail.setAmount(qty.multiply(price));
            detail.setCreateBy(username);
            detail.setRemark("由发货单" + deliveryOrder.getDeliveryOrderNo() + "锁定库存生成");
            list.add(detail);
        }
        return list;
    }

    private void releaseDeliveryLocks(ErpDeliveryOrder order)
    {
        if (order.getDetailList() == null)
        {
            return;
        }
        for (ErpDeliveryOrderDetail detail : order.getDetailList())
        {
            BigDecimal lockedQty = nvl(detail.getLockedQty());
            if (lockedQty.compareTo(BigDecimal.ZERO) <= 0)
            {
                lockedQty = nvl(detail.getDeliveryQty());
            }
            if (lockedQty.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            ErpInventory inventory = inventoryMapper.selectInventoryByIdForUpdate(detail.getInventoryId());
            if (inventory == null)
            {
                throw new ServiceException("库存不存在，无法释放锁定：" + detail.getSkuCode());
            }
            int rows = inventoryMapper.decreaseLockedQty(inventory.getInventoryId(), lockedQty);
            if (rows == 0)
            {
                throw new ServiceException("库存锁定数量不足，无法释放：" + detail.getSkuCode());
            }
        }
    }

    private void deleteDraftOutbound(ErpDeliveryOrder order)
    {
        if (order.getOutboundOrderId() == null)
        {
            return;
        }
        ErpOutboundOrder outboundOrder = outboundOrderMapper.selectOutboundOrderByIdForUpdate(order.getOutboundOrderId());
        if (outboundOrder == null)
        {
            return;
        }
        if (!"草稿".equals(outboundOrder.getOrderStatus()))
        {
            throw new ServiceException("关联出库单不是草稿状态，不能取消发货单");
        }
        outboundDetailMapper.deleteDetailByOrderId(outboundOrder.getOutboundOrderId());
        outboundOrderMapper.deleteOutboundOrderByIds(new Long[] { outboundOrder.getOutboundOrderId() });
    }

    private ErpDeliveryOrder selectDeliveryOrderByIdForUpdate(Long deliveryOrderId)
    {
        ErpDeliveryOrder order = deliveryOrderMapper.selectDeliveryOrderByIdForUpdate(deliveryOrderId);
        if (order != null)
        {
            order.setDetailList(detailMapper.selectDetailByOrderId(deliveryOrderId));
        }
        return order;
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}

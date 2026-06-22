package com.ruoyi.erp.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.erp.domain.ErpInventory;
import com.ruoyi.erp.domain.ErpInventoryTransaction;
import com.ruoyi.erp.domain.ErpDeliveryOrder;
import com.ruoyi.erp.domain.ErpOutboundOrder;
import com.ruoyi.erp.domain.ErpOutboundOrderDetail;
import com.ruoyi.erp.domain.ErpWarehouse;
import com.ruoyi.erp.mapper.ErpInventoryMapper;
import com.ruoyi.erp.mapper.ErpInventoryTransactionMapper;
import com.ruoyi.erp.mapper.ErpDeliveryOrderMapper;
import com.ruoyi.erp.mapper.ErpOutboundOrderDetailMapper;
import com.ruoyi.erp.mapper.ErpOutboundOrderMapper;
import com.ruoyi.erp.mapper.ErpWarehouseMapper;
import com.ruoyi.erp.service.IErpOutboundOrderService;

@Service
public class ErpOutboundOrderServiceImpl implements IErpOutboundOrderService
{
    @Autowired private ErpOutboundOrderMapper outboundOrderMapper;
    @Autowired private ErpOutboundOrderDetailMapper detailMapper;
    @Autowired private ErpInventoryMapper inventoryMapper;
    @Autowired private ErpInventoryTransactionMapper transactionMapper;
    @Autowired private ErpDeliveryOrderMapper deliveryOrderMapper;
    @Autowired private ErpWarehouseMapper warehouseMapper;

    @Override
    public ErpOutboundOrder selectOutboundOrderById(Long outboundOrderId)
    {
        ErpOutboundOrder order = outboundOrderMapper.selectOutboundOrderById(outboundOrderId);
        if (order != null)
        {
            order.setDetailList(detailMapper.selectDetailByOrderId(outboundOrderId));
        }
        return order;
    }

    @Override
    public List<ErpOutboundOrder> selectOutboundOrderList(ErpOutboundOrder outboundOrder)
    {
        return outboundOrderMapper.selectOutboundOrderList(outboundOrder);
    }

    @Override
    public boolean checkOutboundOrderNoUnique(ErpOutboundOrder outboundOrder)
    {
        Long orderId = StringUtils.isNull(outboundOrder.getOutboundOrderId()) ? -1L : outboundOrder.getOutboundOrderId();
        ErpOutboundOrder info = outboundOrderMapper.checkOutboundOrderNoUnique(outboundOrder.getOutboundOrderNo());
        if (StringUtils.isNotNull(info) && info.getOutboundOrderId().longValue() != orderId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertOutboundOrder(ErpOutboundOrder outboundOrder)
    {
        prepareOrder(outboundOrder);
        int rows = outboundOrderMapper.insertOutboundOrder(outboundOrder);
        insertDetails(outboundOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOutboundOrder(ErpOutboundOrder outboundOrder)
    {
        prepareOrder(outboundOrder);
        List<ErpOutboundOrderDetail> detailList = outboundOrder.getDetailList();
        if (detailList != null && !detailList.isEmpty())
        {
            for (ErpOutboundOrderDetail detail : detailList)
            {
                if (StringUtils.isEmpty(detail.getBatchNo()))
                {
                    throw new ServiceException("物料明细中批次号不能存在空值");
                }
            }
        }
        int rows = outboundOrderMapper.updateOutboundOrder(outboundOrder);
        if (rows == 0)
        {
            throw new ServiceException("只有草稿出库单允许修改");
        }
        detailMapper.deleteDetailByOrderId(outboundOrder.getOutboundOrderId());
        insertDetails(outboundOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOutboundOrderByIds(Long[] outboundOrderIds)
    {
        for (Long outboundOrderId : outboundOrderIds)
        {
            ErpOutboundOrder order = outboundOrderMapper.selectOutboundOrderById(outboundOrderId);
            if (order == null || !"草稿".equals(order.getOrderStatus()))
            {
                throw new ServiceException("只有草稿出库单允许删除");
            }
        }
        detailMapper.deleteDetailByOrderIds(outboundOrderIds);
        return outboundOrderMapper.deleteOutboundOrderByIds(outboundOrderIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int postOutboundOrder(Long outboundOrderId, String username)
    {
        ErpOutboundOrder order = selectOutboundOrderByIdForUpdate(outboundOrderId);
        if (order == null)
        {
            throw new ServiceException("出库单不存在");
        }
        if ("已出库".equals(order.getOrderStatus()))
        {
            throw new ServiceException("出库单已过账");
        }
        if ("已完成".equals(order.getOrderStatus()))
        {
            throw new ServiceException("出库单已完成，不能再次过账");
        }
        if (!("草稿".equals(order.getOrderStatus()) || "已审核".equals(order.getOrderStatus()) || "已拣货".equals(order.getOrderStatus())))
        {
            throw new ServiceException("当前出库单状态不允许过账");
        }
        List<ErpOutboundOrderDetail> details = order.getDetailList();
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("出库单明细不能为空");
        }

        Date now = DateUtils.getNowDate();
        for (ErpOutboundOrderDetail detail : details)
        {
            BigDecimal qty = nvl(detail.getOutboundQty());
            if (qty.compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("出库数量必须大于0");
            }
            String batchNo = StringUtils.defaultString(detail.getBatchNo());
            ErpInventory inventory = inventoryMapper.selectInventoryForUpdate(order.getWarehouseId(), detail.getItemType(), detail.getItemId(), batchNo);
            if (inventory == null || nvl(inventory.getAvailableQty()).compareTo(qty) < 0)
            {
                throw new ServiceException("库存不足：" + detail.getItemCode() + " 批次 " + batchNo);
            }
            int rows;
            BigDecimal lockedQty = nvl(detail.getLockedQty());
            if (lockedQty.compareTo(BigDecimal.ZERO) > 0)
            {
                if (nvl(inventory.getLockedQty()).compareTo(lockedQty) < 0)
                {
                    throw new ServiceException("锁定库存不足：" + detail.getItemCode() + " 批次 " + batchNo);
                }
                rows = inventoryMapper.decreaseAvailableAndLockedQty(inventory.getInventoryId(), qty, lockedQty);
            }
            else
            {
                rows = inventoryMapper.decreaseAvailableQty(inventory.getInventoryId(), qty);
            }
            if (rows == 0)
            {
                throw new ServiceException("库存不足：" + detail.getItemCode() + " 批次 " + batchNo);
            }
            BigDecimal balanceQty = nvl(inventory.getAvailableQty()).subtract(qty);
            transactionMapper.insertTransaction(buildTransaction(order, detail, order.getOutboundType(), "OUT", BigDecimal.ZERO, qty, balanceQty, username, now));
        }

        order.setAuditBy(username);
        order.setAuditTime(now);
        order.setPickBy(username);
        order.setPickTime(now);
        order.setPostBy(username);
        order.setPostTime(now);
        order.setUpdateBy(username);
        int rows = outboundOrderMapper.updateOutboundOrderPost(order);
        if (rows == 0)
        {
            throw new ServiceException("出库单状态已变化，请刷新后重试");
        }
        syncDeliveryStatus(order, "已发货", username);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelPostOutboundOrder(Long outboundOrderId, String username)
    {
        ErpOutboundOrder order = selectOutboundOrderByIdForUpdate(outboundOrderId);
        if (order == null)
        {
            throw new ServiceException("出库单不存在");
        }
        if (!"已出库".equals(order.getOrderStatus()))
        {
            if ("已完成".equals(order.getOrderStatus()))
            {
                throw new ServiceException("出库单已完成，不能取消过账");
            }
            throw new ServiceException("只有已出库的出库单允许取消过账");
        }
        List<ErpOutboundOrderDetail> details = order.getDetailList();
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("出库单明细不能为空");
        }

        Date now = DateUtils.getNowDate();
        for (ErpOutboundOrderDetail detail : details)
        {
            BigDecimal qty = nvl(detail.getOutboundQty());
            if (qty.compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("出库数量必须大于0");
            }
            String batchNo = StringUtils.defaultString(detail.getBatchNo());
            ErpInventory inventory = inventoryMapper.selectInventoryForUpdate(order.getWarehouseId(), detail.getItemType(), detail.getItemId(), batchNo);
            BigDecimal balanceQty;
            if (inventory == null)
            {
                inventory = buildInventory(order, detail, qty);
                inventoryMapper.insertInventory(inventory);
                balanceQty = qty;
            }
            else
            {
                BigDecimal lockedQty = nvl(detail.getLockedQty());
                if (lockedQty.compareTo(BigDecimal.ZERO) > 0)
                {
                    inventoryMapper.increaseAvailableAndLockedQty(inventory.getInventoryId(), qty, lockedQty);
                }
                else
                {
                    inventoryMapper.increaseAvailableQty(inventory.getInventoryId(), qty, inventory.getUnitPrice());
                }
                balanceQty = nvl(inventory.getAvailableQty()).add(qty);
            }
            transactionMapper.insertTransaction(buildTransaction(order, detail, "取消" + order.getOutboundType(), "CO", qty, BigDecimal.ZERO, balanceQty, username, now));
        }

        order.setCancelBy(username);
        order.setCancelTime(now);
        order.setUpdateBy(username);
        int rows = outboundOrderMapper.updateOutboundOrderCancel(order);
        if (rows == 0)
        {
            throw new ServiceException("出库单状态已变化，请刷新后重试");
        }
        syncDeliveryStatus(order, "已确认", username);
        return rows;
    }

    private void prepareOrder(ErpOutboundOrder order)
    {
        if (StringUtils.isBlank(order.getOutboundOrderNo()))
        {
            order.setOutboundOrderNo("CK" + DateUtils.dateTimeNow());
        }
        if (StringUtils.isBlank(order.getOrderStatus()))
        {
            order.setOrderStatus("草稿");
        }
        ErpWarehouse warehouse = warehouseMapper.selectWarehouseById(order.getWarehouseId());
        if (warehouse != null)
        {
            order.setWarehouseName(warehouse.getWarehouseName());
        }
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getDetailList() != null)
        {
            for (ErpOutboundOrderDetail detail : order.getDetailList())
            {
                BigDecimal qty = nvl(detail.getOutboundQty());
                BigDecimal price = nvl(detail.getUnitPrice());
                detail.setAmount(qty.multiply(price));
                totalQty = totalQty.add(qty);
                totalAmount = totalAmount.add(detail.getAmount());
            }
        }
        order.setTotalQty(totalQty);
        order.setTotalAmount(totalAmount);
    }

    private void insertDetails(ErpOutboundOrder order)
    {
        List<ErpOutboundOrderDetail> detailList = order.getDetailList();
        if (detailList == null || detailList.isEmpty())
        {
            throw new ServiceException("出库单明细不能为空");
        }
        List<ErpOutboundOrderDetail> insertList = new ArrayList<ErpOutboundOrderDetail>();
        for (ErpOutboundOrderDetail detail : detailList)
        {
            if (detail == null || detail.getItemId() == null || StringUtils.isBlank(detail.getItemCode()))
            {
                continue;
            }
            detail.setOutboundOrderId(order.getOutboundOrderId());
            detail.setOutboundOrderNo(order.getOutboundOrderNo());
            detail.setCreateBy(order.getCreateBy());
            if (StringUtils.isBlank(detail.getItemType())) detail.setItemType("物料");
            if (StringUtils.isBlank(detail.getBatchNo())) detail.setBatchNo("");
            insertList.add(detail);
        }
        if (insertList.isEmpty())
        {
            throw new ServiceException("出库单明细不能为空");
        }
        detailMapper.batchInsertDetail(insertList);
    }

    private ErpOutboundOrder selectOutboundOrderByIdForUpdate(Long outboundOrderId)
    {
        ErpOutboundOrder order = outboundOrderMapper.selectOutboundOrderByIdForUpdate(outboundOrderId);
        if (order != null)
        {
            order.setDetailList(detailMapper.selectDetailByOrderId(outboundOrderId));
        }
        return order;
    }

    private ErpInventory buildInventory(ErpOutboundOrder order, ErpOutboundOrderDetail detail, BigDecimal qty)
    {
        ErpInventory inventory = new ErpInventory();
        inventory.setWarehouseId(order.getWarehouseId());
        inventory.setWarehouseName(order.getWarehouseName());
        inventory.setItemType(detail.getItemType());
        inventory.setItemId(detail.getItemId());
        inventory.setItemCode(detail.getItemCode());
        inventory.setItemName(detail.getItemName());
        inventory.setColorName(detail.getColorName());
        inventory.setSizeName(detail.getSizeName());
        inventory.setSpecName(detail.getSpecName());
        inventory.setBatchNo(StringUtils.defaultString(detail.getBatchNo()));
        inventory.setAvailableQty(qty);
        inventory.setLockedQty(BigDecimal.ZERO);
        inventory.setUnitPrice(nvl(detail.getUnitPrice()));
        inventory.setUnitName(detail.getUnitName());
        inventory.setRemark("取消出库单" + order.getOutboundOrderNo() + "生成");
        return inventory;
    }

    private void syncDeliveryStatus(ErpOutboundOrder order, String status, String username)
    {
        if (!"发货单".equals(order.getSourceType()))
        {
            return;
        }
        ErpDeliveryOrder deliveryOrder = new ErpDeliveryOrder();
        deliveryOrder.setOutboundOrderId(order.getOutboundOrderId());
        deliveryOrder.setDeliveryStatus(status);
        deliveryOrder.setUpdateBy(username);
        deliveryOrderMapper.updateDeliveryStatusByOutboundOrderId(deliveryOrder);
    }

    private ErpInventoryTransaction buildTransaction(ErpOutboundOrder order, ErpOutboundOrderDetail detail, String transactionType,
            String actionCode, BigDecimal inQty, BigDecimal outQty, BigDecimal balanceQty, String username, Date now)
    {
        ErpInventoryTransaction transaction = new ErpInventoryTransaction();
        transaction.setTransactionNo("LS" + DateUtils.dateTimeNow() + actionCode + StringUtils.leftPad(String.valueOf(detail.getOutboundDetailId()), 4, "0"));
        transaction.setTransactionType(transactionType);
        transaction.setBusinessNo(order.getOutboundOrderNo());
        transaction.setWarehouseId(order.getWarehouseId());
        transaction.setWarehouseName(order.getWarehouseName());
        transaction.setItemType(detail.getItemType());
        transaction.setItemId(detail.getItemId());
        transaction.setItemCode(detail.getItemCode());
        transaction.setItemName(detail.getItemName());
        transaction.setBatchNo(StringUtils.defaultString(detail.getBatchNo()));
        transaction.setInQty(inQty);
        transaction.setOutQty(outQty);
        transaction.setBalanceQty(balanceQty);
        transaction.setUnitName(detail.getUnitName());
        transaction.setOperatorName(username);
        transaction.setTransactionTime(now);
        transaction.setCreateBy(username);
        transaction.setRemark(order.getRemark());
        return transaction;
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}

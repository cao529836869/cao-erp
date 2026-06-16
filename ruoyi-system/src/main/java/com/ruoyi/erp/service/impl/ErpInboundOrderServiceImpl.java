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
import com.ruoyi.erp.domain.ErpInboundOrder;
import com.ruoyi.erp.domain.ErpInboundOrderDetail;
import com.ruoyi.erp.domain.ErpInventory;
import com.ruoyi.erp.domain.ErpInventoryTransaction;
import com.ruoyi.erp.domain.ErpWarehouse;
import com.ruoyi.erp.mapper.ErpInboundOrderDetailMapper;
import com.ruoyi.erp.mapper.ErpInboundOrderMapper;
import com.ruoyi.erp.mapper.ErpInventoryMapper;
import com.ruoyi.erp.mapper.ErpInventoryTransactionMapper;
import com.ruoyi.erp.mapper.ErpWarehouseMapper;
import com.ruoyi.erp.service.IErpInboundOrderService;

@Service
public class ErpInboundOrderServiceImpl implements IErpInboundOrderService
{
    @Autowired private ErpInboundOrderMapper inboundOrderMapper;
    @Autowired private ErpInboundOrderDetailMapper detailMapper;
    @Autowired private ErpInventoryMapper inventoryMapper;
    @Autowired private ErpInventoryTransactionMapper transactionMapper;
    @Autowired private ErpWarehouseMapper warehouseMapper;

    @Override
    public ErpInboundOrder selectInboundOrderById(Long inboundOrderId)
    {
        ErpInboundOrder order = inboundOrderMapper.selectInboundOrderById(inboundOrderId);
        if (order != null)
        {
            order.setDetailList(detailMapper.selectDetailByOrderId(inboundOrderId));
        }
        return order;
    }

    @Override
    public List<ErpInboundOrder> selectInboundOrderList(ErpInboundOrder inboundOrder)
    {
        return inboundOrderMapper.selectInboundOrderList(inboundOrder);
    }

    @Override
    public boolean checkInboundOrderNoUnique(ErpInboundOrder inboundOrder)
    {
        Long orderId = StringUtils.isNull(inboundOrder.getInboundOrderId()) ? -1L : inboundOrder.getInboundOrderId();
        ErpInboundOrder info = inboundOrderMapper.checkInboundOrderNoUnique(inboundOrder.getInboundOrderNo());
        if (StringUtils.isNotNull(info) && info.getInboundOrderId().longValue() != orderId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertInboundOrder(ErpInboundOrder inboundOrder)
    {
        prepareOrder(inboundOrder);
        int rows = inboundOrderMapper.insertInboundOrder(inboundOrder);
        insertDetails(inboundOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateInboundOrder(ErpInboundOrder inboundOrder)
    {
        prepareOrder(inboundOrder);
        int rows = inboundOrderMapper.updateInboundOrder(inboundOrder);
        if (rows == 0)
        {
            throw new ServiceException("只有草稿入库单允许修改");
        }
        detailMapper.deleteDetailByOrderId(inboundOrder.getInboundOrderId());
        insertDetails(inboundOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteInboundOrderByIds(Long[] inboundOrderIds)
    {
        for (Long inboundOrderId : inboundOrderIds)
        {
            ErpInboundOrder order = inboundOrderMapper.selectInboundOrderById(inboundOrderId);
            if (order == null || !"草稿".equals(order.getOrderStatus()))
            {
                throw new ServiceException("只有草稿入库单允许删除");
            }
        }
        detailMapper.deleteDetailByOrderIds(inboundOrderIds);
        return inboundOrderMapper.deleteInboundOrderByIds(inboundOrderIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int postInboundOrder(Long inboundOrderId, String username)
    {
        ErpInboundOrder order = selectInboundOrderByIdForUpdate(inboundOrderId);
        if (order == null)
        {
            throw new ServiceException("入库单不存在");
        }
        if ("已入库".equals(order.getOrderStatus()))
        {
            throw new ServiceException("入库单已过账");
        }
        List<ErpInboundOrderDetail> details = order.getDetailList();
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("入库单明细不能为空");
        }

        Date now = DateUtils.getNowDate();
        for (ErpInboundOrderDetail detail : details)
        {
            BigDecimal qty = nvl(detail.getInboundQty());
            if (qty.compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("入库数量必须大于0");
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
                inventoryMapper.increaseAvailableQty(inventory.getInventoryId(), qty, nvl(detail.getUnitPrice()));
                balanceQty = nvl(inventory.getAvailableQty()).add(qty);
            }
            transactionMapper.insertTransaction(buildTransaction(order, detail, order.getInboundType(), "IN", qty, BigDecimal.ZERO, balanceQty, username, now));
        }

        order.setAuditBy(username);
        order.setAuditTime(now);
        order.setPostBy(username);
        order.setPostTime(now);
        order.setUpdateBy(username);
        int rows = inboundOrderMapper.updateInboundOrderPost(order);
        if (rows == 0)
        {
            throw new ServiceException("入库单状态已变化，请刷新后重试");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelPostInboundOrder(Long inboundOrderId, String username)
    {
        ErpInboundOrder order = selectInboundOrderByIdForUpdate(inboundOrderId);
        if (order == null)
        {
            throw new ServiceException("入库单不存在");
        }
        if (!"已入库".equals(order.getOrderStatus()))
        {
            throw new ServiceException("只有已入库的入库单允许取消过账");
        }
        List<ErpInboundOrderDetail> details = order.getDetailList();
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("入库单明细不能为空");
        }

        Date now = DateUtils.getNowDate();
        for (ErpInboundOrderDetail detail : details)
        {
            BigDecimal qty = nvl(detail.getInboundQty());
            if (qty.compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("入库数量必须大于0");
            }
            String batchNo = StringUtils.defaultString(detail.getBatchNo());
            ErpInventory inventory = inventoryMapper.selectInventoryForUpdate(order.getWarehouseId(), detail.getItemType(), detail.getItemId(), batchNo);
            if (inventory == null || nvl(inventory.getAvailableQty()).compareTo(qty) < 0)
            {
                throw new ServiceException("取消过账失败，库存已被后续业务占用：" + detail.getItemCode() + " 批次 " + batchNo);
            }
            int rows = inventoryMapper.decreaseAvailableQty(inventory.getInventoryId(), qty);
            if (rows == 0)
            {
                throw new ServiceException("取消过账失败，库存已被后续业务占用：" + detail.getItemCode() + " 批次 " + batchNo);
            }
            BigDecimal balanceQty = nvl(inventory.getAvailableQty()).subtract(qty);
            transactionMapper.insertTransaction(buildTransaction(order, detail, "取消" + order.getInboundType(), "CI", BigDecimal.ZERO, qty, balanceQty, username, now));
        }

        order.setCancelBy(username);
        order.setCancelTime(now);
        order.setUpdateBy(username);
        int rows = inboundOrderMapper.updateInboundOrderCancel(order);
        if (rows == 0)
        {
            throw new ServiceException("入库单状态已变化，请刷新后重试");
        }
        return rows;
    }

    private void prepareOrder(ErpInboundOrder order)
    {
        if (StringUtils.isBlank(order.getInboundOrderNo()))
        {
            order.setInboundOrderNo("RK" + DateUtils.dateTimeNow());
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
            for (ErpInboundOrderDetail detail : order.getDetailList())
            {
                BigDecimal qty = nvl(detail.getInboundQty());
                BigDecimal price = nvl(detail.getUnitPrice());
                detail.setAmount(qty.multiply(price));
                totalQty = totalQty.add(qty);
                totalAmount = totalAmount.add(detail.getAmount());
            }
        }
        order.setTotalQty(totalQty);
        order.setTotalAmount(totalAmount);
    }

    private void insertDetails(ErpInboundOrder order)
    {
        List<ErpInboundOrderDetail> detailList = order.getDetailList();
        if (detailList == null || detailList.isEmpty())
        {
            throw new ServiceException("入库单明细不能为空");
        }
        List<ErpInboundOrderDetail> insertList = new ArrayList<ErpInboundOrderDetail>();
        for (ErpInboundOrderDetail detail : detailList)
        {
            if (detail == null || detail.getItemId() == null || StringUtils.isBlank(detail.getItemCode()))
            {
                continue;
            }
            detail.setInboundOrderId(order.getInboundOrderId());
            detail.setInboundOrderNo(order.getInboundOrderNo());
            detail.setCreateBy(order.getCreateBy());
            if (StringUtils.isBlank(detail.getItemType())) detail.setItemType("物料");
            if (StringUtils.isBlank(detail.getBatchNo())) detail.setBatchNo("");
            if (StringUtils.isBlank(detail.getQualityStatus())) detail.setQualityStatus("待检");
            insertList.add(detail);
        }
        if (insertList.isEmpty())
        {
            throw new ServiceException("入库单明细不能为空");
        }
        detailMapper.batchInsertDetail(insertList);
    }

    private ErpInventory buildInventory(ErpInboundOrder order, ErpInboundOrderDetail detail, BigDecimal qty)
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
        inventory.setRemark("入库单" + order.getInboundOrderNo() + "生成");
        return inventory;
    }

    private ErpInboundOrder selectInboundOrderByIdForUpdate(Long inboundOrderId)
    {
        ErpInboundOrder order = inboundOrderMapper.selectInboundOrderByIdForUpdate(inboundOrderId);
        if (order != null)
        {
            order.setDetailList(detailMapper.selectDetailByOrderId(inboundOrderId));
        }
        return order;
    }

    private ErpInventoryTransaction buildTransaction(ErpInboundOrder order, ErpInboundOrderDetail detail, String transactionType,
            String actionCode, BigDecimal inQty, BigDecimal outQty, BigDecimal balanceQty, String username, Date now)
    {
        ErpInventoryTransaction transaction = new ErpInventoryTransaction();
        transaction.setTransactionNo("LS" + DateUtils.dateTimeNow() + actionCode + StringUtils.leftPad(String.valueOf(detail.getInboundDetailId()), 4, "0"));
        transaction.setTransactionType(transactionType);
        transaction.setBusinessNo(order.getInboundOrderNo());
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

package com.ruoyi.erp.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.erp.domain.ErpCutOrder;
import com.ruoyi.erp.domain.ErpCutOrderDetail;
import com.ruoyi.erp.domain.ErpInboundOrder;
import com.ruoyi.erp.domain.ErpInboundOrderDetail;
import com.ruoyi.erp.domain.ErpOutboundOrder;
import com.ruoyi.erp.domain.ErpProductionOrder;
import com.ruoyi.erp.domain.ErpProductionOrderDetail;
import com.ruoyi.erp.domain.ErpWarehouse;
import com.ruoyi.erp.mapper.ErpCutOrderDetailMapper;
import com.ruoyi.erp.mapper.ErpCutOrderMapper;
import com.ruoyi.erp.mapper.ErpInboundOrderDetailMapper;
import com.ruoyi.erp.mapper.ErpInboundOrderMapper;
import com.ruoyi.erp.mapper.ErpOutboundOrderMapper;
import com.ruoyi.erp.mapper.ErpProductionOrderDetailMapper;
import com.ruoyi.erp.mapper.ErpProductionOrderMapper;
import com.ruoyi.erp.mapper.ErpWarehouseMapper;
import com.ruoyi.erp.service.IErpCutOrderService;

@Service
public class ErpCutOrderServiceImpl implements IErpCutOrderService
{
    public static final int STATUS_WAIT = 0;
    public static final int STATUS_DOING = 1;
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_CANCELED = 3;

    @Autowired private ErpCutOrderMapper cutOrderMapper;
    @Autowired private ErpCutOrderDetailMapper cutDetailMapper;
    @Autowired private ErpProductionOrderMapper productionOrderMapper;
    @Autowired private ErpProductionOrderDetailMapper productionDetailMapper;
    @Autowired private ErpOutboundOrderMapper outboundOrderMapper;
    @Autowired private ErpInboundOrderMapper inboundOrderMapper;
    @Autowired private ErpInboundOrderDetailMapper inboundDetailMapper;
    @Autowired private ErpWarehouseMapper warehouseMapper;

    @Override
    public ErpCutOrder selectCutOrderById(Long cutOrderId)
    {
        ErpCutOrder order = cutOrderMapper.selectCutOrderById(cutOrderId);
        if (order != null)
        {
            order.setDetailList(cutDetailMapper.selectDetailByOrderId(cutOrderId));
        }
        return order;
    }

    @Override
    public ErpCutOrder selectCutOrderByProductionOrderId(Long productionOrderId)
    {
        ErpCutOrder order = cutOrderMapper.selectCutOrderByProductionOrderId(productionOrderId);
        if (order != null)
        {
            order.setDetailList(cutDetailMapper.selectDetailByOrderId(order.getCutOrderId()));
        }
        return order;
    }

    @Override
    public List<ErpCutOrder> selectCutOrderList(ErpCutOrder cutOrder)
    {
        return cutOrderMapper.selectCutOrderList(cutOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCutOrder(ErpCutOrder cutOrder)
    {
        prepareOrder(cutOrder);
        int rows = cutOrderMapper.insertCutOrder(cutOrder);
        insertDetails(cutOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCutOrder(ErpCutOrder cutOrder)
    {
        ErpCutOrder oldOrder = selectCutOrderById(cutOrder.getCutOrderId());
        if (oldOrder == null) {
            throw new ServiceException("裁剪单不存在");
        }
        if (oldOrder.getCutStatus() == STATUS_FINISHED)
        {
            throw new ServiceException("裁剪单状态已完成,无法修改");
        }
        prepareOrder(cutOrder);
        int rows = cutOrderMapper.updateCutOrder(cutOrder);
        cutDetailMapper.deleteDetailByOrderId(cutOrder.getCutOrderId());
        insertDetails(cutOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCutOrderByIds(Long[] cutOrderIds)
    {
        cutDetailMapper.deleteDetailByOrderIds(cutOrderIds);
        return cutOrderMapper.deleteCutOrderByIds(cutOrderIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int finishCutOrder(Long cutOrderId, BigDecimal actualCutQty, String username)
    {
        if (actualCutQty == null || actualCutQty.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("已裁数必须大于0");
        }
        ErpCutOrder cutOrder = selectCutOrderById(cutOrderId);
        if (cutOrder == null)
        {
            throw new ServiceException("裁剪单不存在");
        }
        if (cutOrder.getCutStatus() == null || (cutOrder.getCutStatus() != STATUS_WAIT && cutOrder.getCutStatus() != STATUS_DOING))
        {
            throw new ServiceException("只有待裁剪或裁剪中的裁剪单允许完成");
        }
        List<ErpCutOrderDetail> details = cutOrder.getDetailList();
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("裁剪单明细不能为空");
        }

        ErpWarehouse warehouse = selectDefaultFinishedGoodsWarehouse();
        List<ErpInboundOrderDetail> inboundDetails = buildInboundDetails(cutOrder, details, actualCutQty, username);
        if (inboundDetails.isEmpty())
        {
            throw new ServiceException("实际已裁数必须大于0");
        }

        ErpCutOrder order = new ErpCutOrder();
        order.setCutOrderId(cutOrderId);
        order.setCutStatus(STATUS_FINISHED);
        order.setFinishTime(DateUtils.getNowDate());
        order.setUpdateBy(username);
        int rows = cutOrderMapper.updateCutStatus(order);
        if (rows == 0)
        {
            throw new ServiceException("裁剪单状态已变化，请刷新后重试");
        }

        createProductionInboundOrder(cutOrder, warehouse, inboundDetails, actualCutQty, username);
        return rows;
    }

    @Override
    public int cancelCutOrder(Long cutOrderId, String username)
    {
        ErpCutOrder order = new ErpCutOrder();
        order.setCutOrderId(cutOrderId);
        order.setCutStatus(STATUS_CANCELED);
        order.setUpdateBy(username);
        return cutOrderMapper.updateCutStatus(order);
    }

    @Override
    public int delayCutOrder(Long cutOrderId, Date planFinishTime, String remark, String username)
    {
        if (planFinishTime == null)
        {
            throw new ServiceException("计划完成时间不能为空");
        }
        ErpCutOrder order = new ErpCutOrder();
        order.setCutOrderId(cutOrderId);
        order.setPlanFinishTime(planFinishTime);
        order.setRemark(remark);
        order.setUpdateBy(username);
        int rows = cutOrderMapper.delayCutOrder(order);
        if (rows == 0)
        {
            throw new ServiceException("只有待裁剪或裁剪中的裁剪单允许延期");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createFromProduction(Long productionOrderId, String username)
    {
        ErpProductionOrder productionOrder = productionOrderMapper.selectProductionOrderById(productionOrderId);
        if (productionOrder == null)
        {
            throw new ServiceException("生产订单不存在");
        }
        if (isProductionFinished(productionOrder.getOrderStatus()))
        {
            throw new ServiceException("生产订单已完成，不能重复创建裁剪单");
        }
        if ("已关闭".equals(productionOrder.getOrderStatus()))
        {
            throw new ServiceException("生产订单已关闭，不能创建裁剪单");
        }
        if (cutOrderMapper.selectCutOrderByProductionOrderId(productionOrderId) != null)
        {
            throw new ServiceException("该生产订单已存在裁剪单");
        }
        assertPickingPosted(productionOrder.getProductionOrderNo());
        List<ErpProductionOrderDetail> productionDetails = productionDetailMapper.selectDetailByOrderId(productionOrderId);
        if (productionDetails == null || productionDetails.isEmpty())
        {
            throw new ServiceException("生产订单明细不能为空");
        }

        ErpCutOrder cutOrder = new ErpCutOrder();
        cutOrder.setCutOrderNo("CJ" + DateUtils.dateTimeNow());
        cutOrder.setProductionOrderId(productionOrder.getProductionOrderId());
        cutOrder.setProductionOrderNo(productionOrder.getProductionOrderNo());
        cutOrder.setSalesOrderId(productionOrder.getSalesOrderId());
        cutOrder.setSalesOrderNo(productionOrder.getSalesOrderNo());
        cutOrder.setCustomerId(productionOrder.getCustomerId());
        cutOrder.setCustomerName(productionOrder.getCustomerName());
        cutOrder.setPlanFinishTime(toPlanFinishTime(productionOrder));
        cutOrder.setTotalQty(productionOrder.getTotalQty());
        cutOrder.setCutStatus(STATUS_WAIT);
        cutOrder.setCreateBy(username);
        cutOrder.setRemark("由生产订单 " + productionOrder.getProductionOrderNo() + " 生成");
        cutOrderMapper.insertCutOrder(cutOrder);

        List<ErpCutOrderDetail> cutDetails = new ArrayList<ErpCutOrderDetail>();
        for (ErpProductionOrderDetail detail : productionDetails)
        {
            ErpCutOrderDetail cutDetail = new ErpCutOrderDetail();
            cutDetail.setCutOrderId(cutOrder.getCutOrderId());
            cutDetail.setCutOrderNo(cutOrder.getCutOrderNo());
            cutDetail.setProductionOrderDetailId(detail.getProductionOrderDetailId());
            cutDetail.setStyleId(detail.getStyleId());
            cutDetail.setSkuId(detail.getSkuId());
            cutDetail.setStyleNo(detail.getStyleNo());
            cutDetail.setStyleName(detail.getStyleName());
            cutDetail.setColorName(detail.getColorName());
            cutDetail.setSizeName(detail.getSizeName());
            cutDetail.setPlanQty(detail.getPlanQty());
            cutDetail.setCutQty(BigDecimal.ZERO);
            cutDetail.setCreateBy(username);
            cutDetails.add(cutDetail);
        }
        cutDetailMapper.batchInsertDetail(cutDetails);

        ErpProductionOrder finishOrder = new ErpProductionOrder();
        finishOrder.setProductionOrderId(productionOrderId);
        finishOrder.setUpdateBy(username);
        int finishRows = productionOrderMapper.updateProductionOrderFinish(finishOrder);
        if (finishRows == 0)
        {
            throw new ServiceException("生产订单状态已变化，请刷新后重试");
        }

        ErpOutboundOrder completeOutbound = new ErpOutboundOrder();
        completeOutbound.setSourceNo(productionOrder.getProductionOrderNo());
        completeOutbound.setOutboundType("生产领料");
        completeOutbound.setUpdateBy(username);
        int outboundRows = outboundOrderMapper.updateOutboundOrderCompleteBySource(completeOutbound);
        if (outboundRows == 0)
        {
            throw new ServiceException("生产领料出库单状态已变化，请刷新后重试");
        }
        return 1;
    }

    private void assertPickingPosted(String productionOrderNo)
    {
        ErpOutboundOrder query = new ErpOutboundOrder();
        query.setSourceNo(productionOrderNo);
        List<ErpOutboundOrder> outboundOrders = outboundOrderMapper.selectOutboundOrderList(query);
        for (ErpOutboundOrder outboundOrder : outboundOrders)
        {
            if (productionOrderNo.equals(outboundOrder.getSourceNo())
                    && "生产领料".equals(outboundOrder.getOutboundType())
                    && "已出库".equals(outboundOrder.getOrderStatus()))
            {
                return;
            }
        }
        throw new ServiceException("生产订单完成领料出库后才允许创建裁剪单");
    }

    private boolean isProductionFinished(String orderStatus)
    {
        return "已完成".equals(orderStatus) || "已完工".equals(orderStatus);
    }

    private Date toPlanFinishTime(ErpProductionOrder productionOrder)
    {
        Date date = productionOrder.getPlanFinishDate();
        return date == null ? DateUtils.getNowDate() : date;
    }

    private void prepareOrder(ErpCutOrder order)
    {
        if (StringUtils.isBlank(order.getCutOrderNo()))
        {
            order.setCutOrderNo("CJ" + DateUtils.dateTimeNow());
        }
        if (order.getCutStatus() == null)
        {
            order.setCutStatus(STATUS_WAIT);
        }
        BigDecimal totalQty = BigDecimal.ZERO;
        if (order.getDetailList() != null)
        {
            for (ErpCutOrderDetail detail : order.getDetailList())
            {
                totalQty = totalQty.add(nvl(detail.getPlanQty()));
            }
        }
        order.setTotalQty(totalQty);
    }

    private void insertDetails(ErpCutOrder order)
    {
        if (order.getDetailList() == null || order.getDetailList().isEmpty())
        {
            throw new ServiceException("裁剪单明细不能为空");
        }
        List<ErpCutOrderDetail> insertList = new ArrayList<ErpCutOrderDetail>();
        for (ErpCutOrderDetail detail : order.getDetailList())
        {
            if (detail == null || detail.getSkuId() == null)
            {
                continue;
            }
            detail.setCutOrderId(order.getCutOrderId());
            detail.setCutOrderNo(order.getCutOrderNo());
            detail.setCreateBy(order.getCreateBy());
            detail.setCutQty(nvl(detail.getCutQty()));
            insertList.add(detail);
        }
        if (insertList.isEmpty())
        {
            throw new ServiceException("裁剪单明细不能为空");
        }
        cutDetailMapper.batchInsertDetail(insertList);
    }

    private ErpWarehouse selectDefaultFinishedGoodsWarehouse()
    {
        List<ErpWarehouse> warehouses = warehouseMapper.selectWarehouseAll();
        if (warehouses == null || warehouses.isEmpty())
        {
            throw new ServiceException("请先维护成衣仓库");
        }
        for (ErpWarehouse warehouse : warehouses)
        {
            if (containsText(warehouse.getWarehouseType(), "成衣") || containsText(warehouse.getWarehouseName(), "成衣"))
            {
                return warehouse;
            }
        }
        throw new ServiceException("未找到成衣仓库，请在仓库档案中维护仓库类型或名称包含“成衣”的仓库");
    }

    private List<ErpInboundOrderDetail> buildInboundDetails(ErpCutOrder order, List<ErpCutOrderDetail> cutDetails,
            BigDecimal actualCutQty, String username)
    {
        List<ErpInboundOrderDetail> inboundDetails = new ArrayList<ErpInboundOrderDetail>();
        BigDecimal remainingQty = actualCutQty;
        for (int i = 0; i < cutDetails.size(); i++)
        {
            ErpCutOrderDetail cutDetail = cutDetails.get(i);
            BigDecimal detailQty = i == cutDetails.size() - 1 ? remainingQty : minPositive(nvl(cutDetail.getPlanQty()), remainingQty);
            if (detailQty.compareTo(BigDecimal.ZERO) < 0)
            {
                detailQty = BigDecimal.ZERO;
            }

            cutDetail.setCutQty(detailQty);
            cutDetail.setUpdateBy(username);
            cutDetailMapper.updateCutQty(cutDetail);

            if (detailQty.compareTo(BigDecimal.ZERO) > 0)
            {
                ErpInboundOrderDetail inboundDetail = new ErpInboundOrderDetail();
                inboundDetail.setSourceDetailId(cutDetail.getCutDetailId());
                inboundDetail.setItemType("成衣");
                inboundDetail.setItemId(cutDetail.getSkuId());
                inboundDetail.setItemCode(StringUtils.defaultIfBlank(cutDetail.getStyleNo(), String.valueOf(cutDetail.getSkuId())));
                inboundDetail.setItemName(StringUtils.defaultIfBlank(cutDetail.getStyleName(), cutDetail.getStyleNo()));
                inboundDetail.setColorName(cutDetail.getColorName());
                inboundDetail.setSizeName(cutDetail.getSizeName());
                inboundDetail.setBatchNo(order.getCutOrderNo());
                inboundDetail.setPlanQty(detailQty);
                inboundDetail.setInboundQty(detailQty);
                inboundDetail.setUnitName("件");
                inboundDetail.setUnitPrice(BigDecimal.ZERO);
                inboundDetail.setAmount(BigDecimal.ZERO);
                inboundDetail.setQualityStatus("合格");
                inboundDetail.setCreateBy(username);
                inboundDetail.setRemark("由裁剪单" + order.getCutOrderNo() + "完成生成");
                inboundDetails.add(inboundDetail);
            }
            remainingQty = remainingQty.subtract(detailQty);
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0)
            {
                remainingQty = BigDecimal.ZERO;
            }
        }
        return inboundDetails;
    }

    private void createProductionInboundOrder(ErpCutOrder cutOrder, ErpWarehouse warehouse,
            List<ErpInboundOrderDetail> inboundDetails, BigDecimal actualCutQty, String username)
    {
        ErpInboundOrder inboundOrder = new ErpInboundOrder();
        inboundOrder.setInboundOrderNo("RK" + DateUtils.dateTimeNow());
        inboundOrder.setInboundType("生产入库");
        inboundOrder.setSourceType("裁剪单");
        inboundOrder.setSourceNo(cutOrder.getCutOrderNo());
        inboundOrder.setCustomerId(cutOrder.getCustomerId());
        inboundOrder.setCustomerName(cutOrder.getCustomerName());
        inboundOrder.setWarehouseId(warehouse.getWarehouseId());
        inboundOrder.setWarehouseName(warehouse.getWarehouseName());
        inboundOrder.setInboundDate(DateUtils.getNowDate());
        inboundOrder.setTotalQty(actualCutQty);
        inboundOrder.setTotalAmount(BigDecimal.ZERO);
        inboundOrder.setOrderStatus("草稿");
        inboundOrder.setCreateBy(username);
        inboundOrder.setRemark("由裁剪单" + cutOrder.getCutOrderNo() + "完成生成");
        inboundOrder.setDetailList(inboundDetails);
        inboundOrderMapper.insertInboundOrder(inboundOrder);

        for (ErpInboundOrderDetail detail : inboundDetails)
        {
            detail.setInboundOrderId(inboundOrder.getInboundOrderId());
            detail.setInboundOrderNo(inboundOrder.getInboundOrderNo());
        }
        inboundDetailMapper.batchInsertDetail(inboundDetails);
    }

    private BigDecimal minPositive(BigDecimal left, BigDecimal right)
    {
        if (left.compareTo(BigDecimal.ZERO) <= 0)
        {
            return BigDecimal.ZERO;
        }
        return left.compareTo(right) <= 0 ? left : right;
    }

    private boolean containsText(String value, String text)
    {
        return value != null && value.contains(text);
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}

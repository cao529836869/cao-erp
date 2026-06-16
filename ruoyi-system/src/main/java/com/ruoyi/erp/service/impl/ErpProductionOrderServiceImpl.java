package com.ruoyi.erp.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ruoyi.erp.domain.*;
import com.ruoyi.erp.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.erp.service.IErpProductionOrderService;

@Service
public class ErpProductionOrderServiceImpl implements IErpProductionOrderService
{
    @Autowired private ErpProductionOrderMapper productionOrderMapper;
    @Autowired private ErpProductionOrderDetailMapper detailMapper;
    @Autowired private ErpStyleBomDetailMapper erpStyleBomDetailMapper;
    @Autowired private ErpStyleBomMapper erpStyleBomMapper;
    @Autowired private ErpOutboundOrderMapper outboundOrderMapper;
    @Autowired private ErpOutboundOrderDetailMapper outboundOrderDetailMapper;
    @Autowired private ErpWarehouseMapper warehouseMapper;
    @Autowired private ErpMaterialSkuMapper materialSkuMapper;

    @Override
    public ErpProductionOrder selectProductionOrderById(Long productionOrderId)
    {
        ErpProductionOrder order = productionOrderMapper.selectProductionOrderById(productionOrderId);
        if (order != null)
        {
            order.setDetailList(detailMapper.selectDetailByOrderId(productionOrderId));
        }
        return order;
    }

    @Override
    public List<ErpProductionOrder> selectProductionOrderList(ErpProductionOrder productionOrder)
    {
        return productionOrderMapper.selectProductionOrderList(productionOrder);
    }

    @Override
    public boolean checkProductionOrderNoUnique(ErpProductionOrder productionOrder)
    {
        Long orderId = StringUtils.isNull(productionOrder.getProductionOrderId()) ? -1L : productionOrder.getProductionOrderId();
        ErpProductionOrder info = productionOrderMapper.checkProductionOrderNoUnique(productionOrder.getProductionOrderNo());
        if (StringUtils.isNotNull(info) && info.getProductionOrderId().longValue() != orderId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertProductionOrder(ErpProductionOrder productionOrder)
    {
        prepareOrder(productionOrder);
        int rows = productionOrderMapper.insertProductionOrder(productionOrder);
        insertDetails(productionOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProductionOrder(ErpProductionOrder productionOrder)
    {
        prepareOrder(productionOrder);
        int rows = productionOrderMapper.updateProductionOrder(productionOrder);
        if (rows == 0)
        {
            throw new ServiceException("只有草稿生产订单允许修改");
        }
        detailMapper.deleteDetailByOrderId(productionOrder.getProductionOrderId());
        insertDetails(productionOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProductionOrderByIds(Long[] productionOrderIds)
    {
        for (Long productionOrderId : productionOrderIds)
        {
            ErpProductionOrder order = productionOrderMapper.selectProductionOrderById(productionOrderId);
            if (order == null || !"草稿".equals(order.getOrderStatus()))
            {
                throw new ServiceException("只有草稿生产订单允许删除");
            }
        }
        detailMapper.deleteDetailByOrderIds(productionOrderIds);
        return productionOrderMapper.deleteProductionOrderByIds(productionOrderIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int releaseProductionOrder(Long productionOrderId, String username)
    {
        ErpProductionOrder order = selectProductionOrderById(productionOrderId);
        if (order == null)
        {
            throw new ServiceException("生产订单不存在");
        }
        if (!"草稿".equals(order.getOrderStatus()))
        {
            throw new ServiceException("只有草稿生产订单允许下达");
        }
        if (order.getDetailList() == null || order.getDetailList().isEmpty())
        {
            throw new ServiceException("生产订单明细不能为空");
        }
        Date now = DateUtils.getNowDate();
        order.setAuditBy(username);
        order.setAuditTime(now);
        order.setUpdateBy(username);
        int rows = productionOrderMapper.updateProductionOrderRelease(order);
        if (rows == 0)
        {
            throw new ServiceException("生产订单状态已变化，请刷新后重试");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeProductionOrder(Long productionOrderId, String username)
    {
        ErpProductionOrder order = productionOrderMapper.selectProductionOrderById(productionOrderId);
        if (order == null)
        {
            throw new ServiceException("生产订单不存在");
        }
        if ("已完成".equals(order.getOrderStatus()) || "已完工".equals(order.getOrderStatus()) || "已关闭".equals(order.getOrderStatus()))
        {
            throw new ServiceException("生产订单已完成或已关闭");
        }
        order.setUpdateBy(username);
        int rows = productionOrderMapper.updateProductionOrderClose(order);
        if (rows == 0)
        {
            throw new ServiceException("生产订单状态已变化，请刷新后重试");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int countBomThenBuildOutboundOrder(Long productionOrderId)
    {
        ErpProductionOrder order = selectProductionOrderById(productionOrderId);
        if (order == null)
        {
            throw new ServiceException("生产订单不存在");
        }
        if ("草稿".equals(order.getOrderStatus()) || "已关闭".equals(order.getOrderStatus()) || "已完成".equals(order.getOrderStatus()) || "已完工".equals(order.getOrderStatus()))
        {
            throw new ServiceException("只有已下达且未关闭的生产订单允许生成领料单");
        }
        if (order.getDetailList() == null || order.getDetailList().isEmpty())
        {
            throw new ServiceException("生产订单明细不能为空");
        }
        assertNoActivePickingOrder(order.getProductionOrderNo());

        ErpWarehouse warehouse = selectDefaultMaterialWarehouse();
        Map<String, ErpOutboundOrderDetail> demandMap = new LinkedHashMap<String, ErpOutboundOrderDetail>();
        for (ErpProductionOrderDetail detail : order.getDetailList())
        {
            BigDecimal planQty = nvl(detail.getPlanQty());
            if (planQty.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            ErpStyleBom bom = selectActiveBom(detail);
            List<ErpStyleBomDetail> bomDetails = erpStyleBomDetailMapper.selectErpStyleBomDetailByBomId(bom.getBomId());
            if (bomDetails == null || bomDetails.isEmpty())
            {
                throw new ServiceException("款号 " + detail.getStyleNo() + " 的BOM明细为空");
            }
            for (ErpStyleBomDetail bomDetail : bomDetails)
            {
                BigDecimal usageQty = nvl(bomDetail.getUsageQty());
                if (bomDetail.getMaterialSkuId() == null || usageQty.compareTo(BigDecimal.ZERO) <= 0)
                {
                    continue;
                }
                BigDecimal outboundQty = roundMaterialQty(planQty.multiply(usageQty).multiply(BigDecimal.ONE.add(nvl(bomDetail.getLossRate()))), bomDetail.getUnitName());
                String key = bomDetail.getMaterialSkuId() + "|" + StringUtils.defaultString(bomDetail.getUnitName());
                ErpOutboundOrderDetail outDetail = demandMap.get(key);
                if (outDetail == null)
                {
                    outDetail = buildOutboundDetail(bomDetail, outboundQty);
                    demandMap.put(key, outDetail);
                }
                else
                {
                    outDetail.setPlanQty(outDetail.getPlanQty().add(outboundQty));
                    outDetail.setOutboundQty(outDetail.getOutboundQty().add(outboundQty));
                    outDetail.setAmount(BigDecimal.ZERO);
                }
            }
        }
        if (demandMap.isEmpty())
        {
            throw new ServiceException("未根据BOM计算出可领用物料");
        }

        ErpOutboundOrder outboundOrder = new ErpOutboundOrder();
        outboundOrder.setOutboundOrderNo("CK" + DateUtils.dateTimeNow());
        outboundOrder.setOutboundType("生产领料");
        outboundOrder.setSourceType("生产订单");
        outboundOrder.setSourceNo(order.getProductionOrderNo());
        outboundOrder.setCustomerId(order.getCustomerId());
        outboundOrder.setCustomerName(order.getCustomerName());
        outboundOrder.setWarehouseId(warehouse.getWarehouseId());
        outboundOrder.setWarehouseName(warehouse.getWarehouseName());
        outboundOrder.setOutboundDate(DateUtils.getNowDate());
        outboundOrder.setOrderStatus("草稿");
        outboundOrder.setCreateBy(StringUtils.defaultIfBlank(order.getUpdateBy(), order.getCreateBy()));
        outboundOrder.setRemark("由生产订单" + order.getProductionOrderNo() + "按BOM生成");

        BigDecimal totalQty = BigDecimal.ZERO;
        List<ErpOutboundOrderDetail> outDetails = new ArrayList<ErpOutboundOrderDetail>(demandMap.values());
        for (ErpOutboundOrderDetail outDetail : outDetails)
        {
            totalQty = totalQty.add(nvl(outDetail.getOutboundQty()));
        }
        outboundOrder.setTotalQty(totalQty);
        outboundOrder.setTotalAmount(BigDecimal.ZERO);
        outboundOrderMapper.insertOutboundOrder(outboundOrder);

        for (ErpOutboundOrderDetail outDetail : outDetails)
        {
            outDetail.setOutboundOrderId(outboundOrder.getOutboundOrderId());
            outDetail.setOutboundOrderNo(outboundOrder.getOutboundOrderNo());
            outDetail.setCreateBy(outboundOrder.getCreateBy());
        }
        outboundOrderDetailMapper.batchInsertDetail(outDetails);
        return 1;
    }

    private void assertNoActivePickingOrder(String productionOrderNo)
    {
        ErpOutboundOrder query = new ErpOutboundOrder();
        query.setSourceNo(productionOrderNo);
        List<ErpOutboundOrder> outboundOrders = outboundOrderMapper.selectOutboundOrderList(query);
        for (ErpOutboundOrder outboundOrder : outboundOrders)
        {
            if (productionOrderNo.equals(outboundOrder.getSourceNo())
                    && "生产领料".equals(outboundOrder.getOutboundType())
                    && !"已取消".equals(outboundOrder.getOrderStatus()))
            {
                throw new ServiceException("该生产订单已存在生产领料出库单：" + outboundOrder.getOutboundOrderNo());
            }
        }
    }

    private ErpStyleBom selectActiveBom(ErpProductionOrderDetail detail)
    {
        List<ErpStyleBom> bomList = erpStyleBomMapper.selectErpStyleBomByStyleId(detail.getStyleId());
        ErpStyleBom selected = null;
        for (ErpStyleBom bom : bomList)
        {
            if ("1".equals(bom.getBomStatus()))
            {
                selected = bom;
            }
        }
        if (selected == null)
        {
            throw new ServiceException("款号 " + detail.getStyleNo() + " 没有已审核BOM，不能生成领料单");
        }
        return selected;
    }

    private ErpWarehouse selectDefaultMaterialWarehouse()
    {
        List<ErpWarehouse> warehouses = warehouseMapper.selectWarehouseAll();
        if (warehouses == null || warehouses.isEmpty())
        {
            throw new ServiceException("未维护可用仓库，不能生成领料单");
        }
        for (ErpWarehouse warehouse : warehouses)
        {
            if (warehouse.getWarehouseType() != null && warehouse.getWarehouseType().contains("物料"))
            {
                return warehouse;
            }
        }
        return warehouses.get(0);
    }

    private ErpOutboundOrderDetail buildOutboundDetail(ErpStyleBomDetail bomDetail, BigDecimal outboundQty)
    {
        ErpOutboundOrderDetail detail = new ErpOutboundOrderDetail();
        ErpMaterialSku sku = materialSkuMapper.selectMaterialSkuById(bomDetail.getMaterialSkuId());
        detail.setItemType("物料");
        detail.setItemId(bomDetail.getMaterialSkuId());
        detail.setItemCode(sku != null && StringUtils.isNotBlank(sku.getMaterialSkuCode()) ? sku.getMaterialSkuCode() : bomDetail.getMaterialCode());
        detail.setItemName(StringUtils.defaultIfBlank(bomDetail.getMaterialName(), sku == null ? "" : sku.getMaterialName()));
        detail.setColorName(StringUtils.defaultIfBlank(bomDetail.getColorName(), sku == null ? "" : sku.getColorName()));
        detail.setSpecName(StringUtils.defaultIfBlank(bomDetail.getSpecName(), sku == null ? "" : sku.getSpecName()));
        detail.setBatchNo("");
        detail.setPlanQty(outboundQty);
        detail.setLockedQty(BigDecimal.ZERO);
        detail.setOutboundQty(outboundQty);
        detail.setUnitName(StringUtils.defaultIfBlank(bomDetail.getUnitName(), sku == null ? "" : sku.getUnitName()));
        detail.setUnitPrice(BigDecimal.ZERO);
        detail.setAmount(BigDecimal.ZERO);
        detail.setRemark("BOM用量：" + nvl(bomDetail.getUsageQty()) + "，损耗率：" + nvl(bomDetail.getLossRate()));
        return detail;
    }

    private BigDecimal roundMaterialQty(BigDecimal qty, String unitName)
    {
        if ("个".equals(unitName) || "颗".equals(unitName) || "条".equals(unitName) || "件".equals(unitName))
        {
            return qty.setScale(0, RoundingMode.CEILING);
        }
        return qty.setScale(3, RoundingMode.CEILING);
    }

    private void prepareOrder(ErpProductionOrder order)
    {
        if (StringUtils.isBlank(order.getProductionOrderNo()))
        {
            order.setProductionOrderNo("MO" + DateUtils.dateTimeNow());
        }
        if (StringUtils.isBlank(order.getOrderStatus()))
        {
            order.setOrderStatus("草稿");
        }
        if (order.getCompletedQty() == null)
        {
            order.setCompletedQty(BigDecimal.ZERO);
        }
        BigDecimal totalQty = BigDecimal.ZERO;
        if (order.getDetailList() != null)
        {
            for (ErpProductionOrderDetail detail : order.getDetailList())
            {
                totalQty = totalQty.add(nvl(detail.getPlanQty()));
            }
        }
        order.setTotalQty(totalQty);
    }

    private void insertDetails(ErpProductionOrder order)
    {
        List<ErpProductionOrderDetail> detailList = order.getDetailList();
        if (detailList == null || detailList.isEmpty())
        {
            throw new ServiceException("生产订单明细不能为空");
        }
        List<ErpProductionOrderDetail> insertList = new ArrayList<ErpProductionOrderDetail>();
        for (ErpProductionOrderDetail detail : detailList)
        {
            if (detail == null || detail.getStyleId() == null || detail.getSkuId() == null)
            {
                continue;
            }
            if (nvl(detail.getPlanQty()).compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("计划生产数量必须大于0");
            }
            detail.setProductionOrderId(order.getProductionOrderId());
            detail.setProductionOrderNo(order.getProductionOrderNo());
            detail.setCreateBy(order.getCreateBy());
            detail.setCutQty(nvl(detail.getCutQty()));
            detail.setSewnQty(nvl(detail.getSewnQty()));
            detail.setFinishedQty(nvl(detail.getFinishedQty()));
            detail.setQualifiedQty(nvl(detail.getQualifiedQty()));
            insertList.add(detail);
        }
        if (insertList.isEmpty())
        {
            throw new ServiceException("生产订单明细不能为空");
        }
        detailMapper.batchInsertDetail(insertList);
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}

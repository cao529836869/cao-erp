package com.ruoyi.erp.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.erp.domain.ErpWarehouse;
import com.ruoyi.erp.mapper.ErpWarehouseMapper;
import com.ruoyi.erp.service.IErpWarehouseService;

@Service
public class ErpWarehouseServiceImpl implements IErpWarehouseService
{
    @Autowired
    private ErpWarehouseMapper warehouseMapper;

    @Override
    public ErpWarehouse selectWarehouseById(Long warehouseId) { return warehouseMapper.selectWarehouseById(warehouseId); }

    @Override
    public List<ErpWarehouse> selectWarehouseList(ErpWarehouse warehouse) { return warehouseMapper.selectWarehouseList(warehouse); }

    @Override
    public List<ErpWarehouse> selectWarehouseAll() { return warehouseMapper.selectWarehouseAll(); }

    @Override
    public boolean checkWarehouseCodeUnique(ErpWarehouse warehouse)
    {
        Long warehouseId = StringUtils.isNull(warehouse.getWarehouseId()) ? -1L : warehouse.getWarehouseId();
        ErpWarehouse info = warehouseMapper.checkWarehouseCodeUnique(warehouse.getWarehouseCode());
        if (StringUtils.isNotNull(info) && info.getWarehouseId().longValue() != warehouseId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public int insertWarehouse(ErpWarehouse warehouse)
    {
        if (StringUtils.isBlank(warehouse.getStatus())) warehouse.setStatus("0");
        if (StringUtils.isBlank(warehouse.getDelFlag())) warehouse.setDelFlag("0");
        return warehouseMapper.insertWarehouse(warehouse);
    }

    @Override
    public int updateWarehouse(ErpWarehouse warehouse) { return warehouseMapper.updateWarehouse(warehouse); }

    @Override
    public int deleteWarehouseByIds(Long[] warehouseIds) { return warehouseMapper.deleteWarehouseByIds(warehouseIds); }
}

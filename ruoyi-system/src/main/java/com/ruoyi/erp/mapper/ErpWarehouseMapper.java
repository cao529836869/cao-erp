package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpWarehouse;

public interface ErpWarehouseMapper
{
    public ErpWarehouse selectWarehouseById(Long warehouseId);
    public List<ErpWarehouse> selectWarehouseList(ErpWarehouse warehouse);
    public List<ErpWarehouse> selectWarehouseAll();
    public ErpWarehouse checkWarehouseCodeUnique(String warehouseCode);
    public int insertWarehouse(ErpWarehouse warehouse);
    public int updateWarehouse(ErpWarehouse warehouse);
    public int deleteWarehouseByIds(Long[] warehouseIds);
}

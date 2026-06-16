package com.ruoyi.erp.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.erp.domain.ErpInventory;

public interface ErpInventoryMapper
{
    public List<ErpInventory> selectInventoryList(ErpInventory inventory);
    public ErpInventory selectInventoryForUpdate(@Param("warehouseId") Long warehouseId, @Param("itemType") String itemType,
            @Param("itemId") Long itemId, @Param("batchNo") String batchNo);
    public int insertInventory(ErpInventory inventory);
    public int increaseAvailableQty(@Param("inventoryId") Long inventoryId, @Param("qty") BigDecimal qty,
            @Param("unitPrice") BigDecimal unitPrice);
    public int decreaseAvailableQty(@Param("inventoryId") Long inventoryId, @Param("qty") BigDecimal qty);
}

package com.ruoyi.erp.service;

import java.util.List;
import com.ruoyi.erp.domain.ErpInventory;

public interface IErpInventoryService
{
    public List<ErpInventory> selectInventoryList(ErpInventory inventory);
}

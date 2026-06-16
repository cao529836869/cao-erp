package com.ruoyi.erp.service;

import java.util.List;
import com.ruoyi.erp.domain.ErpInventoryTransaction;

public interface IErpInventoryTransactionService
{
    public List<ErpInventoryTransaction> selectTransactionList(ErpInventoryTransaction transaction);
}

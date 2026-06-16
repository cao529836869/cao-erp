package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpInventoryTransaction;

public interface ErpInventoryTransactionMapper
{
    public List<ErpInventoryTransaction> selectTransactionList(ErpInventoryTransaction transaction);
    public int insertTransaction(ErpInventoryTransaction transaction);
}

package com.ruoyi.erp.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.erp.domain.ErpInventoryTransaction;
import com.ruoyi.erp.mapper.ErpInventoryTransactionMapper;
import com.ruoyi.erp.service.IErpInventoryTransactionService;

@Service
public class ErpInventoryTransactionServiceImpl implements IErpInventoryTransactionService
{
    @Autowired
    private ErpInventoryTransactionMapper transactionMapper;

    @Override
    public List<ErpInventoryTransaction> selectTransactionList(ErpInventoryTransaction transaction)
    {
        return transactionMapper.selectTransactionList(transaction);
    }
}

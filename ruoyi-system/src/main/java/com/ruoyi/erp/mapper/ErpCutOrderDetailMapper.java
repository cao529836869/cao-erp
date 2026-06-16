package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpCutOrderDetail;

public interface ErpCutOrderDetailMapper
{
    public List<ErpCutOrderDetail> selectDetailByOrderId(Long cutOrderId);
    public int updateCutQty(ErpCutOrderDetail detail);
    public int batchInsertDetail(List<ErpCutOrderDetail> detailList);
    public int deleteDetailByOrderId(Long cutOrderId);
    public int deleteDetailByOrderIds(Long[] cutOrderIds);
}

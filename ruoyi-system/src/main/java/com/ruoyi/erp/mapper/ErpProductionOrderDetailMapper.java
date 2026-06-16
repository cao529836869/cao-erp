package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpProductionOrderDetail;

public interface ErpProductionOrderDetailMapper
{
    public List<ErpProductionOrderDetail> selectDetailByOrderId(Long productionOrderId);
    public int batchInsertDetail(List<ErpProductionOrderDetail> list);
    public int deleteDetailByOrderId(Long productionOrderId);
    public int deleteDetailByOrderIds(Long[] productionOrderIds);
}

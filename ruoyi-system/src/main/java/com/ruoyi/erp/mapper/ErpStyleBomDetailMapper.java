package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpMaterial;
import com.ruoyi.erp.domain.ErpMaterialSku;
import com.ruoyi.erp.domain.ErpStyleBomDetail;

/**
 * 款式BOM明细Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-31
 */
public interface ErpStyleBomDetailMapper
{
    public List<ErpStyleBomDetail> selectErpStyleBomDetailByBomId(Long bomId);

    public int batchInsertErpStyleBomDetail(List<ErpStyleBomDetail> list);

    public int updateMaterialInfoByMaterialId(ErpMaterial material);

    public int updateMaterialSkuInfoBySkuId(ErpMaterialSku materialSku);

    public int deleteErpStyleBomDetailByBomId(Long bomId);

    public int deleteErpStyleBomDetailByStyleId(Long styleId);

    public int deleteErpStyleBomDetailByStyleIds(Long[] styleIds);
}

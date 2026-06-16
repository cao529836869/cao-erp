package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpMaterialSku;

/**
 * 物料SKUMapper接口
 *
 * @author ruoyi
 * @date 2026-05-31
 */
public interface ErpMaterialSkuMapper
{
    public ErpMaterialSku selectMaterialSkuById(Long materialSkuId);

    public List<ErpMaterialSku> selectMaterialSkuList(ErpMaterialSku erpMaterialSku);

    public List<ErpMaterialSku> selectMaterialSkuByMaterialId(Long materialId);

    public int batchInsertMaterialSku(List<ErpMaterialSku> list);

    public int updateMaterialSku(ErpMaterialSku erpMaterialSku);

    public int deleteMaterialSkuByMaterialId(Long materialId);

    public int deleteMaterialSkuByMaterialIds(Long[] materialIds);

    public int deleteMaterialSkuBySkuIds(Long[] materialSkuIds);

    public int countBomDetailByMaterialIds(Long[] materialIds);

    public int countBomDetailByMaterialSkuIds(Long[] materialSkuIds);
}

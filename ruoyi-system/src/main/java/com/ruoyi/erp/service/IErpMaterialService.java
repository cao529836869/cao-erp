package com.ruoyi.erp.service;

import java.util.List;
import com.ruoyi.erp.domain.ErpMaterial;

/**
 * 物料主Service接口
 *
 * @author ruoyi
 * @date 2026-05-31
 */
public interface IErpMaterialService
{
    public ErpMaterial selectMaterialById(Long materialId);

    public List<ErpMaterial> selectMaterialList(ErpMaterial material);

    public List<ErpMaterial> selectMaterialAll();

    public boolean checkMaterialCodeUnique(ErpMaterial material);

    public int insertMaterial(ErpMaterial material);

    public int updateMaterial(ErpMaterial material);

    public int deleteMaterialByIds(Long[] materialIds);
}

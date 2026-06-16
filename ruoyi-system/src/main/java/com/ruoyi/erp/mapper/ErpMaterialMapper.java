package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpMaterial;

/**
 * 物料主Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-31
 */
public interface ErpMaterialMapper
{
    public ErpMaterial selectMaterialById(Long materialId);

    public List<ErpMaterial> selectMaterialList(ErpMaterial material);

    public List<ErpMaterial> selectMaterialAll();

    public ErpMaterial checkMaterialCodeUnique(String materialCode);

    public int insertMaterial(ErpMaterial material);

    public int updateMaterial(ErpMaterial material);

    public int deleteMaterialByIds(Long[] materialIds);
}

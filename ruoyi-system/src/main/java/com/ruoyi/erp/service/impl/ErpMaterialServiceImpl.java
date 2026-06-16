package com.ruoyi.erp.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.erp.domain.ErpMaterial;
import com.ruoyi.erp.domain.ErpMaterialSku;
import com.ruoyi.erp.mapper.ErpMaterialMapper;
import com.ruoyi.erp.mapper.ErpMaterialSkuMapper;
import com.ruoyi.erp.mapper.ErpStyleBomDetailMapper;
import com.ruoyi.erp.service.IErpMaterialCodeService;
import com.ruoyi.erp.service.IErpMaterialService;

/**
 * 物料主Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-31
 */
@Service
public class ErpMaterialServiceImpl implements IErpMaterialService
{
    @Autowired
    private ErpMaterialMapper materialMapper;

    @Autowired
    private ErpMaterialSkuMapper materialSkuMapper;

    @Autowired
    private ErpStyleBomDetailMapper styleBomDetailMapper;

    @Autowired
    private IErpMaterialCodeService materialCodeService;

    @Override
    public ErpMaterial selectMaterialById(Long materialId)
    {
        ErpMaterial material = materialMapper.selectMaterialById(materialId);
        if (material != null)
        {
            material.setSkuList(materialSkuMapper.selectMaterialSkuByMaterialId(materialId));
        }
        return material;
    }

    @Override
    public List<ErpMaterial> selectMaterialList(ErpMaterial material)
    {
        return materialMapper.selectMaterialList(material);
    }

    @Override
    public List<ErpMaterial> selectMaterialAll()
    {
        return materialMapper.selectMaterialAll();
    }

    @Override
    public boolean checkMaterialCodeUnique(ErpMaterial material)
    {
        Long materialId = StringUtils.isNull(material.getMaterialId()) ? -1L : material.getMaterialId();
        ErpMaterial info = materialMapper.checkMaterialCodeUnique(material.getMaterialCode());
        if (StringUtils.isNotNull(info) && info.getMaterialId().longValue() != materialId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMaterial(ErpMaterial material)
    {
        fillDefaultValues(material);
        if (StringUtils.isBlank(material.getMaterialCode()))
        {
            material.setMaterialCode(materialCodeService.generateMaterialCode(material.getMaterialType(), material.getCategoryName()));
        }
        int rows = materialMapper.insertMaterial(material);
        insertSkuData(material, true);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMaterial(ErpMaterial material)
    {
        fillDefaultValues(material);
        int rows = materialMapper.updateMaterial(material);
        styleBomDetailMapper.updateMaterialInfoByMaterialId(material);
        syncSkuData(material);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMaterialByIds(Long[] materialIds)
    {
        if (materialSkuMapper.countBomDetailByMaterialIds(materialIds) > 0)
        {
            throw new ServiceException("物料已被款式BOM引用，不能删除");
        }
        materialSkuMapper.deleteMaterialSkuByMaterialIds(materialIds);
        return materialMapper.deleteMaterialByIds(materialIds);
    }

    private void fillDefaultValues(ErpMaterial material)
    {
        if (StringUtils.isBlank(material.getStatus()))
        {
            material.setStatus("0");
        }
        if (StringUtils.isBlank(material.getDelFlag()))
        {
            material.setDelFlag("0");
        }
    }

    private void insertSkuData(ErpMaterial material, boolean insert)
    {
        List<ErpMaterialSku> list = material.getSkuList();
        if (list == null || list.isEmpty())
        {
            return;
        }
        Date now = DateUtils.getNowDate();
        List<ErpMaterialSku> insertList = new ArrayList<ErpMaterialSku>();
        for (ErpMaterialSku sku : list)
        {
            if (sku == null || StringUtils.isBlank(sku.getUnitName()))
            {
                continue;
            }
            sku.setMaterialSkuId(null);
            prepareSku(material, sku);
            if (insert)
            {
                sku.setCreateBy(material.getCreateBy());
                sku.setCreateTime(now);
            }
            else
            {
                sku.setCreateBy(material.getCreateBy());
                sku.setCreateTime(now);
                sku.setUpdateBy(material.getUpdateBy());
                sku.setUpdateTime(now);
            }
            insertList.add(sku);
        }
        if (!insertList.isEmpty())
        {
            materialSkuMapper.batchInsertMaterialSku(insertList);
        }
    }

    private void syncSkuData(ErpMaterial material)
    {
        Date now = DateUtils.getNowDate();
        List<ErpMaterialSku> oldList = materialSkuMapper.selectMaterialSkuByMaterialId(material.getMaterialId());
        Set<Long> oldSkuIds = new HashSet<Long>();
        for (ErpMaterialSku oldSku : oldList)
        {
            oldSkuIds.add(oldSku.getMaterialSkuId());
        }

        List<ErpMaterialSku> newList = material.getSkuList();
        Set<Long> submitSkuIds = new HashSet<Long>();
        List<ErpMaterialSku> insertList = new ArrayList<ErpMaterialSku>();
        if (newList != null)
        {
            for (ErpMaterialSku sku : newList)
            {
                if (sku == null || StringUtils.isBlank(sku.getUnitName()))
                {
                    continue;
                }
                prepareSku(material, sku);
                sku.setUpdateBy(material.getUpdateBy());
                sku.setUpdateTime(now);
                if (sku.getMaterialSkuId() != null && oldSkuIds.contains(sku.getMaterialSkuId()))
                {
                    submitSkuIds.add(sku.getMaterialSkuId());
                    materialSkuMapper.updateMaterialSku(sku);
                    styleBomDetailMapper.updateMaterialSkuInfoBySkuId(sku);
                }
                else
                {
                    sku.setMaterialSkuId(null);
                    sku.setCreateBy(material.getUpdateBy());
                    sku.setCreateTime(now);
                    insertList.add(sku);
                }
            }
        }

        List<Long> deleteIds = new ArrayList<Long>();
        for (Long oldSkuId : oldSkuIds)
        {
            if (!submitSkuIds.contains(oldSkuId))
            {
                deleteIds.add(oldSkuId);
            }
        }
        if (!deleteIds.isEmpty())
        {
            Long[] deleteIdArray = deleteIds.toArray(new Long[deleteIds.size()]);
            if (materialSkuMapper.countBomDetailByMaterialSkuIds(deleteIdArray) > 0)
            {
                throw new ServiceException("物料SKU已被款式BOM引用，不能删除");
            }
            materialSkuMapper.deleteMaterialSkuBySkuIds(deleteIdArray);
        }
        if (!insertList.isEmpty())
        {
            materialSkuMapper.batchInsertMaterialSku(insertList);
        }
    }

    private void prepareSku(ErpMaterial material, ErpMaterialSku sku)
    {
        sku.setMaterialId(material.getMaterialId());
        sku.setMaterialCode(material.getMaterialCode());
        if (StringUtils.isBlank(sku.getMaterialSkuCode()))
        {
            sku.setMaterialSkuCode(buildMaterialSkuCode(material, sku));
        }
        if (StringUtils.isBlank(sku.getStatus()))
        {
            sku.setStatus("0");
        }
        if (StringUtils.isBlank(sku.getDelFlag()))
        {
            sku.setDelFlag("0");
        }
    }

    private String buildMaterialSkuCode(ErpMaterial material, ErpMaterialSku sku)
    {
        List<String> parts = new ArrayList<String>();
        parts.add(material.getMaterialCode());
        if (StringUtils.isNotBlank(sku.getColorCode()))
        {
            parts.add(sku.getColorCode());
        }
        else if (StringUtils.isNotBlank(sku.getColorName()))
        {
            parts.add(sku.getColorName());
        }
        if (StringUtils.isNotBlank(sku.getSpecName()))
        {
            parts.add(sku.getSpecName());
        }
        if (parts.size() == 1)
        {
            parts.add("STD");
        }
        return StringUtils.join(parts, "-");
    }
}

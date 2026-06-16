package com.ruoyi.erp.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.erp.domain.ErpStyleBom;
import com.ruoyi.erp.domain.ErpStyleBomDetail;
import com.ruoyi.erp.domain.ErpStyleSku;
import com.ruoyi.erp.mapper.ErpStyleBomDetailMapper;
import com.ruoyi.erp.mapper.ErpStyleBomMapper;
import com.ruoyi.erp.mapper.ErpStyleMapper;
import com.ruoyi.erp.mapper.ErpStyleSkuMapper;
import com.ruoyi.erp.domain.ErpStyle;
import com.ruoyi.erp.service.IErpStyleService;

/**
 * 童装款式主Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
@Service
public class ErpStyleServiceImpl implements IErpStyleService 
{
    @Autowired
    private ErpStyleMapper erpStyleMapper;

    @Autowired
    private ErpStyleSkuMapper erpStyleSkuMapper;

    @Autowired
    private ErpStyleBomMapper erpStyleBomMapper;

    @Autowired
    private ErpStyleBomDetailMapper erpStyleBomDetailMapper;

    /**
     * 查询童装款式主
     * 
     * @param styleId 童装款式主主键
     * @return 童装款式主
     */
    @Override
    public ErpStyle selectErpStyleByStyleId(Long styleId)
    {
        ErpStyle erpStyle = erpStyleMapper.selectErpStyleByStyleId(styleId);
        if (erpStyle != null)
        {
            erpStyle.setSkuList(erpStyleSkuMapper.selectErpStyleSkuByStyleId(styleId));
            List<ErpStyleBom> bomList = erpStyleBomMapper.selectErpStyleBomByStyleId(styleId);
            for (ErpStyleBom bom : bomList)
            {
                bom.setDetailList(erpStyleBomDetailMapper.selectErpStyleBomDetailByBomId(bom.getBomId()));
            }
            erpStyle.setBomList(bomList);
        }
        return erpStyle;
    }

    /**
     * 查询童装款式主列表
     * 
     * @param erpStyle 童装款式主
     * @return 童装款式主
     */
    @Override
    public List<ErpStyle> selectErpStyleList(ErpStyle erpStyle)
    {
        return erpStyleMapper.selectErpStyleList(erpStyle);
    }

    /**
     * 新增童装款式主
     * 
     * @param erpStyle 童装款式主
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertErpStyle(ErpStyle erpStyle)
    {
        fillDefaultStatus(erpStyle);
        erpStyle.setCreateTime(DateUtils.getNowDate());
        int rows = erpStyleMapper.insertErpStyle(erpStyle);
        insertChildData(erpStyle, true);
        return rows;
    }

    /**
     * 修改童装款式主
     * 
     * @param erpStyle 童装款式主
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateErpStyle(ErpStyle erpStyle)
    {
        fillDefaultStatus(erpStyle);
        erpStyle.setUpdateTime(DateUtils.getNowDate());
        int rows = erpStyleMapper.updateErpStyle(erpStyle);
        deleteChildDataByStyleId(erpStyle.getStyleId());
        insertChildData(erpStyle, false);
        return rows;
    }

    /**
     * 批量删除童装款式主
     * 
     * @param styleIds 需要删除的童装款式主主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteErpStyleByStyleIds(Long[] styleIds)
    {
        erpStyleBomDetailMapper.deleteErpStyleBomDetailByStyleIds(styleIds);
        erpStyleBomMapper.deleteErpStyleBomByStyleIds(styleIds);
        erpStyleSkuMapper.deleteErpStyleSkuByStyleIds(styleIds);
        return erpStyleMapper.deleteErpStyleByStyleIds(styleIds);
    }

    /**
     * 删除童装款式主信息
     * 
     * @param styleId 童装款式主主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteErpStyleByStyleId(Long styleId)
    {
        deleteChildDataByStyleId(styleId);
        return erpStyleMapper.deleteErpStyleByStyleId(styleId);
    }

    private void fillDefaultStatus(ErpStyle erpStyle)
    {
        if (StringUtils.isBlank(erpStyle.getStatus()))
        {
            erpStyle.setStatus("0");
        }
        if (StringUtils.isBlank(erpStyle.getSampleStatus()))
        {
            erpStyle.setSampleStatus("0");
        }
        if (StringUtils.isBlank(erpStyle.getProductionStatus()))
        {
            erpStyle.setProductionStatus("0");
        }
    }

    private void deleteChildDataByStyleId(Long styleId)
    {
        erpStyleBomDetailMapper.deleteErpStyleBomDetailByStyleId(styleId);
        erpStyleBomMapper.deleteErpStyleBomByStyleId(styleId);
        erpStyleSkuMapper.deleteErpStyleSkuByStyleId(styleId);
    }

    private void insertChildData(ErpStyle erpStyle, boolean insert)
    {
        Date now = DateUtils.getNowDate();
        insertSkuData(erpStyle, now, insert);
        insertBomData(erpStyle, now, insert);
    }

    private void insertSkuData(ErpStyle erpStyle, Date now, boolean insert)
    {
        List<ErpStyleSku> list = erpStyle.getSkuList();
        if (list == null || list.isEmpty())
        {
            return;
        }
        List<ErpStyleSku> insertList = new ArrayList<ErpStyleSku>();
        for (ErpStyleSku sku : list)
        {
            if (sku == null || StringUtils.isBlank(sku.getColorName()) || StringUtils.isBlank(sku.getSizeCode()) || StringUtils.isBlank(sku.getSizeName()))
            {
                continue;
            }
            sku.setSkuId(null);
            sku.setStyleId(erpStyle.getStyleId());
            sku.setStyleNo(erpStyle.getStyleNo());
            if (StringUtils.isBlank(sku.getSkuCode()))
            {
                sku.setSkuCode(buildSkuCode(erpStyle.getStyleNo(), sku));
            }
            if (StringUtils.isBlank(sku.getStatus()))
            {
                sku.setStatus("0");
            }
            if (StringUtils.isBlank(sku.getDelFlag()))
            {
                sku.setDelFlag("0");
            }
            if (insert)
            {
                sku.setCreateBy(erpStyle.getCreateBy());
                sku.setCreateTime(now);
            }
            else
            {
                sku.setCreateBy(erpStyle.getCreateBy());
                sku.setCreateTime(now);
                sku.setUpdateBy(erpStyle.getUpdateBy());
                sku.setUpdateTime(now);
            }
            insertList.add(sku);
        }
        if (!insertList.isEmpty())
        {
            erpStyleSkuMapper.batchInsertErpStyleSku(insertList);
        }
    }

    private String buildSkuCode(String styleNo, ErpStyleSku sku)
    {
        String color = StringUtils.defaultIfBlank(sku.getColorCode(), sku.getColorName());
        return StringUtils.defaultString(styleNo) + "-" + StringUtils.defaultString(color) + "-" + StringUtils.defaultString(sku.getSizeCode());
    }

    private void insertBomData(ErpStyle erpStyle, Date now, boolean insert)
    {
        List<ErpStyleBom> bomList = erpStyle.getBomList();
        if (bomList == null || bomList.isEmpty())
        {
            return;
        }
        for (ErpStyleBom bom : bomList)
        {
            if (bom == null)
            {
                continue;
            }
            if (StringUtils.isBlank(bom.getVersionNo()))
            {
                bom.setVersionNo("V1");
            }
            if (StringUtils.isBlank(bom.getBomNo()))
            {
                bom.setBomNo(erpStyle.getStyleNo() + "-BOM-" + bom.getVersionNo());
            }
            if (StringUtils.isBlank(bom.getBomStatus()))
            {
                bom.setBomStatus("0");
            }
            bom.setBomId(null);
            bom.setStyleId(erpStyle.getStyleId());
            bom.setStyleNo(erpStyle.getStyleNo());
            if (insert)
            {
                bom.setCreateBy(erpStyle.getCreateBy());
                bom.setCreateTime(now);
            }
            else
            {
                bom.setCreateBy(erpStyle.getCreateBy());
                bom.setCreateTime(now);
                bom.setUpdateBy(erpStyle.getUpdateBy());
                bom.setUpdateTime(now);
            }
            erpStyleBomMapper.insertErpStyleBom(bom);
            insertBomDetailData(bom, erpStyle, now, insert);
        }
    }

    private void insertBomDetailData(ErpStyleBom bom, ErpStyle erpStyle, Date now, boolean insert)
    {
        List<ErpStyleBomDetail> detailList = bom.getDetailList();
        if (detailList == null || detailList.isEmpty())
        {
            return;
        }
        List<ErpStyleBomDetail> insertList = new ArrayList<ErpStyleBomDetail>();
        for (ErpStyleBomDetail detail : detailList)
        {
            if (detail == null || StringUtils.isBlank(detail.getMaterialCode()) || StringUtils.isBlank(detail.getMaterialName())
                    || StringUtils.isBlank(detail.getMaterialType()) || StringUtils.isBlank(detail.getUnitName()))
            {
                continue;
            }
            detail.setBomDetailId(null);
            detail.setBomId(bom.getBomId());
            if (detail.getMaterialId() == null)
            {
                detail.setMaterialId(0L);
            }
            if (insert)
            {
                detail.setCreateBy(erpStyle.getCreateBy());
                detail.setCreateTime(now);
            }
            else
            {
                detail.setCreateBy(erpStyle.getCreateBy());
                detail.setCreateTime(now);
                detail.setUpdateBy(erpStyle.getUpdateBy());
                detail.setUpdateTime(now);
            }
            insertList.add(detail);
        }
        if (!insertList.isEmpty())
        {
            erpStyleBomDetailMapper.batchInsertErpStyleBomDetail(insertList);
        }
    }
}

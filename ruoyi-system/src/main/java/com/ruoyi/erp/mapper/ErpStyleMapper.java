package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpStyle;

/**
 * 童装款式主Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
public interface ErpStyleMapper 
{
    /**
     * 查询童装款式主
     * 
     * @param styleId 童装款式主主键
     * @return 童装款式主
     */
    public ErpStyle selectErpStyleByStyleId(Long styleId);

    /**
     * 查询童装款式主列表
     * 
     * @param erpStyle 童装款式主
     * @return 童装款式主集合
     */
    public List<ErpStyle> selectErpStyleList(ErpStyle erpStyle);

    /**
     * 新增童装款式主
     * 
     * @param erpStyle 童装款式主
     * @return 结果
     */
    public int insertErpStyle(ErpStyle erpStyle);

    /**
     * 修改童装款式主
     * 
     * @param erpStyle 童装款式主
     * @return 结果
     */
    public int updateErpStyle(ErpStyle erpStyle);

    /**
     * 删除童装款式主
     * 
     * @param styleId 童装款式主主键
     * @return 结果
     */
    public int deleteErpStyleByStyleId(Long styleId);

    /**
     * 批量删除童装款式主
     * 
     * @param styleIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteErpStyleByStyleIds(Long[] styleIds);
}

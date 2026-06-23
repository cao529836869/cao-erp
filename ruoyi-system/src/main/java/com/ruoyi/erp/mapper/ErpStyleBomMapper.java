package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpStyleBom;

/**
 * 款式BOM主Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-31
 */
public interface ErpStyleBomMapper
{
    public List<ErpStyleBom> selectErpStyleBomByStyleId(Long styleId);

    public List<ErpStyleBom> selectErpStyleBomByStyleNo(String styleNo);

    public int insertErpStyleBom(ErpStyleBom erpStyleBom);

    public int deleteErpStyleBomByStyleId(Long styleId);

    public int deleteErpStyleBomByStyleIds(Long[] styleIds);
}

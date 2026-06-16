package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpStyleSku;

/**
 * 童装款式SKUMapper接口
 *
 * @author ruoyi
 * @date 2026-05-31
 */
public interface ErpStyleSkuMapper
{
    public List<ErpStyleSku> selectErpStyleSkuList(ErpStyleSku erpStyleSku);

    public List<ErpStyleSku> selectErpStyleSkuByStyleId(Long styleId);

    public int batchInsertErpStyleSku(List<ErpStyleSku> list);

    public int deleteErpStyleSkuByStyleId(Long styleId);

    public int deleteErpStyleSkuByStyleIds(Long[] styleIds);
}

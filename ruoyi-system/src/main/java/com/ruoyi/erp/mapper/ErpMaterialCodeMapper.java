package com.ruoyi.erp.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 物料编码生成Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-31
 */
public interface ErpMaterialCodeMapper
{
    public String selectMaxMaterialCodeByPrefix(@Param("prefix") String prefix);
}

package com.ruoyi.erp.service;

/**
 * 物料编码生成Service接口
 *
 * @author ruoyi
 * @date 2026-05-31
 */
public interface IErpMaterialCodeService
{
    public String generateMaterialCode(String materialType, String categoryName);
}

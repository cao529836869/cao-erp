package com.ruoyi.web.controller.erp;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.erp.domain.ErpMaterial;
import com.ruoyi.erp.domain.ErpMaterialSku;
import com.ruoyi.erp.mapper.ErpMaterialSkuMapper;
import com.ruoyi.erp.service.IErpMaterialCodeService;
import com.ruoyi.erp.service.IErpMaterialService;

/**
 * 物料档案Controller
 *
 * @author ruoyi
 * @date 2026-05-31
 */
@RestController
@RequestMapping("/erp/material")
public class ErpMaterialController extends BaseController
{
    @Autowired
    private IErpMaterialService materialService;

    @Autowired
    private IErpMaterialCodeService materialCodeService;

    @Autowired
    private ErpMaterialSkuMapper materialSkuMapper;

    /**
     * 查询物料档案列表
     */
    @PreAuthorize("@ss.hasPermi('erp:material:list')")
    @GetMapping("/list")
    public TableDataInfo list(ErpMaterial material)
    {
        startPage();
        List<ErpMaterial> list = materialService.selectMaterialList(material);
        return getDataTable(list);
    }

    /**
     * 导出物料档案列表
     */
    @PreAuthorize("@ss.hasPermi('erp:material:export')")
    @Log(title = "物料档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ErpMaterial material)
    {
        List<ErpMaterial> list = materialService.selectMaterialList(material);
        ExcelUtil<ErpMaterial> util = new ExcelUtil<ErpMaterial>(ErpMaterial.class);
        util.exportExcel(response, list, "物料档案数据");
    }

    /**
     * 获取物料档案详细信息
     */
    @PreAuthorize("@ss.hasPermi('erp:material:query') or @ss.hasPermi('erp:style:add') or @ss.hasPermi('erp:style:edit')")
    @GetMapping(value = "/{materialId}")
    public AjaxResult getInfo(@PathVariable Long materialId)
    {
        return success(materialService.selectMaterialById(materialId));
    }

    /**
     * 新增物料档案
     */
    @PreAuthorize("@ss.hasPermi('erp:material:add')")
    @Log(title = "物料档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ErpMaterial material)
    {
        if (StringUtils.isBlank(material.getMaterialCode()))
        {
            material.setMaterialCode(materialCodeService.generateMaterialCode(material.getMaterialType(), material.getCategoryName()));
        }
        if (!materialService.checkMaterialCodeUnique(material))
        {
            return error("新增物料'" + material.getMaterialName() + "'失败，物料编码已存在");
        }
        material.setCreateBy(getUsername());
        return toAjax(materialService.insertMaterial(material));
    }

    /**
     * 修改物料档案
     */
    @PreAuthorize("@ss.hasPermi('erp:material:edit')")
    @Log(title = "物料档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ErpMaterial material)
    {
        if (!materialService.checkMaterialCodeUnique(material))
        {
            return error("修改物料'" + material.getMaterialName() + "'失败，物料编码已存在");
        }
        material.setUpdateBy(getUsername());
        return toAjax(materialService.updateMaterial(material));
    }

    /**
     * 删除物料档案
     */
    @PreAuthorize("@ss.hasPermi('erp:material:remove')")
    @Log(title = "物料档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{materialIds}")
    public AjaxResult remove(@PathVariable Long[] materialIds)
    {
        return toAjax(materialService.deleteMaterialByIds(materialIds));
    }

    /**
     * 获取物料选择列表
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        return success(materialService.selectMaterialAll());
    }

    /**
     * 查询可选物料SKU列表
     */
    @PreAuthorize("@ss.hasPermi('erp:material:list') or @ss.hasPermi('erp:inbound:add') or @ss.hasPermi('erp:outbound:add') or @ss.hasPermi('erp:inbound:edit') or @ss.hasPermi('erp:outbound:edit')")
    @GetMapping("/sku/list")
    public TableDataInfo skuList(ErpMaterialSku materialSku)
    {
        startPage();
        List<ErpMaterialSku> list = materialSkuMapper.selectMaterialSkuList(materialSku);
        return getDataTable(list);
    }

    /**
     * 生成物料编码
     */
    @PreAuthorize("@ss.hasPermi('erp:material:code') or @ss.hasPermi('erp:material:add') or @ss.hasPermi('erp:material:edit') or @ss.hasPermi('erp:style:add') or @ss.hasPermi('erp:style:edit')")
    @GetMapping("/generateCode")
    public AjaxResult generateCode(@RequestParam(required = false) String materialType,
                                   @RequestParam(required = false) String categoryName)
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("materialCode", materialCodeService.generateMaterialCode(materialType, categoryName));
        return ajax;
    }
}

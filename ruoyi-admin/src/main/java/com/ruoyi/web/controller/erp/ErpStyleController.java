package com.ruoyi.web.controller.erp;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.erp.domain.ErpStyle;
import com.ruoyi.erp.domain.ErpStyleSku;
import com.ruoyi.erp.mapper.ErpStyleSkuMapper;
import com.ruoyi.erp.service.IErpStyleService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 童装款式主Controller
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
@RestController
@RequestMapping("/erp/style")
public class ErpStyleController extends BaseController
{
    @Autowired
    private IErpStyleService erpStyleService;

    @Autowired
    private ErpStyleSkuMapper erpStyleSkuMapper;

    /**
     * 查询童装款式主列表
     */
    @PreAuthorize("@ss.hasPermi('erp:style:list')")
    @GetMapping("/list")
    public TableDataInfo list(ErpStyle erpStyle)
    {
        startPage();
        List<ErpStyle> list = erpStyleService.selectErpStyleList(erpStyle);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('erp:style:list')")
    @GetMapping("/sku/list")
    public TableDataInfo skuList(ErpStyleSku erpStyleSku)
    {
        startPage();
        return getDataTable(erpStyleSkuMapper.selectErpStyleSkuList(erpStyleSku));
    }

    /**
     * 导出童装款式主列表
     */
    @PreAuthorize("@ss.hasPermi('erp:style:export')")
    @Log(title = "童装款式主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ErpStyle erpStyle)
    {
        List<ErpStyle> list = erpStyleService.selectErpStyleList(erpStyle);
        ExcelUtil<ErpStyle> util = new ExcelUtil<ErpStyle>(ErpStyle.class);
        util.exportExcel(response, list, "童装款式主数据");
    }

    /**
     * 获取童装款式主详细信息
     */
    @PreAuthorize("@ss.hasPermi('erp:style:query')")
    @GetMapping(value = "/{styleId}")
    public AjaxResult getInfo(@PathVariable("styleId") Long styleId)
    {
        return success(erpStyleService.selectErpStyleByStyleId(styleId));
    }

    /**
     * 新增童装款式主
     */
    @PreAuthorize("@ss.hasPermi('erp:style:add')")
    @Log(title = "童装款式主", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ErpStyle erpStyle)
    {
        return toAjax(erpStyleService.insertErpStyle(erpStyle));
    }

    /**
     * 修改童装款式主
     */
    @PreAuthorize("@ss.hasPermi('erp:style:edit')")
    @Log(title = "童装款式主", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ErpStyle erpStyle)
    {
        return toAjax(erpStyleService.updateErpStyle(erpStyle));
    }

    /**
     * 删除童装款式主
     */
    @PreAuthorize("@ss.hasPermi('erp:style:remove')")
    @Log(title = "童装款式主", businessType = BusinessType.DELETE)
	@DeleteMapping("/{styleIds}")
    public AjaxResult remove(@PathVariable Long[] styleIds)
    {
        return toAjax(erpStyleService.deleteErpStyleByStyleIds(styleIds));
    }
}

package com.ruoyi.web.controller.erp;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.erp.domain.ErpCustomer;
import com.ruoyi.erp.service.IErpCustomerService;

/**
 * 客户档案Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/erp/customer")
public class ErpCustomerController extends BaseController
{
    @Autowired
    private IErpCustomerService customerService;

    /**
     * 查询客户档案列表
     */
    @PreAuthorize("@ss.hasPermi('erp:customer:list')")
    @GetMapping("/list")
    public TableDataInfo list(ErpCustomer customer)
    {
        startPage();
        List<ErpCustomer> list = customerService.selectCustomerList(customer);
        return getDataTable(list);
    }

    /**
     * 导出客户档案列表
     */
    @PreAuthorize("@ss.hasPermi('erp:customer:export')")
    @Log(title = "客户档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ErpCustomer customer)
    {
        List<ErpCustomer> list = customerService.selectCustomerList(customer);
        ExcelUtil<ErpCustomer> util = new ExcelUtil<ErpCustomer>(ErpCustomer.class);
        util.exportExcel(response, list, "客户档案数据");
    }

    /**
     * 获取客户档案详细信息
     */
    @PreAuthorize("@ss.hasPermi('erp:customer:query')")
    @GetMapping(value = "/{customerId}")
    public AjaxResult getInfo(@PathVariable Long customerId)
    {
        return success(customerService.selectCustomerById(customerId));
    }

    /**
     * 新增客户档案
     */
    @PreAuthorize("@ss.hasPermi('erp:customer:add')")
    @Log(title = "客户档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ErpCustomer customer)
    {
        if (!customerService.checkCustomerCodeUnique(customer))
        {
            return error("新增客户'" + customer.getCustomerName() + "'失败，客户编码已存在");
        }
        customer.setCreateBy(getUsername());
        return toAjax(customerService.insertCustomer(customer));
    }

    /**
     * 修改客户档案
     */
    @PreAuthorize("@ss.hasPermi('erp:customer:edit')")
    @Log(title = "客户档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ErpCustomer customer)
    {
        if (!customerService.checkCustomerCodeUnique(customer))
        {
            return error("修改客户'" + customer.getCustomerName() + "'失败，客户编码已存在");
        }
        customer.setUpdateBy(getUsername());
        return toAjax(customerService.updateCustomer(customer));
    }

    /**
     * 删除客户档案
     */
    @PreAuthorize("@ss.hasPermi('erp:customer:remove')")
    @Log(title = "客户档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{customerIds}")
    public AjaxResult remove(@PathVariable Long[] customerIds)
    {
        return toAjax(customerService.deleteCustomerByIds(customerIds));
    }

    /**
     * 获取客户选择列表
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        List<ErpCustomer> customers = customerService.selectCustomerAll();
        return success(customers);
    }
}

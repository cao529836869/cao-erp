package com.ruoyi.erp.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.erp.domain.ErpCustomer;
import com.ruoyi.erp.mapper.ErpCustomerMapper;
import com.ruoyi.erp.service.IErpCustomerService;

/**
 * 客户档案Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class ErpCustomerServiceImpl implements IErpCustomerService
{
    @Autowired
    private ErpCustomerMapper customerMapper;

    /**
     * 查询客户档案列表
     *
     * @param customer 客户档案
     * @return 客户档案集合
     */
    @Override
    public List<ErpCustomer> selectCustomerList(ErpCustomer customer)
    {
        return customerMapper.selectCustomerList(customer);
    }

    /**
     * 查询全部正常客户档案
     *
     * @return 客户档案集合
     */
    @Override
    public List<ErpCustomer> selectCustomerAll()
    {
        return customerMapper.selectCustomerAll();
    }

    /**
     * 查询客户档案
     *
     * @param customerId 客户ID
     * @return 客户档案
     */
    @Override
    public ErpCustomer selectCustomerById(Long customerId)
    {
        return customerMapper.selectCustomerById(customerId);
    }

    /**
     * 新增客户档案
     *
     * @param customer 客户档案
     * @return 结果
     */
    @Override
    public int insertCustomer(ErpCustomer customer)
    {
        return customerMapper.insertCustomer(customer);
    }

    /**
     * 修改客户档案
     *
     * @param customer 客户档案
     * @return 结果
     */
    @Override
    public int updateCustomer(ErpCustomer customer)
    {
        return customerMapper.updateCustomer(customer);
    }

    /**
     * 批量删除客户档案
     *
     * @param customerIds 需要删除的客户ID
     * @return 结果
     */
    @Override
    public int deleteCustomerByIds(Long[] customerIds)
    {
        return customerMapper.deleteCustomerByIds(customerIds);
    }

    /**
     * 校验客户编码是否唯一
     *
     * @param customer 客户档案
     * @return 结果
     */
    @Override
    public boolean checkCustomerCodeUnique(ErpCustomer customer)
    {
        Long customerId = StringUtils.isNull(customer.getCustomerId()) ? -1L : customer.getCustomerId();
        ErpCustomer info = customerMapper.checkCustomerCodeUnique(customer.getCustomerCode());
        if (StringUtils.isNotNull(info) && info.getCustomerId().longValue() != customerId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}

package com.ruoyi.erp.mapper;

import java.util.List;
import com.ruoyi.erp.domain.ErpCustomer;

/**
 * 客户档案Mapper接口
 *
 * @author ruoyi
 */
public interface ErpCustomerMapper
{
    /**
     * 查询客户档案列表
     *
     * @param customer 客户档案
     * @return 客户档案集合
     */
    public List<ErpCustomer> selectCustomerList(ErpCustomer customer);

    /**
     * 查询全部正常客户档案
     *
     * @return 客户档案集合
     */
    public List<ErpCustomer> selectCustomerAll();

    /**
     * 查询客户档案
     *
     * @param customerId 客户ID
     * @return 客户档案
     */
    public ErpCustomer selectCustomerById(Long customerId);

    /**
     * 新增客户档案
     *
     * @param customer 客户档案
     * @return 结果
     */
    public int insertCustomer(ErpCustomer customer);

    /**
     * 修改客户档案
     *
     * @param customer 客户档案
     * @return 结果
     */
    public int updateCustomer(ErpCustomer customer);

    /**
     * 批量删除客户档案
     *
     * @param customerIds 需要删除的客户ID
     * @return 结果
     */
    public int deleteCustomerByIds(Long[] customerIds);

    /**
     * 校验客户编码是否唯一
     *
     * @param customerCode 客户编码
     * @return 客户档案
     */
    public ErpCustomer checkCustomerCodeUnique(String customerCode);
}

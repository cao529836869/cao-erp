import request from '@/utils/request'

// 查询客户档案列表
export function listCustomer(query) {
  return request({
    url: '/erp/customer/list',
    method: 'get',
    params: query
  })
}

// 查询客户档案详细
export function getCustomer(customerId) {
  return request({
    url: '/erp/customer/' + customerId,
    method: 'get'
  })
}

// 新增客户档案
export function addCustomer(data) {
  return request({
    url: '/erp/customer',
    method: 'post',
    data: data
  })
}

// 修改客户档案
export function updateCustomer(data) {
  return request({
    url: '/erp/customer',
    method: 'put',
    data: data
  })
}

// 删除客户档案
export function delCustomer(customerId) {
  return request({
    url: '/erp/customer/' + customerId,
    method: 'delete'
  })
}

// 查询客户档案下拉列表
export function optionselectCustomer() {
  return request({
    url: '/erp/customer/optionselect',
    method: 'get'
  })
}

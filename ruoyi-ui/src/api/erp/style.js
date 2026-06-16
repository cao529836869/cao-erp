import request from '@/utils/request'

// 查询童装款式主列表
export function listStyle(query) {
  return request({
    url: '/erp/style/list',
    method: 'get',
    params: query
  })
}

// 查询童装款式主详细
export function getStyle(styleId) {
  return request({
    url: '/erp/style/' + styleId,
    method: 'get'
  })
}

// 新增童装款式主
export function addStyle(data) {
  return request({
    url: '/erp/style',
    method: 'post',
    data: data
  })
}

// 修改童装款式主
export function updateStyle(data) {
  return request({
    url: '/erp/style',
    method: 'put',
    data: data
  })
}

// 删除童装款式主
export function delStyle(styleId) {
  return request({
    url: '/erp/style/' + styleId,
    method: 'delete'
  })
}

export function listStyleSku(query) {
  return request({
    url: '/erp/style/sku/list',
    method: 'get',
    params: query
  })
}

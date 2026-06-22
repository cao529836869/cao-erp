import request from '@/utils/request'

export function listSales(query) {
  return request({ url: '/erp/sales/list', method: 'get', params: query })
}

export function getSales(salesOrderId) {
  return request({ url: '/erp/sales/' + salesOrderId, method: 'get' })
}

export function addSales(data) {
  return request({ url: '/erp/sales', method: 'post', data })
}

export function updateSales(data) {
  return request({ url: '/erp/sales', method: 'put', data })
}

export function delSales(salesOrderId) {
  return request({ url: '/erp/sales/' + salesOrderId, method: 'delete' })
}

export function generateProduction(salesOrderId) {
  return request({ url: '/erp/sales/production/' + salesOrderId, method: 'post' })
}

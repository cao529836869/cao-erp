import request from '@/utils/request'

export function listProduction(query) {
  return request({ url: '/erp/production/list', method: 'get', params: query })
}

export function getProduction(productionOrderId) {
  return request({ url: '/erp/production/' + productionOrderId, method: 'get' })
}

export function addProduction(data) {
  return request({ url: '/erp/production', method: 'post', data })
}

export function updateProduction(data) {
  return request({ url: '/erp/production', method: 'put', data })
}

export function delProduction(productionOrderId) {
  return request({ url: '/erp/production/' + productionOrderId, method: 'delete' })
}

export function releaseProduction(productionOrderId) {
  return request({ url: '/erp/production/release/' + productionOrderId, method: 'put' })
}

export function closeProduction(productionOrderId) {
  return request({ url: '/erp/production/close/' + productionOrderId, method: 'put' })
}

export function buildProductionPicking(productionOrderId) {
  return request({ url: '/erp/production/picking/' + productionOrderId, method: 'put' })
}

export function buildProductionCut(productionOrderId) {
  return request({ url: '/erp/production/cut/' + productionOrderId, method: 'put' })
}

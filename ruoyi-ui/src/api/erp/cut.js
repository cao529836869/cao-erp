import request from '@/utils/request'

export function listCut(query) {
  return request({ url: '/erp/cut/list', method: 'get', params: query })
}

export function getCut(cutOrderId) {
  return request({ url: '/erp/cut/' + cutOrderId, method: 'get' })
}

export function getCutByProduction(productionOrderId) {
  return request({ url: '/erp/cut/production/' + productionOrderId, method: 'get' })
}

export function addCut(data) {
  return request({ url: '/erp/cut', method: 'post', data })
}

export function updateCut(data) {
  return request({ url: '/erp/cut', method: 'put', data })
}

export function delCut(cutOrderId) {
  return request({ url: '/erp/cut/' + cutOrderId, method: 'delete' })
}

export function finishCut(cutOrderId, data) {
  return request({ url: '/erp/cut/finish/' + cutOrderId, method: 'put', data })
}

export function cancelCut(cutOrderId) {
  return request({ url: '/erp/cut/cancel/' + cutOrderId, method: 'put' })
}

export function delayCut(cutOrderId, data) {
  return request({ url: '/erp/cut/delay/' + cutOrderId, method: 'put', data })
}

export function createCutFromProduction(productionOrderId) {
  return request({ url: '/erp/cut/production/' + productionOrderId, method: 'put' })
}

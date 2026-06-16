import request from '@/utils/request'

export function listInbound(query) {
  return request({ url: '/erp/inbound/list', method: 'get', params: query })
}

export function getInbound(inboundOrderId) {
  return request({ url: '/erp/inbound/' + inboundOrderId, method: 'get' })
}

export function addInbound(data) {
  return request({ url: '/erp/inbound', method: 'post', data })
}

export function updateInbound(data) {
  return request({ url: '/erp/inbound', method: 'put', data })
}

export function delInbound(inboundOrderId) {
  return request({ url: '/erp/inbound/' + inboundOrderId, method: 'delete' })
}

export function postInbound(inboundOrderId) {
  return request({ url: '/erp/inbound/post/' + inboundOrderId, method: 'put' })
}

export function cancelPostInbound(inboundOrderId) {
  return request({ url: '/erp/inbound/cancelPost/' + inboundOrderId, method: 'put' })
}

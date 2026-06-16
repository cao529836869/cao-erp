import request from '@/utils/request'

export function listOutbound(query) {
  return request({ url: '/erp/outbound/list', method: 'get', params: query })
}

export function getOutbound(outboundOrderId) {
  return request({ url: '/erp/outbound/' + outboundOrderId, method: 'get' })
}

export function addOutbound(data) {
  return request({ url: '/erp/outbound', method: 'post', data })
}

export function updateOutbound(data) {
  return request({ url: '/erp/outbound', method: 'put', data })
}

export function delOutbound(outboundOrderId) {
  return request({ url: '/erp/outbound/' + outboundOrderId, method: 'delete' })
}

export function postOutbound(outboundOrderId) {
  return request({ url: '/erp/outbound/post/' + outboundOrderId, method: 'put' })
}

export function cancelPostOutbound(outboundOrderId) {
  return request({ url: '/erp/outbound/cancelPost/' + outboundOrderId, method: 'put' })
}

export function listOutboundBatch(query) {
  return request({ url: '/erp/outbound/batches', method: 'get', params: query })
}

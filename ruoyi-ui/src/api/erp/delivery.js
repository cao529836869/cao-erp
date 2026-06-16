import request from '@/utils/request'

export function listDelivery(query) {
  return request({ url: '/erp/delivery/list', method: 'get', params: query })
}

export function getDelivery(deliveryOrderId) {
  return request({ url: '/erp/delivery/' + deliveryOrderId, method: 'get' })
}

export function addDelivery(data) {
  return request({ url: '/erp/delivery', method: 'post', data })
}

export function updateDelivery(data) {
  return request({ url: '/erp/delivery', method: 'put', data })
}

export function delDelivery(deliveryOrderId) {
  return request({ url: '/erp/delivery/' + deliveryOrderId, method: 'delete' })
}

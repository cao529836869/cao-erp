import request from '@/utils/request'

export function listModels() {
  return request({
    url: '/ai/models',
    method: 'get',
    timeout: 120000
  })
}

export function chat(data) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data,
    timeout: 120000
  })
}

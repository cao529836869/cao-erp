import request from '@/utils/request'

export function listKnowledge(query) {
  return request({
    url: '/ai/knowledge/list',
    method: 'get',
    params: query
  })
}

export function getKnowledge(chunkId) {
  return request({
    url: '/ai/knowledge/' + chunkId,
    method: 'get'
  })
}

export function addKnowledge(data) {
  return request({
    url: '/ai/knowledge',
    method: 'post',
    data,
    timeout: 120000
  })
}

export function updateKnowledge(data) {
  return request({
    url: '/ai/knowledge',
    method: 'put',
    data,
    timeout: 120000
  })
}

export function delKnowledge(chunkId) {
  return request({
    url: '/ai/knowledge/' + chunkId,
    method: 'delete'
  })
}

export function rebuildEmbedding(chunkId) {
  return request({
    url: '/ai/knowledge/embedding/' + chunkId,
    method: 'put',
    timeout: 120000
  })
}

import request from '@/utils/request'

export function startProductionWorkflow(data) {
  return request({ url: '/workflow/production/start', method: 'post', data })
}

export function listProductionTodo() {
  return request({ url: '/workflow/production/todo', method: 'get' })
}

export function getProductionTask(taskId) {
  return request({ url: '/workflow/production/task/' + taskId, method: 'get' })
}

export function completeProductionPicking(taskId, data) {
  return request({ url: '/workflow/production/task/' + taskId + '/picking', method: 'put', data })
}

export function completeProductionCut(taskId, data) {
  return request({ url: '/workflow/production/task/' + taskId + '/cut', method: 'put', data })
}

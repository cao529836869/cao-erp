import request from '@/utils/request'

export function getDashboardOverview() {
  return request({
    url: '/erp/dashboard/overview',
    method: 'get'
  })
}

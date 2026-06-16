import request from '@/utils/request'

// 查询物料档案列表
export function listMaterial(query) {
  return request({
    url: '/erp/material/list',
    method: 'get',
    params: query
  })
}

// 查询物料档案详细
export function getMaterial(materialId) {
  return request({
    url: '/erp/material/' + materialId,
    method: 'get'
  })
}

// 新增物料档案
export function addMaterial(data) {
  return request({
    url: '/erp/material',
    method: 'post',
    data: data
  })
}

// 修改物料档案
export function updateMaterial(data) {
  return request({
    url: '/erp/material',
    method: 'put',
    data: data
  })
}

// 删除物料档案
export function delMaterial(materialId) {
  return request({
    url: '/erp/material/' + materialId,
    method: 'delete'
  })
}

// 查询物料档案下拉列表
export function optionselectMaterial() {
  return request({
    url: '/erp/material/optionselect',
    method: 'get'
  })
}

// 生成物料编码
export function listMaterialSku(query) {
  return request({
    url: '/erp/material/sku/list',
    method: 'get',
    params: query
  })
}

export function generateMaterialCode(query) {
  return request({
    url: '/erp/material/generateCode',
    method: 'get',
    params: query
  })
}

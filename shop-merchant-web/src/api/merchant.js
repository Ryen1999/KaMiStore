import request from '../utils/request'

/** 获取店铺配置 */
export function getStoreConfig() {
  return request.get('/merchant/store')
}

/** 更新店铺配置 */
export function updateStoreConfig(data) {
  return request.put('/merchant/store', data)
}

/** 获取店铺链接信息 */
export function getStoreLinks() {
  return request.get('/merchant/store/link')
}

/** 查询角色列表 */
export function listRoles() {
  return request.get('/merchant/role/list')
}

/** 创建角色 */
export function createRole(data) {
  return request.post('/merchant/role', data)
}

/** 更新角色 */
export function updateRole(id, data) {
  return request.put('/merchant/role/' + id, data)
}

/** 删除角色 */
export function deleteRole(id) {
  return request.delete('/merchant/role/' + id)
}

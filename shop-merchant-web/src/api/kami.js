import request from '../utils/request'

/** 分页查询商品列表（供其他模块使用） */
export function pageProducts(params) {
  return request.get('/product/page', { params })
}

/** 分页查询卡密列表 */
export function pageKamiItems(params) {
  return request.get('/kami/item/page', { params })
}

/** 批量添加卡密 */
export function importKami(data) {
  return request.post('/kami/item/import', data)
}

/** 作废卡密 */
export function voidKami(id, reason) {
  return request.put('/kami/item/' + id + '/void', null, { params: { reason } })
}

/** 查看卡密明文 */
export function getKamiContent(id) {
  return request.get('/kami/item/' + id + '/content')
}
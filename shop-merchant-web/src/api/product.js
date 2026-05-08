import request from '../utils/request'

/** 分页查询商品 */
export function pageProducts(params) {
  return request.get('/product/page', { params })
}

/** 查询商品详情 */
export function getProductDetail(id) {
  return request.get('/product/' + id)
}

/** 创建商品 */
export function createProduct(data) {
  return request.post('/product', data)
}

/** 更新商品 */
export function updateProduct(id, data) {
  return request.put('/product/' + id, data)
}

/** 删除商品 */
export function deleteProduct(id) {
  return request.delete('/product/' + id)
}

/** 批量删除商品 */
export function deleteProducts(ids) {
  return request.post('/product/batch/delete', ids)
}

/** 更新商品状态 */
export function updateProductStatus(id, status) {
  return request.put('/product/' + id + '/status', null, { params: { status } })
}

/** 查询分类列表 */
export function listCategories() {
  return request.get('/product/category/list')
}

/** 创建分类 */
export function createCategory(data) {
  return request.post('/product/category', data)
}

/** 更新分类 */
export function updateCategory(id, data) {
  return request.put('/product/category/' + id, data)
}

/** 删除分类 */
export function deleteCategory(id) {
  return request.delete('/product/category/' + id)
}

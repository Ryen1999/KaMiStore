import request from '../utils/request'

export function getShopPublicInfo(shopCode) {
  return request.get(`/public/shop/info/${shopCode}`)
}

export function getPublicPaymentMethods(shopCode) {
  return request.get(`/public/shop/payment-methods/${shopCode}`)
}

export function getShopCategories(tenantId) {
  return request.get('/public/product/categories', { params: { tenantId } })
}

export function getShopProducts(tenantId, params) {
  return request.get('/public/product/list', { params: { tenantId, ...params } })
}

export function getOrderByNo(orderNo, contactValue) {
  return request.get(`/public/shop/order/${orderNo}`, { params: { contactValue } })
}

export function createPublicOrder(data) {
  return request.post('/public/shop/order', data)
}

export function createAlipayPrepay(orderNo) {
  return request.post(`/public/shop/order/${orderNo}/alipay/prepay`)
}

export function syncAlipayPayment(orderNo) {
  return request.post(`/public/shop/order/${orderNo}/alipay/sync`)
}

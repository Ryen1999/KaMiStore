import request from '../utils/request'

/** 分页查询订单列表 */
export function pageOrders(params) {
  return request.get('/order/page', { params })
}

/** 查询订单详情 */
export function getOrderDetail(id) {
  return request.get('/order/' + id)
}

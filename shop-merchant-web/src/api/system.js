import request from '../utils/request'

/** 分页查询登录日志 */
export function pageLoginLogs(params) {
  return request.get('/system/login-log/page', { params })
}

/** 分页查询站内消息 */
export function pageMessages(params) {
  return request.get('/system/message/page', { params })
}

/** 未读站内消息数量 */
export function getUnreadMessageCount() {
  return request.get('/system/message/unread-count')
}

/** 全部已读 */
export function readAllMessages() {
  return request.put('/system/message/read-all')
}

/** 更新单条消息读取状态 */
export function updateMessageReadStatus(id, isRead) {
  return request.put(`/system/message/${id}/read-status`, null, { params: { isRead } })
}

/** 删除站内消息 */
export function deleteMessage(id) {
  return request.delete(`/system/message/${id}`)
}

/** 修改密码 */
export function changePassword(data) {
  return request.put('/system/password', data)
}

/** 获取支付方式列表 */
export function listPaymentMethods() {
  return request.get('/store/payment-methods')
}

/** 更新支付方式状态 */
export function updatePaymentMethodStatus(data) {
  return request.put('/store/payment-methods/status', data)
}

/** 更新支付方式配置 */
export function updatePaymentMethodConfig(id, data) {
  return request.put('/store/payment-methods/' + id, data)
}

/** 获取支付方式配置详情 */
export function getPaymentMethodConfig(id) {
  return request.get('/store/payment-methods/' + id)
}

/** 获取结算设置配置 */
export function getSettlementConfig() {
  return request.get('/settlement/config')
}

/** 更新结算设置配置 */
export function updateSettlementConfig(data) {
  return request.put('/settlement/config', data)
}

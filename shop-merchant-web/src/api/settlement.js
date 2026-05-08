import request from '../utils/request'

/** 分页查询提现列表 */
export function pageWithdrawals(params) {
  return request.get('/settlement/withdrawal/page', { params })
}

/** 申请提现 */
export function applyWithdrawal(data) {
  return request.post('/settlement/withdrawal/apply', data)
}

/** 获取结算概览数据 */
export function getSettlementOverview() {
  return request.get('/settlement/overview')
}

import request from '../utils/request'

/** 商户登录 */
export function login(data) {
  return request.post('/auth/login', data)
}

/** 商户注册 */
export function register(data) {
  return request.post('/auth/register', data)
}

/** 退出登录 */
export function logout() {
  return request.post('/auth/logout')
}

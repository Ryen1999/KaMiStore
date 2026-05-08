const TOKEN_KEY = 'token'

/** 获取token */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

/** 设置token */
export function setToken(token) {
  return localStorage.setItem(TOKEN_KEY, token)
}

/** 移除token */
export function removeToken() {
  return localStorage.removeItem(TOKEN_KEY)
}

import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：注入Token和租户ID
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  const tenantId = localStorage.getItem('tenantId')
  if (tenantId) {
    config.headers['X-Tenant-Id'] = tenantId
  }
  return config
})

// 响应拦截：统一错误处理
request.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code !== 200) {
      ElMessage.error(data.msg || '请求失败')
      // 401跳转登录
      if (data.code === 401) {
        localStorage.clear()
        window.location.hash = '#/login'
      }
      return Promise.reject(data)
    }
    return data
  },
  err => {
    if (err.response && err.response.status === 401) {
      localStorage.clear()
      window.location.hash = '#/login'
    }
    ElMessage.error(err.message || '网络异常')
    return Promise.reject(err)
  }
)

export default request

<template>
  <div class="auth-page">
    <main class="auth-container" :class="{ visible: mounted }">
      <section class="auth-brand">
        <div class="brand-top">
          <div class="brand-logo-row">
            <div class="brand-logo-box">
              <el-icon :size="24"><Wallet /></el-icon>
            </div>
            <span class="brand-logo-text">商户管理平台</span>
          </div>
          <div class="brand-headline">
            <h1>全自动虚拟商品<br />分发平台</h1>
            <p>为商户提供安全、稳定、高效的数字资产管理与自动化交易结算服务。</p>
          </div>
        </div>
        <div class="brand-stats">
          <div class="brand-stat">
            <span class="stat-num">1.2M+</span>
            <span class="stat-label">活跃交易</span>
          </div>
          <div class="brand-stat">
            <span class="stat-num">99.9%</span>
            <span class="stat-label">系统可用性</span>
          </div>
          <div class="brand-stat">
            <span class="stat-num">24/7</span>
            <span class="stat-label">技术支持</span>
          </div>
        </div>
        <div class="brand-decor-1"></div>
        <div class="brand-decor-2"></div>
      </section>

      <section class="auth-form-area">
        <div class="auth-form-inner">
          <header class="auth-header">
            <h2>欢迎回来</h2>
            <p>请输入您的商户账号以管理业务</p>
          </header>

          <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="handleLogin" class="auth-form">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" size="large" :prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                placeholder="请输入密码"
                type="password"
                show-password
                size="large"
                :prefix-icon="Lock"
              />
            </el-form-item>
            <div class="field-row">
              <el-checkbox v-model="rememberMe" label="记住密码" />
              <a href="#" class="forgot-link">忘记密码？</a>
            </div>
            <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">
              {{ loading ? '登录中...' : '立即登录' }}
            </el-button>
          </el-form>

          <footer class="auth-footer">
            还没有账号？ <router-link to="/register" class="register-link">立即注册</router-link>
          </footer>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User, Wallet } from '@element-plus/icons-vue'
import { login } from '../api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const mounted = ref(false)

function canUseStorage() {
  try {
    const key = '__remember_test__'
    localStorage.setItem(key, '1')
    localStorage.removeItem(key)
    return true
  } catch {
    return false
  }
}

const storageAvailable = canUseStorage()
const savedUsername = storageAvailable ? localStorage.getItem('rememberedUsername') || '' : ''
const savedPassword = storageAvailable ? localStorage.getItem('rememberedPassword') || '' : ''

const form = reactive({
  username: savedUsername,
  password: savedPassword
})

const rememberMe = ref(Boolean(savedUsername && savedPassword))

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => {
  setTimeout(() => {
    mounted.value = true
  }, 100)
})

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login({ username: form.username, password: form.password, userType: 'merchant' })

    localStorage.setItem('token', res.data.token)
    localStorage.setItem('username', res.data.username)
    localStorage.setItem('userId', res.data.userId)
    if (res.data.tenantId) {
      localStorage.setItem('tenantId', res.data.tenantId)
    }

    if (storageAvailable) {
      if (rememberMe.value) {
        localStorage.setItem('rememberedUsername', form.username)
        localStorage.setItem('rememberedPassword', form.password)
      } else {
        localStorage.removeItem('rememberedUsername')
        localStorage.removeItem('rememberedPassword')
      }
    } else if (rememberMe.value) {
      ElMessage.warning('当前浏览器不支持本地存储，记住密码不会生效')
    }

    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--background);
  padding: 16px;
}

.auth-container {
  width: 100%;
  max-width: 960px;
  background: var(--surface-container-lowest);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  display: flex;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}
.auth-container.visible {
  opacity: 1;
  transform: translateY(0);
}

.auth-brand {
  flex: 1;
  background: linear-gradient(135deg, #0041d8 0%, #2d5cf7 100%);
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
}
.brand-top {
  position: relative;
  z-index: 2;
}
.brand-logo-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 40px;
}
.brand-logo-box {
  width: 40px;
  height: 40px;
  background: #fff;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
}
.brand-logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
}
.brand-headline h1 {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  line-height: 1.3;
  margin-bottom: 16px;
}
.brand-headline p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
}

.brand-stats {
  position: relative;
  z-index: 2;
  display: flex;
  gap: 40px;
}
.brand-stat {
  display: flex;
  flex-direction: column;
}
.stat-num {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}
.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 2px;
}

.brand-decor-1 {
  position: absolute;
  bottom: -80px;
  right: -80px;
  width: 260px;
  height: 260px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 50%;
  filter: blur(60px);
}
.brand-decor-2 {
  position: absolute;
  top: 40%;
  left: -40px;
  width: 160px;
  height: 160px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 50%;
  filter: blur(40px);
}

.auth-form-area {
  flex: 1;
  padding: 48px 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.auth-form-inner {
  max-width: 340px;
  margin: 0 auto;
  width: 100%;
}
.auth-header {
  margin-bottom: 32px;
}
.auth-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--on-surface);
  margin-bottom: 6px;
}
.auth-header p {
  font-size: 14px;
  color: var(--on-surface-variant);
}

.field-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.forgot-link {
  font-size: 12px;
  color: var(--primary);
  text-decoration: none;
}
.forgot-link:hover {
  text-decoration: underline;
}

.auth-footer {
  margin-top: 28px;
  text-align: center;
  font-size: 14px;
  color: var(--on-surface-variant);
}
.register-link {
  color: var(--primary);
  font-weight: 600;
  text-decoration: none;
}
.register-link:hover {
  text-decoration: underline;
}
</style>

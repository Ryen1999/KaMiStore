<template>
  <!-- 注册页 -->
  <div class="auth-page">
    <main class="auth-container" :class="{ visible: mounted }">
      <!-- 左侧品牌区 -->
      <section class="auth-brand">
        <div class="brand-content">
          <div class="brand-logo-row">
            <div class="brand-logo-box">
              <el-icon :size="24"><Wallet /></el-icon>
            </div>
            <span class="brand-logo-text">商户管理平台</span>
          </div>
          <p class="brand-desc">加入虚拟商品交易平台，为您的数字资产提供稳健管理、高效交易与安全的自动化结算解决方案。</p>
          <div class="feature-list">
            <div class="feature-item">
              <el-icon color="#68fadd"><CircleCheck /></el-icon>
              <span>企业级安全认证体系</span>
            </div>
            <div class="feature-item">
              <el-icon color="#68fadd"><Promotion /></el-icon>
              <span>秒级响应的 API 接口</span>
            </div>
            <div class="feature-item">
              <el-icon color="#68fadd"><Refresh /></el-icon>
              <span>多币种自动化结算系统</span>
            </div>
          </div>
        </div>
        <div class="brand-decor-1"></div>
        <div class="brand-decor-2"></div>
      </section>

      <!-- 右侧注册表单 -->
      <section class="auth-form-area">
        <div class="auth-form-inner">
          <header class="auth-header">
            <h2>创建商户账户</h2>
            <p>请填写以下信息以完成注册流程</p>
          </header>

          <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="handleRegister">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="输入用户名" size="large" :prefix-icon="User" />
            </el-form-item>

            <div class="form-row-double">
              <el-form-item prop="password">
                <el-input v-model="form.password" placeholder="设置密码" type="password" show-password size="large" :prefix-icon="Lock" />
              </el-form-item>
              <el-form-item prop="confirmPassword">
                <el-input v-model="form.confirmPassword" placeholder="确认密码" type="password" show-password size="large" :prefix-icon="Lock" />
              </el-form-item>
            </div>

            <div class="form-row-double">
              <el-form-item>
                <el-input v-model="form.email" placeholder="电子邮箱" size="large" :prefix-icon="Message" />
              </el-form-item>
              <el-form-item>
                <el-input v-model="form.qq" placeholder="QQ号" size="large" :prefix-icon="ChatDotRound" />
              </el-form-item>
            </div>

            <el-form-item>
              <el-input v-model="form.phone" placeholder="手机号码" size="large" :prefix-icon="Phone" />
            </el-form-item>

            <el-checkbox v-model="agreeTerms" style="margin-bottom:16px;">
              我已阅读并同意服务条款及隐私政策
            </el-checkbox>

            <el-button type="primary" size="large" style="width:100%" :loading="loading" :disabled="!agreeTerms" @click="handleRegister">
              {{ loading ? '注册中...' : '立即注册' }}
            </el-button>
          </el-form>

          <footer class="auth-footer">
            已有账号？ <router-link to="/login" class="login-link">直接登录</router-link>
          </footer>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, ChatDotRound, Phone, CircleCheck, Promotion, Refresh, Wallet } from '@element-plus/icons-vue'
import { register } from '../api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const mounted = ref(false)
const agreeTerms = ref(false)

const form = reactive({
  username: '', password: '', confirmPassword: '',
  email: '', qq: '', phone: ''
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }]
}

onMounted(() => { setTimeout(() => { mounted.value = true }, 100) })

const handleRegister = async () => {
  await formRef.value.validate()
  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await register({
      username: form.username,
      password: form.password,
      email: form.email,
      qq: form.qq,
      phone: form.phone
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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
.auth-container.visible { opacity: 1; transform: translateY(0); }

.auth-brand {
  flex: 0 0 380px;
  background: linear-gradient(135deg, #0041d8 0%, #2d5cf7 100%);
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.brand-content { position: relative; z-index: 2; }
.brand-logo-row { display: flex; align-items: center; gap: 10px; margin-bottom: 28px; }
.brand-logo-box {
  width: 40px; height: 40px; background: white; border-radius: var(--radius);
  display: flex; align-items: center; justify-content: center; color: var(--primary);
}
.brand-logo-text { font-size: 18px; font-weight: 700; color: white; }
.brand-desc { font-size: 14px; color: rgba(255, 255, 255, 0.7); line-height: 1.7; margin-bottom: 28px; }
.feature-list { display: flex; flex-direction: column; gap: 14px; }
.feature-item {
  display: flex; align-items: center; gap: 10px;
  color: rgba(255, 255, 255, 0.85); font-size: 14px;
}
.brand-decor-1 {
  position: absolute; bottom: -60px; right: -60px; width: 250px; height: 250px;
  background: rgba(255, 255, 255, 0.06); border-radius: 50%; filter: blur(50px);
}
.brand-decor-2 {
  position: absolute; top: 30%; left: -30px; width: 150px; height: 150px;
  background: rgba(255, 255, 255, 0.05); border-radius: 50%; filter: blur(30px);
}

.auth-form-area {
  flex: 1; padding: 36px 48px; display: flex; flex-direction: column; justify-content: center; overflow-y: auto;
}
.auth-form-inner { max-width: 420px; margin: 0 auto; width: 100%; }
.auth-header { margin-bottom: 24px; }
.auth-header h2 { font-size: 22px; font-weight: 700; color: var(--on-surface); margin-bottom: 4px; }
.auth-header p { font-size: 14px; color: var(--on-surface-variant); }

.form-row-double { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }

.auth-footer { margin-top: 20px; text-align: center; font-size: 14px; color: var(--on-surface-variant); }
.login-link { color: var(--primary); font-weight: 600; text-decoration: none; }
.login-link:hover { text-decoration: underline; }
</style>

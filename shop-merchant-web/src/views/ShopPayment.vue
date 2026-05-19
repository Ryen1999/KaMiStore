<template>
  <div class="payment-page">
    <div class="payment-card">
      <h1>支付宝扫码支付</h1>
      <p class="order-info">订单号：{{ orderNo }}</p>
      <p class="order-info">支付金额：<strong>¥{{ formatPrice(amount) }}</strong></p>

      <div class="qrcode-section" v-loading="loading">
        <div v-if="qrDataUrl" class="qrcode-wrapper">
          <img :src="qrDataUrl" alt="支付二维码" class="qrcode-img" />
          <p class="qrcode-tip">请使用支付宝沙箱钱包扫码支付</p>
        </div>
        <p v-if="!loading && !qrDataUrl && !expired" class="error-text">二维码生成失败，请返回重试</p>
      </div>

      <div v-if="polling && !paid" class="polling-status">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>等待支付中…</span>
      </div>

      <div v-if="paid" class="paid-success">
        <el-icon color="#146c5f" :size="32"><CircleCheckFilled /></el-icon>
        <span>支付成功，正在跳转…</span>
      </div>

      <div v-if="expired && !paid" class="expired-notice">
        <p>订单已超时或二维码已过期</p>
        <el-button type="primary" @click="$router.back()">返回重新下单</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, CircleCheckFilled } from '@element-plus/icons-vue'
import QRCode from 'qrcode'
import { createAlipayPrepay, getOrderByNo, syncAlipayPayment } from '../api/publicShop'

const route = useRoute()
const router = useRouter()

const orderNo = ref(route.params.orderNo)
const qrDataUrl = ref('')
const amount = ref(0)
const loading = ref(true)
const polling = ref(false)
const paid = ref(false)
const expired = ref(false)
let pollTimer = null

const formatPrice = (price) => Number(price || 0).toFixed(2)

const generateQrImage = async (qrCodeText) => {
  try {
    const dataUrl = await QRCode.toDataURL(qrCodeText, {
      width: 260,
      margin: 2,
      color: { dark: '#000000', light: '#ffffff' }
    })
    qrDataUrl.value = dataUrl
  } catch {
    ElMessage.error('二维码渲染失败')
  }
}

const startPolling = () => {
  polling.value = true
  pollTimer = setInterval(async () => {
    try {
      const syncRes = await syncAlipayPayment(orderNo.value)
      const res = syncRes?.data?.status === 1 ? syncRes : await getOrderByNo(orderNo.value)
      if (res.data && res.data.status === 1) {
        paid.value = true
        clearInterval(pollTimer)
        setTimeout(() => {
          router.replace(`/shop/pay-result/${orderNo.value}`)
        }, 1200)
      }
    } catch {
      // 忽略轮询异常
    }
  }, 3000)
}

onMounted(async () => {
  try {
    const res = await createAlipayPrepay(orderNo.value)
    const { qrCode, amount: amt } = res.data
    amount.value = amt || 0
    if (qrCode) {
      await generateQrImage(qrCode)
      startPolling()
    }
  } catch (e) {
    expired.value = true
    ElMessage.error(e.msg || '创建支付订单失败')
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  clearInterval(pollTimer)
})
</script>

<style scoped>
.payment-page {
  min-height: 100vh;
  background: #f3f6f8;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.payment-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(20, 37, 34, 0.08);
  padding: 40px;
  text-align: center;
  max-width: 420px;
  width: 100%;
}

h1 {
  margin: 0 0 12px;
  font-size: 22px;
  color: #1f2933;
}

.order-info {
  color: #6b7c78;
  margin: 4px 0;
}

.order-info strong {
  color: #146c5f;
  font-size: 24px;
}

.qrcode-section {
  margin: 24px 0;
  min-height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qrcode-wrapper {
  display: grid;
  gap: 12px;
  justify-items: center;
}

.qrcode-img {
  border: 1px solid #dfe7e5;
  border-radius: 8px;
  padding: 8px;
  background: #fff;
}

.qrcode-tip {
  color: #6b7c78;
  font-size: 13px;
}

.polling-status, .paid-success {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin: 16px 0;
  color: #146c5f;
  font-weight: 600;
}

.expired-notice p {
  color: #e03e2d;
  margin: 16px 0;
}

.error-text {
  color: #e03e2d;
}
</style>

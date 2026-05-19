<template>
  <div class="result-page">
    <div class="result-card">
      <div class="result-header">
        <el-icon color="#146c5f" :size="48"><CircleCheckFilled /></el-icon>
        <h1>支付成功</h1>
        <p class="order-no">订单号：{{ orderNo }}</p>
      </div>

      <div class="kami-section" v-if="kamiList.length > 0">
        <h2>您的卡密</h2>
        <div class="kami-list">
          <div v-for="(kami, idx) in kamiList" :key="idx" class="kami-item">
            <code>{{ kami }}</code>
            <el-button size="small" type="primary" plain @click="copyKami(kami)">
              复制
            </el-button>
          </div>
        </div>
      </div>

      <div class="verify-section" v-else>
        <p class="verify-hint">{{ verifyMessage || '请输入下单时填写的联系方式查看卡密' }}</p>
        <div class="verify-form">
          <el-input
            v-model="contactValue"
            placeholder="请输入联系人QQ/手机号"
            maxlength="50"
            clearable
            @keyup.enter="doQuery"
          />
          <el-button type="primary" :loading="querying" @click="doQuery">查询</el-button>
        </div>
      </div>

      <div class="actions">
        <el-button type="primary" size="large" @click="goShop">返回店铺</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { getOrderByNo } from '../api/publicShop'

const route = useRoute()
const router = useRouter()

const orderNo = ref(route.params.orderNo)
const contactValue = ref('')
const kamiList = ref([])
const verifyMessage = ref('')
const querying = ref(false)
const shopCode = ref('')

const doQuery = async () => {
  if (!contactValue.value.trim()) {
    ElMessage.warning('请输入联系方式')
    return
  }
  querying.value = true
  try {
    const res = await getOrderByNo(orderNo.value, contactValue.value.trim())
    if (res.data?.shopCode) {
      shopCode.value = res.data.shopCode
    }
    if (res.data && res.data.kamiList && res.data.kamiList.length > 0) {
      kamiList.value = res.data.kamiList
      verifyMessage.value = ''
    } else if (res.data && res.data.payMessage) {
      verifyMessage.value = res.data.payMessage
    } else {
      verifyMessage.value = '未找到卡密信息'
    }
  } catch (e) {
    verifyMessage.value = e.msg || '查询失败，请稍后重试'
  } finally {
    querying.value = false
  }
}

const copyKami = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

const goShop = () => {
  if (shopCode.value) {
    router.push(`/shop/${shopCode.value}`)
  }
}

onMounted(async () => {
  try {
    const res = await getOrderByNo(orderNo.value)
    if (res.data?.shopCode) {
      shopCode.value = res.data.shopCode
    }
    if (res.data?.payMessage) {
      verifyMessage.value = res.data.payMessage
    }
  } catch {
    // ignore initial lookup failure
  }
})
</script>

<style scoped>
.result-page {
  min-height: 100vh;
  background: #f3f6f8;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.result-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(20, 37, 34, 0.08);
  padding: 40px;
  max-width: 520px;
  width: 100%;
}

.result-header {
  text-align: center;
  margin-bottom: 28px;
}

.result-header h1 {
  margin: 12px 0 4px;
  font-size: 22px;
  color: #1f2933;
}

.order-no {
  color: #6b7c78;
  font-size: 14px;
}

.kami-section h2 {
  font-size: 18px;
  margin: 0 0 16px;
  color: #1f2933;
}

.kami-list {
  display: grid;
  gap: 10px;
}

.kami-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  background: #f6faf9;
  border: 1px solid #dfe7e5;
  border-radius: 8px;
}

.kami-item code {
  font-family: monospace;
  font-size: 15px;
  color: #146c5f;
  word-break: break-all;
}

.verify-section {
  margin: 20px 0;
}

.verify-hint {
  color: #6b7c78;
  font-size: 14px;
  margin-bottom: 12px;
}

.verify-form {
  display: flex;
  gap: 10px;
}

.actions {
  margin-top: 28px;
  text-align: center;
}
</style>

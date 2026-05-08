<template>
  <!-- 结算设置 -->
  <div class="animate-in">
    <div class="page-header">
      <div>
        <p class="breadcrumb">店铺管理 &gt; 结算设置</p>
        <h3 class="page-heading">结算设置</h3>
      </div>
    </div>

    <!-- 收款设置 -->
    <div class="glass-card section-card" v-loading="loading">
      <div class="section-header">
        <h4>收款设置</h4>
      </div>
      <div class="radio-group">
        <el-radio-group v-model="settlementForm.collectionType">
          <el-radio label="system">系统默认</el-radio>
          <el-radio label="manual">手工提现</el-radio>
          <el-radio label="auto">自动提现</el-radio>
        </el-radio-group>
        <el-button type="primary" size="small" @click="handleSaveSettlement" :loading="saving">
          <el-icon style="margin-right: 4px;"><Check /></el-icon>
          保存设置
        </el-button>
      </div>
    </div>

    <!-- 商户结算账号信息 -->
    <div class="glass-card section-card" v-loading="loading">
      <div class="section-header">
        <h4>商户结算账号信息</h4>
      </div>
      <div class="warning-text">
        -> 仅支持支付宝提现,请正确填写支付宝帐号和帐号姓名
      </div>
      <el-form :model="settlementForm" label-width="120px" class="settlement-form">
        <el-form-item label="支付宝账号">
          <el-input 
            v-model="settlementForm.alipayAccount" 
            placeholder="请输入支付宝账号" 
            clearable
          />
        </el-form-item>
        <el-form-item label="收款人姓名">
          <el-input 
            v-model="settlementForm.payeeName" 
            placeholder="请输入收款人姓名" 
            clearable
          />
        </el-form-item>
        <el-form-item label="收款二维码">
          <div class="qrcode-upload">
            <el-upload
              class="qrcode-uploader"
              :action="uploadAction"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
              :headers="uploadHeaders"
            >
              <div class="qrcode-content">
                <img v-if="settlementForm.qrcodeUrl" :src="settlementForm.qrcodeUrl" class="qrcode-image" />
                <div v-else class="qrcode-placeholder">
                  <el-icon :size="40" color="#c0c4cc"><Plus /></el-icon>
                  <div class="upload-text">点击上传二维码</div>
                </div>
                <!-- 点击更换遮罩 -->
                <div v-if="settlementForm.qrcodeUrl" class="qrcode-overlay">
                  <el-icon :size="24"><Edit /></el-icon>
                  <span>点击更换</span>
                </div>
              </div>
            </el-upload>
            <div class="upload-tips">支持 JPG、PNG 格式，文件大小不超过 10MB</div>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getSettlementConfig, updateSettlementConfig } from '../../api/system'
import { ElMessage } from 'element-plus'
import { Check, Plus, Edit } from '@element-plus/icons-vue'
import { getToken } from '../../utils/auth'

/* 表单数据 */
const settlementForm = ref({
  collectionType: 'system',
  alipayAccount: '',
  payeeName: '',
  qrcodeUrl: ''
})

const loading = ref(false)
const saving = ref(false)

/* 上传配置 */
const uploadAction = '/api/settlement/upload/qrcode'
const uploadHeaders = computed(() => ({
  'Authorization': 'Bearer ' + getToken()
}))

/* 加载数据 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getSettlementConfig()
    if (res.data) {
      settlementForm.value = {
        collectionType: res.data.collectionType || 'system',
        alipayAccount: res.data.alipayAccount || '',
        payeeName: res.data.payeeName || '',
        qrcodeUrl: res.data.qrcodeUrl || ''
      }
    }
  } catch (e) {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

/* 保存结算设置 */
const handleSaveSettlement = async () => {
  if (!settlementForm.value.alipayAccount) {
    ElMessage.warning('请填写支付宝账号')
    return
  }
  if (!settlementForm.value.payeeName) {
    ElMessage.warning('请填写收款人姓名')
    return
  }

  saving.value = true
  try {
    await updateSettlementConfig(settlementForm.value)
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

/* 上传前校验 */
const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
  }
  return isImage && isLt10M
}

/* 上传成功 */
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    settlementForm.value.qrcodeUrl = response.data.url
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}
</script>

<style scoped>
.breadcrumb { font-size: 12px; color: var(--outline); margin-bottom: 4px; }

/* 卡片样式 */
.section-card { padding: 24px; margin-bottom: var(--gutter); }
.section-header { margin-bottom: 20px; }
.section-header h4 { font-size: 16px; font-weight: 700; color: var(--on-surface); }

/* 收款设置 */
.radio-group {
  display: flex;
  align-items: center;
  gap: 24px;
}

/* 警告文本 */
.warning-text {
  color: #ff0000;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: rgba(255, 0, 0, 0.05);
  border-radius: var(--radius-sm);
}

/* 表单样式 */
.settlement-form {
  max-width: 800px;
}

/* 二维码上传 */
.qrcode-upload {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.qrcode-uploader {
  border: 1px dashed var(--outline-variant);
  border-radius: var(--radius);
  width: 200px;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  overflow: hidden;
}

.qrcode-uploader:hover {
  border-color: var(--primary);
  background: rgba(0, 65, 216, 0.02);
}

.qrcode-content {
  position: relative;
  width: 100%;
  height: 100%;
}

.qrcode-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.qrcode-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.qrcode-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
}

.upload-text {
  font-size: 12px;
  color: var(--outline);
}

.upload-tips {
  font-size: 12px;
  color: var(--outline);
}
</style>

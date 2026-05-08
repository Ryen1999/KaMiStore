<template>
  <!-- 店铺基础设置 - 基于需求文档 3.3.2 -->
  <div class="animate-in">
    <div class="page-header">
      <div>
        <p class="breadcrumb">店铺管理 &gt; 基础设置</p>
        <h3 class="page-heading">店铺设置</h3>
        <p class="page-desc">管理您的店铺展示信息、公告和页面风格</p>
      </div>
    </div>

    <div class="settings-layout">
      <div class="glass-card" style="padding: 28px;" v-loading="loading">
        <el-form :model="form" label-width="110px" label-position="left">
          <!-- 基本信息 -->
          <div class="section-block">
            <div class="section-label">
              <el-icon :size="20"><Shop /></el-icon>
              <span>基本信息</span>
            </div>
            <el-form-item label="店铺开关">
              <el-switch v-model="form.storeEnabled" :active-value="1" :inactive-value="0" active-text="开启" inactive-text="关闭" />
            </el-form-item>
            <el-form-item label="店铺名称">
              <el-input v-model="form.storeName" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item label="商户QQ">
              <el-input v-model="form.merchantQq" placeholder="客服QQ号" />
            </el-form-item>
            <el-form-item label="商户群链接">
              <el-input v-model="form.groupLink" placeholder="QQ群或微信群链接" />
            </el-form-item>
            <el-form-item label="店铺Logo">
              <el-input v-model="form.storeLogo" placeholder="Logo图片URL" />
            </el-form-item>
          </div>

          <!-- 公告设置 -->
          <div class="section-block">
            <div class="section-label">
              <el-icon :size="20"><Bell /></el-icon>
              <span>公告设置</span>
            </div>
            <el-form-item label="店铺公告">
              <el-input v-model="form.storeNotice" type="textarea" :rows="4" placeholder="支持富文本，向买家展示的公告内容" />
            </el-form-item>
            <el-form-item label="自动弹出公告">
              <el-switch v-model="form.autoPopNotice" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </div>

          <!-- 页面风格 -->
          <div class="section-block">
            <div class="section-label">
              <el-icon :size="20"><Brush /></el-icon>
              <span>页面风格</span>
            </div>
            <el-form-item label="PC端风格">
              <el-select v-model="form.pcStyle" style="width: 100%;">
                <el-option label="简约经典" value="classic" />
                <el-option label="卡片模式" value="card" />
                <el-option label="列表模式" value="list" />
              </el-select>
            </el-form-item>
            <el-form-item label="移动端风格">
              <el-select v-model="form.mobileStyle" style="width: 100%;">
                <el-option label="简约经典" value="classic" />
                <el-option label="卡片模式" value="card" />
              </el-select>
            </el-form-item>
            <el-form-item label="主题色">
              <div style="display: flex; align-items: center; gap: 12px;">
                <el-color-picker v-model="form.themeColor" />
                <code style="font-size: 13px; color: var(--on-surface-variant);">{{ form.themeColor }}</code>
              </div>
            </el-form-item>
            <el-form-item label="背景音乐">
              <el-input v-model="form.bgMusicUrl" placeholder="音乐文件URL（可选）" />
            </el-form-item>
          </div>

          <!-- SEO 优化 -->
          <div class="section-block">
            <div class="section-label">
              <el-icon :size="20"><Search /></el-icon>
              <span>SEO 优化</span>
            </div>
            <el-form-item label="SEO标题">
              <el-input v-model="form.seoTitle" maxlength="200" />
            </el-form-item>
            <el-form-item label="SEO关键词">
              <el-input v-model="form.seoKeywords" maxlength="300" placeholder="多个关键词用逗号分隔" />
            </el-form-item>
            <el-form-item label="SEO描述">
              <el-input v-model="form.seoDescription" type="textarea" :rows="2" />
            </el-form-item>
          </div>

          <div class="form-actions">
            <el-button type="primary" size="large" :loading="saving" @click="handleSave">
              <el-icon style="margin-right: 4px;"><Check /></el-icon>
              保存配置
            </el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStoreConfig, updateStoreConfig } from '../../api/merchant'
import { ElMessage } from 'element-plus'
import { Shop, Bell, Brush, Search, Check } from '@element-plus/icons-vue'

const loading = ref(false)
const saving = ref(false)
const form = reactive({
  storeEnabled: 1, storeName: '', merchantQq: '', groupLink: '', storeLogo: '',
  storeNotice: '', autoPopNotice: 0,
  pcStyle: 'classic', mobileStyle: 'classic', themeColor: '#2d5cf7', bgMusicUrl: '',
  seoTitle: '', seoKeywords: '', seoDescription: ''
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await getStoreConfig()
    Object.assign(form, res.data)
  } finally { loading.value = false }
})

const handleSave = async () => {
  saving.value = true
  try {
    await updateStoreConfig(form)
    ElMessage.success('保存成功')
  } finally { saving.value = false }
}
</script>

<style scoped>
.breadcrumb { font-size: 12px; color: var(--outline); margin-bottom: 4px; }

.settings-layout { max-width: 760px; }

.section-block { margin-bottom: 28px; }
.section-label {
  display: flex; align-items: center; gap: 8px;
  font-size: 15px; font-weight: 700;
  color: var(--on-surface); margin-bottom: 16px; padding-bottom: 10px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
}
.section-label .el-icon { color: var(--primary); }

.form-actions { padding-top: 16px; border-top: 1px solid rgba(226, 232, 240, 0.6); }
</style>

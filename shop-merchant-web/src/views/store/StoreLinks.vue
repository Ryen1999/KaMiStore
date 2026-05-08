<template>
  <!-- 店铺链接管理 -->
  <div class="animate-in">
    <div class="page-header">
      <div>
        <p class="breadcrumb">店铺管理 &gt; 店铺链接</p>
        <h3 class="page-heading">店铺链接</h3>
        <p class="page-desc">管理您的分享链接，独立域名以及API接口配置</p>
      </div>
    </div>

    <div class="links-layout">
      <!-- 左侧：链接类型卡片 -->
      <div class="link-types-card glass-card">
        <div class="link-type" :class="{ active: activeTab === 'store' }" @click="activeTab = 'store'">
          <el-icon :size="20"><Shop /></el-icon>
          <span>店铺链接</span>
        </div>
        <div class="link-type" :class="{ active: activeTab === 'domain' }" @click="activeTab = 'domain'">
          <el-icon :size="20"><ChromeFilled /></el-icon>
          <span>独立域名和短链接</span>
        </div>
        <div class="link-type" :class="{ active: activeTab === 'api' }" @click="activeTab = 'api'">
          <el-icon :size="20"><Connection /></el-icon>
          <span>API接口</span>
        </div>

        <!-- 安全提示 -->
        <div class="security-tip">
          <div class="tip-icon">
            <el-icon :size="20" color="white"><Lock /></el-icon>
          </div>
          <h5>安全提示</h5>
          <p>建议使用独立域名提升品牌识别度，同时启用HTTPS确保数据传输安全</p>
        </div>
      </div>

      <!-- 右侧：内容区 -->
      <div class="link-content">
        <!-- 店铺链接管理 -->
        <div v-if="activeTab === 'store'" class="glass-card" style="padding: 24px;">
          <div class="control-header">
            <h4>店铺链接</h4>
            <div class="control-actions">
              <el-tag :type="shopEnabled ? 'success' : 'danger'" size="small">
                {{ shopEnabled ? '已开启' : '已关闭' }}
              </el-tag>
            </div>
          </div>

          <p class="section-desc">您的专属店铺链接，复制后在浏览器打开即可访问您的店铺</p>

          <!-- 店铺链接 -->
          <div class="link-row">
            <label class="link-label">默认店铺链接</label>
            <div class="link-url-box">
              <code class="link-url">{{ shopLink || '加载中...' }}</code>
              <button class="copy-btn" @click="copyLink(shopLink)" :disabled="!shopLink">
                <el-icon><CopyDocument /></el-icon>
              </button>
            </div>
            <div class="link-info">
              <el-icon><InfoFilled /></el-icon>
              <span>此链接为永久有效链接，每个商户的链接都是唯一的</span>
            </div>
          </div>

          <!-- 店铺标识码 -->
          <div class="link-row">
            <label class="link-label">店铺标识码</label>
            <div class="link-url-box">
              <code class="link-url">{{ shopCode || '未生成' }}</code>
              <button class="copy-btn" @click="copyLink(shopCode)" :disabled="!shopCode">
                <el-icon><CopyDocument /></el-icon>
              </button>
              <el-button size="small" @click="openInNewTab" :disabled="!shopLink">
                <el-icon style="margin-right: 4px;"><TopRight /></el-icon>
                预览店铺
              </el-button>
            </div>
          </div>

          <!-- 二维码区域 -->
          <div class="qr-section">
            <h5 class="qr-title">店铺二维码</h5>
            <div class="qr-box">
              <div class="qr-placeholder" v-if="!shopCode">
                <el-icon :size="48"><Picture /></el-icon>
                <span>生成链接后显示二维码</span>
              </div>
              <div class="qr-image" v-else>
                <!-- 二维码将通过API生成，这里使用占位 -->
                <div class="qrcode-box" :data-code="shopCode">{{ shopCode }}</div>
              </div>
            </div>
            <el-button type="primary" size="small" @click="generateQRCode" :disabled="!shopCode">
              <el-icon style="margin-right: 4px;"><Download /></el-icon>
              下载二维码
            </el-button>
          </div>
        </div>

        <!-- 其他Tab内容 -->
        <div v-else-if="activeTab === 'domain'" class="glass-card" style="padding: 24px;">
          <div class="control-header">
            <div class="flex-row">
              <el-icon :size="20" color="var(--primary)"><Monitor /></el-icon>
              <h4>自定义域名配置说明</h4>
            </div>
          </div>
          <div class="domain-steps">
            <div class="step-item">
              <div class="step-num">1</div>
              <div>
                <h5>配置域名解析</h5>
                <p>在您的域名管理面板添加 CNAME 记录</p>
              </div>
            </div>
            <div class="step-item">
              <div class="step-num">2</div>
              <div>
                <h5>配置 CNAME 解析</h5>
                <p>将您的域名 CNAME 指向 <code>shop.vpro.com</code></p>
              </div>
            </div>
            <div class="step-item">
              <div class="step-num">3</div>
              <div>
                <h5>等待生效</h5>
                <p>DNS解析通常需要 5-30 分钟生效</p>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="activeTab === 'api'" class="glass-card" style="padding: 24px;">
          <div class="control-header">
            <div class="flex-row">
              <el-icon :size="20" color="var(--primary)"><Connection /></el-icon>
              <h4>API接口说明</h4>
            </div>
          </div>
          <div class="api-info">
            <p class="section-desc">店铺API接口用于第三方平台对接，实现商品同步等功能</p>
            <div class="api-endpoint">
              <span class="method-tag">POST</span>
              <code>/api/shop/products</code>
            </div>
            <div class="api-endpoint">
              <span class="method-tag">GET</span>
              <code>/api/shop/info/{{ shopCode || ':shopCode' }}</code>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Shop,
  ChromeFilled,
  Connection,
  Lock,
  CopyDocument,
  InfoFilled,
  TopRight,
  Picture,
  Download
} from '@element-plus/icons-vue'
import { getStoreLinks } from '../../api/merchant'
import request from '../../utils/request'

const activeTab = ref('store')
const shopCode = ref('')
const shopLink = ref('')
const shopEnabled = ref(true)

/* 加载店铺链接信息 */
const loadShopLinks = async () => {
  try {
    const res = await getStoreLinks()
    shopCode.value = res.data?.shopCode || ''
    shopEnabled.value = res.data?.enabled !== false
    if (shopCode.value) {
      // 构建完整的店铺链接
      const baseUrl = window.location.origin
      shopLink.value = `${baseUrl}/shop/${shopCode.value}`
    }
  } catch (e) {
    // 使用默认演示数据
    shopCode.value = 'demo1234'
    shopLink.value = `${window.location.origin}/shop/demo1234`
  }
}

onMounted(() => {
  loadShopLinks()
})

/* 复制链接 */
const copyLink = (text) => {
  if (!text) return
  navigator.clipboard.writeText(text)
  ElMessage.success('链接已复制')
}

/* 在新标签页打开 */
const openInNewTab = () => {
  if (shopLink.value) {
    window.open(shopLink.value, '_blank')
  }
}

/* 生成二维码 */
const generateQRCode = () => {
  if (!shopCode.value) return
  ElMessage.info('二维码功能开发中')
  // TODO: 集成二维码生成库
}
</script>

<style scoped>
.breadcrumb { font-size: 12px; color: var(--outline); margin-bottom: 4px; }

.links-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: var(--gutter);
  align-items: start;
}

/* 左侧链接类型 */
.link-types-card { padding: 16px; }
.link-type {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 14px; border-radius: var(--radius);
  font-size: 14px; color: var(--outline); cursor: pointer;
  transition: all var(--transition-fast);
}
.link-type:hover { color: var(--on-surface); background: var(--surface-container-low); }
.link-type.active {
  color: var(--primary); background: rgba(0, 65, 216, 0.08);
  font-weight: 600; border-right: 3px solid var(--primary);
}

.security-tip {
  margin-top: 20px; padding: 16px; border-radius: var(--radius);
  background: rgba(0, 65, 216, 0.06);
}
.tip-icon {
  width: 36px; height: 36px; border-radius: var(--radius);
  background: var(--primary); color: white;
  display: flex; align-items: center; justify-content: center; margin-bottom: 10px;
}
.security-tip h5 { font-size: 14px; font-weight: 600; color: var(--on-surface); margin-bottom: 6px; }
.security-tip p { font-size: 12px; color: var(--outline); line-height: 1.5; }

/* 右侧内容 */
.control-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
}
.control-header h4 {
  font-size: 16px; font-weight: 700; color: var(--on-surface);
}
.control-actions { display: flex; gap: 8px; }
.flex-row { display: flex; align-items: center; gap: 8px; }

/* 链接行 */
.link-row { margin-bottom: 20px; }
.link-label { display: block; font-size: 12px; font-weight: 500; color: var(--on-surface-variant); margin-bottom: 8px; }
.link-url-box {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 14px; background: var(--surface-container-low);
  border: 1px solid var(--outline-variant); border-radius: var(--radius);
}
.link-url { flex: 1; font-size: 13px; color: var(--on-surface); word-break: break-all; }
.copy-btn {
  width: 32px; height: 32px; border: none; background: transparent;
  color: var(--outline); cursor: pointer; border-radius: var(--radius-sm);
  display: flex; align-items: center; justify-content: center;
  transition: all var(--transition-fast);
}
.copy-btn:hover { color: var(--primary); background: rgba(0, 65, 216, 0.08); }
.link-extra-actions { display: flex; gap: 8px; margin-top: 10px; }

/* 域名配置步骤 */
.domain-steps { display: flex; flex-direction: column; gap: 16px; }
.step-item { display: flex; gap: 14px; align-items: flex-start; }
.step-num {
  width: 28px; height: 28px; border-radius: 50%; flex-shrink: 0;
  background: rgba(0, 65, 216, 0.08); color: var(--primary);
  font-size: 13px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.step-item h5 { font-size: 14px; font-weight: 600; color: var(--on-surface); margin-bottom: 2px; }
.step-item p { font-size: 13px; color: var(--outline); }
.step-item code {
  padding: 1px 6px; background: var(--surface-container); border-radius: 3px;
  font-size: 12px; color: var(--primary);
}
</style>

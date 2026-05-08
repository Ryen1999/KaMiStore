<template>
  <!-- 批量库存导入 - 匹配 Stitch _3 原型 -->
  <div class="animate-in">
    <div class="page-header">
      <div>
        <p class="breadcrumb">库存管理 &gt; 添加库存</p>
        <h3 class="page-heading">添加库存</h3>
        <p class="page-desc">将虚拟卡密、兑换码或凭证添加到商品库存中。</p>
      </div>
    </div>

    <div class="import-layout">
      <!-- 左侧：导入表单 -->
      <div class="glass-card import-form-card">
        <!-- 选择商品 -->
        <div class="form-row-2">
          <div class="field-block">
            <label class="fb-label">选择商品</label>
            <el-select v-model="selectedProduct" placeholder="请选择要补货的商品..." style="width: 100%;" filterable>
              <el-option v-for="g in groups" :key="g.id" :label="g.productName" :value="g.id" />
            </el-select>
          </div>
          <div class="field-block">
            <label class="fb-label">插入顺序</label>
            <div class="order-btns">
              <button class="order-btn" :class="{ active: insertOrder === 'sequential' }" @click="insertOrder = 'sequential'">
                <el-icon><Sort /></el-icon> 顺序插入
              </button>
              <button class="order-btn" :class="{ active: insertOrder === 'random' }" @click="insertOrder = 'random'">
                <el-icon><Switch /></el-icon> 随机插入
              </button>
            </div>
          </div>
        </div>

        <!-- 导入方式 -->
        <div class="import-mode">
          <button class="mode-tab" :class="{ active: importMode === 'manual' }" @click="importMode = 'manual'">
            <el-icon><Edit /></el-icon> 手动输入
          </button>
          <button class="mode-tab" :class="{ active: importMode === 'file' }" @click="importMode = 'file'">
            <el-icon><UploadFilled /></el-icon> TXT文件上传
          </button>
        </div>

        <!-- 卡密内容 -->
        <div class="field-block">
          <label class="fb-label">卡密内容输入</label>
          <el-input v-model="importText" type="textarea" :rows="10"
            placeholder="YOUR-XXXX-CODE-1234567890&#10;ABCD-EFGH-IJKL-MNOP&#10;9999-8888-7777-6666&#10;&#10;在此粘贴卡密、兑换码或凭证。&#10;每行一条，请确保格式正确以便自动解析。" />
          <div class="import-meta">
            <span class="meta-note">
              <el-icon style="font-size: 14px;"><InfoFilled /></el-icon>
              提示：每行代表一条卡密，请确保格式正确以便自动解析。
            </span>
            <span class="meta-count">
              已识别 <b>{{ importCount }}</b> 条卡密
            </span>
          </div>
        </div>

        <!-- 重复检查 -->
        <div class="check-dup">
          <el-switch v-model="checkDuplicate" />
          <span>检查系统中是否存在重复卡密</span>
        </div>

        <!-- 操作按钮 -->
        <div class="import-actions">
          <el-button size="large" @click="importText = ''">清空表单</el-button>
          <el-button size="large" @click="handleSaveDraft">保存为草稿</el-button>
          <el-button type="primary" size="large" :loading="importing" @click="handleImport" :disabled="!selectedProduct || importCount === 0">
            <el-icon style="font-size: 18px; margin-right: 4px;"><Upload /></el-icon>
            确认添加
          </el-button>
        </div>
      </div>

      <!-- 右侧：提示 & 历史 -->
      <div class="import-side">
        <!-- 快捷提示 -->
        <div class="glass-card tip-card">
          <h4>
            <el-icon style="color: var(--warning);"><Sunny /></el-icon>
            快捷提示
          </h4>
          <ul class="tip-list">
            <li><el-icon><CircleCheck /></el-icon> 超过100条时，建议使用TXT文件上传。</li>
            <li><el-icon><CircleCheck /></el-icon> 顺序插入适用于需要按订单绑定的商品。</li>
            <li><el-icon><CircleCheck /></el-icon> 卡密区分大小写，提交前请仔细核对。</li>
          </ul>
        </div>

        <!-- 最近导入记录 -->
        <div class="glass-card history-card">
          <div class="history-header">
            <h4>批次导入记录</h4>
            <a href="#" class="view-all">查看全部</a>
          </div>
          <div class="history-item" v-for="h in recentHistory" :key="h.name">
            <div class="hi-icon">
              <el-icon><Box /></el-icon>
            </div>
            <div class="hi-info">
              <span class="hi-name">{{ h.name }}</span>
              <span class="hi-detail">{{ h.detail }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { pageProducts } from '../../api/product'
import { importKami } from '../../api/kami'
import { ElMessage } from 'element-plus'
import {
  Sort,
  Switch,
  Edit,
  UploadFilled,
  InfoFilled,
  Upload,
  Sunny,
  CircleCheck,
  Box
} from '@element-plus/icons-vue'

const groups = ref([])
const selectedProduct = ref(null)
const insertOrder = ref('sequential')
const importMode = ref('manual')
const importText = ref('')
const checkDuplicate = ref(true)
const importing = ref(false)

const importCount = computed(() => importText.value.split('\n').filter(l => l.trim()).length)

/* 最近历史（模拟数据） */
const recentHistory = ref([
  { name: 'Steam $20 充值卡', detail: '手动输入 · 50 条卡密' },
  { name: 'Amazon $50 草稿', detail: '手动输入 · 待处理草稿' }
])

onMounted(async () => {
  try {
    const res = await pageProducts({ pageNum: 1, pageSize: 100 })
    groups.value = res.data.list || []
  } catch (e) {}
})

const handleSaveDraft = () => {
  ElMessage.success('已保存为草稿')
}

const handleImport = async () => {
  const kamiList = importText.value.split('\n').map(l => l.trim()).filter(l => l)
  if (!kamiList.length) return
  importing.value = true
  try {
    const res = await importKami({ productId: selectedProduct.value, kamiList })
    ElMessage.success(`添加完成：成功 ${res.data?.successCount || kamiList.length} 条`)
    importText.value = ''
  } finally { importing.value = false }
}
</script>

<style scoped>
.breadcrumb { font-size: 12px; color: var(--outline); margin-bottom: 4px; }

.import-layout { display: grid; grid-template-columns: 1fr 340px; gap: var(--gutter); align-items: start; }
.import-form-card { padding: 28px; }

.form-row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px; }
.field-block { display: flex; flex-direction: column; gap: 6px; margin-bottom: 16px; }
.fb-label { font-size: 13px; font-weight: 600; color: var(--on-surface-variant); }

/* 插入顺序按钮 */
.order-btns { display: flex; gap: 8px; }
.order-btn {
  flex: 1; padding: 10px; border: 1px solid var(--outline-variant);
  border-radius: var(--radius); background: transparent;
  color: var(--on-surface-variant); font-size: 13px; font-weight: 500;
  cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 6px;
  transition: all var(--transition-fast);
}
.order-btn .el-icon { font-size: 18px; }
.order-btn.active { border-color: var(--primary); color: var(--primary); background: rgba(0, 65, 216, 0.04); }
.order-btn:hover { border-color: var(--primary); }

/* 导入模式 */
.import-mode { display: flex; gap: 0; margin-bottom: 16px; }
.mode-tab {
  flex: 1; padding: 10px; border: 1px solid var(--outline-variant);
  background: transparent; color: var(--on-surface-variant);
  font-size: 13px; font-weight: 500; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  transition: all var(--transition-fast);
}
.mode-tab:first-child { border-radius: var(--radius) 0 0 var(--radius); }
.mode-tab:last-child { border-radius: 0 var(--radius) var(--radius) 0; border-left: none; }
.mode-tab.active { background: var(--primary); color: white; border-color: var(--primary); }
.mode-tab .el-icon { font-size: 18px; }

/* 导入元信息 */
.import-meta { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.meta-note { display: flex; align-items: center; gap: 4px; font-size: 12px; color: var(--outline); }
.meta-count { font-size: 13px; color: var(--on-surface-variant); }
.meta-count b { color: var(--primary); font-family: inherit; }

.check-dup { display: flex; align-items: center; gap: 10px; font-size: 14px; color: var(--on-surface-variant); margin-bottom: 20px; }

.import-actions { display: flex; justify-content: center; gap: 12px; padding-top: 16px; border-top: 1px solid rgba(226, 232, 240, 0.6); }

/* 右侧提示 */
.import-side { display: flex; flex-direction: column; gap: var(--gutter); }
.tip-card { padding: 20px; }
.tip-card h4 { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 700; color: var(--on-surface); margin-bottom: 14px; }
.tip-list { list-style: none; display: flex; flex-direction: column; gap: 10px; }
.tip-list li { display: flex; align-items: flex-start; gap: 8px; font-size: 13px; color: var(--on-surface-variant); line-height: 1.5; }
.tip-list .el-icon { font-size: 16px; color: var(--secondary); flex-shrink: 0; margin-top: 2px; }

.history-card { padding: 20px; }
.history-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.history-header h4 { font-size: 14px; font-weight: 700; color: var(--on-surface); }
.view-all { font-size: 11px; font-weight: 600; color: var(--primary); text-decoration: none; text-transform: uppercase; }
.history-item { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid rgba(226, 232, 240, 0.4); }
.history-item:last-child { border-bottom: none; }
.hi-icon {
  width: 36px; height: 36px; border-radius: var(--radius);
  background: rgba(0, 65, 216, 0.06); color: var(--primary);
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.hi-icon .el-icon { font-size: 18px; }
.hi-info { display: flex; flex-direction: column; }
.hi-name { font-size: 13px; font-weight: 600; color: var(--on-surface); }
.hi-detail { font-size: 11px; color: var(--outline); }
</style>

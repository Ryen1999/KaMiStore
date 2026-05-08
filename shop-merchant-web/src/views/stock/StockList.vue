<template>
  <!-- 库存列表 - 基于 KamiItemList 重构 -->
  <div class="animate-in">
    <div class="page-header">
      <div>
        <p class="breadcrumb">库存管理 &gt; 库存列表</p>
        <h3 class="page-heading">库存列表</h3>
        <p class="page-desc">查看和管理所有卡密库存</p>
      </div>
      <div class="header-actions">
        <el-select v-model="query.productId" placeholder="选择商品" clearable style="width: 200px" @change="fetchList">
          <el-option v-for="p in products" :key="p.id" :label="p.productName" :value="p.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="fetchList">
          <el-option label="未售出" :value="0" /><el-option label="已锁定" :value="1" />
          <el-option label="已售出" :value="2" /><el-option label="已作废" :value="3" />
        </el-select>
        <el-button @click="fetchList">
          <el-icon style="margin-right: 4px;"><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stock-stats">
      <div class="ss-card" v-for="s in stockStats" :key="s.label">
        <el-icon :size="24" :style="{ color: s.color }">
          <component :is="s.icon" />
        </el-icon>
        <div>
          <p class="ss-value">{{ s.value }}</p>
          <p class="ss-label">{{ s.label }}</p>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="id" label="ID" width="200" />
        <el-table-column prop="kamiHash" label="哈希标识" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <code class="hash-text">{{ row.kamiHash }}</code>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="statusClass[row.status]">
              {{ statusMap[row.status] }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="导入时间" width="170" />
        <el-table-column prop="soldAt" label="售出时间" width="170">
          <template #default="{ row }">
            <span v-if="row.soldAt">{{ row.soldAt }}</span>
            <span v-else style="color: var(--outline);">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleViewContent(row)">
              <el-icon style="margin-right: 2px;"><View /></el-icon>
              明文
            </el-button>
            <el-button text type="danger" size="small" v-if="row.status === 0" @click="handleVoid(row)">
              <el-icon style="margin-right: 2px;"><CircleClose /></el-icon>
              作废
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-bar">
      <span class="total-text">共 {{ total }} 条</span>
      <el-pagination background layout="prev, pager, next" :total="total"
        :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="fetchList" />
    </div>

    <!-- 查看卡密明文 -->
    <el-dialog v-model="contentVisible" title="卡密明文" width="480px" destroy-on-close>
      <div class="content-display">
        <code>{{ kamiContent }}</code>
        <button class="copy-btn-inline" @click="copyContent">
          <el-icon :size="18"><CopyDocument /></el-icon>
        </button>
      </div>
      <template #footer>
        <el-button @click="contentVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageProducts, pageKamiItems, getKamiContent, voidKami } from '../../api/kami'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, View, CircleClose, CopyDocument, Box, CircleCheck, Van, CircleCloseFilled } from '@element-plus/icons-vue'

const statusMap = { 0: '未售出', 1: '已锁定', 2: '已售出', 3: '已作废' }
const statusClass = { 0: 'active', 1: 'warning', 2: 'processing', 3: 'disabled' }

const loading = ref(false)
const products = ref([])
const list = ref([])
const total = ref(0)
const query = reactive({ productId: null, status: null, pageNum: 1, pageSize: 10 })
const contentVisible = ref(false)
const kamiContent = ref('')

/* 统计 */
const stockStats = ref([
  { icon: Box, label: '总库存', value: 0, color: 'var(--primary)' },
  { icon: CircleCheck, label: '可售', value: 0, color: 'var(--secondary)' },
  { icon: Van, label: '已售出', value: 0, color: 'var(--primary)' },
  { icon: CircleCloseFilled, label: '已作废', value: 0, color: 'var(--danger)' }
])

onMounted(async () => {
  try {
    const res = await pageProducts({ pageNum: 1, pageSize: 100 })
    products.value = res.data.list || []
  } catch (e) {}
  fetchList()
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await pageKamiItems(query)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleViewContent = async (row) => {
  try {
    const res = await getKamiContent(row.id)
    kamiContent.value = res.data
    contentVisible.value = true
  } catch (e) {}
}

const copyContent = () => {
  navigator.clipboard.writeText(kamiContent.value)
  ElMessage.success('已复制到剪贴板')
}

const handleVoid = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入作废原因', '作废卡密', {
    confirmButtonText: '确定作废', cancelButtonText: '取消',
    inputPattern: /\S+/, inputErrorMessage: '请输入作废原因'
  })
  await voidKami(row.id, value)
  ElMessage.success('作废成功')
  fetchList()
}
</script>

<style scoped>
.breadcrumb { font-size: 12px; color: var(--outline); margin-bottom: 4px; }
.header-actions { display: flex; gap: 8px; }

/* 统计卡片 */
.stock-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--gutter); margin-bottom: var(--gutter); }
.ss-card {
  display: flex; align-items: center; gap: 12px; padding: 16px 20px;
  background: var(--surface-container-lowest); border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: var(--radius-md); box-shadow: var(--shadow-sm);
}
.ss-value { font-size: 20px; font-weight: 800; color: var(--on-surface); }
.ss-label { font-size: 12px; color: var(--outline); }

.hash-text {
  font-family: monospace; font-size: 12px; color: var(--outline);
  background: var(--surface-container-low); padding: 2px 6px; border-radius: 3px;
}

/* 明文展示 */
.content-display {
  padding: 20px; background: var(--surface-container-low); border-radius: var(--radius);
  border: 1px solid var(--outline-variant); display: flex; align-items: center; justify-content: space-between;
}
.content-display code {
  font-family: monospace; font-size: 16px; font-weight: 600;
  color: var(--primary); word-break: break-all;
}
.copy-btn-inline {
  width: 32px; height: 32px; border: none; background: transparent;
  color: var(--outline); cursor: pointer; border-radius: var(--radius-sm);
  display: flex; align-items: center; justify-content: center;
}
.copy-btn-inline:hover { color: var(--primary); background: rgba(0, 65, 216, 0.06); }
</style>

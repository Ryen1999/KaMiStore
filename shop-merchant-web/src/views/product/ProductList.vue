<template>
  <!-- 商品列表 - 匹配 Stitch _8 原型 -->
  <div class="animate-in">
    <div class="page-header">
      <div>
        <p class="breadcrumb">商品管理 &gt; 商品列表</p>
        <h3 class="page-heading">商品组合 <span class="total-badge">共 {{ total }} 件商品</span></h3>
      </div>
      <div class="header-actions">
        <el-button size="small">
          <el-icon style="margin-right: 4px;"><Download /></el-icon>
          导出CSV
        </el-button>
        <el-button type="primary" @click="$router.push('/product/add')">
          <el-icon style="margin-right: 4px;"><CirclePlus /></el-icon>
          新建商品
        </el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-group">
        <span class="filter-label">搜索商品</span>
        <el-input v-model="query.productName" placeholder="商品名称或SKU..." clearable style="width: 200px" @keyup.enter="fetchList">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
      <div class="filter-group">
        <span class="filter-label">分类</span>
        <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 160px" @change="fetchList">
          <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
        </el-select>
      </div>
      <div class="filter-group">
        <span class="filter-label">状态</span>
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="fetchList">
          <el-option label="草稿" :value="0" /><el-option label="待审核" :value="1" />
          <el-option label="已上架" :value="2" /><el-option label="已下架" :value="3" />
        </el-select>
      </div>
      <el-button type="primary" @click="fetchList">应用筛选</el-button>
      <button class="icon-btn-sm" @click="fetchList" title="刷新">
        <el-icon><Refresh /></el-icon>
      </button>
    </div>

    <!-- 批量操作栏 -->
    <div class="bulk-bar" v-if="selectedIds.length > 0">
      <el-checkbox v-model="allSelected" @change="toggleAll" />
      <span class="bulk-text">已选中 {{ selectedIds.length }} 项</span>
      <el-button type="danger" size="small" @click="handleBatchDelete">
        <el-icon style="margin-right: 4px;"><Delete /></el-icon>
        批量删除
      </el-button>
      <el-button size="small">
        <el-icon style="margin-right: 4px;"><Box /></el-icon>
        批量归档
      </el-button>
    </div>

    <!-- 商品表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column label="商品名称" min-width="260">
          <template #default="{ row }">
            <div class="product-cell">
              <img v-if="row.mainImage" :src="row.mainImage" class="product-thumb" />
              <div v-else class="product-thumb-ph">
                <el-icon><Picture /></el-icon>
              </div>
              <div>
                <div class="product-name">{{ row.productName }}</div>
                <div class="product-sku">SKU-{{ row.id }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="130">
          <template #default="{ row }">
            <span class="cat-tag">{{ row.categoryName || '未分类' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="130" align="right">
          <template #default="{ row }">
            <div class="price-cell">
              <span class="price-main">
                <span v-if="row.minPrice === row.maxPrice">¥{{ row.minPrice }}</span>
                <span v-else>¥{{ row.minPrice }}~{{ row.maxPrice }}</span>
              </span>
              <span class="price-margin" v-if="row.marginRate">{{ row.marginRate }}% 利润率</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="库存状态" width="130" align="center">
          <template #default="{ row }">
            <div class="stock-cell">
              <span class="stock-num" :class="{ warn: getStock(row) <= 5 }">{{ getStock(row) }}</span>
              <div class="stock-bar">
                <div class="stock-fill" :style="{ width: getStockRate(row) + '%' }"></div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalSales" label="已售" width="80" align="center">
          <template #default="{ row }">
            <span style="font-weight: 600;">{{ row.totalSales || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="上架" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="2" :inactive-value="3" size="small"
              @change="(val) => handleStatus(row, val)" />
          </template>
        </el-table-column>
        <el-table-column label="代理" width="70" align="center">
          <template #default="{ row }">
            <el-checkbox v-model="row.proxyEnabled" disabled />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-bar">
      <el-select v-model="query.pageSize" style="width: 120px;" @change="fetchList">
        <el-option label="每页10条" :value="10" />
        <el-option label="每页20条" :value="20" />
        <el-option label="每页50条" :value="50" />
      </el-select>
      <div class="page-info">显示 {{ (query.pageNum - 1) * query.pageSize + 1 }}-{{ Math.min(query.pageNum * query.pageSize, total) }}，共 {{ total }} 件商品</div>
      <el-pagination background layout="prev, pager, next" :total="total"
        :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="fetchList" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageProducts, deleteProduct, updateProductStatus, listCategories, deleteProducts } from '../../api/product'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, CirclePlus, Search, Refresh, Delete, Box, Picture } from '@element-plus/icons-vue'

const statusMap = { 0: '草稿', 1: '待审核', 2: '已上架', 3: '已下架', 4: '审核驳回' }
const loading = ref(false)
const list = ref([])
const total = ref(0)
const categories = ref([])
const selectedIds = ref([])
const allSelected = ref(false)
const query = reactive({ productName: '', categoryId: null, status: null, pageNum: 1, pageSize: 10 })

const getStock = row => Number(row?.totalStock || 0)
const getStockRate = row => {
  const stock = getStock(row)
  const sales = Number(row?.totalSales || 0)
  const total = stock + sales
  return total > 0 ? Math.min((stock / total) * 100, 100) : 0
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await pageProducts(query)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

onMounted(async () => {
  try { const res = await listCategories(); categories.value = res.data || [] } catch (e) {}
  fetchList()
})

const onSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }
const toggleAll = (val) => { /* 全选切换由 el-table 自动处理 */ }

const handleStatus = async (row, status) => {
  await updateProductStatus(row.id, status)
  ElMessage.success('操作成功')
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
  await deleteProduct(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) return
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 件商品？`, '提示', { type: 'warning' })
  await deleteProducts(selectedIds.value)
  ElMessage.success('删除成功')
  selectedIds.value = []
  fetchList()
}
</script>

<style scoped>
.breadcrumb { font-size: 12px; color: var(--outline); margin-bottom: 4px; }
.header-actions { display: flex; gap: 8px; }
.total-badge {
  font-size: 12px; font-weight: 700; color: var(--primary);
  background: rgba(0, 65, 216, 0.1); padding: 3px 10px; border-radius: var(--radius-full);
  margin-left: 10px; vertical-align: middle;
}

/* 筛选栏 */
.filter-bar {
  display: flex; gap: 14px; align-items: flex-end; margin-bottom: 16px;
  padding: 16px 20px; background: var(--surface-container-lowest);
  border: 1px solid rgba(226, 232, 240, 0.8); border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}
.filter-group { display: flex; flex-direction: column; gap: 4px; }
.filter-label { font-size: 11px; font-weight: 600; color: var(--outline); text-transform: uppercase; letter-spacing: 0.04em; }

/* 批量操作栏 */
.bulk-bar {
  display: flex; align-items: center; gap: 12px; margin-bottom: 12px;
  padding: 10px 16px; background: var(--surface-container-low); border-radius: var(--radius);
}
.bulk-text { font-size: 13px; color: var(--on-surface-variant); font-weight: 500; }

/* 商品单元格 */
.product-cell { display: flex; align-items: center; gap: 12px; }
.product-thumb {
  width: 44px; height: 44px; border-radius: var(--radius); object-fit: cover;
  border: 1px solid rgba(226, 232, 240, 0.8);
}
.product-thumb-ph {
  width: 44px; height: 44px; border-radius: var(--radius);
  background: var(--surface-container-low); display: flex; align-items: center; justify-content: center;
  color: var(--outline-variant);
}
.product-name { font-size: 14px; font-weight: 600; color: var(--on-surface); }
.product-sku { font-size: 11px; color: var(--outline); margin-top: 2px; }

.cat-tag {
  font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: var(--radius-full);
  background: rgba(0, 65, 216, 0.08); color: var(--primary);
}

.price-cell { display: flex; flex-direction: column; align-items: flex-end; }
.price-main { font-size: 15px; font-weight: 700; color: var(--on-surface); }
.price-margin { font-size: 11px; color: var(--secondary); }

/* 库存单元格 */
.stock-cell { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.stock-num { font-size: 15px; font-weight: 700; color: var(--on-surface); }
.stock-num.warn { color: var(--danger); }
.stock-bar { width: 60px; height: 3px; background: var(--surface-container); border-radius: 2px; overflow: hidden; }
.stock-fill { height: 100%; background: var(--primary); border-radius: 2px; transition: width 0.6s ease; }

/* 分页 */
.pagination-bar { display: flex; align-items: center; justify-content: space-between; margin-top: 16px; }
.page-info { font-size: 12px; color: var(--outline); }

/* 通用 */
.icon-btn-sm {
  width: 28px; height: 28px; border: none; background: transparent;
  color: var(--outline); cursor: pointer; border-radius: var(--radius-sm);
  display: inline-flex; align-items: center; justify-content: center;
  transition: all var(--transition-fast);
}
.icon-btn-sm:hover { color: var(--primary); background: rgba(0, 65, 216, 0.06); }
</style>

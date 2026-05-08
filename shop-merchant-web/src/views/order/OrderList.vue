<template>
  <!-- 订单列表 -->
  <div class="animate-in">
    <div class="page-header">
      <div>
        <p class="breadcrumb">订单管理 &gt; 订单列表</p>
        <h3 class="page-heading">订单列表</h3>
        <p class="page-desc">管理和追踪所有虚拟商品交易记录。</p>
      </div>
      <div class="header-actions">
        <el-button size="small">
          <el-icon style="margin-right: 4px;"><Download /></el-icon>
          导出CSV
        </el-button>
        <el-button size="small" @click="fetchList">
          <el-icon style="margin-right: 4px;"><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 状态标签页 -->
    <div class="tab-bar">
      <button class="tab-item" :class="{ active: query.payStatus === null }" @click="query.payStatus = null; fetchList()">全部订单</button>
      <button class="tab-item" :class="{ active: query.payStatus === 1 }" @click="query.payStatus = 1; fetchList()">已付款</button>
      <button class="tab-item" :class="{ active: query.payStatus === 0 }" @click="query.payStatus = 0; fetchList()">未付款</button>
      <button class="tab-item" :class="{ active: query.payStatus === 2 }" @click="query.payStatus = 2; fetchList()">已退款</button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-group">
        <span class="filter-label">订单号搜索</span>
        <el-input v-model="query.orderNo" placeholder="例如 ORD-12345" clearable style="width: 160px" />
      </div>
      <div class="filter-group">
        <span class="filter-label">关键词搜索</span>
        <el-input v-model="query.keyword" placeholder="商品名称、买家名称..." clearable style="width: 200px" />
      </div>
      <div class="filter-group">
        <span class="filter-label">日期范围</span>
        <el-date-picker v-model="query.dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束"
          style="width: 220px;" value-format="YYYY-MM-DD" />
      </div>
      <div class="filter-group">
        <span class="filter-label">支付方式</span>
        <el-select v-model="query.payMethod" placeholder="全部方式" clearable style="width: 140px">
          <el-option label="支付宝" value="alipay" />
          <el-option label="微信支付" value="wechat" />
          <el-option label="银行转账" value="bank" />
        </el-select>
      </div>
    </div>

    <!-- 第二行筛选 -->
    <div class="filter-bar" style="margin-top: -8px;">
      <div class="filter-group">
        <span class="filter-label">支付状态</span>
        <el-select v-model="query.payStatusFilter" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="已付款" :value="1" />
          <el-option label="未付款" :value="0" />
        </el-select>
      </div>
      <div class="filter-group">
        <span class="filter-label">分类</span>
        <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 140px" />
      </div>
      <div class="filter-group">
        <span class="filter-label">订单类型</span>
        <el-select v-model="query.orderType" placeholder="全部类型" clearable style="width: 140px" />
      </div>
      <el-button type="primary" @click="fetchList">
        <el-icon style="margin-right: 4px;"><Filter /></el-icon>
        应用筛选
      </el-button>
    </div>

    <!-- 订单表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading">
        <el-table-column label="订单号 & 类型" min-width="180">
          <template #default="{ row }">
            <div class="order-id-cell">
              <span class="order-no">{{ row.orderNo || 'ORD-' + row.id }}</span>
              <span class="order-type-tag" v-if="row.orderType">{{ row.orderType }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="商品 & 供应商" min-width="200">
          <template #default="{ row }">
            <div class="product-info">
              <span class="pi-name">{{ row.productName || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="160">
          <template #default="{ row }">
            <div class="pay-method-cell">
              <el-icon><CreditCard /></el-icon>
              <span>{{ row.payMethod || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="买家信息" width="140">
          <template #default="{ row }">
            <span>{{ row.buyerContact || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="130" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="getStatusClass(row.status)">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 底部统计 + 分页 -->
    <div class="bottom-bar">
      <div class="bottom-stats">
        <div class="bs-item">
          <el-icon class="bs-icon"><ShoppingCart /></el-icon>
          <div>
            <p class="bs-label">今日总订单</p>
            <p class="bs-value">{{ total }}</p>
          </div>
        </div>
        <div class="bs-item">
          <el-icon class="bs-icon"><CircleCheck /></el-icon>
          <div>
            <p class="bs-label">发放率</p>
            <p class="bs-value">98.2%</p>
          </div>
        </div>
        <div class="bs-item">
          <el-icon class="bs-icon"><Flag /></el-icon>
          <div>
            <p class="bs-label">待审核</p>
            <p class="bs-value link">14</p>
          </div>
        </div>
      </div>
      <el-pagination background layout="prev, pager, next, jumper" :total="total"
        :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="fetchList" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageOrders } from '../../api/order'
import {
  Download,
  Refresh,
  Filter,
  CreditCard,
  ShoppingCart,
  CircleCheck,
  Flag
} from '@element-plus/icons-vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({
  orderNo: '', keyword: '', dateRange: null, payMethod: null,
  payStatus: null, payStatusFilter: null, categoryId: null, orderType: null,
  pageNum: 1, pageSize: 10
})

/* 状态映射 */
const getStatusClass = (s) => {
  const map = { 0: 'warning', 1: 'active', 2: 'processing', 3: 'disabled' }
  return map[s] || ''
}
const getStatusText = (s) => {
  const map = { 0: '未付款', 1: '已付款', 2: '处理中', 3: '已退款' }
  return map[s] || '未知'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await pageOrders(query)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    /* 接口未就绪时使用空数据 */
    list.value = []
    total.value = 0
  } finally { loading.value = false }
}
onMounted(fetchList)
</script>

<style scoped>
.breadcrumb { font-size: 12px; color: var(--outline); margin-bottom: 4px; }
.header-actions { display: flex; gap: 8px; }

/* 标签页 */
.tab-bar {
  display: flex; gap: 0; margin-bottom: 16px;
  border-bottom: 2px solid rgba(226, 232, 240, 0.6);
}
.tab-item {
  padding: 10px 20px; border: none; background: transparent;
  font-size: 14px; font-weight: 500; color: var(--outline);
  cursor: pointer; border-bottom: 2px solid transparent;
  margin-bottom: -2px; transition: all var(--transition-fast);
}
.tab-item:hover { color: var(--on-surface); }
.tab-item.active { color: var(--primary); border-bottom-color: var(--primary); font-weight: 600; }

/* 筛选栏 */
.filter-bar {
  display: flex; gap: 14px; align-items: flex-end; margin-bottom: 16px;
  padding: 14px 20px; background: var(--surface-container-lowest);
  border: 1px solid rgba(226, 232, 240, 0.8); border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}
.filter-group { display: flex; flex-direction: column; gap: 4px; }
.filter-label { font-size: 11px; font-weight: 600; color: var(--outline); letter-spacing: 0.04em; }

/* 订单ID单元格 */
.order-id-cell { display: flex; flex-direction: column; gap: 2px; }
.order-no { font-size: 13px; font-weight: 700; color: var(--primary); }
.order-type-tag { font-size: 11px; color: var(--outline); }

.product-info { display: flex; flex-direction: column; }
.pi-name { font-size: 13px; font-weight: 500; }

.pay-method-cell { display: flex; align-items: center; gap: 6px; font-size: 13px; }

/* 底部统计 */
.bottom-bar {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 20px; padding: 20px; background: var(--surface-container-lowest);
  border: 1px solid rgba(226, 232, 240, 0.8); border-radius: var(--radius-md);
}
.bottom-stats { display: flex; gap: 40px; }
.bs-item { display: flex; align-items: center; gap: 10px; }
.bs-icon { font-size: 22px; color: var(--primary); }
.bs-label { font-size: 11px; color: var(--outline); }
.bs-value { font-size: 20px; font-weight: 800; color: var(--on-surface); }
.bs-value.link { color: var(--primary); cursor: pointer; }
</style>

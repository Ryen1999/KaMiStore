<template>
  <!-- 提现列表 -->
  <div class="animate-in">
    <div class="page-header">
      <div>
        <p class="breadcrumb">结算管理 &gt; 提现列表</p>
        <h3 class="page-heading">提现列表</h3>
      </div>
      <el-button size="small">
        <el-icon style="margin-right: 4px;"><Download /></el-icon>
        导出列表
      </el-button>
    </div>

    <!-- 概览统计 -->
    <div class="overview-grid">
      <div class="ov-card">
        <p class="ov-label">累计结算金额</p>
        <h3 class="ov-value">¥1,246,500.00</h3>
        <span class="ov-trend up">较上月 +10.5%</span>
      </div>
      <div class="ov-card">
        <p class="ov-label">待结算金额</p>
        <h3 class="ov-value">¥42,180.50</h3>
        <span class="ov-sub">3 笔待处理</span>
      </div>
      <div class="ov-card">
        <p class="ov-label">本月手续费</p>
        <h3 class="ov-value">¥3,420.00</h3>
        <span class="ov-sub">平均每笔 0.21%</span>
      </div>
      <div class="ov-card highlight">
        <p class="ov-label" style="color: rgba(255,255,255,0.7);">当前余额</p>
        <h3 class="ov-value" style="color: white; font-size: 28px;">¥312,450.00</h3>
        <el-button type="primary" size="small" style="margin-top: 10px; background: white; color: var(--primary); border: none;"
          @click="$router.push('/settlement/withdraw')">
          申请提现
        </el-button>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <div class="filter-group">
        <span class="filter-label">交易编号</span>
        <el-input v-model="query.searchId" placeholder="TXN-082..." clearable style="width: 160px" />
      </div>
      <div class="filter-group">
        <span class="filter-label">状态</span>
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="已完成" value="completed" />
          <el-option label="处理中" value="processing" />
          <el-option label="待审批" value="pending" />
          <el-option label="已退回" value="returned" />
        </el-select>
      </div>
      <div class="filter-group">
        <span class="filter-label">日期范围</span>
        <el-date-picker v-model="query.dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束"
          style="width: 240px;" value-format="YYYY-MM-DD" />
      </div>
      <el-button type="primary" @click="fetchList">
        <el-icon style="margin-right: 4px;"><Filter /></el-icon>
        筛选
      </el-button>
    </div>

    <!-- 提现列表 -->
    <div class="table-card">
      <div class="table-header-row">
        <h4>近期交易记录</h4>
        <span class="showing-text">显示 1-{{ list.length }} 共 {{ total }} 条记录</span>
      </div>
      <el-table :data="list" v-loading="loading">
        <el-table-column label="发起时间" width="150">
          <template #default="{ row }">{{ row.createdAt || '-' }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="130" align="right">
          <template #default="{ row }">
            <span class="amount-text">¥{{ row.amount || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="fee" label="手续费" width="100" align="right">
          <template #default="{ row }">¥{{ row.fee || '0.00' }}</template>
        </el-table-column>
        <el-table-column label="实际到账" width="140" align="right">
          <template #default="{ row }">
            <span style="font-weight: 700; color: var(--on-surface);">¥{{ row.actualAmount || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结算状态" width="160" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="row.statusClass || 'active'">
              {{ row.statusText || '已完成' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="结算时间" width="150">
          <template #default="{ row }">{{ row.settledAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="" width="60" align="center">
          <template #default>
            <button class="icon-btn-sm">
              <el-icon><MoreFilled /></el-icon>
            </button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-bar">
      <span class="total-text">每页显示:</span>
      <el-pagination background layout="prev, pager, next" :total="total"
        :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="fetchList" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Download, Filter, MoreFilled } from '@element-plus/icons-vue'
import { pageWithdrawals } from '../../api/settlement'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ searchId: '', status: null, dateRange: null, pageNum: 1, pageSize: 10 })

const fetchList = async () => {
  loading.value = true
  try {
    const res = await pageWithdrawals(query)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally { loading.value = false }
}
onMounted(fetchList)
</script>

<style scoped>
.breadcrumb { font-size: 12px; color: var(--outline); margin-bottom: 4px; }

/* 概览网格 */
.overview-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--gutter); margin-bottom: var(--gutter); }
.ov-card {
  background: var(--surface-container-lowest); padding: 20px;
  border-radius: var(--radius-md); border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: var(--shadow-card);
}
.ov-card.highlight {
  background: linear-gradient(135deg, #0041d8, #2d5cf7);
  border: none;
}
.ov-label { font-size: 12px; color: var(--outline); margin-bottom: 6px; }
.ov-value { font-size: 22px; font-weight: 800; color: var(--on-surface); }
.ov-trend { font-size: 12px; }
.ov-trend.up { color: var(--secondary); }
.ov-sub { font-size: 12px; color: var(--outline); }

/* 筛选 */
.filter-bar {
  display: flex; gap: 14px; align-items: flex-end; margin-bottom: 16px;
  padding: 14px 20px; background: var(--surface-container-lowest);
  border: 1px solid rgba(226, 232, 240, 0.8); border-radius: var(--radius-md);
}
.filter-group { display: flex; flex-direction: column; gap: 4px; }
.filter-label { font-size: 11px; font-weight: 600; color: var(--outline); text-transform: uppercase; }

/* 表格头 */
.table-header-row {
  display: flex; justify-content: space-between; align-items: center; padding: 14px 20px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
}
.table-header-row h4 { font-size: 15px; font-weight: 700; }
.showing-text { font-size: 12px; color: var(--outline); }

.amount-text { font-weight: 600; }

.icon-btn-sm {
  width: 28px; height: 28px; border: none; background: transparent;
  color: var(--outline); cursor: pointer; border-radius: var(--radius-sm);
  display: inline-flex; align-items: center; justify-content: center;
}
.icon-btn-sm:hover { color: var(--primary); background: rgba(0, 65, 216, 0.06); }
</style>

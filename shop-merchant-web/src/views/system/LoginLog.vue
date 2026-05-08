<template>
  <!-- 登录日志 -->
  <div class="animate-in">
    <div class="page-header">
      <div>
        <p class="breadcrumb">系统管理 &gt; 登录日志</p>
        <h3 class="page-heading">安全审计追踪</h3>
        <p class="page-desc">显示最近30天的记录，用于合规监控。</p>
      </div>
      <el-button size="small">
        <el-icon style="margin-right: 4px;"><Download /></el-icon>
        导出CSV
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="audit-stats">
      <div class="as-card">
        <h3 class="as-value">{{ stats.totalLogins }}</h3>
        <p class="as-label">总登录次数</p>
        <span class="as-trend up">+{{ stats.loginGrowth }}%</span>
      </div>
      <div class="as-card">
        <h3 class="as-value">{{ stats.uniqueIps }}</h3>
        <p class="as-label">独立IP地址</p>
        <span class="as-sub">稳定状态</span>
      </div>
      <div class="as-card">
        <h3 class="as-value warn">{{ stats.failedAttempts }}</h3>
        <p class="as-label">失败尝试</p>
        <span class="as-trend down">高风险警告</span>
      </div>
      <div class="as-card">
        <h3 class="as-value">{{ stats.peakTime }}</h3>
        <p class="as-label">登录高峰时段</p>
        <span class="as-sub">UTC+8 时区</span>
      </div>
    </div>

    <!-- 日志列表 -->
    <div class="table-card">
      <div class="table-header-row">
        <h4>登录记录</h4>
        <span class="audit-badge">审计已启用</span>
      </div>
      <el-table :data="list" v-loading="loading">
        <el-table-column label="商户" width="80" align="center">
          <template #default="{ row }">
            <div class="avatar-sm" :class="row.avatarColor">
              {{ (row.username || '?')[0].toUpperCase() }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="160">
          <template #default="{ row }">
            <span class="username-text">{{ row.username || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="loginTime" label="登录时间" width="170" />
        <el-table-column prop="loginIp" label="登录IP" width="160">
          <template #default="{ row }">
            <code class="ip-text" :class="{ danger: row.suspicious }">{{ row.loginIp || '-' }}</code>
          </template>
        </el-table-column>
        <el-table-column label="位置" width="160">
          <template #default="{ row }">
            <div class="location-cell">
              <span>{{ row.location || '未知' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="row.success ? 'active' : 'disabled'">
              {{ row.success ? '成功' : '失败' }}
            </span>
          </template>
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
      <span class="total-text">显示第 1 至 {{ list.length }} 条，共 {{ total }} 条记录</span>
      <el-pagination background layout="prev, pager, next" :total="total"
        :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="fetchList" />
    </div>

    <!-- 数据保留策略 -->
    <div class="retention-banner">
      <el-icon class="retention-icon"><InfoFilled /></el-icon>
      <div>
        <strong>数据保留策略</strong>
        <p>为确保平台性能，登录日志将保留30天，之后归档至冷存储。如需查看更早的记录，请联系安全团队。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Download, MoreFilled, InfoFilled } from '@element-plus/icons-vue'
import { pageLoginLogs } from '../../api/system'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

const stats = reactive({
  totalLogins: 12482, uniqueIps: 842, failedAttempts: 28, peakTime: '09:00 AM',
  loginGrowth: 14.2
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await pageLoginLogs(query)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    /* 使用模拟数据 */
    list.value = [
      { username: 'john_doe_admin', loginTime: '2023-11-24 14:27:01', loginIp: '192.168.1.184', location: '新加坡', success: true, avatarColor: 'blue' },
      { username: 'alice_smith', loginTime: '2023-11-24 13:45:12', loginIp: '172.18.254.1', location: '伦敦, 英国', success: true, avatarColor: 'green' },
      { username: 'root_access', loginTime: '2023-11-23 13:02:44', loginIp: '45.22.111.88', location: '莫斯科, 俄罗斯', success: false, suspicious: true, avatarColor: 'red' },
      { username: 'bob_vance_99', loginTime: '2023-11-24 12:55:30', loginIp: '8.8.8.8', location: '纽约, 美国', success: true, avatarColor: 'purple' },
      { username: 'sarah_king_mgr', loginTime: '2023-11-24 11:20:18', loginIp: '10.0.0.45', location: '内网', success: true, avatarColor: 'teal' }
    ]
    total.value = 1240
  } finally { loading.value = false }
}
onMounted(fetchList)
</script>

<style scoped>
.breadcrumb { font-size: 12px; color: var(--outline); margin-bottom: 4px; }

/* 统计卡片 */
.audit-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--gutter); margin-bottom: var(--gutter); }
.as-card {
  background: var(--surface-container-lowest); padding: 20px;
  border-radius: var(--radius-md); border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: var(--shadow-card);
}
.as-value { font-size: 28px; font-weight: 800; color: var(--on-surface); margin-bottom: 4px; }
.as-value.warn { color: var(--danger); }
.as-label { font-size: 12px; color: var(--outline); margin-bottom: 6px; }
.as-trend { font-size: 12px; font-weight: 600; }
.as-trend.up { color: var(--secondary); }
.as-trend.down { color: var(--danger); }
.as-sub { font-size: 12px; color: var(--outline); }

/* 表格头 */
.table-header-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 20px; border-bottom: 1px solid rgba(226, 232, 240, 0.6);
}
.table-header-row h4 { font-size: 15px; font-weight: 700; }
.audit-badge {
  font-size: 10px; font-weight: 700; color: var(--secondary);
  background: rgba(0, 107, 92, 0.08); padding: 3px 10px; border-radius: var(--radius-full);
  text-transform: uppercase; letter-spacing: 0.05em;
}

/* 头像 */
.avatar-sm {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; color: white;
  margin: 0 auto;
}
.avatar-sm.blue { background: var(--primary); }
.avatar-sm.green { background: var(--secondary); }
.avatar-sm.red { background: var(--danger); }
.avatar-sm.purple { background: #6366f1; }
.avatar-sm.teal { background: #0891b2; }

.username-text { font-weight: 500; }
.ip-text { font-size: 12px; padding: 2px 6px; background: var(--surface-container-low); border-radius: 3px; }
.ip-text.danger { background: rgba(186, 26, 26, 0.08); color: var(--danger); }
.location-cell { font-size: 13px; }

/* 数据保留横幅 */
.retention-banner {
  display: flex; align-items: flex-start; gap: 12px; margin-top: 20px;
  padding: 16px 20px; background: var(--surface-container-low);
  border: 1px solid rgba(226, 232, 240, 0.6); border-radius: var(--radius-md);
}
.retention-icon { font-size: 20px; color: var(--primary); flex-shrink: 0; margin-top: 2px; }
.retention-banner strong { font-size: 14px; color: var(--on-surface); }
.retention-banner p { font-size: 13px; color: var(--outline); margin-top: 4px; line-height: 1.5; }

.icon-btn-sm {
  width: 28px; height: 28px; border: none; background: transparent;
  color: var(--outline); cursor: pointer; border-radius: var(--radius-sm);
  display: inline-flex; align-items: center; justify-content: center;
}
.icon-btn-sm:hover { color: var(--primary); background: rgba(0, 65, 216, 0.06); }
</style>

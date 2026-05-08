<template>
  <!-- 控制台仪表盘 -->
  <div class="dashboard animate-in">
    <!-- 统计卡片 Bento Grid -->
    <div class="stat-grid">
      <div class="stat-card" v-for="(stat, i) in mainStats" :key="i">
        <div class="stat-top">
          <div class="stat-icon-wrap">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <span class="stat-sub-label">{{ stat.subLabel }}</span>
        </div>
        <h3 class="stat-value">{{ stat.value }}</h3>
        <div class="stat-trend" v-if="stat.trend">
          <el-icon class="trend-icon"><TrendCharts /></el-icon>
          <span>{{ stat.trend }}</span>
        </div>
        <p class="stat-note" v-if="stat.note">{{ stat.note }}</p>
      </div>
    </div>

    <!-- 次要数据行 -->
    <div class="secondary-grid">
      <div class="sec-card" v-for="(item, i) in secondaryStats" :key="i">
        <div class="sec-icon-wrap">
          <el-icon :size="20"><component :is="item.icon" /></el-icon>
        </div>
        <div>
          <p class="sec-label">{{ item.label }}</p>
          <p class="sec-value">{{ item.value }}</p>
        </div>
      </div>
    </div>

    <!-- 主体内容：罗盘 + 图表 -->
    <div class="main-grid">
      <!-- 生意罗盘 -->
      <div class="compass-card">
        <div class="compass-header">
          <h4>生意罗盘</h4>
          <el-icon style="color: var(--outline-variant);"><InfoFilled /></el-icon>
        </div>
        <div class="compass-ring">
          <svg viewBox="0 0 192 192" class="ring-svg">
            <circle cx="96" cy="96" r="80" fill="transparent" stroke="var(--surface-container)" stroke-width="12" />
            <circle cx="96" cy="96" r="80" fill="transparent" stroke="var(--primary)" stroke-width="12"
              stroke-dasharray="502.4" stroke-dashoffset="0" stroke-linecap="round"
              style="transform: rotate(-90deg); transform-origin: center;" />
          </svg>
          <div class="ring-center">
            <span class="ring-num">{{ uvCount }}</span>
            <span class="ring-label">今日访客 UV</span>
          </div>
        </div>
        <div class="growth-badge">
          <el-icon :size="18"><Top /></el-icon>
          <span>增长趋势 100%</span>
        </div>
        <p class="compass-tip">店铺流量当前处于极速增长阶段，建议增加热门商品库存。</p>
      </div>

      <!-- 交易统计图表 -->
      <div class="chart-card">
        <div class="chart-header">
          <div>
            <h4>交易统计</h4>
            <p class="chart-sub">最近七天业务增长走势图</p>
          </div>
          <div class="chart-tabs">
            <button class="chart-tab" :class="{ active: chartTab === '7d' }" @click="chartTab = '7d'">七日统计</button>
            <button class="chart-tab" :class="{ active: chartTab === '30d' }" @click="chartTab = '30d'">月度统计</button>
          </div>
        </div>
        <!-- SVG 折线图 -->
        <div class="chart-area">
          <svg viewBox="0 0 800 300" class="chart-svg">
            <defs>
              <linearGradient id="blueGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#2d5cf7" stop-opacity="0.15" />
                <stop offset="100%" stop-color="#ffffff" stop-opacity="0" />
              </linearGradient>
            </defs>
            <!-- 网格线 -->
            <line v-for="n in 5" :key="n" x1="0" :y1="n * 60 - 60" x2="800" :y2="n * 60 - 60" stroke="var(--surface-container)" stroke-width="1" />
            <!-- 填充区域 -->
            <path d="M0,250 L114,180 L228,210 L342,120 L456,150 L570,80 L684,100 L800,40 L800,300 L0,300 Z" fill="url(#blueGrad)" />
            <!-- 交易额线 -->
            <path d="M0,250 L114,180 L228,210 L342,120 L456,150 L570,80 L684,100 L800,40" fill="none" stroke="#2d5cf7" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
            <!-- 结算额线 -->
            <path d="M0,280 L114,230 L228,250 L342,180 L456,200 L570,140 L684,160 L800,120" fill="none" stroke="#68fadd" stroke-width="3" stroke-dasharray="6,4" stroke-linecap="round" />
          </svg>
        </div>
        <div class="chart-legend">
          <div class="legend-item">
            <span class="legend-dot" style="background: var(--primary);"></span>
            <span>交易金额</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot" style="background: var(--secondary);"></span>
            <span>结算金额</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { pageProducts } from '../api/product'
import { pageKamiItems } from '../api/kami'
import {
  Wallet,
  Goods,
  ShoppingCart,
  Money,
  TrendCharts,
  DataLine,
  Timer,
  Top,
  InfoFilled
} from '@element-plus/icons-vue'

/* 统计数据 */
const uvCount = ref(1840)
const chartTab = ref('7d')

const mainStats = ref([
  { icon: 'Wallet', subLabel: '账户余额', value: '¥128,450.00', trend: '较上月 +12.5%', trendUp: true },
  { icon: 'Goods', subLabel: '商品总数', value: '1,248', note: '在售商品数量' },
  { icon: 'ShoppingCart', subLabel: '今日订单', value: '342', trend: '较昨日 +8%', trendUp: true },
  { icon: 'Money', subLabel: '待结算金额', value: '¥15,200.00', note: '下一周期结算' }
])

const secondaryStats = ref([
  { icon: 'DataLine', label: '今日流水', value: '¥45,280.00' },
  { icon: 'Money', label: '今日利润', value: '¥8,940.00' },
  { icon: 'Timer', label: '昨日流水', value: '¥42,150.00' },
  { icon: 'Wallet', label: '昨日利润', value: '¥7,820.00' }
])

/* 加载真实数据覆盖默认值 */
onMounted(async () => {
  try {
    const pRes = await pageProducts({ pageNum: 1, pageSize: 1 })
    const kRes = await pageKamiItems({ pageNum: 1, pageSize: 1, status: 0 })
    const productCount = pRes.data.total || 0
    const stockCount = kRes.data.total || 0
    /* 用真实数据更新卡片 */
    mainStats.value[1].value = productCount.toLocaleString()
    mainStats.value[1].note = `库存 ${stockCount} 件`
  } catch (e) { /* 使用默认模拟数据 */ }
})
</script>

<style scoped>
.dashboard > * { animation: fadeInUp 0.4s ease-out both; }

/* 主统计卡片网格 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--gutter);
  margin-bottom: var(--gutter);
}
.stat-card {
  background: var(--surface-container-lowest);
  padding: 20px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: var(--shadow-card);
  transition: all var(--transition-base);
}
.stat-card:hover {
  border-color: rgba(0, 65, 216, 0.2);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.stat-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.stat-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(0, 65, 216, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
  transition: transform var(--transition-fast);
}
.stat-card:hover .stat-icon-wrap { transform: scale(1.1); }
.stat-sub-label {
  font-size: 12px;
  color: var(--outline);
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--on-surface);
  line-height: 1.2;
}
.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 13px;
  color: var(--secondary);
}
.trend-icon { font-size: 16px; }
.stat-note {
  font-size: 12px;
  color: var(--outline);
  margin-top: 8px;
}

/* 次要数据网格 */
.secondary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--gutter);
  margin-bottom: var(--gutter);
}
.sec-card {
  background: var(--surface-container-lowest);
  padding: 16px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(226, 232, 240, 0.8);
  display: flex;
  align-items: center;
  gap: 16px;
}
.sec-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(0, 65, 216, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
  flex-shrink: 0;
}
.sec-label { font-size: 12px; color: var(--outline); margin-bottom: 2px; }
.sec-value { font-size: 15px; font-weight: 700; color: var(--on-surface); }

/* 主体网格：罗盘 + 图表 */
.main-grid {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: var(--gutter);
}

/* 生意罗盘 */
.compass-card {
  background: var(--surface-container-lowest);
  padding: 24px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: var(--shadow-card);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.compass-header {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.compass-header h4 {
  font-size: 20px;
  font-weight: 600;
  color: var(--on-surface);
}
.compass-ring {
  position: relative;
  width: 192px;
  height: 192px;
  margin-bottom: 20px;
}
.ring-svg { width: 100%; height: 100%; }
.ring-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.ring-num {
  font-size: 28px;
  font-weight: 800;
  color: var(--on-surface);
}
.ring-label { font-size: 12px; color: var(--outline); }
.growth-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-full);
  background: rgba(0, 107, 92, 0.1);
  color: var(--secondary);
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 12px;
}
.compass-tip { font-size: 14px; color: var(--outline); max-width: 200px; line-height: 1.5; }

/* 交易统计图表 */
.chart-card {
  background: var(--surface-container-lowest);
  padding: 24px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: var(--shadow-card);
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}
.chart-header h4 {
  font-size: 20px;
  font-weight: 600;
  color: var(--on-surface);
}
.chart-sub { font-size: 12px; color: var(--outline); margin-top: 2px; }
.chart-tabs {
  display: flex;
  background: var(--surface-container);
  padding: 3px;
  border-radius: var(--radius);
}
.chart-tab {
  padding: 6px 16px;
  border: none;
  background: transparent;
  font-size: 13px;
  font-weight: 500;
  color: var(--outline);
  cursor: pointer;
  border-radius: 5px;
  transition: all var(--transition-fast);
}
.chart-tab.active {
  background: var(--surface-container-lowest);
  color: var(--on-surface);
  font-weight: 600;
  box-shadow: var(--shadow-sm);
}
.chart-area {
  width: 100%;
  height: 280px;
}
.chart-svg { width: 100%; height: 100%; }
.chart-legend {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-top: 16px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--outline);
}
.legend-dot { width: 10px; height: 10px; border-radius: 50%; }
</style>

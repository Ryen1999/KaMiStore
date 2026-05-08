<template>
  <div class="layout-shell">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <!-- 品牌区 -->
      <div class="sidebar-brand" @click="$router.push('/')">
        <div class="brand-icon">
          <el-icon :size="22"><Shop /></el-icon>
        </div>
        <div>
          <h2 class="brand-title">商户管理中心</h2>
          <p class="brand-sub">虚拟商品平台</p>
        </div>
      </div>

      <!-- 导航菜单 -->
      <nav class="sidebar-nav">
        <router-link to="/dashboard" class="nav-item" :class="{ active: $route.path === '/dashboard' }">
          <el-icon><DataBoard /></el-icon>
          <span>控制台</span>
        </router-link>

        <!-- 店铺管理 -->
        <div class="nav-group">
          <div class="nav-group-label" @click="toggleGroup('shop')">
            <el-icon><Shop /></el-icon>
            <span>店铺管理</span>
            <el-icon class="nav-arrow" :class="{ expanded: expandedGroups.shop }"><ArrowDown /></el-icon>
          </div>
          <div class="nav-sub" v-show="expandedGroups.shop">
            <router-link to="/store/links" class="nav-item sub" :class="{ active: $route.path === '/store/links' }">
              <span>店铺链接</span>
            </router-link>
            <router-link to="/store/settings" class="nav-item sub" :class="{ active: $route.path === '/store/settings' }">
              <span>基础设置</span>
            </router-link>
            <router-link to="/store/payment" class="nav-item sub" :class="{ active: $route.path === '/store/payment' }">
              <span>支付方式</span>
            </router-link>
          </div>
        </div>

        <!-- 商品管理 -->
        <div class="nav-group">
          <div class="nav-group-label" @click="toggleGroup('goods')">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
            <el-icon class="nav-arrow" :class="{ expanded: expandedGroups.goods }"><ArrowDown /></el-icon>
          </div>
          <div class="nav-sub" v-show="expandedGroups.goods">
            <router-link to="/product/list" class="nav-item sub" :class="{ active: $route.path.startsWith('/product/list') || $route.path.startsWith('/product/add') || $route.path.startsWith('/product/edit') }">
              <span>商品列表</span>
            </router-link>
            <router-link to="/product/category" class="nav-item sub" :class="{ active: $route.path === '/product/category' }">
              <span>分类管理</span>
            </router-link>
          </div>
        </div>

        <!-- 订单管理 -->
        <router-link to="/order/list" class="nav-item" :class="{ active: $route.path.startsWith('/order') }">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </router-link>

        <!-- 库存管理 -->
        <div class="nav-group">
          <div class="nav-group-label" @click="toggleGroup('inventory')">
            <el-icon><Box /></el-icon>
            <span>库存管理</span>
            <el-icon class="nav-arrow" :class="{ expanded: expandedGroups.inventory }"><ArrowDown /></el-icon>
          </div>
          <div class="nav-sub" v-show="expandedGroups.inventory">
            <router-link to="/stock/import" class="nav-item sub" :class="{ active: $route.path === '/stock/import' }">
              <span>添加库存</span>
            </router-link>
            <router-link to="/stock/list" class="nav-item sub" :class="{ active: $route.path === '/stock/list' }">
              <span>库存列表</span>
            </router-link>
          </div>
        </div>

        <!-- 结算管理 -->
        <div class="nav-group">
          <div class="nav-group-label" @click="toggleGroup('settlement')">
            <el-icon><Wallet /></el-icon>
            <span>结算管理</span>
            <el-icon class="nav-arrow" :class="{ expanded: expandedGroups.settlement }"><ArrowDown /></el-icon>
          </div>
          <div class="nav-sub" v-show="expandedGroups.settlement">
            <router-link to="/settlement/withdraw" class="nav-item sub" :class="{ active: $route.path === '/settlement/withdraw' }">
              <span>申请提现</span>
            </router-link>
            <router-link to="/settlement/list" class="nav-item sub" :class="{ active: $route.path === '/settlement/list' }">
              <span>提现列表</span>
            </router-link>
          </div>
        </div>

        <!-- 系统管理 -->
        <div class="nav-group-divider"></div>
        <div class="nav-group-section">系统管理</div>
        <router-link to="/system/login-log" class="nav-item" :class="{ active: $route.path === '/system/login-log' }">
          <el-icon><Clock /></el-icon>
          <span>登录日志</span>
        </router-link>
      </nav>

      <!-- 底部操作 -->
      <div class="sidebar-footer">
        <router-link to="/system/settings" class="nav-item" :class="{ active: $route.path === '/system/settings' }">
          <el-icon><Setting /></el-icon>
          <span>修改密码</span>
        </router-link>
        <a class="nav-item" @click="handleLogout" style="cursor: pointer;">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </a>
      </div>
    </aside>

    <!-- 主体区域 -->
    <div class="layout-main">
      <!-- 顶栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <div class="search-box">
            <el-icon class="search-icon"><Search /></el-icon>
            <input type="text" placeholder="搜索订单、商品..." class="search-input" />
          </div>
        </div>
        <div class="topbar-right">
          <!-- 通知下拉 -->
          <el-popover placement="bottom-end" :width="320" trigger="click">
            <template #reference>
              <el-badge :value="unreadCount" :max="99" class="notify-badge" :hidden="unreadCount===0">
                <el-icon :size="20" style="cursor:pointer;"><Bell /></el-icon>
              </el-badge>
            </template>
            <div class="notify-panel">
              <div class="notify-header">
                <span class="notify-title">系统通知</span>
                <el-button link type="primary" size="small" @click="handleReadAll">全部已读</el-button>
              </div>
              <div class="notify-list" v-if="messages.length">
                <div class="notify-item" v-for="msg in messages" :key="msg.id" :class="{ unread: !msg.isRead }">
                  <div class="notify-dot" v-if="!msg.isRead"></div>
                  <div class="notify-body">
                    <p class="notify-text">{{ msg.title }}</p>
                    <span class="notify-time">{{ msg.createdAt }}</span>
                  </div>
                </div>
              </div>
              <div class="notify-empty" v-else>暂无通知</div>
            </div>
          </el-popover>

          <el-button type="primary" size="small" @click="$router.push('/settlement/withdraw')">申请提现</el-button>

          <!-- 头像下拉 -->
          <el-dropdown trigger="click" @command="handleAvatarCmd">
            <div class="user-avatar" :title="username">
              {{ (username || 'A')[0].toUpperCase() }}
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <span style="font-weight:600;">{{ username }}</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="log">登录日志</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容 -->
      <main class="content-area">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { logout } from '../api/auth'
import { pageMessages, readAllMessages } from '../api/system'

const router = useRouter()
const route = useRoute()
const username = computed(() => localStorage.getItem('username') || '')

/* 侧边栏折叠组 */
const expandedGroups = reactive({
  shop: true,
  goods: true,
  inventory: true,
  settlement: true
})
const toggleGroup = (key) => { expandedGroups[key] = !expandedGroups[key] }

/* 通知消息 */
const messages = ref([])
const unreadCount = ref(0)
const fetchMessages = async () => {
  try {
    const res = await pageMessages({ pageNum: 1, pageSize: 5 })
    messages.value = res.data?.list || []
    unreadCount.value = messages.value.filter(m => !m.isRead).length
  } catch (e) {
    messages.value = []
    unreadCount.value = 0
  }
}
const handleReadAll = async () => {
  try { await readAllMessages() } catch (e) { /* ignore */ }
  messages.value.forEach(m => m.isRead = 1)
  unreadCount.value = 0
}
onMounted(fetchMessages)

/* 头像下拉命令 */
const handleAvatarCmd = (cmd) => {
  if (cmd === 'password') router.push('/system/settings')
  else if (cmd === 'log') router.push('/system/login-log')
  else if (cmd === 'logout') handleLogout()
}

/* 退出登录 */
const handleLogout = async () => {
  try { await logout() } catch (e) { /* ignore */ }
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
/* ===== 布局壳 ===== */
.layout-shell {
  display: flex;
  min-height: 100vh;
  background: var(--background);
}

/* ===== 侧边栏 ===== */
.sidebar {
  width: 220px;
  background: var(--surface-container-lowest);
  border-right: 1px solid rgba(226, 232, 240, 0.8);
  display: flex;
  flex-direction: column;
  padding: 20px 12px;
  gap: 4px;
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  z-index: 40;
  overflow-y: auto;
}

/* 品牌区 */
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 8px;
  margin-bottom: 24px;
  cursor: pointer;
}
.brand-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius);
  background: var(--primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.brand-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--on-surface);
  line-height: 1;
}
.brand-sub {
  font-size: 10px;
  color: var(--outline);
  margin-top: 3px;
}

/* 导航项 */
.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: var(--radius);
  color: var(--outline);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all var(--transition-fast);
}
.nav-item:hover {
  color: var(--on-surface);
  background: var(--surface-container-low);
}
.nav-item.active {
  color: var(--primary);
  background: rgba(0, 65, 216, 0.08);
  font-weight: 600;
}

/* 子导航 */
.nav-item.sub {
  padding-left: 40px;
  font-size: 13px;
}

/* 导航分组 */
.nav-group {
  margin-bottom: 2px;
}
.nav-group-label {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: var(--radius);
  color: var(--outline);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}
.nav-group-label:hover {
  color: var(--on-surface);
  background: var(--surface-container-low);
}
.nav-arrow {
  margin-left: auto;
  font-size: 12px;
  transition: transform var(--transition-fast);
}
.nav-arrow.expanded {
  transform: rotate(180deg);
}
.nav-sub {
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.nav-group-divider {
  height: 1px;
  background: rgba(226, 232, 240, 0.6);
  margin: 8px 0;
}
.nav-group-section {
  font-size: 11px;
  font-weight: 600;
  color: var(--outline);
  padding: 8px 12px 4px;
}

/* 底部 */
.sidebar-footer {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid rgba(226, 232, 240, 0.6);
  display: flex;
  flex-direction: column;
  gap: 2px;
}

/* ===== 主体 ===== */
.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 220px;
  min-height: 100vh;
}

/* ===== 顶栏 ===== */
.topbar {
  height: 56px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  position: sticky;
  top: 0;
  z-index: 30;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

/* 搜索框 */
.search-box {
  position: relative;
}
.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--outline);
  font-size: 16px;
}
.search-input {
  padding: 7px 16px 7px 34px;
  background: var(--surface-container);
  border: none;
  border-radius: var(--radius-full);
  font-size: 13px;
  color: var(--on-surface);
  width: 220px;
  outline: none;
  transition: all var(--transition-fast);
}
.search-input::placeholder {
  color: var(--outline);
}
.search-input:focus {
  box-shadow: 0 0 0 2px rgba(0, 65, 216, 0.15);
  width: 280px;
}

/* 右侧操作 */
.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.notify-badge {
  line-height: 1;
}
/* 通知面板 */
.notify-panel { max-height: 360px; overflow-y: auto; }
.notify-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 10px; border-bottom: 1px solid rgba(226,232,240,0.6); margin-bottom: 8px; }
.notify-title { font-size: 14px; font-weight: 700; }
.notify-list { display: flex; flex-direction: column; gap: 4px; }
.notify-item { display: flex; align-items: flex-start; gap: 8px; padding: 8px; border-radius: 6px; }
.notify-item.unread { background: rgba(0,65,216,0.04); }
.notify-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--primary); margin-top: 6px; flex-shrink: 0; }
.notify-body { flex: 1; }
.notify-text { font-size: 13px; color: var(--on-surface); line-height: 1.4; }
.notify-time { font-size: 11px; color: var(--outline); }
.notify-empty { text-align: center; padding: 24px; font-size: 13px; color: var(--outline); }

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--surface-container-highest);
  border: 2px solid white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: var(--on-surface);
  cursor: pointer;
}

/* ===== 内容区 ===== */
.content-area {
  flex: 1;
  padding: var(--container-padding);
  overflow-y: auto;
}

</style>

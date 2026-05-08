import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      /* 控制台 */
      { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: 'Dashboard' } },

      /* 店铺管理 */
      { path: 'store/links', component: () => import('../views/store/StoreLinks.vue'), meta: { title: '店铺链接' } },
      { path: 'store/settings', component: () => import('../views/store/StoreSettings.vue'), meta: { title: '基础设置' } },
      { path: 'store/payment', component: () => import('../views/store/PaymentMethods.vue'), meta: { title: '支付方式' } },

      /* 商品管理 */
      { path: 'product/category', component: () => import('../views/product/CategoryList.vue'), meta: { title: '分类管理' } },
      { path: 'product/list', component: () => import('../views/product/ProductList.vue'), meta: { title: '商品列表' } },
      { path: 'product/add', component: () => import('../views/product/ProductForm.vue'), meta: { title: '新增商品' } },
      { path: 'product/edit/:id', component: () => import('../views/product/ProductForm.vue'), meta: { title: '编辑商品' } },

      /* 订单管理 */
      { path: 'order/list', component: () => import('../views/order/OrderList.vue'), meta: { title: '订单列表' } },

      /* 库存管理 */
      { path: 'stock/import', component: () => import('../views/stock/StockImport.vue'), meta: { title: '批量导入' } },
      { path: 'stock/list', component: () => import('../views/stock/StockList.vue'), meta: { title: '库存列表' } },

      /* 结算管理 */
      { path: 'settlement/withdraw', component: () => import('../views/settlement/WithdrawalRequest.vue'), meta: { title: '申请提现' } },
      { path: 'settlement/list', component: () => import('../views/settlement/WithdrawalList.vue'), meta: { title: '提现列表' } },

      /* 系统管理 */
      { path: 'system/login-log', component: () => import('../views/system/LoginLog.vue'), meta: { title: '登录日志' } },
      { path: 'system/settings', component: () => import('../views/system/ChangePassword.vue'), meta: { title: '修改密码' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

/* 路由守卫：未登录跳转登录页 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router

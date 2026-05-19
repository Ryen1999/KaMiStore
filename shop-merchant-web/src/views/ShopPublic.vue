<template>
  <div class="shop-public">
    <header class="shop-header">
      <div class="header-inner">
        <div>
          <p class="eyebrow">Verified merchant</p>
          <h1>{{ shopInfo?.storeName || '店铺' }}</h1>
          <p class="desc">{{ shopInfo?.storeDesc || '选择商品并填写联系方式后提交订单。' }}</p>
        </div>
        <div class="header-actions">
          <el-button @click="contactQQ">联系客服</el-button>
          <el-button type="primary" @click="showOrderModal = true">订单查询</el-button>
        </div>
      </div>
    </header>

    <main class="shop-main">
      <section class="panel">
        <div class="toolbar">
          <el-input v-model="searchKeyword" placeholder="搜索商品" clearable @keyup.enter="loadProductsByShop" />
          <el-button type="primary" @click="loadProductsByShop">搜索</el-button>
        </div>

        <div class="category-row">
          <button
            class="category-pill"
            :class="{ active: selectedCategory === null }"
            @click="selectCategory(null)"
          >
            全部
            <span>{{ totalProductCount }}</span>
          </button>
          <button
            v-for="cat in categories"
            :key="cat.id"
            class="category-pill"
            :class="{ active: selectedCategory === cat.id }"
            @click="selectCategory(cat.id)"
          >
            {{ cat.categoryName }}
            <span>{{ cat.productCount || 0 }}</span>
          </button>
        </div>

        <div class="product-grid" v-loading="productLoading">
          <button
            v-for="product in filteredProducts"
            :key="product.id"
            class="product-card"
            :class="{ selected: selectedProduct?.id === product.id }"
            @click="selectProduct(product)"
          >
            <span class="product-name">{{ product.productName }}</span>
            <span class="product-price">¥{{ formatPrice(product.minPrice) }}</span>
            <span class="product-stock">{{ getStockText(product.totalStock) }}</span>
          </button>
          <el-empty v-if="filteredProducts.length === 0 && !productLoading" description="暂无商品" />
        </div>
      </section>

      <aside class="checkout">
        <h2>提交订单</h2>
        <div class="selected-box">
          <span>已选商品</span>
          <strong>{{ selectedProduct?.productName || '请选择商品' }}</strong>
        </div>

        <label class="field-label">购买数量</label>
        <div class="qty-row">
          <el-button :disabled="!selectedProduct" @click="changeQty(-1)">-</el-button>
          <span>{{ purchaseForm.quantity }}</span>
          <el-button :disabled="!selectedProduct" @click="changeQty(1)">+</el-button>
        </div>

        <label class="field-label">联系方式</label>
        <el-input v-model="purchaseForm.contact" placeholder="QQ号或手机号" maxlength="50" />

        <label class="field-label">支付方式</label>
        <div class="payment-list">
          <button
            v-for="pm in paymentMethods"
            :key="pm.methodCode"
            class="payment-option"
            :class="{ active: purchaseForm.paymentMethod === pm.methodCode }"
            @click="purchaseForm.paymentMethod = pm.methodCode"
          >
            {{ getPaymentName(pm.methodCode) }}
          </button>
        </div>

        <div class="amount-row">
          <span>支付金额</span>
          <strong>¥{{ formatPrice(totalPrice) }}</strong>
        </div>

        <el-button
          type="primary"
          size="large"
          class="submit-btn"
          :loading="submitting"
          :disabled="!selectedProduct || !purchaseForm.contact"
          @click="submitOrder"
        >
          确认下单
        </el-button>
      </aside>
    </main>

    <el-dialog v-model="showOrderModal" title="订单查询" width="520px">
      <div class="order-query-form">
        <el-input v-model="orderQueryNo" placeholder="请输入订单号" clearable @keyup.enter="queryOrder" />
        <el-button type="primary" :loading="orderLoading" @click="queryOrder">查询</el-button>
      </div>
      <div v-if="orderDetail" class="order-result">
        <div><span>订单号</span><strong>{{ orderDetail.orderNo }}</strong></div>
        <div><span>商品</span><strong>{{ orderDetail.productName }}</strong></div>
        <div><span>数量</span><strong>{{ orderDetail.quantity }}</strong></div>
        <div><span>金额</span><strong>¥{{ formatPrice(orderDetail.amount) }}</strong></div>
        <div><span>状态</span><strong>{{ getStatusText(orderDetail.status) }}</strong></div>
      </div>
      <el-empty v-else-if="orderQueried" description="未找到订单" />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getShopPublicInfo,
  getPublicPaymentMethods,
  getShopCategories,
  getShopProducts,
  getOrderByNo,
  createPublicOrder
} from '../api/publicShop'

const route = useRoute()
const router = useRouter()

const shopInfo = ref(null)
const categories = ref([])
const products = ref([])
const selectedCategory = ref(null)
const selectedProduct = ref(null)
const productLoading = ref(false)
const submitting = ref(false)
const searchKeyword = ref('')

const purchaseForm = ref({
  quantity: 1,
  contact: '',
  paymentMethod: 'alipay_scan'
})

const paymentMethods = ref([
  { methodCode: 'alipay_scan', enabled: true }
])

const showOrderModal = ref(false)
const orderQueryNo = ref('')
const orderLoading = ref(false)
const orderQueried = ref(false)
const orderDetail = ref(null)

const normalizeId = value => (value === null || value === undefined ? null : String(value))

const filteredProducts = computed(() => {
  let list = products.value
  if (selectedCategory.value) {
    list = list.filter(product => normalizeId(product.categoryId) === selectedCategory.value)
  }
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(product => product.productName.toLowerCase().includes(keyword))
  }
  return list
})

const totalPrice = computed(() => {
  if (!selectedProduct.value) return 0
  return Number(selectedProduct.value.minPrice || 0) * purchaseForm.value.quantity
})

const totalProductCount = computed(() => {
  return categories.value.reduce((sum, cat) => sum + Number(cat.productCount || 0), 0)
})

const formatPrice = price => Number(price || 0).toFixed(2)

const getStockText = stock => {
  if (!stock) return '已售罄'
  if (stock <= 10) return `库存紧张：${stock}`
  return `库存：${stock}`
}

const getPaymentName = code => {
  const names = {
    wechat_pay: '微信支付',
    alipay_scan: '支付宝扫码'
  }
  return names[code] || code
}

const getStatusText = status => {
  const map = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }
  return map[status] || '未知状态'
}

const selectCategory = categoryId => {
  const nextCategory = normalizeId(categoryId)
  selectedCategory.value = selectedCategory.value === nextCategory ? null : nextCategory
  selectedProduct.value = null
  loadProductsByShop()
}

const selectProduct = product => {
  if (!product.totalStock) {
    ElMessage.warning('该商品库存不足')
    return
  }
  selectedProduct.value = product
  purchaseForm.value.quantity = 1
}

const changeQty = delta => {
  if (!selectedProduct.value) return
  const max = Math.min(selectedProduct.value.totalStock || 1, 99)
  purchaseForm.value.quantity = Math.max(1, Math.min(purchaseForm.value.quantity + delta, max))
}

const contactQQ = () => {
  if (shopInfo.value?.merchantQq) {
    window.open(`https://wpa.qq.com/msgrd?v=3&uin=${shopInfo.value.merchantQq}&site=qq&menu=yes`, '_blank')
    return
  }
  ElMessage.info('商户暂未配置客服QQ')
}

const submitOrder = async () => {
  if (!selectedProduct.value) {
    ElMessage.warning('请选择商品')
    return
  }
  if (!purchaseForm.value.contact) {
    ElMessage.warning('请填写联系方式')
    return
  }
  if (!purchaseForm.value.paymentMethod) {
    ElMessage.warning('请选择支付方式')
    return
  }

  submitting.value = true
  try {
    const orderRes = await createPublicOrder({
      shopCode: route.params.shopCode,
      productId: selectedProduct.value.id,
      quantity: purchaseForm.value.quantity,
      contactType: 1,
      contactValue: purchaseForm.value.contact,
      paymentMethod: purchaseForm.value.paymentMethod
    })
    const orderNo = orderRes.data.orderNo
    ElMessage.success('订单创建成功，正在跳转支付页面')
    router.push(`/shop/pay/${orderNo}`)
  } catch (e) {
    ElMessage.error(e.msg || '创建订单失败')
  } finally {
    submitting.value = false
  }
}

const queryOrder = async () => {
  if (!orderQueryNo.value) {
    ElMessage.warning('请输入订单号')
    return
  }
  orderLoading.value = true
  orderQueried.value = true
  orderDetail.value = null
  try {
    const res = await getOrderByNo(orderQueryNo.value)
    orderDetail.value = res.data
  } finally {
    orderLoading.value = false
  }
}

const loadShopInfo = async () => {
  const shopCode = route.params.shopCode
  if (!shopCode) return
  const res = await getShopPublicInfo(shopCode)
  shopInfo.value = res.data
  if (res.data?.tenantId) {
    await loadCategories(res.data.tenantId)
    await loadProducts(res.data.tenantId)
  }
}

const loadCategories = async tenantId => {
  const res = await getShopCategories(tenantId)
  categories.value = (res.data || []).map(category => ({
    ...category,
    id: normalizeId(category.id)
  }))
  selectedCategory.value = null
}

const loadProducts = async tenantId => {
  productLoading.value = true
  try {
    const res = await getShopProducts(tenantId, {
      pageNum: 1,
      pageSize: 100,
      categoryId: selectedCategory.value || undefined,
      keyword: searchKeyword.value || undefined
    })
    products.value = (res.data?.list || res.data?.records || []).map(product => ({
      ...product,
      id: normalizeId(product.id),
      categoryId: normalizeId(product.categoryId)
    }))
  } finally {
    productLoading.value = false
  }
}

const loadProductsByShop = async () => {
  if (shopInfo.value?.tenantId) {
    await loadProducts(shopInfo.value.tenantId)
  }
}

const loadPaymentMethods = async () => {
  try {
    const shopCode = route.params.shopCode
    if (!shopCode) return
    const res = await getPublicPaymentMethods(shopCode)
    const enabled = (res.data || []).filter(item => item.enabled === true || item.enabled === 1)
    if (enabled.length > 0) {
      paymentMethods.value = enabled
      purchaseForm.value.paymentMethod = enabled[0].methodCode
    }
  } catch (e) {
    // 使用默认支付方式
  }
}

let searchTimer = null
watch(searchKeyword, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(loadProductsByShop, 400)
})

onMounted(() => {
  loadShopInfo()
  loadPaymentMethods()
})
</script>

<style scoped>
.shop-public {
  min-height: 100vh;
  background: #f3f6f8;
  color: #1f2933;
  padding-bottom: 40px;
}

.shop-header {
  background: linear-gradient(135deg, #063b33 0%, #146c5f 100%);
  color: #fff;
  padding: 42px 20px;
}

.header-inner {
  max-width: 1180px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;
}

.eyebrow {
  margin: 0 0 8px;
  color: rgba(255, 255, 255, 0.68);
  font-size: 12px;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  font-size: 34px;
  letter-spacing: 0;
}

.desc {
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.76);
  max-width: 620px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.shop-main {
  max-width: 1180px;
  margin: 24px auto 0;
  padding: 0 20px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 20px;
}

.panel,
.checkout {
  background: #fff;
  border: 1px solid #dfe7e5;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(20, 37, 34, 0.06);
}

.panel {
  padding: 20px;
}

.checkout {
  padding: 22px;
  align-self: start;
  position: sticky;
  top: 20px;
}

.checkout h2 {
  margin: 0 0 18px;
  font-size: 20px;
}

.toolbar,
.order-query-form {
  display: flex;
  gap: 10px;
}

.category-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 18px 0;
}

.category-pill,
.payment-option,
.product-card {
  border: 1px solid #d7e1df;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.16s, background 0.16s, transform 0.16s;
}

.category-pill {
  border-radius: 999px;
  padding: 8px 14px;
}

.category-pill span {
  color: #6b7c78;
  margin-left: 6px;
}

.category-pill.active,
.payment-option.active {
  border-color: #146c5f;
  background: #e9f4f1;
}

.product-grid {
  min-height: 220px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}

.product-card {
  border-radius: 8px;
  padding: 16px;
  text-align: left;
  display: grid;
  gap: 10px;
}

.product-card:hover {
  transform: translateY(-2px);
}

.product-card.selected {
  border-color: #146c5f;
  background: #f0f8f6;
}

.product-name {
  font-weight: 700;
}

.product-price {
  color: #146c5f;
  font-size: 22px;
  font-weight: 800;
}

.product-stock {
  color: #6b7c78;
  font-size: 12px;
}

.selected-box {
  display: grid;
  gap: 6px;
  padding: 14px;
  background: #f6faf9;
  border-radius: 8px;
  margin-bottom: 16px;
}

.selected-box span,
.field-label,
.amount-row span,
.order-result span {
  color: #6b7c78;
  font-size: 13px;
}

.field-label {
  display: block;
  margin: 16px 0 8px;
}

.qty-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.qty-row span {
  width: 40px;
  text-align: center;
  font-weight: 700;
}

.payment-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.payment-option {
  border-radius: 8px;
  padding: 10px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
}

.amount-row strong {
  color: #146c5f;
  font-size: 26px;
}

.submit-btn {
  width: 100%;
}

.order-result {
  display: grid;
  gap: 12px;
  background: #f6faf9;
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}

.order-result div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

@media (max-width: 900px) {
  .header-inner,
  .shop-main {
    grid-template-columns: 1fr;
    display: grid;
  }

  .checkout {
    position: static;
  }
}
</style>

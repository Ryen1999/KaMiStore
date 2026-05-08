<template>
  <!-- 添加/编辑商品 - 匹配 Stitch _7 原型 -->
  <div class="animate-in">
    <div class="page-header">
      <div>
        <h3 class="page-heading">{{ isEdit ? '编辑商品' : '添加商品' }}</h3>
        <p class="page-desc">创建新的虚拟商品并配置售卖规则</p>
      </div>
      <el-button size="small" @click="$router.push('/product/list')">
        <el-icon style="margin-right: 4px;"><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <div class="form-layout" v-loading="loading">
      <!-- 基础信息 -->
      <div class="glass-card form-card">
        <div class="section-title">
          <el-icon><InfoFilled /></el-icon>
          <span>基础信息</span>
        </div>
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <div class="form-row-2">
            <el-form-item label="商品名称 *" prop="productName">
              <el-input v-model="form.productName" placeholder="输入商品名称，例如：100元话费充值" />
            </el-form-item>
            <el-form-item label="商品分类 *" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%;">
                <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
              </el-select>
            </el-form-item>
          </div>
          <el-form-item label="商品说明 (公开显示)">
            <div class="editor-toolbar">
              <button class="tb-btn"><b>B</b></button>
              <button class="tb-btn"><i>I</i></button>
              <button class="tb-btn"><el-icon style="font-size: 16px;"><List /></el-icon></button>
              <button class="tb-btn"><el-icon style="font-size: 16px;"><Picture /></el-icon></button>
              <button class="tb-btn"><el-icon style="font-size: 16px;"><Link /></el-icon></button>
            </div>
            <el-input v-model="form.detailHtml" type="textarea" :rows="5" placeholder="请输入商品详细描述、使用流程或售后须知..." />
            </el-form-item>
        </el-form>
      </div>

      <!-- 价格与库存 -->
      <div class="glass-card form-card">
        <div class="section-title">
          <el-icon><Wallet /></el-icon>
          <span>价格与库存</span>
        </div>
        <div class="form-row-3">
          <div class="field-block">
            <label class="fb-label">售价 (元) *</label>
            <el-input v-model="skuList[0].price" placeholder="¥ 0.00" />
          </div>
          <div class="field-block">
            <label class="fb-label">成本价 (元)</label>
            <el-input v-model="skuList[0].costPrice" placeholder="¥ 0.00" />
          </div>
          <div class="field-block">
            <label class="fb-label">原价 (元)</label>
            <el-input v-model="skuList[0].originalPrice" placeholder="¥ 0.00" />
          </div>
        </div>
        <div class="form-row-3">
          <div class="field-block">
            <label class="fb-label">库存数量</label>
            <el-input v-model="skuList[0].stock" placeholder="0" />
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions-bar">
        <el-button size="large" @click="$router.push('/product/list')">返回列表</el-button>
        <el-button type="primary" size="large" :loading="saving" @click="handleSubmit">
          <el-icon style="margin-right: 4px;"><Check /></el-icon>
          {{ isEdit ? '保存商品' : '保存商品' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createProduct, updateProduct, getProductDetail, listCategories } from '../../api/product'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  InfoFilled,
  View,
  List,
  Picture,
  Link,
  Wallet,
  Setting,
  Check
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const categories = ref([])
const isEdit = computed(() => !!route.params.id)

const form = reactive({
  productName: '', categoryId: null, productType: 2,
  mainImage: '', images: '', detailHtml: '', sortOrder: 0, isRecommend: 0
})
const skuList = ref([{ skuName: '', price: 0, originalPrice: 0, costPrice: 0, stock: 0 }])
const rules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

onMounted(async () => {
  try { const res = await listCategories(); categories.value = res.data || [] } catch (e) {}
  if (isEdit.value) {
    loading.value = true
    try {
      const res = await getProductDetail(route.params.id)
      const p = res.data
      Object.assign(form, {
        productName: p.productName, categoryId: p.categoryId, productType: p.productType,
        mainImage: p.mainImage, images: p.images, detailHtml: p.detailHtml,
        sortOrder: p.sortOrder, isRecommend: p.isRecommend
      })
      if (p.skuList?.length) skuList.value = p.skuList.map(s => ({
        skuName: s.skuName, price: s.price, originalPrice: s.originalPrice, costPrice: s.costPrice, stock: s.stock
      }))
    } finally { loading.value = false }
  }
})

const handleSubmit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    const data = { product: { ...form }, skuList: skuList.value }
    isEdit.value ? await updateProduct(route.params.id, data) : await createProduct(data)
    ElMessage.success(isEdit.value ? '修改成功' : '创建成功')
    router.push('/product/list')
  } finally { saving.value = false }
}
</script>

<style scoped>
.form-layout { max-width: 800px; display: flex; flex-direction: column; gap: var(--gutter); }
.form-card { padding: 28px; }

.section-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 16px; font-weight: 700;
  color: var(--on-surface); margin-bottom: 20px; padding-bottom: 12px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
}
.section-title .el-icon { font-size: 20px; color: var(--primary); }

.form-row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.form-row-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px; margin-bottom: 16px; }

/* 简单字段块 */
.field-block { display: flex; flex-direction: column; gap: 6px; }
.fb-label { font-size: 13px; font-weight: 500; color: var(--on-surface-variant); }

/* 编辑器工具栏 */
.editor-toolbar {
  display: flex; gap: 4px; margin-bottom: 6px;
  padding: 6px 8px; background: var(--surface-container-low);
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
  border: 1px solid var(--outline-variant); border-bottom: none;
}
.tb-btn {
  width: 28px; height: 28px; border: none; background: transparent;
  color: var(--on-surface-variant); cursor: pointer; border-radius: 3px;
  display: flex; align-items: center; justify-content: center; font-size: 14px;
}
.tb-btn:hover { background: var(--surface-container); }

/* 单选组 */
.radio-group { display: flex; gap: 16px; flex-wrap: wrap; }
.radio-item {
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; color: var(--on-surface-variant); cursor: pointer;
}
.radio-item input { accent-color: var(--primary); }

/* 开关网格 */
.toggle-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.toggle-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; background: var(--surface-container-low);
  border-radius: var(--radius); border: 1px solid rgba(226, 232, 240, 0.6);
}
.toggle-item.full-width { grid-column: 1 / -1; }
.toggle-item h5 { font-size: 14px; font-weight: 600; color: var(--on-surface); }
.toggle-item p { font-size: 12px; color: var(--outline); margin-top: 2px; }

/* 底部操作 */
.form-actions-bar {
  display: flex; justify-content: flex-end; gap: 12px;
  padding: 20px 0;
}
</style>

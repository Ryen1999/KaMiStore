<template>
  <!-- 分类管理 - 匹配 Stitch _2 原型 -->
  <div class="animate-in">
    <div class="page-header">
      <div>
        <p class="breadcrumb">商品管理 &gt; 分类管理</p>
        <h3 class="page-heading">分类管理</h3>
      </div>
      <el-button type="primary" @click="showDialog()">
        <el-icon style="margin-right: 4px;"><Plus /></el-icon>
        新增分类
      </el-button>
    </div>

    <div class="cat-layout">
      <!-- 左侧：分类列表 -->
      <div class="cat-list-card glass-card">
        <div class="cat-list-header">
          <h4>全部分类</h4>
          <div class="cat-list-actions">
            <button class="icon-btn-sm"><el-icon><Filter /></el-icon></button>
            <button class="icon-btn-sm"><el-icon><Sort /></el-icon></button>
          </div>
        </div>
        <el-table :data="list" v-loading="loading" style="width: 100%;">
          <el-table-column prop="categoryName" label="分类名称" min-width="160">
            <template #default="{ row }">
              <div class="cat-name-cell">
                <span class="cat-dot" :class="row.status === 1 ? 'on' : 'off'"></span>
                {{ row.categoryName }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-switch v-model="row.status" :active-value="1" :inactive-value="0" size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="150" />
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <button class="icon-btn-sm" @click="showDialog(row)" title="编辑">
                <el-icon><Edit /></el-icon>
              </button>
              <button class="icon-btn-sm danger" @click="handleDelete(row)" title="删除">
                <el-icon><Delete /></el-icon>
              </button>
            </template>
          </el-table-column>
        </el-table>
        <p class="view-more" v-if="list.length > 10">查看更多 {{ list.length }} 个分类</p>
      </div>

      <!-- 右侧：分类详情/链接 -->
      <div class="cat-detail-panel">
        <!-- 链接管理 -->
        <div class="glass-card" style="padding: 20px; margin-bottom: 16px;">
          <div class="detail-header">
            <h4>链接管理</h4>
            <span class="status-badge active">已启用</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">标准短链接</span>
            <code class="detail-value">https://vc.pro/c/steam</code>
          </div>
          <div class="detail-row">
            <span class="detail-label">自定义域名</span>
            <span class="detail-value muted">未配置</span>
          </div>
          <el-button size="small" style="margin-top: 12px;">
            <el-icon style="margin-right: 4px;"><Setting /></el-icon>
            配置
          </el-button>
        </div>

        <!-- 满赠规则 -->
        <div class="glass-card" style="padding: 20px;">
          <div class="detail-header">
            <el-icon style="color: var(--warning);"><Present /></el-icon>
            <h4>买赠规则</h4>
          </div>
          <p class="promo-desc">通过"推广区"促销活动提升分类表现</p>
          <div class="promo-card">
            <div class="promo-badge">STEAM 分类规则</div>
            <p class="promo-rule">购买 <b>5</b> 张礼品卡</p>
            <p class="promo-rule">随机赠送 <b>1</b> 张</p>
          </div>
          <el-button type="primary" size="small" style="width: 100%; margin-top: 12px;">
            创建新促销规则
          </el-button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑分类' : '新增分类'" width="480px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称"><el-input v-model="form.categoryName" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="图标标识" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="页面风格">
          <el-select v-model="form.pageStyle" style="width: 100%;">
            <el-option label="默认" value="default" />
            <el-option label="卡片" value="card" />
            <el-option label="列表" value="list" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listCategories, createCategory, updateCategory, deleteCategory } from '../../api/product'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Filter, Sort, Edit, Delete, Setting, Present } from '@element-plus/icons-vue'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const form = reactive({ categoryName: '', icon: '', sortOrder: 0, parentId: 0, pageStyle: 'default' })

const fetchList = async () => {
  loading.value = true
  try { const res = await listCategories(); list.value = res.data || [] }
  finally { loading.value = false }
}
onMounted(fetchList)

const showDialog = (row) => {
  editId.value = row ? row.id : null
  Object.assign(form, row
    ? { categoryName: row.categoryName, icon: row.icon, sortOrder: row.sortOrder, parentId: row.parentId || 0, pageStyle: row.pageStyle || 'default' }
    : { categoryName: '', icon: '', sortOrder: 0, parentId: 0, pageStyle: 'default' })
  dialogVisible.value = true
}

const handleSave = async () => {
  saving.value = true
  try {
    editId.value ? await updateCategory(editId.value, form) : await createCategory(form)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchList()
  } finally { saving.value = false }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该分类？', '提示', { type: 'warning' })
  await deleteCategory(row.id)
  ElMessage.success('删除成功')
  fetchList()
}
</script>

<style scoped>
.breadcrumb { font-size: 12px; color: var(--outline); margin-bottom: 4px; }

.cat-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: var(--gutter);
  align-items: start;
}

/* 分类列表卡 */
.cat-list-card { padding: 0; overflow: hidden; }
.cat-list-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px; border-bottom: 1px solid rgba(226, 232, 240, 0.6);
}
.cat-list-header h4 { font-size: 15px; font-weight: 700; color: var(--on-surface); }
.cat-list-actions { display: flex; gap: 4px; }

.cat-name-cell { display: flex; align-items: center; gap: 8px; }
.cat-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.cat-dot.on { background: var(--secondary); }
.cat-dot.off { background: var(--outline); }

.view-more { text-align: center; padding: 12px; font-size: 13px; color: var(--primary); cursor: pointer; }

/* 右侧面板 */
.detail-header { display: flex; align-items: center; gap: 8px; margin-bottom: 14px; }
.detail-header h4 { font-size: 14px; font-weight: 700; color: var(--on-surface); }
.detail-row { margin-bottom: 10px; }
.detail-label { display: block; font-size: 11px; color: var(--outline); text-transform: uppercase; letter-spacing: 0.04em; margin-bottom: 2px; }
.detail-value { font-size: 13px; color: var(--on-surface); }
.detail-value.muted { color: var(--outline); }

/* 满赠卡 */
.promo-desc { font-size: 12px; color: var(--outline); margin-bottom: 12px; }
.promo-card {
  padding: 14px; background: rgba(0, 65, 216, 0.04);
  border: 1px solid rgba(0, 65, 216, 0.12); border-radius: var(--radius);
}
.promo-badge {
  font-size: 10px; font-weight: 700; color: var(--primary);
  background: rgba(0, 65, 216, 0.1); padding: 2px 8px; border-radius: var(--radius-full);
  display: inline-block; margin-bottom: 8px; text-transform: uppercase;
}
.promo-rule { font-size: 13px; color: var(--on-surface); }
.promo-rule b { color: var(--primary); }

/* 通用小图标按钮 */
.icon-btn-sm {
  width: 28px; height: 28px; border: none; background: transparent;
  color: var(--outline); cursor: pointer; border-radius: var(--radius-sm);
  display: inline-flex; align-items: center; justify-content: center;
  transition: all var(--transition-fast);
}
.icon-btn-sm:hover { color: var(--primary); background: rgba(0, 65, 216, 0.06); }
.icon-btn-sm.danger:hover { color: var(--danger); background: rgba(186, 26, 26, 0.06); }
</style>

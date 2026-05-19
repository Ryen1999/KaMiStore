<template>
  <div class="animate-in">
    <div class="page-header">
      <div>
        <p class="breadcrumb">后台管理 / 站内消息</p>
        <h3 class="page-heading">站内消息</h3>
      </div>
      <el-button type="primary" plain :disabled="!total" @click="handleReadAll">
        <el-icon style="margin-right: 4px;"><Check /></el-icon>
        全部已读
      </el-button>
    </div>

    <div class="message-card">
      <div class="message-toolbar">
        <el-select v-model="query.status" placeholder="全部状态" style="width: 116px">
          <el-option label="全部状态" value="" />
          <el-option label="未读" value="0" />
          <el-option label="已读" value="1" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon style="margin-right: 4px;"><Search /></el-icon>
          搜索
        </el-button>
      </div>

      <el-table :data="list" v-loading="loading" class="message-table">
        <el-table-column label="发送人" width="100">
          <template #default>
            <el-tag type="danger" size="small" effect="dark">系统</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="message-title" :class="{ unread: row.isRead !== 1 }">{{ row.title || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="420" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="message-content">{{ row.content || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="读取状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isRead === 1"
              :loading="updatingId === row.id"
              @change="value => handleReadStatusChange(row, value)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发送时间" width="180" />
        <el-table-column label="操作" width="110" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon style="margin-right: 4px;"><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-bar">
      <span class="total-text">共 {{ total }} 条消息</span>
      <el-pagination
        background
        layout="prev, pager, next, sizes"
        :total="total"
        :page-sizes="[10, 20, 50]"
        v-model:page-size="query.pageSize"
        v-model:current-page="query.pageNum"
        @current-change="fetchList"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Delete, Search } from '@element-plus/icons-vue'
import {
  deleteMessage,
  pageMessages,
  readAllMessages,
  updateMessageReadStatus
} from '../../api/system'

const loading = ref(false)
const updatingId = ref(null)
const list = ref([])
const total = ref(0)
const query = reactive({
  status: '',
  pageNum: 1,
  pageSize: 10
})

const notifyMessageChanged = () => {
  window.dispatchEvent(new Event('shop-message-updated'))
}

const buildParams = () => {
  const params = {
    pageNum: query.pageNum,
    pageSize: query.pageSize
  }
  if (query.status !== '') {
    params.isRead = Number(query.status)
  }
  return params
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await pageMessages(buildParams())
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  fetchList()
}

const handleSizeChange = () => {
  query.pageNum = 1
  fetchList()
}

const handleReadStatusChange = async (row, value) => {
  updatingId.value = row.id
  const oldStatus = row.isRead
  const nextStatus = value ? 1 : 0
  try {
    await updateMessageReadStatus(row.id, nextStatus)
    row.isRead = nextStatus
    ElMessage.success(value ? '已标记为已读' : '已标记为未读')
    await fetchList()
    notifyMessageChanged()
  } catch (e) {
    row.isRead = oldStatus
    await fetchList()
  } finally {
    updatingId.value = null
  }
}

const handleReadAll = async () => {
  await readAllMessages()
  ElMessage.success('已全部标记为已读')
  query.pageNum = 1
  await fetchList()
  notifyMessageChanged()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除这条站内消息吗？', '删除消息', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  await deleteMessage(row.id)
  ElMessage.success('删除成功')
  if (list.value.length === 1 && query.pageNum > 1) {
    query.pageNum -= 1
  }
  await fetchList()
  notifyMessageChanged()
}

onMounted(fetchList)
</script>

<style scoped>
.breadcrumb {
  font-size: 12px;
  color: var(--outline);
  margin-bottom: 4px;
}

.message-card {
  background: var(--surface-container-lowest);
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.message-toolbar {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}

.message-table {
  width: 100%;
}

.message-title {
  color: var(--on-surface);
}

.message-title.unread {
  font-weight: 700;
}

.message-content {
  color: var(--on-surface-variant);
}

@media (max-width: 960px) {
  .page-header {
    align-items: flex-start;
    gap: 12px;
    flex-direction: column;
  }

  .pagination-bar {
    align-items: flex-start;
    gap: 12px;
    flex-direction: column;
  }
}
</style>

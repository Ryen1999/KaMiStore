<template>
  <!-- 修改密码 -->
  <div class="animate-in">
    <div class="page-header">
      <div>
        <p class="breadcrumb">系统管理 &gt; 安全设置</p>
        <h3 class="page-heading">修改密码</h3>
        <p class="page-desc">更新您的账户安全密码</p>
      </div>
    </div>

    <div class="glass-card pwd-card">
      <div class="pwd-icon">
        <el-icon :size="28"><Refresh /></el-icon>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" style="max-width: 400px;">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入当前密码" size="large">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码" size="large">
            <template #prefix><el-icon><Key /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入新密码" size="large">
            <template #prefix><el-icon><Key /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%;" :loading="saving" @click="handleSubmit">
          确认修改
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { changePassword } from '../../api/system'
import { ElMessage } from 'element-plus'
import { Lock, Key, Refresh } from '@element-plus/icons-vue'

const formRef = ref()
const saving = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码不少于6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }]
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  saving.value = true
  try {
    await changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    ElMessage.success('密码修改成功')
    Object.assign(form, { oldPassword: '', newPassword: '', confirmPassword: '' })
  } finally { saving.value = false }
}
</script>

<style scoped>
.breadcrumb { font-size: 12px; color: var(--outline); margin-bottom: 4px; font-family: inherit; }
.pwd-card { padding: 40px; max-width: 540px; }
.pwd-icon {
  width: 56px; height: 56px; border-radius: 50%;
  background: rgba(0, 65, 216, 0.08); color: var(--primary);
  display: flex; align-items: center; justify-content: center; margin-bottom: 24px;
}
</style>

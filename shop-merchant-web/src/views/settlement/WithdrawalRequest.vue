<template>
  <!-- 申请提现 -->
  <div class="animate-in">
    <div class="page-header">
      <div>
        <p class="breadcrumb">结算管理 &gt; 申请提现</p>
        <h3 class="page-heading">提现申请</h3>
        <p class="page-desc">管理您的资金并申请手动结算到您的注册账户。</p>
      </div>
    </div>

    <!-- 概览统计 -->
    <div class="wd-stats">
      <div class="ws-card">
        <p class="ws-label">今日次数</p>
        <h3 class="ws-value">02 / 05</h3>
        <p class="ws-sub">今日剩余</p>
      </div>
      <div class="ws-card">
        <p class="ws-label">可提现金额</p>
        <h3 class="ws-value">¥12,450.00</h3>
        <p class="ws-sub success">可转账</p>
      </div>
      <div class="ws-card">
        <p class="ws-label">不可提现金额</p>
        <h3 class="ws-value">¥3,120.40</h3>
        <p class="ws-sub">等待结算周期</p>
      </div>
      <div class="ws-card">
        <p class="ws-label">服务费率</p>
        <h3 class="ws-value">0.05%</h3>
        <p class="ws-sub">每笔交易</p>
      </div>
    </div>

    <div class="wd-layout">
      <!-- 左：提现表单 -->
      <div class="glass-card wd-form">
        <h4 class="wd-section-title">
          <el-icon><Promotion /></el-icon>
          转账详情
        </h4>
        <div class="field-block">
          <label class="fb-label">提现金额</label>
          <div class="amount-input-wrap">
            <span class="currency">¥</span>
            <input type="text" v-model="amount" class="amount-input" placeholder="0.00" />
          </div>
          <div class="amount-meta">
            <span>预计手续费: ¥{{ estimatedFee }}</span>
            <a href="#" class="withdraw-all" @click.prevent="amount = '12450.00'">全部提现</a>
          </div>
        </div>

        <div class="field-block">
          <label class="fb-label">收款账户</label>
          <div class="account-box">
            <el-icon class="acc-icon"><OfficeBuilding /></el-icon>
            <div>
              <p class="acc-name">招商银行 •••• 6619（主营业务账户）</p>
              <a href="#" class="acc-change">更换银行账户</a>
            </div>
          </div>
        </div>

        <el-button type="primary" size="large" style="width: 100%; margin-top: 16px;" :loading="submitting" @click="handleSubmit">
          <el-icon style="font-size: 18px; margin-right: 6px;"><Right /></el-icon>
          发起提现申请
        </el-button>
        <p class="security-note">
          <el-icon style="font-size: 14px;"><Lock /></el-icon>
          下一步将需要进行安全验证。
        </p>
      </div>

      <!-- 右：提现规则 -->
      <div class="glass-card wd-rules">
        <h4 class="wd-section-title">
          <el-icon><Document /></el-icon>
          提现规则
        </h4>

        <div class="rule-block">
          <div class="rule-badge auto">
            <el-icon><Refresh /></el-icon>
          </div>
          <h5>自动结算</h5>
          <ul class="rule-list">
            <li>超过 ¥50,000 的资金将于每周五 23:59（GMT+8）自动结算至您的主绑定账户。</li>
          </ul>
        </div>

        <div class="rule-block">
          <div class="rule-badge manual">
            <el-icon><Pointer /></el-icon>
          </div>
          <h5>手动提现</h5>
          <ul class="rule-list">
            <li>单笔最低金额：¥100.00</li>
            <li>每日最多提现 5 次</li>
            <li>大额处理时间：1-3 个工作日</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 安全提示横幅 -->
    <div class="security-banner">
      <div class="sb-badge">
        <el-icon><CircleCheck /></el-icon>
        银行级安全保障
      </div>
      <h4>企业资金保护</h4>
      <p>平台所有交易均采用端到端 256 位加密及多因素认证协议。我们的结算网关完全符合 PCI DSS 一级标准，确保您的资金在每个环节都受到保护。</p>
      <div class="sb-certs">
        <span>AES-256 加密</span>
        <span>SOC2 Type II</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { applyWithdrawal } from '../../api/settlement'
import { ElMessage } from 'element-plus'
import {
  Promotion,
  OfficeBuilding,
  Right,
  Lock,
  Document,
  Refresh,
  Pointer,
  CircleCheck
} from '@element-plus/icons-vue'

const amount = ref('')
const submitting = ref(false)
const estimatedFee = computed(() => {
  const val = parseFloat(amount.value) || 0
  return (val * 0.0005).toFixed(2)
})

const handleSubmit = async () => {
  const val = parseFloat(amount.value)
  if (!val || val < 1) { ElMessage.warning('请输入有效提现金额'); return }
  submitting.value = true
  try {
    await applyWithdrawal({ amount: val })
    ElMessage.success('提现申请已提交')
    amount.value = ''
  } catch (e) {
    /* 接口暂未实现 */
  } finally { submitting.value = false }
}
</script>

<style scoped>
.breadcrumb { font-size: 12px; color: var(--outline); margin-bottom: 4px; }

/* 统计 */
.wd-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--gutter); margin-bottom: var(--gutter); }
.ws-card {
  background: var(--surface-container-lowest); padding: 20px;
  border-radius: var(--radius-md); border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: var(--shadow-card);
}
.ws-label { font-size: 11px; color: var(--outline); text-transform: uppercase; letter-spacing: 0.04em; margin-bottom: 6px; }
.ws-value { font-size: 22px; font-weight: 800; color: var(--on-surface); }
.ws-sub { font-size: 12px; color: var(--outline); margin-top: 4px; }
.ws-sub.success { color: var(--secondary); }

/* 布局 */
.wd-layout { display: grid; grid-template-columns: 1fr 1fr; gap: var(--gutter); margin-bottom: var(--gutter); }
.wd-form, .wd-rules { padding: 28px; }
.wd-section-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 16px; font-weight: 700;
  color: var(--on-surface); margin-bottom: 20px;
}
.wd-section-title .el-icon { font-size: 20px; color: var(--primary); }

/* 金额输入 */
.field-block { margin-bottom: 20px; }
.fb-label { display: block; font-size: 13px; font-weight: 600; color: var(--on-surface-variant); margin-bottom: 8px; }
.amount-input-wrap {
  display: flex; align-items: center;
  border: 1px solid var(--outline-variant); border-radius: var(--radius);
  padding: 12px 16px; background: var(--background);
  transition: all var(--transition-fast);
}
.amount-input-wrap:focus-within { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(0, 65, 216, 0.12); }
.currency { font-size: 18px; font-weight: 700; color: var(--outline); margin-right: 8px; }
.amount-input {
  flex: 1; border: none; outline: none; background: transparent;
  font-size: 24px; font-weight: 800;
  color: var(--on-surface);
}
.amount-input::placeholder { color: var(--outline-variant); }
.amount-meta { display: flex; justify-content: space-between; margin-top: 8px; font-size: 12px; color: var(--outline); }
.withdraw-all { color: var(--primary); text-decoration: none; font-weight: 600; }

/* 收款账户 */
.account-box {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 16px; background: var(--surface-container-low);
  border: 1px solid var(--outline-variant); border-radius: var(--radius);
}
.account-box .acc-icon { font-size: 22px; color: var(--primary); }
.acc-name { font-size: 14px; font-weight: 500; color: var(--on-surface); }
.acc-change { font-size: 12px; color: var(--primary); text-decoration: none; }

.security-note {
  display: flex; align-items: center; gap: 4px; justify-content: center;
  margin-top: 12px; font-size: 12px; color: var(--outline);
}

/* 规则 */
.rule-block { margin-bottom: 20px; }
.rule-badge {
  width: 36px; height: 36px; border-radius: var(--radius);
  display: flex; align-items: center; justify-content: center; margin-bottom: 10px;
}
.rule-badge.auto { background: rgba(0, 65, 216, 0.08); color: var(--primary); }
.rule-badge.manual { background: rgba(245, 158, 11, 0.08); color: var(--warning); }
.rule-block h5 { font-size: 14px; font-weight: 700; color: var(--on-surface); margin-bottom: 8px; }
.rule-list { padding-left: 16px; }
.rule-list li { font-size: 13px; color: var(--on-surface-variant); line-height: 1.6; margin-bottom: 4px; }

/* 安全横幅 */
.security-banner {
  background: linear-gradient(135deg, #0a1628, #111c2d);
  border-radius: var(--radius-md); padding: 28px 32px; color: white;
}
.sb-badge {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.1em;
  padding: 4px 10px; background: rgba(255, 255, 255, 0.1); border-radius: var(--radius-full);
  margin-bottom: 12px;
}
.sb-badge .el-icon { font-size: 14px; }
.security-banner h4 { font-size: 18px; font-weight: 800; margin-bottom: 8px; }
.security-banner p { font-size: 13px; color: rgba(255, 255, 255, 0.6); line-height: 1.6; max-width: 700px; }
.sb-certs { display: flex; gap: 16px; margin-top: 16px; }
.sb-certs span {
  font-size: 11px; font-weight: 600; padding: 4px 12px;
  border: 1px solid rgba(255, 255, 255, 0.15); border-radius: var(--radius-full);
  color: rgba(255, 255, 255, 0.7);
}
</style>

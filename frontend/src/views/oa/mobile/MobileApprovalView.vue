<template>
  <div class="mobile-approval-view">
    <!-- 移动端审批统计 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">待我审批</div>
              <div class="stat-value">{{ stats.pending }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">我已审批</div>
              <div class="stat-value">{{ stats.approved }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">我发起的</div>
              <div class="stat-value">{{ stats.initiated }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 审批列表 -->
    <div class="approval-list-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>审批列表</span>
            <el-select v-model="approvalType" placeholder="类型" style="width: 150px;">
              <el-option label="全部" value="all" />
              <el-option label="待我审批" value="pending" />
              <el-option label="我已审批" value="approved" />
              <el-option label="我发起的" value="initiated" />
            </el-select>
          </div>
        </template>
        <el-list v-loading="loading" :border="false" :data="filteredApprovals" style="width: 100%">
          <el-list-item v-for="approval in filteredApprovals" :key="approval.id" :header="approval.title">
            <template #default>
              <div class="approval-item">
                <div class="approval-info">
                  <div class="info-row">
                    <span class="label">流程类型：</span>
                    <span class="value">{{ approval?.type || '未知类型' }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">发起人：</span>
                    <span class="value">{{ approval?.initiator || '未知发起人' }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">发起时间：</span>
                    <span class="value">{{ approval?.createTime || '' }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">当前节点：</span>
                    <span class="value">{{ approval?.currentNode || '未知节点' }}</span>
                  </div>
                </div>
                <div class="approval-status">
                  <el-tag :type="statusTypeMap[approval?.status || '']">{{ approval?.status || '未知状态' }}</el-tag>
                </div>
              </div>
            </template>
            <template #extra>
              <div class="approval-actions">
                <el-button size="small" type="primary" @click="viewDetail(approval)" v-if="approvalType === 'pending'">
                  审批
                </el-button>
                <el-button size="small" @click="viewDetail(approval)">
                  详情
                </el-button>
              </div>
            </template>
          </el-list-item>
        </el-list>
        <div class="pagination" v-if="filteredApprovals.length > 0">
          <el-pagination
            layout="prev, pager, next"
            :total="filteredApprovals.length"
            :page-size="10"
            :current-page="currentPage"
            @current-change="currentPage = $event"
          />
        </div>
      </el-card>
    </div>

    <!-- 审批详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="审批详情"
      width="800px"
    >
      <div v-if="selectedApproval" class="approval-detail">
        <div class="detail-header">
          <h3>{{ selectedApproval?.title || '无标题' }}</h3>
          <el-tag :type="statusTypeMap[selectedApproval?.status || '']">{{ selectedApproval?.status || '未知状态' }}</el-tag>
        </div>
        <div class="detail-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="流程类型">{{ selectedApproval?.type || '未知类型' }}</el-descriptions-item>
            <el-descriptions-item label="流程编号">{{ selectedApproval?.processNo || '' }}</el-descriptions-item>
            <el-descriptions-item label="发起人">{{ selectedApproval?.initiator || '未知发起人' }}</el-descriptions-item>
            <el-descriptions-item label="发起时间">{{ selectedApproval?.createTime || '' }}</el-descriptions-item>
            <el-descriptions-item label="当前节点" :span="2">{{ selectedApproval?.currentNode || '未知节点' }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-form">
          <h4>表单内容</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item v-for="field in selectedApproval.formData" :key="field.key" :label="field.label">
              {{ field.value }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-history">
          <h4>审批历史</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(history, index) in selectedApproval.approvalHistory"
              :key="index"
              :timestamp="history.approveTime"
            >
              <div class="history-item">
                <div class="history-title">
                  <strong>{{ history.nodeName }}</strong>
                  <el-tag :type="history.action === 'approve' ? 'success' : 'danger'">
                    {{ history.action === 'approve' ? '同意' : '拒绝' }}
                  </el-tag>
                </div>
                <div class="history-content">
                  <div class="approver">{{ history.approver }}</div>
                  <div class="comment" v-if="history.comment">{{ history.comment }}</div>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
        <div class="detail-actions" v-if="selectedApproval.status === 'pending'">
          <h4>审批操作</h4>
          <el-form :model="approvalForm" label-width="80px">
            <el-form-item label="审批意见">
              <el-input
                v-model="approvalForm.comment"
                type="textarea"
                placeholder="请输入审批意见"
                :rows="4"
              />
            </el-form-item>
          </el-form>
          <div class="dialog-footer">
            <el-button @click="rejectApproval">拒绝</el-button>
            <el-button type="primary" @click="approveApproval">同意</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

// 定义审批类型
interface Approval {
  id: number
  processNo: string
  title: string
  type: string
  initiator: string
  createTime: string
  currentNode: string
  status: 'pending' | 'approved' | 'rejected' | 'completed'
  formData: Array<{
    key: string
    label: string
    value: string
  }>
  approvalHistory: Array<{
    nodeName: string
    approver: string
    action: 'approve' | 'reject'
    comment: string
    approveTime: string
  }>
}

// 模拟数据 - 审批列表
const approvals = ref<Approval[]>([
  {
    id: 1,
    processNo: 'OA-20251217-001',
    title: '请假申请',
    type: 'leave',
    initiator: '张三',
    createTime: '2025-12-17 10:30:00',
    currentNode: '部门主管审批',
    status: 'pending',
    formData: [
      { key: 'leaveType', label: '请假类型', value: '年假' },
      { key: 'startDate', label: '开始日期', value: '2025-12-20' },
      { key: 'endDate', label: '结束日期', value: '2025-12-22' },
      { key: 'days', label: '请假天数', value: '3天' },
      { key: 'reason', label: '请假原因', value: '个人事务处理' }
    ],
    approvalHistory: [
      {
        nodeName: '发起人提交',
        approver: '张三',
        action: 'approve',
        comment: '发起请假申请',
        approveTime: '2025-12-17 10:30:00'
      }
    ]
  },
  {
    id: 2,
    processNo: 'OA-20251216-002',
    title: '报销申请',
    type: 'expense',
    initiator: '李四',
    createTime: '2025-12-16 14:20:00',
    currentNode: '财务审批',
    status: 'approved',
    formData: [
      { key: 'expenseType', label: '报销类型', value: '差旅费' },
      { key: 'amount', label: '报销金额', value: '¥2,500.00' },
      { key: 'expenseDate', label: '报销日期', value: '2025-12-10' },
      { key: 'description', label: '报销说明', value: '客户拜访差旅费' }
    ],
    approvalHistory: [
      {
        nodeName: '发起人提交',
        approver: '李四',
        action: 'approve',
        comment: '提交报销申请',
        approveTime: '2025-12-16 14:20:00'
      },
      {
        nodeName: '部门主管审批',
        approver: '王五',
        action: 'approve',
        comment: '同意报销',
        approveTime: '2025-12-16 15:30:00'
      },
      {
        nodeName: '财务审批',
        approver: '赵六',
        action: 'approve',
        comment: '已审核通过',
        approveTime: '2025-12-17 09:15:00'
      }
    ]
  },
  {
    id: 3,
    processNo: 'OA-20251215-003',
    title: '采购申请',
    type: 'purchase',
    initiator: '王五',
    createTime: '2025-12-15 09:45:00',
    currentNode: '采购部审批',
    status: 'rejected',
    formData: [
      { key: 'purchaseType', label: '采购类型', value: '办公用品' },
      { key: 'items', label: '采购物品', value: 'A4打印纸 50包' },
      { key: 'amount', label: '采购金额', value: '¥1,200.00' },
      { key: 'reason', label: '采购原因', value: '部门办公用品补充' }
    ],
    approvalHistory: [
      {
        nodeName: '发起人提交',
        approver: '王五',
        action: 'approve',
        comment: '提交采购申请',
        approveTime: '2025-12-15 09:45:00'
      },
      {
        nodeName: '部门主管审批',
        approver: '赵六',
        action: 'approve',
        comment: '同意采购',
        approveTime: '2025-12-15 10:30:00'
      },
      {
        nodeName: '采购部审批',
        approver: '孙七',
        action: 'reject',
        comment: '当前库存充足，暂不需要采购',
        approveTime: '2025-12-15 14:20:00'
      }
    ]
  },
  {
    id: 4,
    processNo: 'OA-20251214-004',
    title: '合同审批',
    type: 'contract',
    initiator: '赵六',
    createTime: '2025-12-14 16:15:00',
    currentNode: '总经理审批',
    status: 'completed',
    formData: [
      { key: 'contractName', label: '合同名称', value: '2026年度服务合同' },
      { key: 'contractNo', label: '合同编号', value: 'HT-20251214-001' },
      { key: 'partner', label: '合作方', value: 'XX科技有限公司' },
      { key: 'amount', label: '合同金额', value: '¥500,000.00' },
      { key: 'term', label: '合同期限', value: '2026-01-01 至 2026-12-31' }
    ],
    approvalHistory: [
      {
        nodeName: '发起人提交',
        approver: '赵六',
        action: 'approve',
        comment: '提交合同审批',
        approveTime: '2025-12-14 16:15:00'
      },
      {
        nodeName: '法务审批',
        approver: '周八',
        action: 'approve',
        comment: '合同条款符合法律要求',
        approveTime: '2025-12-15 10:00:00'
      },
      {
        nodeName: '财务审批',
        approver: '吴九',
        action: 'approve',
        comment: '财务条款已审核',
        approveTime: '2025-12-15 14:30:00'
      },
      {
        nodeName: '总经理审批',
        approver: '郑十',
        action: 'approve',
        comment: '同意签订合同',
        approveTime: '2025-12-16 09:45:00'
      }
    ]
  }
])

// 筛选条件
const approvalType = ref('all')
const currentPage = ref(1)
const loading = ref(false)

// 状态类型映射
const statusTypeMap = {
  pending: 'info',
  approved: 'success',
  rejected: 'danger',
  completed: 'primary'
}

// 计算筛选后的审批列表
const filteredApprovals = computed(() => {
  if (approvalType.value === 'all') {
    return approvals.value
  }
  if (approvalType.value === 'pending') {
    return approvals.value.filter(a => a.status === 'pending')
  }
  if (approvalType.value === 'approved') {
    return approvals.value.filter(a => a.status === 'approved' || a.status === 'completed')
  }
  if (approvalType.value === 'initiated') {
    return approvals.value.filter(a => a.initiator === '张三') // 模拟当前用户为张三
  }
  return approvals.value
})

// 统计数据
const stats = ref({
  pending: approvals.value.filter(a => a.status === 'pending').length,
  approved: approvals.value.filter(a => a.status === 'approved' || a.status === 'completed').length,
  initiated: approvals.value.filter(a => a.initiator === '张三').length
})

// 审批详情
const showDetailDialog = ref(false)
const selectedApproval = ref<Approval | null>(null)

// 审批表单
const approvalForm = ref({
  comment: ''
})

// 查看详情
const viewDetail = (approval: Approval) => {
  selectedApproval.value = approval
  showDetailDialog.value = true
}

// 同意审批
const approveApproval = () => {
  if (selectedApproval.value) {
    selectedApproval.value.status = 'approved'
    showDetailDialog.value = false
  }
}

// 拒绝审批
const rejectApproval = () => {
  if (selectedApproval.value) {
    selectedApproval.value.status = 'rejected'
    showDetailDialog.value = false
  }
}
</script>

<style scoped>
.mobile-approval-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #ffffff;
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.approval-list-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.approval-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.approval-info {
  flex: 1;
}

.info-row {
  margin-bottom: 5px;
  font-size: 14px;
}

.info-row .label {
  font-weight: bold;
  margin-right: 5px;
  color: #606266;
}

.approval-status {
  margin-left: 20px;
}

.approval-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.approval-detail {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.detail-header h3 {
  margin: 0;
  margin-right: 20px;
}

.detail-info {
  margin-bottom: 20px;
}

.detail-form {
  margin-bottom: 20px;
}

.detail-form h4 {
  margin: 0 0 10px 0;
}

.detail-history {
  margin-bottom: 20px;
}

.detail-history h4 {
  margin: 0 0 15px 0;
}

.history-item {
  padding: 10px 0;
}

.history-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.history-content {
  margin-left: 20px;
}

.approver {
  font-weight: bold;
  margin-bottom: 5px;
}

.comment {
  font-style: italic;
  color: #606266;
}

.detail-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.detail-actions h4 {
  margin: 0 0 15px 0;
}

.dialog-footer {
  margin-top: 20px;
  text-align: right;
}
</style>

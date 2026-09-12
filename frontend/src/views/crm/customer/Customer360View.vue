<template>
  <div class="customer-360-view">
    <el-card shadow="hover" class="customer-header-card">
      <div class="customer-header">
        <div class="customer-basic-info">
          <h3 class="customer-name">{{ customerInfo.customerName }}</h3>
          <div class="customer-tags">
            <el-tag
              v-for="tag in customerInfo.tags"
              :key="tag"
              size="small"
              :type="'info'"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
        <div class="customer-meta-info">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="客户编号">{{ customerInfo.customerNo }}</el-descriptions-item>
            <el-descriptions-item label="客户类型">{{ customerTypeMap[customerInfo.customerType as string] || '-' }}</el-descriptions-item>
            <el-descriptions-item label="客户级别"><el-tag :type="levelTypeMap[customerInfo.level as string] || 'info'">{{ customerInfo.level || '-' }}</el-tag></el-descriptions-item>
            <el-descriptions-item label="客户状态"><el-tag :type="statusTypeMap[customerInfo.status as string] || 'info'">{{ statusMap[customerInfo.status as string] || '-' }}</el-tag></el-descriptions-item>
            <el-descriptions-item label="所属行业">{{ customerInfo.industry }}</el-descriptions-item>
            <el-descriptions-item label="企业规模">{{ scaleMap[customerInfo.scale as string] || '-' }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ customerInfo.ownerName }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ customerInfo.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ customerInfo.updateTime }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-card>

    <!-- 客户360°视图导航 -->
    <el-tabs v-model="activeTab" type="card" class="tabs-container">
      <!-- 基本信息标签页 -->
      <el-tab-pane label="基本信息" name="basic-info">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <span>详细信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="详细地址">{{ customerInfo.address }}</el-descriptions-item>
            <el-descriptions-item label="公司网址">{{ customerInfo.website || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="客户来源">{{ customerInfo.source }}</el-descriptions-item>
            <el-descriptions-item label="所在地区">{{ customerInfo.region }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 联系人信息 -->
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <span>联系人信息</span>
              <el-button type="primary" size="small" @click="handleAddContact">新增联系人</el-button>
            </div>
          </template>
          <el-table :data="contacts" style="width: 100%">
            <el-table-column prop="contactName" label="姓名" width="100" />
            <el-table-column prop="position" label="职位" width="120" />
            <el-table-column prop="phone" label="电话" width="120" />
            <el-table-column prop="mobile" label="手机" width="120" />
            <el-table-column prop="email" label="邮箱" width="150" />
            <el-table-column prop="wechat" label="微信" width="120" />
            <el-table-column prop="isPrimary" label="是否主要联系人" width="120">
              <template #default="scope">
                <el-switch v-model="scope.row.isPrimary" disabled />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" type="primary">编辑</el-button>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 交易记录标签页 -->
      <el-tab-pane label="交易记录" name="transactions">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <span>交易历史</span>
              <el-button type="primary" size="small" @click="handleExportTransactions">导出记录</el-button>
            </div>
          </template>
          <el-table :data="transactions" style="width: 100%">
            <el-table-column prop="orderNo" label="订单编号" width="150" />
            <el-table-column prop="amount" label="交易金额" width="120">
              <template #default="scope">
                <span style="color: #67C23A; font-weight: bold">¥{{ DataTransformer.formatMoney(scope.row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="dealDate" label="成交日期" width="120" />
            <el-table-column prop="productInfo" label="产品信息" width="200">
              <template #default="scope">
                <el-tag v-for="product in parseProducts(scope.row.productInfo)" :key="product.id" size="small" style="margin-right: 5px;">
                  {{ product.name }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="订单状态" width="100">
              <template #default="scope">
                <el-tag :type="transactionStatusTypeMap[scope.row.status]">
                  {{ transactionStatusMap[scope.row.status] }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 跟进记录标签页 -->
      <el-tab-pane label="跟进记录" name="follow-ups">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <span>跟进历史</span>
              <el-button type="primary" size="small" @click="handleAddFollowUp">新增跟进</el-button>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="followUp in followUps"
              :key="followUp.id"
              :timestamp="followUp.followUpTime"
              :type="followUpTypeMap[followUp.followUpType]"
              placement="top"
            >
              <el-card shadow="hover">
                <h4>{{ followUpTypeTextMap[followUp.followUpType] }} - 跟进记录</h4>
                <p>{{ followUp.content }}</p>
                <div class="follow-up-meta">
                  <span class="meta-item">跟进人: {{ followUp.followUpUserId }}</span>
                  <span class="meta-item">下次计划: {{ followUp.nextPlan || '无' }}</span>
                  <span class="meta-item">下次跟进时间: {{ followUp.nextTime || '未安排' }}</span>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-tab-pane>

      <!-- 商机信息标签页 -->
      <el-tab-pane label="商机信息" name="opportunities">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <span>相关商机</span>
            </div>
          </template>
          <el-table :data="opportunities" style="width: 100%">
            <el-table-column prop="opportunityName" label="商机名称" width="200" />
            <el-table-column prop="stage" label="当前阶段" width="120">
              <template #default="scope">
                <el-tag :type="stageTypeMap[scope.row.stage]">{{ scope.row.stage }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="winProbability" label="赢单概率" width="120">
              <template #default="scope">
                <el-progress :percentage="scope.row.winProbability" :stroke-width="10" :color="winProbabilityColorMap(scope.row.winProbability)"></el-progress>
              </template>
            </el-table-column>
            <el-table-column prop="estimatedAmount" label="预计金额" width="120">
              <template #default="scope">
                <span style="color: #67C23A; font-weight: bold">¥{{ DataTransformer.formatMoney(scope.row.estimatedAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="expectedCloseDate" label="预计成交日期" width="120" />
            <el-table-column prop="status" label="商机状态" width="100">
              <template #default="scope">
                <el-tag :type="opportunityStatusTypeMap[scope.row.status]">
                  {{ opportunityStatusMap[scope.row.status] }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增联系人对话框 -->
    <ContactDialog
      :visible="contactDialogVisible"
      :contact="currentContact"
      @close="handleContactDialogClose"
      @save="handleSaveContact"
    />

    <!-- 新增跟进记录对话框 -->
    <FollowUpDialog
      :visible="followUpDialogVisible"
      :follow-up="currentFollowUp"
      @close="handleFollowUpDialogClose"
      @save="handleSaveFollowUp"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { unwrapResponseData } from '../../../api'
import { customerApi, type Customer } from '../../../api/crm/customer'
import ContactDialog from './components/ContactDialog.vue'
import FollowUpDialog from './components/FollowUpDialog.vue'
import { DataTransformer } from '../../../utils/data-transformer'

const route = useRoute()

const parseProducts = (value: unknown): Array<{ id: string | number; name: string }> => {
  if (Array.isArray(value)) {
    return value.filter(v => v && typeof v === 'object' && 'name' in (v as any)) as any
  }
  if (!value) return []
  try {
    const parsed = typeof value === 'string' ? JSON.parse(value) : value
    if (!Array.isArray(parsed)) return []
    return parsed.filter(v => v && typeof v === 'object' && 'name' in (v as any)) as any
  } catch {
    return []
  }
}

// 当前激活的标签页
const activeTab = ref<string>('basic-info')

// 客户基本信息
const customerInfo = reactive<Customer>({
  id: undefined,
  customerNo: '',
  customerName: '',
  customerType: '',
  industry: '',
  scale: '',
  level: '',
  status: '',
  source: '',
  tags: [],
  region: '',
  address: '',
  website: '',
  ownerId: 0,
  ownerName: '',
  createTime: '',
  updateTime: ''
})

// 联系人列表
const contacts = ref<any[]>([])

// 交易记录
const transactions = ref<any[]>([])

// 跟进记录
const followUps = ref<any[]>([])

// 商机信息
const opportunities = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 新增联系人对话框
const contactDialogVisible = ref(false)
const currentContact = ref<any>({ customerId: 0 })

// 新增跟进记录对话框
const followUpDialogVisible = ref(false)
const currentFollowUp = ref<any>({ customerId: 0 })

// 映射表
const customerTypeMap: Record<string, string> = {
  'enterprise': '企业',
  'individual': '个人'
}

const statusMap: Record<string, string> = {
  'potential': '潜在客户',
  'active': '活跃客户',
  'inactive': '不活跃客户',
  'lost': '流失客户'
}

const statusTypeMap: Record<string, string> = {
  'potential': 'info',
  'active': 'success',
  'inactive': 'warning',
  'lost': 'danger'
}

const levelTypeMap: Record<string, string> = {
  'A': 'danger',
  'B': 'warning',
  'C': 'info'
}

const scaleMap: Record<string, string> = {
  'large': '大型企业',
  'medium': '中型企业',
  'small': '小型企业'
}

const transactionStatusMap: Record<string, string> = {
  'pending': '待处理',
  'completed': '已完成',
  'cancelled': '已取消'
}

const transactionStatusTypeMap: Record<string, string> = {
  'pending': 'warning',
  'completed': 'success',
  'cancelled': 'danger'
}

const followUpTypeMap: Record<string, string> = {
  'call': 'primary',
  'email': 'success',
  'visit': 'warning',
  'wechat': 'info'
}

const followUpTypeTextMap: Record<string, string> = {
  'call': '电话',
  'email': '邮件',
  'visit': '拜访',
  'wechat': '微信'
}

const stageTypeMap: Record<string, string> = {
  'initial': 'info',
  '需求确认': 'primary',
  '方案报价': 'success',
  '谈判': 'warning',
  '成交': 'success',
  '失败': 'danger'
}

const opportunityStatusMap: Record<string, string> = {
  'ongoing': '进行中',
  'won': '已赢单',
  'lost': '已输单'
}

const opportunityStatusTypeMap: Record<string, string> = {
  'ongoing': 'info',
  'won': 'success',
  'lost': 'danger'
}

const winProbabilityColorMap = (probability: number): string => {
  if (probability >= 80) return '#67C23A'
  if (probability >= 50) return '#E6A23C'
  return '#F56C6C'
}

// 获取客户360°视图数据
const fetchCustomer360Data = async () => {
  // 优先从query参数获取，其次从params获取
  const customerId = route.query.id || route.params.id as string | number
  console.log('客户360视图 - 客户ID:', customerId)
  if (!customerId) {
    // 静默处理，不再弹出错误消息
    console.log('客户ID为空，不获取客户360数据')
    return
  }
  
  loading.value = true
  try {
    // 发送API请求，使用客户ID作为缓存键
    console.log('客户360视图 - 发送API请求')
    const id = Array.isArray(customerId) ? customerId[0] : customerId
    // 确保id不是undefined
    if (!id) {
      console.log('解析后的客户ID为空，不获取客户360数据')
      loading.value = false
      return
    }
    const response = await customerApi.getCustomer360View(id as string | number)
    console.log('客户360视图 - API响应:', response)

    const normalized = DataTransformer.normalizeResponse(response)
    console.log('客户360视图 - 响应数据:', normalized)

    if (!DataTransformer.isSuccessCode(normalized?.code)) {
      console.error('客户360视图 - API返回错误:', normalized?.msg || normalized?.message || '未知错误')
      ElMessage.error(normalized?.msg || normalized?.message || '获取客户360°视图失败')
      return
    }

    const data = unwrapResponseData<any>(response) || {}
    console.log('客户360视图 - 实际数据:', data)
    console.log('客户360视图 - 数据结构:', Object.keys(data))
    
    // 更新客户基本信息
    if (data.customerInfo) {
      Object.assign(customerInfo, data.customerInfo)
      console.log('客户360视图 - 更新后的客户信息:', customerInfo)
    } else {
      console.error('客户360视图 - 没有customerInfo字段')
      ElMessage.warning('客户信息不完整')
    }
    
    // 更新关联数据，支持懒加载
    contacts.value = data.contacts || []
    console.log('客户360视图 - 更新后的联系人列表:', contacts.value)
    transactions.value = data.transactions || []
    console.log('客户360视图 - 更新后的交易记录列表:', transactions.value)
    followUps.value = data.followUps || []
    console.log('客户360视图 - 更新后的跟进记录列表:', followUps.value)
    opportunities.value = data.opportunities || []
    console.log('客户360视图 - 更新后的商机列表:', opportunities.value)
    
    // 显示成功消息
    ElMessage.success('获取客户360°视图成功')
  } catch (error) {
    console.error('获取客户360°视图失败:', error)
    ElMessage.error('获取客户360°视图失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 处理新增联系人
const handleAddContact = () => {
  currentContact.value = { customerId: customerInfo.id }
  contactDialogVisible.value = true
}

// 处理联系人对话框关闭
const handleContactDialogClose = () => {
  contactDialogVisible.value = false
  currentContact.value = { customerId: 0 }
}

// 保存联系人
const handleSaveContact = async (contactData: any) => {
  try {
    await customerApi.addCustomerContact(contactData.customerId, contactData)
    ElMessage.success('新增联系人成功')
    contactDialogVisible.value = false
    // 重新获取客户360数据
    fetchCustomer360Data()
  } catch (error) {
    console.error('新增联系人失败:', error)
    ElMessage.error('新增联系人失败')
  }
}

// 处理导出交易记录
const handleExportTransactions = async () => {
  const customerId = route.params.id as string | number
  try {
    const response = await customerApi.exportCustomerTransactions(customerId, {})
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `客户${customerInfo.customerName}_交易记录_${new Date().toISOString().split('T')[0]}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出交易记录失败:', error)
    ElMessage.error('导出交易记录失败')
  }
}

// 处理新增跟进记录
const handleAddFollowUp = () => {
  currentFollowUp.value = { 
    customerId: customerInfo.id,
    followUpTime: new Date().toISOString().slice(0, 16),
    followUpUserId: 1, // 示例用户ID，实际应从登录信息获取
    followUpUserName: '管理员' // 示例用户名，实际应从登录信息获取
  }
  followUpDialogVisible.value = true
}

// 处理跟进记录对话框关闭
const handleFollowUpDialogClose = () => {
  followUpDialogVisible.value = false
  currentFollowUp.value = { 
    customerId: 0,
    followUpTime: new Date().toISOString().slice(0, 16),
    followUpUserId: 0,
    followUpUserName: ''
  }
}

// 保存跟进记录
const handleSaveFollowUp = async (followUpData: any) => {
  try {
    await customerApi.addCustomerFollowUp(followUpData.customerId, followUpData)
    ElMessage.success('新增跟进记录成功')
    followUpDialogVisible.value = false
    // 重新获取客户360数据
    fetchCustomer360Data()
  } catch (error) {
    console.error('新增跟进记录失败:', error)
    ElMessage.error('新增跟进记录失败')
  }
}

// 初始化数据
onMounted(() => {
  // 只有当路由中包含客户ID时才获取数据，避免在客户列表页面触发错误
  const customerId = route.query.id || route.params.id as string | number
  if (customerId) {
    fetchCustomer360Data()
  }
})
</script>

<style scoped>
.customer-360-view {
  padding: 10px;
}

.customer-header-card {
  margin-bottom: 16px;
}

.customer-header {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.customer-basic-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.customer-name {
  font-size: 1.5rem;
  font-weight: bold;
  margin: 0;
}

.customer-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.customer-meta-info {
  margin-top: 8px;
}

.tabs-container {
  margin-top: 16px;
}

.info-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1.1rem;
}

.follow-up-meta {
  margin-top: 12px;
  display: flex;
  gap: 16px;
  font-size: 0.9rem;
  color: #606266;
}

.meta-item {
  display: inline-block;
}

.follow-up-type-text {
  font-weight: bold;
  margin-right: 8px;
}
</style>

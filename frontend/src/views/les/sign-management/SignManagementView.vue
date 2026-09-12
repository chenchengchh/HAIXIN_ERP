<template>
  <div class="sign-management-view">
    <div class="page-header">
      <h2>签收管理</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home/les">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/les">LES系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/les/sign-management">签收管理</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/les/sign-management#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 功能标签页 -->
    <el-tabs v-model="activeTab" class="function-tabs">
      <!-- 电子签收 -->
      <el-tab-pane label="电子签收" name="esign">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>移动端确认送达，支持手写签名直接同步至后台</span>
              </div>
            </template>
            <div class="esign-content">
              <!-- 筛选条件 -->
              <div class="filter-bar">
                <el-input placeholder="搜索计划编号" prefix-icon="Search" clearable style="width: 200px; margin-right: 16px;"></el-input>
                <el-select v-model="esignFilter.status" placeholder="选择状态" style="width: 120px; margin-right: 16px;">
                  <el-option label="全部" value="" />
                  <el-option label="待签收" value="pending" />
                  <el-option label="已签收" value="signed" />
                  <el-option label="异常" value="exception" />
                </el-select>
                <el-date-picker v-model="esignFilter.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 300px;"></el-date-picker>
              </div>
              
              <!-- 签收列表 -->
              <el-table :data="signList" style="width: 100%" height="500">
                <el-table-column prop="signNo" label="签收编号" width="150" />
                <el-table-column prop="planId" label="计划ID" width="100" />
                <el-table-column prop="vehicleId" label="车辆ID" width="100" />
                <el-table-column prop="driverName" label="司机" width="100" />
                <el-table-column prop="customerName" label="客户" width="120" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getStatusTag(scope.row.status)">
                      {{ getStatusLabel(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <!-- ERP 回写状态（F3 前端配套：LES签收回流ERP） -->
                <el-table-column prop="erpSyncStatus" label="ERP回写" width="110">
                  <template #default="scope">
                    <el-tag :type="getSyncStatusTag(scope.row.erpSyncStatus)" size="small">
                      {{ getSyncStatusLabel(scope.row.erpSyncStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <!-- CRM 回写状态（F3 前端配套：LES签收回流CRM） -->
                <el-table-column prop="crmSyncStatus" label="CRM回写" width="110">
                  <template #default="scope">
                    <el-tag :type="getSyncStatusTag(scope.row.crmSyncStatus)" size="small">
                      {{ getSyncStatusLabel(scope.row.crmSyncStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="signTime" label="签收时间" width="180" />
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button size="small" type="primary" @click="handleViewSign(scope.row)">查看签收</el-button>
                    <el-button size="small" type="success" @click="handleConfirmSign(scope.row)" v-if="scope.row.status === 'pending'">确认签收</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 签收凭证上传 -->
      <el-tab-pane label="凭证上传" name="upload">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>支持关联上传货物现场照片、破损证明等图片/视频文件</span>
              </div>
            </template>
            <div class="upload-content">
              <!-- 凭证上传区域 -->
              <div class="upload-section">
                <el-select v-model="selectedUploadPlanId" placeholder="选择签收单" style="width: 200px; margin-bottom: 16px;">
                  <el-option v-for="sign in signList" :key="sign.planId" :label="sign.signNo" :value="sign.planId" />
                </el-select>
                
                <el-upload
                  class="upload-demo"
                  drag
                  multiple
                  :auto-upload="false"
                  :limit="5"
                  :on-exceed="handleExceed"
                  :file-list="fileList"
                >
                  <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                  <div class="el-upload__text">
                    将文件拖到此处，或<em>点击上传</em>
                  </div>
                  <template #tip>
                    <div class="el-upload__tip">
                      只能上传jpg/png/gif/mp4文件，且不超过20MB
                    </div>
                  </template>
                </el-upload>
                
                <div class="upload-actions">
                  <el-button type="primary" @click="handleSubmitUpload">上传凭证</el-button>
                  <el-button @click="handleClearFiles">清空</el-button>
                </div>
              </div>
              
              <!-- 已上传凭证列表 -->
              <div class="uploaded-files" v-if="uploadedFiles.length > 0">
                <h3>已上传凭证</h3>
                <el-table :data="uploadedFiles" style="width: 100%" height="300">
                  <el-table-column prop="fileName" label="文件名" min-width="200" />
                  <el-table-column prop="fileType" label="文件类型" width="100">
                    <template #default="scope">
                      <el-tag>{{ scope.row.fileType }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="fileSize" label="文件大小" width="100">
                    <template #default="scope">
                      {{ formatFileSize(scope.row.fileSize) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="uploadTime" label="上传时间" width="180" />
                  <el-table-column prop="uploader" label="上传人" width="100" />
                  <el-table-column label="操作" width="150">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="handleViewFile(scope.row)">查看</el-button>
                      <el-button size="small" type="danger" @click="handleDeleteFile(scope.row)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 签收异常处理 -->
      <el-tab-pane label="异常处理" name="exception">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>管理拒签理由、破损换货处理流程及现场二次协调录入</span>
              </div>
            </template>
            <div class="exception-content">
              <!-- 异常列表 -->
              <el-table :data="exceptionList" style="width: 100%" height="500">
                <el-table-column prop="exceptionNo" label="异常编号" width="150" />
                <el-table-column prop="signId" label="签收ID" width="100" />
                <el-table-column prop="planId" label="计划ID" width="100" />
                <el-table-column prop="exceptionType" label="异常类型" width="120">
                  <template #default="scope">
                    <el-tag type="danger">{{ scope.row.exceptionType }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="reason" label="异常原因" min-width="200" />
                <el-table-column prop="customerFeedback" label="客户反馈" min-width="150" />
                <el-table-column prop="createTime" label="创建时间" width="180" />
                <el-table-column prop="handleStatus" label="处理状态" width="120">
                  <template #default="scope">
                    <el-tag :type="scope.row.handleStatus === 'resolved' ? 'success' : 'warning'">
                      {{ scope.row.handleStatus === 'resolved' ? '已解决' : '待处理' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button size="small" type="primary" @click="handleViewException(scope.row)">详情</el-button>
                    <el-button size="small" type="success" @click="handleResolveException(scope.row)" v-if="scope.row.handleStatus === 'pending'">处理</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 签收数据统计 -->
      <el-tab-pane label="数据统计" name="stats">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>可视化展示每日/每月的签收率、准时率等核心服务指标</span>
              </div>
            </template>
            <div class="stats-content">
              <!-- 统计筛选 -->
              <div class="stats-filter">
                <el-radio-group v-model="statsType" size="large" style="margin-right: 16px;">
                  <el-radio-button value="daily">日统计</el-radio-button>
                  <el-radio-button value="monthly">月统计</el-radio-button>
                </el-radio-group>
                
                <el-date-picker v-model="statsDate" type="date" placeholder="选择日期" style="width: 180px; margin-right: 16px;"></el-date-picker>
                
                <el-button type="primary" @click="handleRefreshStats">刷新数据</el-button>
              </div>
              
              <!-- 核心指标卡片 -->
              <div class="stats-cards">
                <el-row :gutter="20">
                  <el-col :span="6">
                    <el-card shadow="hover" class="stat-card">
                      <el-statistic :value="totalSigns" title="总签收单">
                      </el-statistic>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover" class="stat-card">
                      <el-statistic :value="signRate" title="签收率" :precision="1">
                        <template #suffix>
                          <span style="font-size: 14px;">%</span>
                        </template>
                      </el-statistic>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover" class="stat-card">
                      <el-statistic :value="onTimeRate" title="准时率" :precision="1">
                        <template #suffix>
                          <span style="font-size: 14px;">%</span>
                        </template>
                      </el-statistic>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover" class="stat-card">
                      <el-statistic :value="exceptionRate" title="异常率" :precision="1">
                        <template #suffix>
                          <span style="font-size: 14px;">%</span>
                        </template>
                      </el-statistic>
                    </el-card>
                  </el-col>
                </el-row>
              </div>
              
              <!-- 详细统计数据 -->
              <div class="stats-detail">
                <h3>详细统计数据</h3>
                <el-table :data="statsDetail" style="width: 100%" height="300">
                  <el-table-column prop="date" label="日期" width="120" />
                  <el-table-column prop="totalOrders" label="总订单" width="100" />
                  <el-table-column prop="signedOrders" label="已签收" width="100" />
                  <el-table-column prop="onTimeOrders" label="准时签收" width="120" />
                  <el-table-column prop="exceptionOrders" label="异常订单" width="120" />
                  <el-table-column prop="signRate" label="签收率" width="100">
                    <template #default="scope">
                      {{ scope.row.signRate }}%
                    </template>
                  </el-table-column>
                  <el-table-column prop="onTimeRate" label="准时率" width="100">
                    <template #default="scope">
                      {{ scope.row.onTimeRate }}%
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="signDetailVisible" title="签收详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="签收编号">{{ signDetail?.signNo }}</el-descriptions-item>
        <el-descriptions-item label="计划ID">{{ signDetail?.planId }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ signDetail?.customerName }}</el-descriptions-item>
        <el-descriptions-item label="司机">{{ signDetail?.driverName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusLabel(signDetail?.status || '') }}</el-descriptions-item>
        <el-descriptions-item label="签收时间">{{ signDetail?.signTime }}</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top: 16px;">
        <el-button v-if="signVoucherDetail?.customerSign" type="primary" plain @click="handleOpenUrl(signVoucherDetail.customerSign)">查看签名</el-button>
        <el-button v-for="u in signVoucherPhotoUrls" :key="u" type="primary" plain style="margin-left: 8px;" @click="handleOpenUrl(u)">查看照片</el-button>
      </div>
      <template #footer>
        <el-button type="primary" @click="signDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="confirmSignVisible" title="确认签收" width="720px">
      <el-form :model="confirmForm" label-width="100px">
        <el-form-item label="计划ID">
          <el-input v-model="confirmForm.planId" disabled />
        </el-form-item>
        <el-form-item label="到达时间">
          <el-date-picker v-model="confirmArrivalTime" type="datetime" placeholder="选择到达时间" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="准时状态">
          <el-select v-model="confirmForm.onTimeStatus" style="width: 100%;">
            <el-option label="准时" :value="types.OnTimeStatus.ON_TIME" />
            <el-option label="早到" :value="types.OnTimeStatus.EARLY" />
            <el-option label="延误" :value="types.OnTimeStatus.DELAYED" />
          </el-select>
        </el-form-item>
        <el-form-item label="签名文件">
          <el-upload :auto-upload="false" :limit="1" :file-list="confirmSignFiles">
            <el-button type="primary">选择签名图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="现场照片">
          <el-upload :auto-upload="false" multiple :limit="5" :file-list="confirmPhotoFiles">
            <el-button type="primary">选择照片</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmSignVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitConfirmSign">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="exceptionDetailVisible" title="异常详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="异常编号">{{ exceptionDetail?.exceptionNo }}</el-descriptions-item>
        <el-descriptions-item label="签收ID">{{ exceptionDetail?.signId }}</el-descriptions-item>
        <el-descriptions-item label="计划ID">{{ exceptionDetail?.planId }}</el-descriptions-item>
        <el-descriptions-item label="异常类型">{{ exceptionDetail?.exceptionType }}</el-descriptions-item>
        <el-descriptions-item label="原因" :span="2">{{ exceptionDetail?.reason }}</el-descriptions-item>
        <el-descriptions-item label="客户反馈" :span="2">{{ exceptionDetail?.customerFeedback }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ exceptionDetail?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">{{ exceptionDetail?.handleStatus }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="exceptionDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import useLesStore from '../../../stores/les'
import * as types from '../../../types/les'

// 激活的标签页
const activeTab = ref('esign')

// 初始化store
const store = useLesStore()

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  esign: '电子签收',
  upload: '凭证上传',
  exception: '异常处理',
  stats: '数据统计'
}

// 电子签收筛选条件
const esignFilter = ref({
  status: '',
  dateRange: null
})

const selectedUploadPlanId = ref<number | undefined>(undefined)

// 文件列表
const fileList = ref([])

const selectedUploadVoucher = computed(() => {
  if (!selectedUploadPlanId.value) return undefined
  return store.signVouchers.find(v => v.planId === selectedUploadPlanId.value)
})

const uploadedFiles = computed(() => {
  const urls = (selectedUploadVoucher.value?.photoUrls || '')
    .split(',')
    .map(s => s.trim())
    .filter(Boolean)
  return urls.map((url, index) => {
    const name = url.split('/').pop() || url
    return {
      id: `${index}-${url}`,
      fileName: name,
      fileType: '',
      fileSize: 0,
      uploadTime: '',
      uploader: '',
      url
    }
  })
})

// 统计类型
const statsType = ref('daily')

// 统计日期
const statsDate = ref(new Date())

// 签收列表（从store计算）
const signList = computed(() => {
  return store.transportPlans.map(plan => {
    const voucher = store.signVouchers.find(v => v.planId === plan.id)
    const driver = store.drivers.find(d => d.id === plan.driverId)
    const order = store.salesOrders.find(o => o.orderNo === plan.salesOrderNo)
    const status = voucher
      ? (voucher.onTimeStatus === types.OnTimeStatus.DELAYED ? 'exception' : 'signed')
      : 'pending'
    return {
      id: voucher?.id ?? plan.id,
      signNo: voucher ? `SIGN-${voucher.id}` : plan.planNo,
      planId: plan.id,
      vehicleId: plan.vehicleId || 0,
      driverName: driver?.name || '未知',
      customerName: order?.customerName || '',
      status,
      signTime: voucher?.arrivalTime || ''
    }
  })
})

const exceptionList = ref<any[]>([])

// 核心统计指标（从store计算）
const totalSigns = computed(() => store.signVouchers.length)
const signRate = computed(() => {
  const totalPlans = store.transportStats.totalPlans
  return totalPlans > 0 ? (store.signVouchers.length / totalPlans) * 100 : 0
})
const onTimeRate = computed(() => store.transportStats.averageOnTimeRate)
const exceptionRate = computed(() => {
  const total = store.signVouchers.length
  const exceptions = store.signVouchers.filter(v => v.onTimeStatus === types.OnTimeStatus.DELAYED).length
  return total > 0 ? (exceptions / total) * 100 : 0
})

const statsDetail = computed(() => {
  const formatDate = (text: string) => {
    if (!text) return ''
    return text.slice(0, 10)
  }

  const planByDate = new Map<string, number>()
  store.transportPlans.forEach(p => {
    const d = formatDate(p.createTime)
    if (!d) return
    planByDate.set(d, (planByDate.get(d) || 0) + 1)
  })

  const voucherByDate = new Map<string, { signed: number; onTime: number; exception: number }>()
  store.signVouchers.forEach(v => {
    const d = formatDate(v.arrivalTime)
    if (!d) return
    const current = voucherByDate.get(d) || { signed: 0, onTime: 0, exception: 0 }
    current.signed += 1
    if (v.onTimeStatus === types.OnTimeStatus.DELAYED) {
      current.exception += 1
    } else {
      current.onTime += 1
    }
    voucherByDate.set(d, current)
  })

  const dates = Array.from(new Set([...planByDate.keys(), ...voucherByDate.keys()])).sort((a, b) => (a < b ? 1 : -1))
  return dates.slice(0, 30).map(date => {
    const totalOrders = planByDate.get(date) || 0
    const v = voucherByDate.get(date) || { signed: 0, onTime: 0, exception: 0 }
    const signRate = totalOrders > 0 ? (v.signed / totalOrders) * 100 : 0
    const onTimeRate = v.signed > 0 ? (v.onTime / v.signed) * 100 : 0
    return {
      date,
      totalOrders,
      signedOrders: v.signed,
      onTimeOrders: v.onTime,
      exceptionOrders: v.exception,
      signRate: Number(signRate.toFixed(1)),
      onTimeRate: Number(onTimeRate.toFixed(1))
    }
  })
})

// 组件挂载时加载数据
onMounted(async () => {
  await Promise.all([
    store.fetchTransportPlans(),
    store.fetchVehicles(),
    store.fetchDrivers(),
    store.fetchSalesOrders(),
    store.fetchSignVouchers(),
    store.fetchTransportStats(),
    store.fetchServiceQualities()
  ])
  exceptionList.value = await store.fetchSignAnomalies()
})

// 获取状态标签类型
const getStatusTag = (status: string) => {
  switch (status) {
    case 'signed': return 'success'
    case 'pending': return 'warning'
    case 'exception': return 'danger'
    default: return 'info'
  }
}

// 获取状态标签
const getStatusLabel = (status: string) => {
  switch (status) {
    case 'signed': return '已签收'
    case 'pending': return '待签收'
    case 'exception': return '异常'
    default: return status
  }
}

/**
 * 获取回写状态标签类型（F3 前端配套：LES签收回流ERP/CRM）
 * @param status 回写状态（PENDING/SENT/FAILED）
 * @returns el-tag 类型
 */
const getSyncStatusTag = (status: string) => {
  switch (status) {
    case 'SENT': return 'success'
    case 'PENDING': return 'warning'
    case 'FAILED': return 'danger'
    default: return 'info'
  }
}

/**
 * 获取回写状态标签文本
 * @param status 回写状态（PENDING/SENT/FAILED）
 * @returns 中文标签
 */
const getSyncStatusLabel = (status: string) => {
  switch (status) {
    case 'SENT': return '已回写'
    case 'PENDING': return '待回写'
    case 'FAILED': return '回写失败'
    default: return '未推送'
  }
}

// 查看签收
const handleViewSign = async (sign: any) => {
  try {
    signDetailVisible.value = true
    signDetail.value = sign
    signVoucherDetail.value = store.signVouchers.find(v => v.planId === sign.planId) || null
  } catch (error) {
    ElMessage.error(`加载签收 ${sign.signNo} 详情失败`)
    console.error('Failed to view sign:', error)
  }
}

// 确认签收
const handleConfirmSign = async (sign: any) => {
  try {
    confirmForm.value = {
      planId: sign.planId,
      onTimeStatus: types.OnTimeStatus.ON_TIME
    }
    confirmArrivalTime.value = new Date()
    confirmSignFiles.value = []
    confirmPhotoFiles.value = []
    confirmSignVisible.value = true
  } catch (error) {
    ElMessage.error(`确认签收 ${sign.signNo} 失败`)
    console.error('确认签收失败:', error)
  }
}

// 文件超出限制处理
const handleExceed = (files: any, fileList: any) => {
  ElMessage.warning('每次最多只能上传5个文件')
  console.log('文件超出限制')
}

// 提交上传
const handleSubmitUpload = async () => {
  try {
    if (!selectedUploadPlanId.value) {
      ElMessage.warning('请先选择签收单')
      return
    }
    const voucher = store.signVouchers.find(v => v.planId === selectedUploadPlanId.value)
    if (!voucher) {
      ElMessage.warning('该计划尚未生成签收凭证，请先在“电子签收”中确认签收')
      return
    }
    if (fileList.value.length === 0) {
      ElMessage.warning('请先选择要上传的文件')
      return
    }
    const files = [...fileList.value]
    const urls: string[] = []
    for (const f of files as any[]) {
      const raw = (f as any)?.raw || f
      if (!raw) continue
      const res = await store.uploadSignImage(raw)
      if (res?.url) urls.push(res.url)
    }
    fileList.value = []
    const existing = (voucher.photoUrls || '').split(',').map(s => s.trim()).filter(Boolean)
    const merged = [...existing, ...urls].filter(Boolean)
    await store.updateSignVoucher(voucher.id, { photoUrls: merged.join(',') } as any)
    await store.fetchSignVouchers()
    ElMessage.success(`已成功上传 ${urls.length} 个文件`)
  } catch (error) {
    ElMessage.error('文件上传失败')
    console.error('Failed to submit upload:', error)
  }
}

// 清空文件
const handleClearFiles = () => {
  try {
    fileList.value = []
    ElMessage.success('已清空文件列表')
  } catch (error) {
    ElMessage.error('清空文件列表失败')
    console.error('Failed to clear files:', error)
  }
}

// 查看文件
const handleViewFile = async (file: any) => {
  try {
    if (file.url) {
      window.open(file.url, '_blank')
      ElMessage.success(`文件 ${file.fileName} 已打开`)
      return
    }
    ElMessage.warning('文件缺少可访问URL')
  } catch (error) {
    ElMessage.error(`查看文件 ${file.fileName} 失败`)
    console.error('Failed to view file:', error)
  }
}

// 删除文件
const handleDeleteFile = async (file: any) => {
  try {
    if (!selectedUploadPlanId.value) return
    const voucher = store.signVouchers.find(v => v.planId === selectedUploadPlanId.value)
    if (!voucher) return
    const existing = (voucher.photoUrls || '').split(',').map(s => s.trim()).filter(Boolean)
    const next = existing.filter(u => u !== file.url)
    await store.updateSignVoucher(voucher.id, { photoUrls: next.join(',') } as any)
    await store.fetchSignVouchers()
    ElMessage.success(`文件 ${file.fileName} 已删除`)
  } catch (error) {
    ElMessage.error(`删除文件 ${file.fileName} 失败`)
    console.error('Failed to delete file:', error)
  }
}

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (size < 1024) {
    return size + ' B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(1) + ' KB'
  } else {
    return (size / (1024 * 1024)).toFixed(1) + ' MB'
  }
}

// 查看异常详情
const handleViewException = async (exception: any) => {
  try {
    exceptionDetail.value = exception
    exceptionDetailVisible.value = true
  } catch (error) {
    ElMessage.error(`加载异常 ${exception.exceptionNo} 详情失败`)
    console.error('Failed to view exception:', error)
  }
}

// 处理异常
const handleResolveException = async (exception: any) => {
  try {
    ElMessage({ message: `正在处理异常 ${exception.exceptionNo}...`, type: 'info' })
    await store.updateSignVoucher(exception.signId, { status: 'signed' } as any)
    await Promise.all([store.fetchSignVouchers(), store.fetchTransportStats()])
    exceptionList.value = await store.fetchSignAnomalies()
    ElMessage.success(`异常 ${exception.exceptionNo} 已处理`)
  } catch (error) {
    ElMessage.error(`处理异常 ${exception.exceptionNo} 失败`)
    console.error('Failed to resolve exception:', error)
  }
}

// 刷新统计数据
const handleRefreshStats = async () => {
  try {
    ElMessage({ message: '正在刷新统计数据...', type: 'info' })
    await Promise.all([
      store.fetchSignVouchers(),
      store.fetchTransportStats()
    ])
    exceptionList.value = await store.fetchSignAnomalies()
    ElMessage.success('统计数据已刷新')
  } catch (error) {
    ElMessage.error('刷新统计数据失败')
    console.error('Failed to refresh stats:', error)
  }
}

const signDetailVisible = ref(false)
const signDetail = ref<any | null>(null)
const signVoucherDetail = ref<any | null>(null)
const signVoucherPhotoUrls = computed(() => {
  const urls = (signVoucherDetail.value?.photoUrls || '')
    .split(',')
    .map((s: string) => s.trim())
    .filter(Boolean)
  return urls
})

const handleOpenUrl = (url: string) => {
  if (!url) return
  window.open(url, '_blank')
}

const confirmSignVisible = ref(false)
const confirmForm = ref<{ planId: number; onTimeStatus: number }>({ planId: 0, onTimeStatus: types.OnTimeStatus.ON_TIME })
const confirmArrivalTime = ref<Date | null>(new Date())
const confirmSignFiles = ref<any[]>([])
const confirmPhotoFiles = ref<any[]>([])

const handleSubmitConfirmSign = async () => {
  try {
    if (!confirmForm.value.planId) return
    const signFile = confirmSignFiles.value?.[0]?.raw || confirmSignFiles.value?.[0]
    if (!signFile) {
      ElMessage.warning('请先选择签名图片')
      return
    }
    const uploadedSign = await store.uploadSignImage(signFile)
    const photoUrls: string[] = []
    for (const f of confirmPhotoFiles.value as any[]) {
      const raw = f?.raw || f
      if (!raw) continue
      const res = await store.uploadSignImage(raw)
      if (res?.url) photoUrls.push(res.url)
    }
    const arrival = confirmArrivalTime.value ? new Date(confirmArrivalTime.value) : new Date()
    const pad = (n: number) => String(n).padStart(2, '0')
    const arrivalTime =
      `${arrival.getFullYear()}-${pad(arrival.getMonth() + 1)}-${pad(arrival.getDate())}` +
      `T${pad(arrival.getHours())}:${pad(arrival.getMinutes())}:${pad(arrival.getSeconds())}`
    await store.createSignVoucher({
      planId: confirmForm.value.planId,
      customerSign: uploadedSign.url,
      photoUrls: photoUrls.join(','),
      arrivalTime,
      onTimeStatus: confirmForm.value.onTimeStatus as any
    } as any)
    await Promise.all([store.fetchSignVouchers(), store.fetchTransportStats()])
    exceptionList.value = await store.fetchSignAnomalies()
    confirmSignVisible.value = false
    ElMessage.success('签收已确认')
  } catch (error) {
    ElMessage.error('确认签收失败')
    console.error('Failed to confirm sign:', error)
  }
}

const exceptionDetailVisible = ref(false)
const exceptionDetail = ref<any | null>(null)
</script>

<style scoped>
.sign-management-view {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color-light);
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.8rem;
}

/* 覆盖默认的h2样式，确保只影响页面标题 */
h2 {
  margin-bottom: 0;
}

/* 功能标签页 */
.function-tabs {
  margin-bottom: 20px;
}

.tab-content {
  padding: 10px 0;
}

/* 卡片样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

/* 上传区域 */
.upload-section {
  margin-bottom: 20px;
}

.upload-demo {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  padding: 20px;
  margin-bottom: 16px;
}

.upload-actions {
  display: flex;
  gap: 10px;
}

/* 已上传文件区域 */
.uploaded-files {
  margin-top: 20px;
}

.uploaded-files h3 {
  margin-bottom: 16px;
  font-size: 1.2rem;
}

/* 统计筛选 */
.stats-filter {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

/* 统计卡片 */
.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

/* 统计图表 */
.stats-charts {
  margin-bottom: 20px;
}

.chart-card {
  height: 300px;
}

.chart-header {
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
}

.chart-content {
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 统计详情 */
.stats-detail {
  margin-top: 20px;
}

.stats-detail h3 {
  margin-bottom: 16px;
  font-size: 1.2rem;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .sign-management-view {
    padding: 16px;
  }
  
  h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 768px) {
  .sign-management-view {
    padding: 12px;
  }
  
  h2 {
    font-size: 1.3rem;
    margin-bottom: 16px;
  }
  
  .filter-bar,
  .stats-filter {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .filter-bar .el-select,
  .stats-filter .el-date-picker {
    width: 100% !important;
  }
  
  .stats-cards .el-col,
  .stats-charts .el-col {
    :span: 24;
  }
  
  .chart-card {
    margin-bottom: 20px;
  }
}
</style>

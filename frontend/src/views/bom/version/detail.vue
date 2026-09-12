<template>
  <bom-layout 
    :title="mode === 'edit' ? '编辑BOM版本' : id ? '查看BOM版本' : '新增BOM版本'" 
    :breadcrumb-items="breadcrumbItems"
  >
    <template #action-bar>
      <div class="header-actions">
        <el-button @click="handleBack" class="btn-subtle">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <el-button v-if="id && !isEditable" type="primary" @click="handleEdit" class="btn-glow">
          <el-icon><Edit /></el-icon>
          开始编辑
        </el-button>
        <el-button v-if="isEditable" type="success" @click="handleSave" :loading="saving" class="btn-glow">
          <el-icon><Check /></el-icon>
          保存更改
        </el-button>
      </div>
    </template>
    
    <!-- BOM版本信息展示区 -->
    <div class="version-detail-container">
      <el-tabs v-model="activeTab" class="premium-tabs">
        <!-- 核心数据 -->
        <el-tab-pane name="basic">
          <template #label>
            <span class="tab-label"><el-icon><InfoFilled /></el-icon> 核心信息</span>
          </template>
          <el-form :model="versionForm" :rules="versionRules" ref="versionFormRef" label-position="top">
            <el-row :gutter="32">
              <el-col :span="12">
                <el-form-item label="BOM 业务编码" prop="bomCode">
                  <el-input v-model="versionForm.bomCode" placeholder="如：BOM-PRD-001" :disabled="!isEditable" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="所属物料编码" prop="materialCode">
                  <el-input v-model="versionForm.materialCode" placeholder="关联的物料编码" :disabled="!isEditable" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="所属物料名称" prop="materialName">
                  <el-input v-model="versionForm.materialName" placeholder="关联的物料名称" :disabled="!isEditable" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="版本标识" prop="version">
                  <el-input v-model="versionForm.version" placeholder="如：V1.0.0" :disabled="!isEditable" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="BOM 类型属性" prop="type">
                  <el-select v-model="versionForm.type" placeholder="选择 BOM 定义类型" :disabled="!isEditable" style="width: 100%">
                    <el-option label="EBOM - 设计物料清单" :value="1" />
                    <el-option label="MBOM - 制造物料清单" :value="2" />
                    <el-option label="PBOM - 计划物料清单" :value="3" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="当前生命周期状态" prop="status">
                  <el-radio-group v-model="versionForm.status" :disabled="!isEditable">
                    <el-radio-button :value="0">草稿箱</el-radio-button>
                    <el-radio-button :value="1">正式生效</el-radio-button>
                    <el-radio-button :value="2">归档历史</el-radio-button>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="计划生效日期" prop="effectiveDate">
                  <el-date-picker v-model="versionForm.effectiveDate" type="date" placeholder="选择生效时间" :disabled="!isEditable" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="变更背景或设计备注" prop="remark">
                  <el-input
                    v-model="versionForm.remark"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入该版本的设计背景或变更摘要..."
                    :disabled="!isEditable"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>
        
        <!-- 变更足迹 -->
        <el-tab-pane v-if="id" name="change">
          <template #label>
            <span class="tab-label"><el-icon><History /></el-icon> 变更足迹</span>
          </template>
          <div class="history-timeline">
            <!-- 筛选和搜索 -->
            <div class="history-filter">
              <el-input
                v-model="historySearch"
                placeholder="搜索变更内容或原因"
                clearable
                prefix-icon="Search"
                size="small"
                class="search-input"
              />
              <el-select
                v-model="historyFilter"
                placeholder="按版本筛选"
                clearable
                size="small"
                class="filter-select"
              >
                <el-option
                  v-for="version in versionOptions"
                  :key="version"
                  :label="version"
                  :value="version"
                />
              </el-select>
            </div>
            
            <!-- 变更历史表格 -->
            <el-table 
              :data="filteredHistory" 
              style="width: 100%" 
              row-class-name="hover-row-effect"
              border
              stripe
            >
              <el-table-column type="index" label="序号" width="60" />
              <el-table-column prop="version" label="版本" width="100">
                <template #default="scope">
                  <el-tag effect="plain" type="info">{{ scope.row.version }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="changeReason" label="变更动因" min-width="150" show-overflow-tooltip />
              <el-table-column prop="changedBy" label="执行人" width="100" />
              <el-table-column prop="changeTime" label="执行时间" width="160">
                <template #default="scope">
                  <span>{{ new Date(scope.row.changeTime).toLocaleString() }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="changeContent" label="变更核心内容" min-width="250">
                <template #default="scope">
                  <el-popover
                    placement="top"
                    :width="400"
                    trigger="hover"
                  >
                    <template #reference>
                      <span class="content-text">{{ scope.row.changeContent }}</span>
                    </template>
                    <div class="popover-content">
                      <h4>变更详情</h4>
                      <p>{{ scope.row.changeContent }}</p>
                    </div>
                  </el-popover>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button 
                    size="small" 
                    type="primary" 
                    text
                    @click="handleViewDiff(scope.row)"
                  >
                    查看差异
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 空状态 -->
            <el-empty 
              v-if="filteredHistory.length === 0" 
              description="暂无变更记录"
              :image-size="100"
            >
              <el-button type="primary" text @click="loadChangeHistory">刷新</el-button>
            </el-empty>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </bom-layout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Edit, Check, InfoFilled, Clock as History } from '@element-plus/icons-vue'
import { unwrapListResponse, unwrapResponseData } from '@/api'
import { bomApi } from '@/api/bom'
import { ElMessage } from 'element-plus'
import BomLayout from '../../../components/BomLayout.vue'

// 路由实例
const router = useRouter()
const route = useRoute()

// 表单引用
const versionFormRef = ref()

// 加载状态
const saving = ref(false)

// 获取路由参数
const id = computed(() => route.query.id)
const mode = computed(() => route.query.mode || '')

// 面包屑项
const breadcrumbItems = computed(() => {
  return [
    { label: 'BOM版本列表', to: '/home/bom/version/list' },
    { label: mode.value === 'edit' ? '编辑BOM版本' : id.value ? '查看BOM版本' : '新增BOM版本' }
  ]
})

// 判断是否为编辑模式
const isEditable = computed(() => mode.value === 'edit' || !id.value)

// 判断是否为查看模式
const isViewMode = computed(() => !!id.value && mode.value !== 'edit')

// 当前激活的标签页
const activeTab = ref('basic')

// 监听id变化，重新加载变更历史
watch(() => id.value, (newId) => {
  if (newId) {
    loadChangeHistory()
  }
})

// BOM版本表单
const versionForm = ref<any>({
  id: '',
  bomCode: '',
  materialId: '',
  materialCode: '',
  materialName: '',
  version: '',
  type: '',
  status: 0,
  effectiveDate: '',
  expireDate: '',
  remark: ''
})

// 变更历史
const changeHistory = ref<any[]>([])

// 搜索和筛选
const historySearch = ref('')
const historyFilter = ref('')

// 版本选项（从变更历史中提取）
const versionOptions = computed(() => {
  const versions = [...new Set(changeHistory.value.map(item => item.version))]
  return versions.sort().reverse()
})

// 筛选后的变更历史
const filteredHistory = computed(() => {
  return changeHistory.value.filter(item => {
    // 版本筛选
    if (historyFilter.value && item.version !== historyFilter.value) {
      return false
    }
    // 关键词搜索
    if (historySearch.value) {
      const searchLower = historySearch.value.toLowerCase()
      return (
        item.changeReason?.toLowerCase().includes(searchLower) ||
        item.changeContent?.toLowerCase().includes(searchLower) ||
        item.changedBy?.toLowerCase().includes(searchLower)
      )
    }
    return true
  }).sort((a, b) => {
    // 按时间倒序排序
    return new Date(b.changeTime).getTime() - new Date(a.changeTime).getTime()
  })
})

// BOM版本表单验证规则
const versionRules = {
  bomCode: [
    { required: true, message: '请输入BOM编码', trigger: 'blur' },
    { min: 3, max: 50, message: 'BOM编码长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  materialCode: [
    { required: true, message: '请输入物料编码', trigger: 'blur' }
  ],
  version: [
    { required: true, message: '请输入版本号', trigger: 'blur' },
    { min: 1, max: 20, message: '版本号长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择BOM类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 初始化
onMounted(() => {
  if (id.value) {
    loadVersionDetail()
    loadChangeHistory()
  }
})

// 加载BOM版本详情
const loadVersionDetail = async () => {
  if (!id.value) return // 新增模式下不加载详情
  
  try {
    const res = await bomApi.getBomVersionDetail(String(id.value) || '1')
    versionForm.value = unwrapResponseData<any>(res) || versionForm.value
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载详情失败，请检查网络或联系管理员')
  }
}

// 加载变更历史
const loadChangeHistory = async () => {
  try {
    // 确保id为字符串或默认值'1'
    const bomId = id.value ? String(id.value) : '1'
    const res = await bomApi.getBomChangeHistory(bomId)
    changeHistory.value = unwrapListResponse<any>(res)
  } catch (error) {
    console.error('加载变更历史失败:', error)
    changeHistory.value = []
  }
}

// 返回BOM版本列表
const handleBack = () => {
  router.push('/home/bom/version/list')
}

// 切换到编辑模式
const handleEdit = () => {
  router.push(`/home/bom/version/detail?id=${id.value}&mode=edit`)
}

// 查看变更差异：跳转版本差异对比页，预选当前版本与历史版本
const handleViewDiff = (row: any) => {
  const currentId = row?.bomVersionId || id.value
  const historyId = row?.id
  if (!currentId || !historyId || String(currentId) === String(historyId)) {
    ElMessage.warning('请选择两个不同的版本进行对比')
    return
  }
  router.push(`/home/bom/structure/compare?id1=${currentId}&id2=${historyId}`)
}

// 保存BOM版本
const handleSave = async () => {
  if (!versionFormRef.value) return
  
  saving.value = true
  try {
    // 验证表单
    await versionFormRef.value.validate()

    if (!versionForm.value.materialId && versionForm.value.materialCode) {
      const matRes = await bomApi.getMaterialByCode(versionForm.value.materialCode)
      const list = unwrapListResponse<any>(matRes)
      versionForm.value.materialId = list?.[0]?.id || ''
      if (!versionForm.value.materialName) {
        versionForm.value.materialName = list?.[0]?.materialName || ''
      }
    }

    if (!versionForm.value.materialId) {
      ElMessage.error('请先填写有效的物料编码，或补充物料ID')
      return
    }
    
    let res
    if (id.value) {
      res = await bomApi.updateBomVersion(String(id.value), versionForm.value)
    } else {
      res = await bomApi.createBomVersion(versionForm.value)
    }
    
    if (res) {
      ElMessage.success('BOM版本保存成功')
      router.push('/home/bom/version/list')
      return
    }
  } catch (error: any) {
    console.error('保存版本失败:', error)
    if (error.name === 'Error') {
      ElMessage.error('表单验证失败，请检查输入内容')
    }
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 16px;
}

.btn-subtle {
  background: rgba(0,0,0,0.03);
  border: none;
  color: var(--text-regular);
}

.btn-subtle:hover {
  background: rgba(0,0,0,0.08);
  color: var(--text-primary);
}

.btn-glow {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
}

.btn-glow:hover {
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.5);
  transform: translateY(-2px);
}

.premium-tabs {
  margin-top: -10px;
}

:deep(.el-tabs__header) {
  margin-bottom: 30px;
  border-bottom: 2px solid rgba(0,0,0,0.05);
}

:deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
  height: 50px;
  line-height: 50px;
  color: var(--text-secondary);
  transition: all 0.3s;
}

:deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
  font-weight: 600;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--text-regular);
  padding-bottom: 8px !important;
}

:deep(.el-input__inner), :deep(.el-textarea__inner) {
  background: rgba(255,255,255,0.6) !important;
  border: 1px solid rgba(0,0,0,0.05) !important;
}

:deep(.el-table) {
  background: transparent !important;
  border-radius: var(--border-radius-md);
  overflow: hidden;
}

:deep(.el-table tr) {
  background: transparent !important;
}

:deep(.hover-row-effect:hover) {
  background: rgba(64, 104, 255, 0.05) !important;
}

/* 变更历史模块样式 */
.history-timeline {
  margin-top: 20px;
  padding: 20px;
  background: white;
  border-radius: var(--border-radius-lg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.history-filter {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.filter-select {
  width: 150px;
}

.content-text {
  cursor: pointer;
  color: var(--primary-color);
  text-decoration: underline;
}

.content-text:hover {
  color: var(--primary-color-dark);
}

.popover-content {
  padding: 10px;
}

.popover-content h4 {
  margin: 0 0 10px 0;
  color: var(--text-primary);
  font-size: 16px;
}

.popover-content p {
  margin: 0;
  color: var(--text-regular);
  line-height: 1.5;
}

:deep(.el-empty) {
  margin: 40px 0;
}
</style>

<template>
  <div class="process-route-container">
    <div class="process-route-header">
      <h2>工艺路线设计</h2>
      <el-button type="primary" @click="createProcessRoute">
        <el-icon><Plus /></el-icon> 新建工艺路线
      </el-button>
    </div>

    <div class="process-route-content">
      <!-- 工艺路线列表 -->
      <div class="route-list">
        <el-card class="route-card" v-for="route in filteredProcessRoutes" :key="route.id" @click="selectRoute(route.id)">
          <div class="route-header">
            <div class="route-info">
              <h3>{{ route.name }}</h3>
              <div class="route-meta">
                <span class="route-code">{{ route.code }}</span>
                <el-tag :type="routeStatusMap[route.status]" size="small">{{ routeStatusText[route.status] }}</el-tag>
              </div>
            </div>
            <el-button link type="primary" @click.stop="editRoute(route.id)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
          </div>
          <div class="route-details">
            <div class="route-detail-item">
              <span class="label">产品：</span>
              <span>{{ route.productName }} ({{ route.productCode }})</span>
            </div>
            <div class="route-detail-item">
              <span class="label">版本：</span>
              <span>{{ route.version }}</span>
            </div>
            <div class="route-detail-item">
              <span class="label">类型：</span>
              <span>{{ route.type === 'assembly' ? '组装工艺' : '生产工艺' }}</span>
            </div>
            <div class="route-detail-item">
              <span class="label">创建人：</span>
              <span>{{ route.createUser }}</span>
            </div>
          </div>
          <div class="route-steps">
            <h4>工艺步骤：</h4>
            <div class="steps-list">
              <div class="step-item" v-for="(step, index) in route.steps" :key="step.id">
                <div class="step-index">{{ index + 1 }}</div>
                <div class="step-content">
                  <div class="step-name">{{ step.name }}</div>
                  <div class="step-info">{{ step.equipment }} | {{ step.operator }} | {{ step.time }}分钟</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  <!-- 新建/编辑工艺路线对话框 -->
  <el-dialog
    v-model="routeDialogVisible"
    :title="isEditMode ? '编辑工艺路线' : '新建工艺路线'"
    width="70%"
    :close-on-click-modal="false"
  >
    <el-form :model="routeForm" ref="routeFormRef" :rules="routeRules" label-position="top">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="工艺路线名称" prop="name">
            <el-input v-model="routeForm.name" placeholder="请输入工艺路线名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工艺路线编码" prop="code">
            <el-input v-model="routeForm.code" placeholder="请输入工艺路线编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="产品名称" prop="productName">
            <el-input v-model="routeForm.productName" placeholder="请输入产品名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="产品编码" prop="productCode">
            <el-input v-model="routeForm.productCode" placeholder="请输入产品编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="版本" prop="version">
            <el-input v-model="routeForm.version" placeholder="请输入版本号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工艺类型" prop="type">
            <el-select v-model="routeForm.type" placeholder="请选择工艺类型">
              <el-option label="组装工艺" value="assembly" />
              <el-option label="生产工艺" value="production" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="工艺步骤">
            <div class="steps-container">
              <div v-for="(step, index) in routeForm.steps" :key="step.id" class="step-item-form">
                <div class="step-header">
                  <h4>步骤 {{ index + 1 }}</h4>
                  <el-button type="danger" text @click="removeStep(index)" :disabled="routeForm.steps.length <= 1">
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                </div>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item :prop="`steps.${index}.name`" :rules="[{ required: true, message: '请输入步骤名称', trigger: 'blur' }]">
                      <el-input v-model="step.name" placeholder="请输入步骤名称" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item :prop="`steps.${index}.equipment`" :rules="[{ required: true, message: '请输入设备名称', trigger: 'blur' }]">
                      <el-input v-model="step.equipment" placeholder="请输入设备名称" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item :prop="`steps.${index}.operator`" :rules="[{ required: true, message: '请输入操作人', trigger: 'blur' }]">
                      <el-input v-model="step.operator" placeholder="请输入操作人" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item :prop="`steps.${index}.time`" :rules="[{ required: true, message: '请输入操作时间', trigger: 'blur' }]">
                      <el-input-number v-model="step.time" :min="1" placeholder="请输入操作时间（分钟）" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item :prop="`steps.${index}.standard`" :rules="[{ required: true, message: '请输入操作标准', trigger: 'blur' }]">
                      <el-input v-model="step.standard" placeholder="请输入操作标准" type="textarea" rows="2" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
            </div>
            <el-button type="primary" text @click="addStep">
              <el-icon><Plus /></el-icon> 添加步骤
            </el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancelRouteDialog">取消</el-button>
        <el-button type="primary" @click="submitRouteForm" :loading="routeFormLoading">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 工艺路线详情对话框 -->
  <el-dialog
    v-model="detailDialogVisible"
    title="工艺路线详情"
    width="80%"
    :close-on-click-modal="false"
  >
    <div class="route-detail" v-if="selectedRoute">
      <div class="detail-section">
        <h3>基本信息</h3>
        <div class="detail-grid">
          <div class="detail-item">
            <span class="label">工艺路线名称：</span>
            <span>{{ selectedRoute.name }}</span>
          </div>
          <div class="detail-item">
            <span class="label">工艺路线编码：</span>
            <span>{{ selectedRoute.code }}</span>
          </div>
          <div class="detail-item">
            <span class="label">产品名称：</span>
            <span>{{ selectedRoute.productName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">产品编码：</span>
            <span>{{ selectedRoute.productCode }}</span>
          </div>
          <div class="detail-item">
            <span class="label">版本：</span>
            <span>{{ selectedRoute.version }}</span>
          </div>
          <div class="detail-item">
            <span class="label">工艺类型：</span>
            <span>{{ selectedRoute.type === 'assembly' ? '组装工艺' : '生产工艺' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">创建人：</span>
            <span>{{ selectedRoute.createUser }}</span>
          </div>
          <div class="detail-item">
            <span class="label">状态：</span>
            <el-tag :type="routeStatusMap[selectedRoute.status]">
              {{ routeStatusText[selectedRoute.status] }}
            </el-tag>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <h3>工艺步骤</h3>
        <div class="detail-steps">
          <div class="detail-step" v-for="(step, index) in selectedRoute.steps" :key="step.id">
            <div class="step-number">{{ index + 1 }}</div>
            <div class="step-content">
              <h4>{{ step.name }}</h4>
              <div class="step-meta">
                <span><el-icon><Tools /></el-icon> {{ step.equipment }}</span>
                <span><el-icon><User /></el-icon> {{ step.operator }}</span>
                <span><el-icon><Clock /></el-icon> {{ step.time }}分钟</span>
              </div>
              <div class="step-standard">
                <strong>操作标准：</strong>{{ step.standard }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Edit, Delete, Tools, User, Clock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useProcessCollaborationStore } from '../../../stores/plm/processCollaboration'

const router = useRouter()
const store = useProcessCollaborationStore()

// 工艺路线状态映射
const routeStatusMap: Record<string, string> = {
  active: 'success',
  draft: 'warning',
  inactive: 'info',
  deleted: 'danger'
}

const routeStatusText: Record<string, string> = {
  active: '生效',
  draft: '草稿',
  inactive: '失效',
  deleted: '已删除'
}

// 分页信息
const pagination = computed(() => ({
  page: store.pagination.page,
  pageSize: store.pagination.pageSize,
  total: store.pagination.total
}))

// 筛选后的工艺路线列表
const filteredProcessRoutes = computed(() => store.filteredProcessRoutes)

// 对话框状态
const routeDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEditMode = ref(false)
const selectedRoute = ref<any>(null)
const routeFormLoading = ref(false)
const routeFormRef = ref<any>(null)

// 工艺路线表单
const routeForm = ref({
  id: '',
  name: '',
  code: '',
  productName: '',
  productCode: '',
  version: 'V1.0',
  type: 'assembly',
  status: 'draft',
  createUser: '当前用户',
  createTime: new Date().toISOString().split('T')[0],
  steps: [
    {
      id: '1',
      name: '',
      equipment: '',
      operator: '',
      time: 10,
      standard: ''
    }
  ]
})

// 表单验证规则
const routeRules = reactive({
  name: [
    { required: true, message: '请输入工艺路线名称', trigger: 'blur' },
    { min: 2, max: 50, message: '工艺路线名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入工艺路线编码', trigger: 'blur' },
    { min: 2, max: 20, message: '工艺路线编码长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  productName: [
    { required: true, message: '请输入产品名称', trigger: 'blur' }
  ],
  productCode: [
    { required: true, message: '请输入产品编码', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择工艺类型', trigger: 'change' }
  ]
})

// 页面加载时获取工艺路线数据
onMounted(async () => {
  await store.fetchProcessRoutes()
})

// 创建工艺路线
const createProcessRoute = () => {
  isEditMode.value = false
  resetRouteForm()
  routeDialogVisible.value = true
}

// 编辑工艺路线
const editRoute = (routeId: string) => {
  const route = store.processRoutes.find(r => r.id === routeId)
  if (route) {
    isEditMode.value = true
    routeForm.value = {
      ...route,
      createTime: new Date().toISOString().split('T')[0] as string
    } as any
    routeDialogVisible.value = true
  }
}

// 查看工艺路线详情
const selectRoute = (routeId: string) => {
  const route = store.processRoutes.find(r => r.id === routeId)
  if (route) {
    selectedRoute.value = { ...route }
    detailDialogVisible.value = true
  }
}

// 重置表单
const resetRouteForm = () => {
  routeForm.value = {
    id: '',
    name: '',
    code: '',
    productName: '',
    productCode: '',
    version: 'V1.0',
    type: 'assembly',
    status: 'draft',
    createUser: '当前用户',
    createTime: new Date().toISOString().split('T')[0],
    steps: [
      {
        id: '1',
        name: '',
        equipment: '',
        operator: '',
        time: 10,
        standard: ''
      }
    ]
  }
  if (routeFormRef.value) {
    routeFormRef.value.resetFields()
  }
}

// 取消对话框
const cancelRouteDialog = () => {
  routeDialogVisible.value = false
  if (routeFormRef.value) {
    routeFormRef.value.resetFields()
  }
}

// 添加步骤
const addStep = () => {
  const newStep = {
    id: `${Date.now()}`,
    name: '',
    equipment: '',
    operator: '',
    time: 10,
    standard: ''
  }
  routeForm.value.steps.push(newStep)
}

// 删除步骤
const removeStep = (index: number) => {
  if (routeForm.value.steps.length > 1) {
    routeForm.value.steps.splice(index, 1)
  }
}

// 提交表单
const submitRouteForm = async () => {
  if (!routeFormRef.value) return
  
  try {
    await routeFormRef.value.validate()
    routeFormLoading.value = true

    if (isEditMode.value) {
      // 更新工艺路线
      const updated = await store.updateProcessRoute(routeForm.value)
      if (updated) ElMessage.success('工艺路线更新成功')
    } else {
      // 添加新工艺路线
      const created = await store.addProcessRoute(routeForm.value)
      if (created) ElMessage.success('工艺路线创建成功')
    }
    
    routeDialogVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    routeFormLoading.value = false
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  store.setPagination(1, size)
}

// 页码变化
const handleCurrentChange = (current: number) => {
  store.setPagination(current, store.pagination.pageSize)
}
</script>

<style scoped>
.process-route-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.process-route-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.process-route-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.process-route-content {
  margin-bottom: 20px;
}

.route-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.route-card {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.route-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.route-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.route-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #333;
}

.route-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.route-code {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.route-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f9fafb;
  border-radius: 4px;
}

.route-detail-item {
  display: flex;
  align-items: center;
}

.route-detail-item .label {
  font-weight: 500;
  margin-right: 8px;
  color: #666;
}

.route-steps h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.steps-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.step-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.step-index {
  width: 24px;
  height: 24px;
  background-color: #409eff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
}

.step-name {
  font-weight: 500;
  margin-bottom: 4px;
  color: #333;
}

.step-info {
  font-size: 14px;
  color: #666;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 对话框样式 */
.steps-container {
  margin-bottom: 15px;
}

.step-item-form {
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
}

.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.step-header h4 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

/* 详情页样式 */
.route-detail {
  padding: 20px;
}

.detail-section {
  margin-bottom: 25px;
}

.detail-section h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #333;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 8px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.detail-item .label {
  font-weight: 500;
  margin-right: 10px;
  color: #666;
  width: 120px;
  flex-shrink: 0;
}

.detail-steps {
  margin-top: 20px;
}

.detail-step {
  display: flex;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9fafb;
  border-radius: 4px;
  border-left: 4px solid #409eff;
}

.step-number {
  width: 36px;
  height: 36px;
  background-color: #409eff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  margin-right: 15px;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
}

.step-content h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.step-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.step-standard {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .step-item-form {
    padding: 12px;
  }
  
  .detail-step {
    padding: 12px;
  }
}
</style>

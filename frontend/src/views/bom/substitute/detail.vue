<template>
  <bom-layout 
    :title="title" 
    :breadcrumb-items="breadcrumbItems"
  >
    <template #action-bar>
      <div class="header-actions">
        <el-button @click="handleBack" class="btn-subtle">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <el-button
          type="primary"
          class="btn-glow"
          :loading="submitting"
          @click="handleSubmit"
        >
          <el-icon><Check /></el-icon>
          {{ isViewMode ? '返回列表' : (isEditMode ? '提交变更' : '立即创建') }}
        </el-button>
      </div>
    </template>

    <!-- 替代料详情表单 -->
    <el-card class="detail-card card-glossy">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        :disabled="isViewMode"
      >
        <!-- 主料信息 -->
        <div class="section-title">
          <span>主料信息 (Primary Material)</span>
          <div class="title-line"></div>
        </div>
        <el-row :gutter="32">
          <el-col :span="8">
            <el-form-item label="主料编码" prop="mainMaterialCode">
              <el-input v-model="formData.mainMaterialCode" placeholder="输入主料编码" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="主料名称" prop="mainMaterialName">
              <el-input v-model="formData.mainMaterialName" placeholder="主料全称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="规格型号">
              <el-input v-model="formData.mainMaterialSpec" placeholder="封装规格" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 替代料信息 -->
        <div class="section-title">
          <span>替代料信息 (Substitute Material)</span>
          <div class="title-line"></div>
        </div>
        <el-row :gutter="32">
          <el-col :span="8">
            <el-form-item label="替代料编码" prop="subMaterialCode">
              <el-input v-model="formData.subMaterialCode" placeholder="输入替代料编码" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="替代料名称" prop="subMaterialName">
              <el-input v-model="formData.subMaterialName" placeholder="替代料全称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="规格型号">
              <el-input v-model="formData.subMaterialSpec" placeholder="替代品规格" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 逻辑属性 -->
        <div class="section-title">
          <span>替代规则逻辑 (Substitute Logic)</span>
          <div class="title-line"></div>
        </div>
        <el-row :gutter="32">
          <el-col :span="8">
            <el-form-item label="替代类型判断" prop="substituteType">
              <el-select v-model="formData.substituteType" style="width: 100%">
                <el-option label="全局替代 - 适用于所有 BOM" :value="1" />
                <el-option label="局部替代 - 仅限特定 BOM 行" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="比例 (1:X)" prop="ratio">
              <el-input-number v-model="formData.ratio" :min="0.1" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="优先级 (1-10)" prop="priority">
              <el-input-number v-model="formData.priority" :min="1" :max="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio-button :value="1">启用关系</el-radio-button>
                <el-radio-button :value="0">暂时失效</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="32">
          <el-col :span="24">
            <el-form-item label="规则备注">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                placeholder="详细说明替代场景、限制条件或特殊的工艺要求..."
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </bom-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Check, InfoFilled, Setting } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { unwrapResponseData } from '@/api'
import { bomApi } from '@/api/bom'
import { ElMessage } from 'element-plus'
import { DataTransformer } from '@/utils/data-transformer'
import BomLayout from '../../../components/BomLayout.vue'

// 路由实例
const router = useRouter()
const route = useRoute()

// 表单引用
const formRef = ref<FormInstance>()

// 提交状态
const submitting = ref(false)

// 面包屑
const breadcrumbItems = [
  { label: '物料替代管理', path: '/home/bom/substitute/list' },
  { label: '替代关系详情' }
]

// 表单数据
const formData = ref<any>({
  id: null,
  mainMaterialId: null,
  mainMaterialCode: '',
  mainMaterialName: '',
  subMaterialId: null,
  subMaterialCode: '',
  subMaterialName: '',
  substituteType: 1,
  ratio: 1.0,
  priority: 1,
  status: 1,
  bomLineId: '',
  remark: '',
  createTime: '',
  updateTime: ''
})

// 表单验证规则
const formRules = {
  mainMaterialCode: [
    { required: true, message: '请输入主料编码', trigger: 'blur' },
    { min: 3, max: 50, message: '主料编码长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  mainMaterialName: [
    { required: true, message: '请输入主料名称', trigger: 'blur' },
    { min: 1, max: 100, message: '主料名称长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  mainMaterialSpec: [
    { required: true, message: '请输入主料规格', trigger: 'blur' },
    { min: 1, max: 200, message: '主料规格长度在 1 到 200 个字符', trigger: 'blur' }
  ],
  subMaterialCode: [
    { required: true, message: '请输入替代料编码', trigger: 'blur' },
    { min: 3, max: 50, message: '替代料编码长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  subMaterialName: [
    { required: true, message: '请输入替代料名称', trigger: 'blur' },
    { min: 1, max: 100, message: '替代料名称长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  subMaterialSpec: [
    { required: true, message: '请输入替代料规格', trigger: 'blur' },
    { min: 1, max: 200, message: '替代料规格长度在 1 到 200 个字符', trigger: 'blur' }
  ],
  substituteType: [
    { required: true, message: '请选择替代类型', trigger: 'change' }
  ],
  ratio: [
    { required: true, message: '请输入替代比例', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请输入优先级', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 路由参数
const routeParams = computed(() => route.params)
const queryParams = computed(() => route.query)

// 记录ID：同时兼容路径参数（/detail/5）与查询参数（/detail?id=5）两种跳转方式
const recordId = computed(() => String(routeParams.value.id || queryParams.value.id || ''))

// 模式判断
// 注意：路由可选参数 :id? 未传时为 ''（假值但非布尔），必须转严格布尔，
// 否则 el-form 的 Boolean 型 disabled 会将 '' 当作 true 导致整个表单被禁用
const isEditMode = computed(() => queryParams.value.mode === 'edit')
const isViewMode = computed(() => Boolean(!queryParams.value.mode && recordId.value))
const isAddMode = computed(() => !isEditMode.value && !isViewMode.value)

// 页面标题
const title = computed(() => {
  if (isAddMode.value) return '新增替代料'
  if (isEditMode.value) return '编辑替代料'
  return '查看替代料'
})

const getResponseMessage = (response: any, fallback: string) => {
  const normalized = DataTransformer.normalizeResponse(response)
  return normalized?.msg || normalized?.message || fallback
}

// 生命周期钩子
onMounted(() => {
  // 如果有ID，加载替代料详情
  if (recordId.value) {
    fetchSubstituteDetail()
  }
})

// 获取替代料详情
const fetchSubstituteDetail = async () => {
  const id = recordId.value
  if (!id) return
  
  try {
    const res = await bomApi.getSubstituteDetail(id)
    formData.value = unwrapResponseData<any>(res, formData.value) || formData.value
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败，请检查网络或联系管理员')
  }
}

// 返回列表
const handleBack = () => {
  router.push('/home/bom/substitute/list')
}

// 提交表单
const handleSubmit = async () => {
  if (isViewMode.value) {
    handleBack()
    return
  }
  
  if (!formRef.value) return
  
  try {
    submitting.value = true
    await formRef.value.validate()
    
    let res
    if (formData.value.id) {
      res = await bomApi.updateSubstitute(formData.value.id, formData.value)
    } else {
      res = await bomApi.createSubstitute(formData.value)
    }

    const responseData = DataTransformer.normalizeResponse(res)
    if (DataTransformer.isSuccessCode(responseData?.code)) {
      ElMessage.success('保存成功')
      handleBack()
      return
    }
    ElMessage.error(getResponseMessage(responseData, '保存失败'))
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请检查网络或联系管理员')
  } finally {
    submitting.value = false
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

.detail-card {
  padding: 30px;
  border-radius: var(--border-radius-lg);
}

.section-title {
  margin: 24px 0 24px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  position: relative;
  display: inline-block;
}

.title-line {
  position: absolute;
  bottom: -6px;
  left: 0;
  width: 40px;
  height: 4px;
  background: var(--grad-primary);
  border-radius: 2px;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-card {
    padding: 16px;
  }
}
</style>

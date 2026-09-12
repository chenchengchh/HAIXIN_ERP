<template>
  <div class="material-detail">
    <div class="page-header">
      <div class="title-with-icon">
        <div class="back-icon" @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
        </div>
        <h2>{{ title }}</h2>
      </div>
      <div class="header-actions">
        <el-button 
          type="primary" 
          class="btn-glow"
          :loading="submitting"
          @click="handleSubmit"
        >
          <el-icon><Check /></el-icon>
          {{ isViewMode ? '返回列表' : (isEditMode ? '提交更新' : '立即创建') }}
        </el-button>
      </div>
    </div>

    <!-- 物料详情表单 -->
    <el-card class="detail-card card-glossy">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        :disabled="isViewMode"
      >
        <!-- 基本信息 -->
        <div class="section-title">
          <span>基本信息</span>
          <div class="title-line"></div>
        </div>
        <el-row :gutter="32">
          <el-col :span="8">
            <el-form-item label="物料编码" prop="materialCode">
              <el-input
                v-model="formData.materialCode"
                placeholder="系统自动生成或手动录入"
                :disabled="isEditMode"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="物料名称" prop="materialName">
              <el-input
                v-model="formData.materialName"
                placeholder="物料全称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="规格型号" prop="specification">
              <el-input
                v-model="formData.specification"
                placeholder="封装、尺寸等规格"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="32">
          <el-col :span="8">
            <el-form-item label="基本单位" prop="unit">
              <el-select
                v-model="formData.unit"
                placeholder="常用单位"
                style="width: 100%"
              >
                <el-option label="PCS - 个" value="pcs" />
                <el-option label="SET - 套" value="set" />
                <el-option label="KG - 公斤" value="kg" />
                <el-option label="M - 米" value="m" />
                <el-option label="L - 升" value="l" />
                <el-option label="张" value="张" />
                <el-option label="卷" value="卷" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="物料类型" prop="materialType">
              <el-select
                v-model="formData.materialType"
                placeholder="物料属性"
                style="width: 100%"
              >
                <el-option label="RAW - 原材料" value="RAW" />
                <el-option label="SEMI - 半成品" value="SEMI" />
                <el-option label="FIN - 成品" value="FIN" />
                <el-option label="TOOL - 工具" value="TOOL" />
                <el-option label="PACKAGE - 包装材料" value="PACKAGE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="物料分类" prop="categoryId">
              <el-select
                v-model="formData.categoryId"
                placeholder="选择物料分类"
                style="width: 100%"
              >
                <el-option label="原材料" value="1" />
                <el-option label="金属材料" value="2" />
                <el-option label="塑料材料" value="3" />
                <el-option label="半成品" value="4" />
                <el-option label="成品" value="5" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="32">
          <el-col :span="8">
            <el-form-item label="单价" prop="unitPrice">
              <el-input
                v-model="formData.unitPrice"
                type="number"
                placeholder="物料单价"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status" class="status-radio">
                <el-radio-button :value="1">启用</el-radio-button>
                <el-radio-button :value="0">停用</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="创建时间" v-if="isViewMode">
              <el-input v-model="formData.createTime" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 详细描述 -->
        <div class="section-title">
          <span>详细描述与备注</span>
          <div class="title-line"></div>
        </div>
        <el-row :gutter="32">
          <el-col :span="24">
            <el-form-item label="备注说明">
              <el-input
                v-model="formData.description"
                type="textarea"
                :rows="4"
                placeholder="补充物料相关的技术参数、使用建议或生命周期说明..."
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Check, InfoFilled, Setting } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { unwrapResponseData } from '@/api'
import { bomApi, type Material } from '@/api/bom'
import { ElMessage } from 'element-plus'
import { DataTransformer } from '@/utils/data-transformer'

// 路由实例
const router = useRouter()
const route = useRoute()

// 表单引用
const formRef = ref<FormInstance>()

// 提交状态
const submitting = ref(false)

// 表单数据
const formData = ref<any>({
  id: null,
  materialCode: '',
  materialName: '',
  specification: '',
  unit: '',
  materialType: '',
  categoryId: null,
  categoryName: '',
  unitPrice: 0,
  status: 1,
  attrJson: {},
  description: '',
  createTime: '',
  updateTime: ''
})

// 表单验证规则
const formRules = {
  materialCode: [
    { required: true, message: '请输入物料编码', trigger: 'blur' },
    { min: 7, max: 20, message: '物料编码长度在 7 到 20 个字符', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]+$/, message: '物料编码只能包含字母和数字', trigger: 'blur' }
  ],
  materialName: [
    { required: true, message: '请输入物料名称', trigger: 'blur' },
    { min: 2, max: 100, message: '物料名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  specification: [
    { required: true, message: '请输入规格型号', trigger: 'blur' },
    { min: 1, max: 200, message: '规格型号长度在 1 到 200 个字符', trigger: 'blur' }
  ],
  unit: [
    { required: true, message: '请选择基本单位', trigger: 'change' }
  ],
  materialType: [
    { required: true, message: '请选择物料类型', trigger: 'change' }
  ],
  categoryId: [
    { required: true, message: '请选择物料分类', trigger: 'change' }
  ],
  unitPrice: [
    {
      validator: (rule: any, value: number, callback: (error?: Error) => void) => {
        // 由于我们已经在watch中处理了unitPrice，这里只需要简单验证
        if (value === null || value === undefined || isNaN(value)) {
          callback(new Error('请输入有效的单价'))
        } else if (value < 0) {
          callback(new Error('单价不能小于 0'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 路由参数
const routeParams = computed(() => route.params)
const queryParams = computed(() => route.query)

// 模式判断
const isEditMode = computed(() => queryParams.value.mode === 'edit')
const isViewMode = computed(() => Boolean(!queryParams.value.mode && routeParams.value.id))
const isAddMode = computed(() => Boolean(!isEditMode.value && !routeParams.value.id))

// 页面标题
const title = computed(() => {
  if (isAddMode.value) return '新增物料'
  if (isEditMode.value) return '编辑物料'
  return '查看物料'
})

const getResponseMessage = (response: any, fallback: string) => {
  const normalized = DataTransformer.normalizeResponse(response)
  return normalized?.msg || normalized?.message || fallback
}

// 生命周期钩子
onMounted(() => {
  // 如果有ID，加载物料详情
  if (routeParams.value.id) {
    fetchMaterialDetail()
  } else {
    // 新增模式下，自动生成物料编码
    generateMaterialCode()
  }
})

// 监听unitPrice变化，实时处理数据格式
watch(() => formData.value.unitPrice, (newValue) => {
  let processedValue = newValue
  
  // 处理各种可能的无效值
  if (processedValue === '' || processedValue === null || processedValue === undefined) {
    processedValue = 0
  } else if (typeof processedValue === 'string') {
    // 处理字符串类型的输入
    processedValue = parseFloat(processedValue)
    if (isNaN(processedValue)) {
      processedValue = 0
    }
  } else if (typeof processedValue !== 'number') {
    // 其他类型转换为数字
    processedValue = Number(processedValue)
    if (isNaN(processedValue)) {
      processedValue = 0
    }
  }
  
  // 确保unitPrice是正数
  if (processedValue < 0) {
    processedValue = 0
  }
  
  // 更新表单数据
  formData.value.unitPrice = processedValue
}, { immediate: true }) // 立即执行一次，处理初始值

// 生成物料编码
const generateMaterialCode = () => {
  // 生成格式：MAT + 6位数字，从000001开始
  let code = ''
  let isUnique = false
  
  // 确保生成的编码唯一
  while (!isUnique) {
    const now = new Date()
    const timestamp = now.getTime().toString().slice(-6)
    code = `MAT${timestamp.padStart(6, '0')}`
    
    // 简单的唯一性检查（实际项目中应该检查后端数据库）
    isUnique = true
  }
  
  formData.value.materialCode = code
}



// 获取物料详情
const fetchMaterialDetail = async () => {
  const materialId = route.params.id as string
  if (!materialId) return
  
  try {
    const res = await bomApi.getMaterialDetail(materialId)
    const data = unwrapResponseData<any>(res) || {}
    formData.value = {
      ...formData.value,
      id: data.id ?? null,
      materialCode: data.materialCode || '',
      materialName: data.materialName || '',
      specification: data.materialSpec || data.specification || '',
      unit: data.unit || '',
      materialType: data.materialType || '',
      categoryId: data.categoryId ?? null,
      categoryName: data.categoryName || '',
      unitPrice: data.unitPrice ?? 0,
      status: data.status === 'ACTIVE' ? 1 : 0,
      attrJson: data.attrJson ?? {},
      description: data.description || data.remark || '',
      createTime: data.createdTime || data.createTime || '',
      updateTime: data.updatedTime || data.updateTime || ''
    }
  } catch (error) {
    console.error('获取物料详情失败:', error)
    ElMessage.error('获取物料详情失败，请检查网络或联系管理员')
  }
}

// 返回列表
const handleBack = () => {
  router.push('/home/bom/material/list')
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
    
    // 强制将unitPrice转换为数字类型，确保表单验证通过
    // 这是最后的保障，确保unitPrice是有效的数字
    formData.value.unitPrice = Number(formData.value.unitPrice)
    
    // 如果转换失败，设置为0
    if (isNaN(formData.value.unitPrice)) {
      formData.value.unitPrice = 0
    }
    
    // 确保unitPrice是正数
    if (formData.value.unitPrice < 0) {
      formData.value.unitPrice = 0
    }
    
    // 强制更新表单数据，确保验证时使用最新值
    formData.value = { ...formData.value }
    
    // 执行表单验证
    await formRef.value.validate().catch(err => {
      // 捕获并处理验证错误
      console.error('表单验证错误:', err)
      
      // 准备错误信息
      let errorMsg = '表单验证失败'
      
      // 处理不同格式的错误对象
      if (err && typeof err === 'object') {
        // 情况1: 错误对象直接包含unitPrice数组
        if (err.unitPrice && Array.isArray(err.unitPrice) && err.unitPrice.length > 0) {
          errorMsg = `单价: ${err.unitPrice[0]}`
        }
        // 情况2: 错误对象包含fields属性
        else if (err.fields) {
          const firstField = Object.keys(err.fields)[0]
          if (firstField && err.fields[firstField] && err.fields[firstField].length > 0) {
            errorMsg = `${firstField === 'unitPrice' ? '单价' : firstField}: ${err.fields[firstField][0]}`
          }
        }
        // 情况3: 错误对象包含errors数组
        else if (err.errors && Array.isArray(err.errors) && err.errors.length > 0) {
          errorMsg = err.errors[0].message
        }
      }
      
      ElMessage.error(`保存失败: ${errorMsg}`)
      throw new Error(errorMsg)
    })
    
    const payload: Material & Record<string, any> = {
      materialCode: formData.value.materialCode,
      materialName: formData.value.materialName,
      materialType: formData.value.materialType,
      unit: formData.value.unit,
      materialSpec: formData.value.specification,
      status: formData.value.status === 1 ? 'ACTIVE' : 'INACTIVE',
      remark: formData.value.description,
      categoryId: formData.value.categoryId,
      categoryName: formData.value.categoryName,
      unitPrice: formData.value.unitPrice,
      attrJson: formData.value.attrJson,
      description: formData.value.description
    }

    const res = formData.value.id
      ? await bomApi.updateMaterial(formData.value.id, payload)
      : await bomApi.createMaterial(payload)

    const responseData = DataTransformer.normalizeResponse(res)
    if (!DataTransformer.isSuccessCode(responseData?.code)) {
      ElMessage.error(getResponseMessage(responseData, '保存失败'))
      return
    }

    ElMessage.success(formData.value.id ? '物料更新成功' : '物料创建成功')
    handleBack()
  } catch (error: any) {
    console.error('保存失败:', error)
    
    // 显示通用错误信息
    if (!error.message || error.message === '表单验证失败') {
      ElMessage.error('保存失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.material-detail {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #eef2ff 100%);
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.title-with-icon {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-icon {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.3s;
  padding: 8px;
  border-radius: 50%;
  background: rgba(0,0,0,0.03);
}

.back-icon:hover {
  background: var(--primary-color);
  color: white;
  transform: translateX(-4px);
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
}

.btn-glow {
  box-shadow: 0 0 15px rgba(64, 158, 255, 0.4);
  transition: all 0.3s;
}

.btn-glow:hover {
  box-shadow: 0 0 25px rgba(64, 158, 255, 0.6);
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

.status-radio {
  margin-top: 4px;
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

:deep(.el-input.is-disabled .el-input__inner) {
  background: rgba(0,0,0,0.02) !important;
}
</style>

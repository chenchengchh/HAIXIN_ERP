<template>
  <div class="bom-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>BOM查询</span>
        </div>
      </template>

      <el-form :inline="true" class="query-form">
        <el-form-item label="物料ID">
          <el-input-number v-model="materialId" :min="1" />
        </el-form-item>
        <el-form-item label="产品ID">
          <el-input-number v-model="productId" :min="1" />
        </el-form-item>
        <el-form-item label="版本">
          <el-input v-model="version" style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :loading="loading">查询</el-button>
          <el-button @click="handleClear">清空</el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <el-empty v-if="!loading && !result" description="请输入物料ID或产品ID进行查询" />

      <el-scrollbar v-else class="result-scroll">
        <pre class="result-pre">{{ pretty }}</pre>
      </el-scrollbar>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'

const materialId = ref<number | null>(null)
const productId = ref<number | null>(null)
const version = ref('')
const loading = ref(false)
const result = ref<any>(null)

const pretty = computed(() => {
  if (!result.value) return ''
  try {
    return JSON.stringify(result.value, null, 2)
  } catch {
    return String(result.value)
  }
})

const handleQuery = async () => {
  try {
    loading.value = true
    const params: any = {}
    if (materialId.value) params.materialId = materialId.value
    if (productId.value) params.productId = productId.value
    if (version.value) params.version = version.value
    const res = await erpApi.basicData.getBOMs(params)
    result.value = (res as any)?.data
  } catch (e) {
    ErrorHandler.handleApiError(e)
    result.value = null
  } finally {
    loading.value = false
  }
}

const handleClear = () => {
  materialId.value = null
  productId.value = null
  version.value = ''
  result.value = null
}
</script>

<style scoped lang="scss">
.bom-view {
  padding: 20px;
}

.query-form {
  margin-bottom: 10px;
}

.result-scroll {
  max-height: 520px;
}

.result-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>


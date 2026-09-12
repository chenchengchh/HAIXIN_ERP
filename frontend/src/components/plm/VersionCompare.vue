<template>
  <div class="version-compare">
    <div class="compare-header">
      <h3>版本对比</h3>
      <div class="version-selectors">
        <div class="version-selector">
          <label>源版本：</label>
          <el-select
            v-model="sourceVersion"
            placeholder="选择源版本"
            @change="compareVersions"
          >
            <el-option
              v-for="version in versions"
              :key="version.id"
              :label="version.name"
              :value="version"
            />
          </el-select>
        </div>
        <div class="compare-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="version-selector">
          <label>目标版本：</label>
          <el-select
            v-model="targetVersion"
            placeholder="选择目标版本"
            @change="compareVersions"
          >
            <el-option
              v-for="version in versions"
              :key="version.id"
              :label="version.name"
              :value="version"
            />
          </el-select>
        </div>
      </div>
    </div>

    <div class="compare-content">
      <div v-if="!comparisonResult" class="empty-state">
        <el-empty description="请选择两个版本进行对比" />
      </div>
      <div v-else class="comparison-result">
        <div class="result-summary">
          <div class="summary-item">
            <el-statistic title="新增项" :value="comparisonResult.added" prefix="<el-icon><Plus /></el-icon>" />
          </div>
          <div class="summary-item">
            <el-statistic title="修改项" :value="comparisonResult.modified" prefix="<el-icon><Edit /></el-icon>" />
          </div>
          <div class="summary-item">
            <el-statistic title="删除项" :value="comparisonResult.deleted" prefix="<el-icon><Delete /></el-icon>" />
          </div>
        </div>

        <div class="comparison-details">
          <!-- 新增项 -->
          <div class="detail-section" v-if="comparisonResult.added > 0">
            <h4>
              <el-icon><Plus /></el-icon> 新增项 ({{ comparisonResult.added }})
            </h4>
            <el-table
              :data="comparisonResult.addedItems"
              style="width: 100%"
              class="comparison-table added-table"
            >
              <el-table-column prop="code" label="编码" width="120" />
              <el-table-column prop="name" label="名称" min-width="200" />
              <el-table-column prop="spec" label="规格" min-width="150" />
              <el-table-column prop="unit" label="单位" width="80" />
              <el-table-column prop="qty" label="数量" width="80" />
            </el-table>
          </div>

          <!-- 修改项 -->
          <div class="detail-section" v-if="comparisonResult.modified > 0">
            <h4>
              <el-icon><Edit /></el-icon> 修改项 ({{ comparisonResult.modified }})
            </h4>
            <el-table
              :data="comparisonResult.modifiedItems"
              style="width: 100%"
              class="comparison-table modified-table"
            >
              <el-table-column prop="code" label="编码" width="120" />
              <el-table-column prop="name" label="名称" min-width="200" />
              <el-table-column label="规格" min-width="250">
                <template #default="scope">
                  <div class="compare-field">
                    <div class="source-value">{{ scope.row.source.spec }}</div>
                    <div class="target-value">{{ scope.row.target.spec }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="数量" width="250">
                <template #default="scope">
                  <div class="compare-field">
                    <div class="source-value">{{ scope.row.source.qty }}</div>
                    <div class="target-value">{{ scope.row.target.qty }}</div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 删除项 -->
          <div class="detail-section" v-if="comparisonResult.deleted > 0">
            <h4>
              <el-icon><Delete /></el-icon> 删除项 ({{ comparisonResult.deleted }})
            </h4>
            <el-table
              :data="comparisonResult.deletedItems"
              style="width: 100%"
              class="comparison-table deleted-table"
            >
              <el-table-column prop="code" label="编码" width="120" />
              <el-table-column prop="name" label="名称" min-width="200" />
              <el-table-column prop="spec" label="规格" min-width="150" />
              <el-table-column prop="unit" label="单位" width="80" />
              <el-table-column prop="qty" label="数量" width="80" />
            </el-table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ArrowRight, Plus, Edit, Delete } from '@element-plus/icons-vue'

// 定义版本数据结构
interface Version {
  id: string
  name: string
  content: any
}

// 定义组件属性
const props = defineProps<{
  versions: Version[]
}>()

// 定义组件事件
const emit = defineEmits<{
  (e: 'compare', source: Version, target: Version): void
}>()

// 源版本和目标版本
const sourceVersion = ref<Version | null>(null)
const targetVersion = ref<Version | null>(null)

// 对比结果
const comparisonResult = ref<any>(null)

// 比较版本
const compareVersions = () => {
  if (!sourceVersion.value || !targetVersion.value) {
    return
  }

  // 触发对比事件，由父组件处理对比逻辑
  emit('compare', sourceVersion.value, targetVersion.value)

  // 这里可以添加默认的对比逻辑，或者直接使用父组件传递的对比结果
  // 示例：模拟对比结果
  const mockResult = {
    added: 2,
    modified: 3,
    deleted: 1,
    addedItems: [
      { id: '1', code: 'MAT-009', name: '新物料1', spec: '规格1', unit: '个', qty: 1 },
      { id: '2', code: 'MAT-010', name: '新物料2', spec: '规格2', unit: '个', qty: 2 }
    ],
    modifiedItems: [
      {
        id: '3',
        code: 'MAT-001',
        name: '锂电池电芯',
        source: { spec: '3.7V/2000mAh', qty: 1 },
        target: { spec: '3.7V/2500mAh', qty: 1 }
      },
      {
        id: '4',
        code: 'MAT-002',
        name: 'PCB主板',
        source: { spec: '100mm×150mm', qty: 1 },
        target: { spec: '110mm×160mm', qty: 1 }
      },
      {
        id: '5',
        code: 'MAT-003',
        name: '塑料外壳',
        source: { spec: 'ABS材质', qty: 1 },
        target: { spec: 'PC材质', qty: 1 }
      }
    ],
    deletedItems: [
      { id: '6', code: 'MAT-008', name: '天线', spec: '2.4GHz', unit: '个', qty: 1 }
    ]
  }

  comparisonResult.value = mockResult
}
</script>

<style scoped>
.version-compare {
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  background-color: white;
  padding: 16px;
}

.compare-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.compare-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.version-selectors {
  display: flex;
  align-items: center;
  gap: 16px;
}

.version-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.version-selector label {
  font-weight: 500;
  color: #666;
}

.compare-arrow {
  display: flex;
  align-items: center;
  color: #999;
}

.compare-content {
  min-height: 400px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
  background-color: #f9fafb;
  border-radius: 4px;
}

.comparison-result {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-summary {
  display: flex;
  gap: 30px;
  padding: 20px;
  background-color: #f9fafb;
  border-radius: 4px;
}

.summary-item {
  flex: 1;
  text-align: center;
}

.comparison-details {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-section {
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.detail-section h4 {
  margin: 0;
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e5e7eb;
  font-size: 16px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.comparison-table {
  width: 100%;
}

.comparison-table.added-table .el-table__row {
  background-color: #f0f9eb;
}

.comparison-table.modified-table .el-table__row {
  background-color: #fef6e4;
}

.comparison-table.deleted-table .el-table__row {
  background-color: #fef2f2;
  text-decoration: line-through;
}

.compare-field {
  display: flex;
  gap: 16px;
  align-items: center;
}

.source-value {
  color: #999;
  text-decoration: line-through;
}

.target-value {
  color: #333;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .compare-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .version-selectors {
    width: 100%;
    flex-wrap: wrap;
  }

  .version-selector {
    flex: 1;
    min-width: 200px;
  }

  .result-summary {
    flex-wrap: wrap;
    gap: 20px;
  }

  .summary-item {
    min-width: 150px;
  }
}

@media (max-width: 768px) {
  .version-selectors {
    flex-direction: column;
    align-items: flex-start;
  }

  .version-selector {
    width: 100%;
    min-width: auto;
  }

  .compare-arrow {
    transform: rotate(90deg);
  }
}
</style>
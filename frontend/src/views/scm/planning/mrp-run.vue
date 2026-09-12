<template>
  <div class="mrp-run-container" :class="{ embedded }">
    <!-- 页面标题 -->
    <div v-if="!embedded" class="page-header">
      <h2>MRP运算配置</h2>
      <p>配置MRP运算参数，包括需求来源、考虑因素和运算范围</p>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- MRP运算配置卡片 -->
      <el-card shadow="hover" class="config-card">
        <template #header>
          <div class="card-header">
            <span>运算参数配置</span>
          </div>
        </template>

        <el-form :model="mrpConfig" label-position="top" class="mrp-config-form">
          <!-- 基础信息 -->
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="运算名称" required>
                <el-input v-model="mrpConfig.runName" placeholder="请输入运算名称" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="运算日期" required>
                <el-date-picker
                  v-model="mrpConfig.runDate"
                  type="date"
                  placeholder="选择运算日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="需求来源" required>
                <el-select v-model="mrpConfig.demandSource" placeholder="选择需求来源" style="width: 100%">
                  <el-option label="预测需求" value="FORECAST" />
                  <el-option label="销售订单" value="ORDER" />
                  <el-option label="两者结合" value="BOTH" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 考虑因素 -->
          <el-form-item label="考虑因素">
            <el-checkbox-group v-model="mrpConfig.considerFactors">
              <el-checkbox label="安全库存" value="安全库存" />
              <el-checkbox label="在途库存" value="在途库存" />
              <el-checkbox label="预留库存" value="预留库存" />
              <el-checkbox label="在制品" value="在制品" />
              <el-checkbox label="最小订货量" value="最小订货量" />
              <el-checkbox label="经济订货批量" value="经济订货批量" />
              <el-checkbox label="供应商交货周期" value="供应商交货周期" />
              <el-checkbox label="生产周期" value="生产周期" />
            </el-checkbox-group>
          </el-form-item>

          <!-- 运算范围 -->
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="工厂范围">
                <el-select v-model="mrpConfig.factoryScope" placeholder="选择工厂" multiple style="width: 100%">
                  <el-option label="工厂A" value="factory_a" />
                  <el-option label="工厂B" value="factory_b" />
                  <el-option label="工厂C" value="factory_c" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="仓库范围">
                <el-select v-model="mrpConfig.warehouseScope" placeholder="选择仓库" multiple style="width: 100%">
                  <el-option label="主仓库" value="main_warehouse" />
                  <el-option label="区域仓库1" value="regional_warehouse_1" />
                  <el-option label="区域仓库2" value="regional_warehouse_2" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="物料范围">
                <el-select v-model="mrpConfig.materialScope" placeholder="选择物料类别" multiple style="width: 100%">
                  <el-option label="原材料" value="raw_material" />
                  <el-option label="半成品" value="semi_finished" />
                  <el-option label="成品" value="finished_product" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 高级选项 -->
          <el-collapse v-model="activeNames">
            <el-collapse-item title="高级选项" name="1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="计划展望期">
                    <el-input-number v-model="mrpConfig.planHorizon" :min="1" :max="12" placeholder="输入计划展望期（月）" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="时区设置">
                    <el-select v-model="mrpConfig.timeZone" placeholder="选择时区" style="width: 100%">
                      <el-option label="按天" value="day" />
                      <el-option label="按周" value="week" />
                      <el-option label="按月" value="month" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-collapse-item>
          </el-collapse>

          <!-- 操作按钮 -->
          <div class="form-actions">
            <el-button type="primary" @click="runMrp" :loading="running">运行MRP</el-button>
            <el-button @click="saveConfig">保存配置</el-button>
            <el-button @click="resetForm">清空</el-button>
          </div>
        </el-form>
      </el-card>

      <!-- 运算历史记录卡片 -->
      <el-card shadow="hover" class="history-card">
        <template #header>
          <div class="card-header">
            <span>运算历史记录</span>
          </div>
        </template>

        <el-table :data="mrpHistory" style="width: 100%">
          <el-table-column prop="runId" label="运算ID" width="120" />
          <el-table-column prop="runName" label="运算名称" />
          <el-table-column prop="runDate" label="运算日期" width="150" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getTagType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="180" />
          <el-table-column prop="endTime" label="结束时间" width="180" />
          <el-table-column prop="duration" label="耗时" width="100" />
          <el-table-column prop="resultCount" label="生成建议数" width="120" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="viewResult(scope.row)">查看结果</el-button>
              <el-button size="small" @click="deleteHistory(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalHistory"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import { forecastApi } from '@/api/scm';
import { extractArray, extractTotal } from '../scm-utils';

const props = withDefaults(defineProps<{ embedded?: boolean }>(), {
  embedded: false
});

const embedded = computed(() => props.embedded);

const emit = defineEmits<{
  (e: 'view-result', planId: number): void
}>();

// 路由实例
const router = useRouter();

// MRP配置数据
const mrpConfig = reactive({
  runName: 'MRP-' + new Date().toISOString().slice(0, 10),
  runDate: new Date(),
  demandSource: 'BOTH',
  considerFactors: ['安全库存', '在途库存', '预留库存'],
  factoryScope: [],
  warehouseScope: [],
  materialScope: [],
  planHorizon: 3,
  timeZone: 'week'
});

// 折叠面板状态
const activeNames = ref(['1']);

// 运行状态
const running = ref(false);

// 运算历史记录
const mrpHistory = ref<any[]>([]);

// 分页数据
const currentPage = ref(1);
const pageSize = ref(10);
const totalHistory = ref(0);

// 页面加载时获取历史
onMounted(() => {
  fetchMrpHistory();
});

// 获取历史记录
const fetchMrpHistory = async () => {
  try {
    const res = await forecastApi.getMrpHistory({
      page: currentPage.value,
      size: pageSize.value
    });
    mrpHistory.value = extractArray(res);
    totalHistory.value = extractTotal(res);
  } catch (error: any) {
    // 请求层已统一处理401跳转登录与错误提示，此处仅记录警告，避免控制台误报错误
    console.warn('获取MRP历史失败:', error?.message || error);
  }
};

// 根据状态获取标签类型
const getTagType = (status: string): string => {
  switch (status) {
    case 'COMPLETED':
      return 'success';
    case 'RUNNING':
      return 'warning';
    case 'FAILED':
      return 'danger';
    default:
      return 'info';
  }
};

// 运行MRP
const runMrp = async () => {
  // 表单验证
  if (!mrpConfig.runName) {
    ElMessage.warning('请输入运算名称');
    return;
  }

  running.value = true;
  ElMessage.info('MRP运算已开始，请耐心等待...');

  try {
    const res = await forecastApi.runMrp(mrpConfig);
    ElMessage.success('MRP运算完成！');
    
    // 刷新历史
    fetchMrpHistory();
    
    const planId = res?.data?.id;
    if (embedded.value) {
      if (typeof planId === 'number') emit('view-result', planId);
    } else {
      router.push({
        name: 'scm-planning-plan-result',
        query: { planId }
      });
    }
  } catch (error) {
    console.error('MRP运算失败:', error);
    ElMessage.error('MRP运算失败');
  } finally {
    running.value = false;
  }
};

// 保存配置
const saveConfig = () => {
  ElMessage.success('配置已保存！');
};

// 重置表单
const resetForm = () => {
  Object.assign(mrpConfig, {
    runName: 'MRP-' + new Date().toISOString().slice(0, 10),
    runDate: new Date(),
    demandSource: 'BOTH',
    considerFactors: ['安全库存', '在途库存', '预留库存'],
    factoryScope: [],
    warehouseScope: [],
    materialScope: [],
    planHorizon: 3,
    timeZone: 'week'
  });
  ElMessage.info('表单已重置');
};

// 查看结果
const viewResult = (row: any) => {
  const planId = row?.id;
  if (embedded.value) {
    if (typeof planId === 'number') emit('view-result', planId);
  } else {
    router.push({
      name: 'scm-planning-plan-result',
      query: { planId }
    });
  }
};

// 下达计划
const handleRelease = async (row: any) => {
  try {
    await forecastApi.releasePlan(row.id);
    ElMessage.success('计划已下达，已生成采购申请');
  } catch (error) {
    console.error('下达失败:', error);
    ElMessage.error('下达失败');
  }
};

// 删除历史记录
const deleteHistory = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除MRP计划"${row?.planName || row?.id || ''}"？删除后关联的运算结果也会被清除，且不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
    await forecastApi.deleteMrpPlan(Number(row.id))
    ElMessage.success('删除成功')
    fetchMrpHistory()
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error('删除失败:', e)
      ElMessage.error(e?.message || '删除失败')
    }
  }
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchMrpHistory();
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  fetchMrpHistory();
};
</script>

<style scoped>
.mrp-run-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.mrp-run-container.embedded {
  padding: 0;
  background-color: transparent;
  min-height: auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.page-header p {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.config-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mrp-config-form {
  width: 100%;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  justify-content: center;
}

.history-card {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .mrp-run-container {
    padding: 10px;
  }
  
  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>

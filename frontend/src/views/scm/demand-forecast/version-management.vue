<template>
  <div class="version-management-component">
    <h3>版本管理</h3>
    <p>管理不同版本的预测结果，支持版本比较、回滚和发布操作</p>
    
    <!-- 版本列表卡片 -->
    <el-card shadow="hover" class="version-list-card">
      <template #header>
        <div class="card-header">
          <span>预测版本列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="createVersion">
              <el-icon><Plus /></el-icon> 创建新版本
            </el-button>
            <el-button type="warning" @click="compareVersions" :disabled="selectedVersions.length !== 2">
              版本比较
            </el-button>
          </div>
        </div>
      </template>
      <div class="card-content">
        <el-table
          v-loading="loading"
          :data="versionList"
          style="width: 100%"
          @selection-change="handleSelectionChange"
          :default-sort="{ prop: 'createTime', order: 'descending' }"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="versionId" label="版本号" width="150" />
          <el-table-column prop="versionName" label="版本名称" width="200" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column prop="creator" label="创建人" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="forecastPeriod" label="预测周期" width="120" />
          <el-table-column prop="forecastStartDate" label="预测开始日期" width="180" />
          <el-table-column prop="forecastEndDate" label="预测结束日期" width="180" />
          <el-table-column prop="description" label="版本描述" min-width="200" show-overflow-tooltip />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="viewDetails(scope.row)">
                <el-icon><View /></el-icon> 详情
              </el-button>
              <el-button size="small" type="success" @click="publishVersion(scope.row)" v-if="scope.row.status === '草稿'">
                <el-icon><Upload /></el-icon> 发布
              </el-button>
              <el-button size="small" type="warning" @click="rollbackVersion(scope.row)" v-if="scope.row.status !== '当前版本'">
                <el-icon><Refresh /></el-icon> 回滚
              </el-button>
              <el-button size="small" type="danger" @click="deleteVersion(scope.row)" v-if="scope.row.status !== '当前版本'">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
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
            :total="versionList.length"
          />
        </div>
      </div>
    </el-card>
    
    <!-- 版本详情对话框 -->
    <el-dialog v-model="detailVisible" title="版本详情" width="80%" center>
      <div class="version-detail" v-if="selectedVersion">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="版本号" span="1">{{ selectedVersion.versionId }}</el-descriptions-item>
          <el-descriptions-item label="版本名称" span="1">{{ selectedVersion.versionName }}</el-descriptions-item>
          <el-descriptions-item label="状态" span="1">
            <el-tag :type="getStatusTagType(selectedVersion.status)">{{ selectedVersion.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ selectedVersion.createTime }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ selectedVersion.creator }}</el-descriptions-item>
          <el-descriptions-item label="预测周期">{{ selectedVersion.forecastPeriod }}</el-descriptions-item>
          <el-descriptions-item label="预测开始日期">{{ selectedVersion.forecastStartDate }}</el-descriptions-item>
          <el-descriptions-item label="预测结束日期">{{ selectedVersion.forecastEndDate }}</el-descriptions-item>
          <el-descriptions-item label="预测产品数量">{{ selectedVersion.productCount }}个</el-descriptions-item>
          <el-descriptions-item label="总预测数量">{{ selectedVersion.totalForecast }}</el-descriptions-item>
          <el-descriptions-item label="平均准确率">{{ selectedVersion.averageAccuracy }}%</el-descriptions-item>
          <el-descriptions-item label="版本描述">{{ selectedVersion.description }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-actions">
          <el-button type="primary" @click="publishVersion(selectedVersion)" v-if="selectedVersion.status === '草稿'">
            <el-icon><Upload /></el-icon> 发布版本
          </el-button>
          <el-button type="warning" @click="rollbackVersion(selectedVersion)" v-if="selectedVersion.status !== '当前版本'">
            <el-icon><Refresh /></el-icon> 回滚到此版本
          </el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 版本比较对话框 -->
    <el-dialog v-model="compareVisible" title="版本比较" width="90%" center>
      <div class="version-compare" v-if="compareVersionsData.length > 0">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="version-info">
              <h4>{{ compareVersionsData[0].versionName }} ({{ compareVersionsData[0].versionId }})</h4>
              <div class="version-meta">
                <span class="meta-item">创建时间：{{ compareVersionsData[0].createTime }}</span>
                <span class="meta-item">状态：<el-tag :type="getStatusTagType(compareVersionsData[0].status)">{{ compareVersionsData[0].status }}</el-tag></span>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="version-info">
              <h4>{{ compareVersionsData[1].versionName }} ({{ compareVersionsData[1].versionId }})</h4>
              <div class="version-meta">
                <span class="meta-item">创建时间：{{ compareVersionsData[1].createTime }}</span>
                <span class="meta-item">状态：<el-tag :type="getStatusTagType(compareVersionsData[1].status)">{{ compareVersionsData[1].status }}</el-tag></span>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <div class="compare-stats">
          <h4>版本差异统计</h4>
          <el-row :gutter="30">
            <el-col :span="8">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value">{{ versionDiff.totalForecastDiff }}</div>
                <div class="stat-label">总预测数量差异</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value">{{ versionDiff.productCountDiff }}</div>
                <div class="stat-label">产品数量差异</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value">{{ versionDiff.accuracyDiff }}%</div>
                <div class="stat-label">准确率差异</div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <div class="compare-details">
          <h4>详细差异</h4>
          <el-table :data="versionDiff.details" border style="width: 100%">
            <el-table-column prop="productName" label="产品名称" width="200" />
            <el-table-column prop="version1Forecast" label="版本1预测" width="150" align="right" />
            <el-table-column prop="version2Forecast" label="版本2预测" width="150" align="right" />
            <el-table-column prop="diffValue" label="差异值" width="150" align="right">
              <template #default="scope">
                <span :class="scope.row.diffValue > 0 ? 'diff-positive' : scope.row.diffValue < 0 ? 'diff-negative' : ''">
                  {{ scope.row.diffValue }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="diffPercentage" label="差异百分比" width="150" align="right">
              <template #default="scope">
                <span :class="scope.row.diffPercentage > 0 ? 'diff-positive' : scope.row.diffPercentage < 0 ? 'diff-negative' : ''">
                  {{ scope.row.diffPercentage }}%
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
    
    <!-- 创建新版本对话框 -->
    <el-dialog v-model="createVersionVisible" title="创建新版本" width="60%" center>
      <div class="create-version-form">
        <el-form :model="newVersionForm" label-position="top" label-width="120px" class="version-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="版本名称" prop="versionName">
                <el-input v-model="newVersionForm.versionName" placeholder="请输入版本名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预测周期" prop="forecastPeriod">
                <el-select v-model="newVersionForm.forecastPeriod" placeholder="选择预测周期">
                  <el-option label="月度" value="月度" />
                  <el-option label="季度" value="季度" />
                  <el-option label="年度" value="年度" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="预测开始日期" prop="forecastStartDate">
                <el-date-picker
                  v-model="newVersionForm.forecastStartDate"
                  type="date"
                  placeholder="选择开始日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预测结束日期" prop="forecastEndDate">
                <el-date-picker
                  v-model="newVersionForm.forecastEndDate"
                  type="date"
                  placeholder="选择结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="版本描述" prop="description">
                <el-input
                  v-model="newVersionForm.description"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入版本描述"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="创建来源">
                <el-radio-group v-model="newVersionForm.createSource">
                  <el-radio value="基于当前版本">基于当前版本</el-radio>
                  <el-radio value="基于历史数据">基于历史数据</el-radio>
                  <el-radio value="空白版本">空白版本</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createVersionVisible = false">取消</el-button>
          <el-button type="primary" @click="saveNewVersion">创建</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, View, Upload, Refresh, Delete } from '@element-plus/icons-vue';
import { forecastApi } from '@/api/scm';

// 加载状态
const loading = ref(false);

// 分页数据
const currentPage = ref(1);
const pageSize = ref(10);

// 选择的版本
const selectedVersion = ref<any>(null);
const detailVisible = ref(false);
const selectedVersions = ref<any[]>([]);

// 版本比较相关
const compareVisible = ref(false);
const compareVersionsData = ref<any[]>([]);

// 定义版本差异详情类型
interface VersionDiffDetail {
  productName: string;
  version1Forecast: number;
  version2Forecast: number;
  diffValue: number;
  diffPercentage: number;
}

// 定义版本差异类型
interface VersionDiff {
  totalForecastDiff: number;
  productCountDiff: number;
  accuracyDiff: number;
  details: VersionDiffDetail[];
}

const versionDiff = ref<VersionDiff>({
  totalForecastDiff: 0,
  productCountDiff: 0,
  accuracyDiff: 0,
  details: []
});

// 创建新版本相关
const createVersionVisible = ref(false);
const newVersionForm = reactive({
  versionName: '',
  forecastPeriod: '月度',
  forecastStartDate: '',
  forecastEndDate: '',
  description: '',
  createSource: '基于当前版本'
});

const versionList = ref<any[]>([]);

const toPeriod = (dateStr: string) => {
  const d = new Date(dateStr);
  if (Number.isNaN(d.getTime())) return '';
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
};

const periodToStartEnd = (period: string) => {
  const [y, m] = period.split('-').map(v => Number(v));
  if (!y || !m) return { start: '', end: '' };
  const start = `${y}-${String(m).padStart(2, '0')}-01`;
  const endDate = new Date(y, m, 0);
  const end = `${endDate.getFullYear()}-${String(endDate.getMonth() + 1).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')}`;
  return { start, end };
};

const mapStatusLabel = (status: string) => {
  if (status === 'PUBLISHED') return '当前版本';
  if (status === 'DRAFT') return '草稿';
  if (status === 'ARCHIVED') return '已发布';
  return status || '未知';
};

const loadVersions = async (period?: string) => {
  const targetPeriod = period || `${new Date().getFullYear()}-${String(new Date().getMonth() + 1).padStart(2, '0')}`;
  loading.value = true;
  try {
    const versionsRes: any = await forecastApi.getForecastVersions(targetPeriod);
    const versions = versionsRes?.data?.list || versionsRes?.data?.records || [];
    const rows: any[] = [];
    for (const v of (Array.isArray(versions) ? versions : [])) {
      const p = v.period || targetPeriod;
      const se = periodToStartEnd(p);
      const statusLabel = mapStatusLabel(v.status);
      const versionId = `V${p.replace('-', '')}-${String(v.versionNo).padStart(3, '0')}`;
      const versionName = `${p}预测${statusLabel === '草稿' ? '（草稿）' : ''}`;

      let totalForecast = 0;
      let productCount = 0;
      let averageAccuracy = 0;
      try {
        const listRes: any = await forecastApi.getForecastList({ page: 1, size: 1000, period: p, versionId: v.id });
        const records = listRes?.data?.records || listRes?.data?.list || [];
        productCount = records.length;
        totalForecast = records.reduce((sum: number, item: any) => sum + Number(item.finalForecast || 0), 0);
        const accRes: any = await forecastApi.getForecastAccuracy(p, v.id);
        const acc = accRes?.data || {};
        if (acc?.mape !== null && acc?.mape !== undefined) {
          averageAccuracy = Math.max(0, Math.min(100, (1 - Number(acc.mape)) * 100));
        }
      } catch (e) {
        averageAccuracy = 0;
      }

      rows.push({
        id: v.id,
        period: p,
        versionNo: v.versionNo,
        statusCode: v.status,
        versionId,
        versionName,
        createTime: v.createdTime ? String(v.createdTime).replace('T', ' ').slice(0, 19) : '',
        creator: 'SYSTEM',
        status: statusLabel,
        forecastPeriod: '月度',
        forecastStartDate: se.start,
        forecastEndDate: se.end,
        productCount,
        totalForecast: Number(totalForecast.toFixed(2)),
        averageAccuracy: Number(averageAccuracy.toFixed(1)),
        description: v.status === 'DRAFT' ? '预测草稿版本' : '预测版本'
      });
    }
    versionList.value = rows;
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadVersions();
});

// 获取状态标签类型
const getStatusTagType = (status: string): string => {
  switch (status) {
    case '当前版本':
      return 'success';
    case '已发布':
      return 'info';
    case '草稿':
      return 'warning';
    case '已过期':
      return 'danger';
    default:
      return 'info';
  }
};

// 选择版本变化
const handleSelectionChange = (selection: any[]) => {
  selectedVersions.value = selection;
};

// 创建新版本
const createVersion = () => {
  // 重置表单
  Object.assign(newVersionForm, {
    versionName: '',
    forecastPeriod: '月度',
    forecastStartDate: new Date().toISOString().split('T')[0],
    forecastEndDate: '',
    description: '',
    createSource: '基于当前版本'
  });
  
  // 打开创建对话框
  createVersionVisible.value = true;
};

// 保存新版本
const saveNewVersion = async () => {
  const period = toPeriod(newVersionForm.forecastStartDate);
  if (!period) {
    ElMessage.warning('请选择预测开始日期');
    return;
  }
  loading.value = true;
  try {
    await forecastApi.createForecastVersion(period);
    ElMessage.success('新版本创建成功');
    createVersionVisible.value = false;
    await loadVersions(period);
  } catch (e) {
    ElMessage.error('新版本创建失败');
  } finally {
    loading.value = false;
  }
};

// 查看详情
const viewDetails = (row: any) => {
  selectedVersion.value = row;
  detailVisible.value = true;
};

// 发布版本
const publishVersion = (row: any) => {
  if (!row?.id) {
    ElMessage.error('版本ID缺失');
    return;
  }
  if (row.status === '已发布' || row.status === '当前版本') {
    ElMessage.warning('该版本已发布');
    return;
  }

  ElMessageBox.confirm('确定要发布此版本吗？', '发布确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    loading.value = true;
    try {
      await forecastApi.publishForecastVersion(row.id);
      ElMessage.success('版本发布成功');
      await loadVersions(row.period);
    } catch (e) {
      ElMessage.error('版本发布失败');
    } finally {
      loading.value = false;
    }
  }).catch(() => {
  });
};

// 回滚版本
const rollbackVersion = (row: any) => {
  if (!row?.id) {
    ElMessage.error('版本ID缺失');
    return;
  }
  if (row.status === '当前版本') {
    ElMessage.warning('该版本已是当前版本');
    return;
  }
  
  ElMessageBox.confirm('确定要回滚到此版本吗？', '回滚确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    loading.value = true;
    try {
      await forecastApi.rollbackForecastVersion(row.id);
      ElMessage.success('已基于该版本创建回滚草稿');
      await loadVersions(row.period);
    } catch (e) {
      ElMessage.error('回滚失败');
    } finally {
      loading.value = false;
    }
  }).catch(() => {
    // 取消回滚
  });
};

// 删除版本
const deleteVersion = (row: any) => {
  ElMessage.warning('当前不支持删除版本（后端未提供删除接口）');
};

// 版本比较
const compareVersions = () => {
  if (selectedVersions.value.length !== 2) {
    ElMessage.warning('请选择两个版本进行比较');
    return;
  }
  
  // 设置比较的版本数据
  compareVersionsData.value = [...selectedVersions.value];
  
  // 计算版本差异
  calculateVersionDiff();
  
  // 打开比较对话框
  compareVisible.value = true;
};

// 计算版本差异
const calculateVersionDiff = async () => {
  if (compareVersionsData.value.length !== 2) return;
  
  const v1 = compareVersionsData.value[0];
  const v2 = compareVersionsData.value[1];
  
  versionDiff.value.totalForecastDiff = v2.totalForecast - v1.totalForecast;
  versionDiff.value.productCountDiff = v2.productCount - v1.productCount;
  versionDiff.value.accuracyDiff = v2.averageAccuracy - v1.averageAccuracy;

  try {
    loading.value = true;
    const [l1, l2] = await Promise.all([
      forecastApi.getForecastList({ page: 1, size: 2000, period: v1.period, versionId: v1.id }),
      forecastApi.getForecastList({ page: 1, size: 2000, period: v2.period, versionId: v2.id })
    ]);
    const r1 = (l1?.data?.records || l1?.data?.list || []) as any[];
    const r2 = (l2?.data?.records || l2?.data?.list || []) as any[];
    const m1 = new Map<string, any>();
    const m2 = new Map<string, any>();
    r1.forEach(item => m1.set(String(item.productCode || item.materialCode || item.productName), item));
    r2.forEach(item => m2.set(String(item.productCode || item.materialCode || item.productName), item));

    const keys = Array.from(new Set([...m1.keys(), ...m2.keys()]));
    const details: any[] = [];
    for (const key of keys) {
      const a = m1.get(key);
      const b = m2.get(key);
      const v1Forecast = Number(a?.finalForecast || 0);
      const v2Forecast = Number(b?.finalForecast || 0);
      const diffValue = v2Forecast - v1Forecast;
      const denom = v1Forecast === 0 ? 0 : v1Forecast;
      const diffPercentage = denom === 0 ? 0 : (diffValue / denom) * 100;
      details.push({
        productName: b?.productName || a?.productName || key,
        version1Forecast: v1Forecast,
        version2Forecast: v2Forecast,
        diffValue: Number(diffValue.toFixed(2)),
        diffPercentage: Number(diffPercentage.toFixed(2))
      });
    }
    versionDiff.value.details = details.slice(0, 200);
  } catch (e) {
    versionDiff.value.details = [];
  } finally {
    loading.value = false;
  }
};

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
};
</script>

<style scoped>
.version-management-component {
  padding: 20px;
}

.version-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 版本详情 */
.version-detail {
  padding: 20px 0;
}

.detail-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

/* 版本比较 */
.version-compare {
  padding: 20px 0;
}

.version-info {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

.version-info h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
}

.version-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #606266;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.compare-stats {
  margin: 20px 0;
}

.compare-stats h4 {
  margin: 0 0 20px 0;
  font-size: 16px;
  color: #303133;
}

.stat-card {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.compare-details {
  margin: 20px 0;
}

.compare-details h4 {
  margin: 0 0 20px 0;
  font-size: 16px;
  color: #303133;
}

.diff-positive {
  color: #67c23a;
}

.diff-negative {
  color: #f56c6c;
}

/* 创建新版本表单 */
.create-version-form {
  padding: 10px 0;
}

.version-form {
  max-width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .version-management-component {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    flex-wrap: wrap;
    width: 100%;
  }
  
  .pagination-container {
    justify-content: center;
  }
  
  .detail-actions {
    flex-direction: column;
  }
  
  .version-info {
    margin-bottom: 10px;
  }
  
  .version-meta {
    flex-direction: column;
    gap: 10px;
  }
}

</style>

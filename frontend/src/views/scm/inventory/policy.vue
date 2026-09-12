<template>
  <div class="inventory-policy-container">
    <!-- 页面标题和面包屑导航 -->
    <div class="page-header">
      <h2>库存策略配置</h2>
      <el-breadcrumb aria-label="Breadcrumb" role="navigation">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/scm' }">SCM系统</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/scm/inventory-optimization' }">库存优化</el-breadcrumb-item>
        <el-breadcrumb-item>库存策略配置</el-breadcrumb-item>
      </el-breadcrumb>
      <p>配置物料的库存策略，包括安全库存、再订货点、经济订货批量等参数</p>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 筛选条件卡片 -->
      <el-card shadow="hover" class="filter-card">
        <template #header>
          <div class="card-header">
            <span>筛选条件</span>
          </div>
        </template>

        <el-form :model="filterForm" label-position="left" label-width="100px" class="filter-form">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="物料编码">
                <el-input v-model="filterForm.materialCode" placeholder="输入物料编码" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="物料名称">
                <el-input v-model="filterForm.materialName" placeholder="输入物料名称" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="仓库">
                <el-select v-model="filterForm.warehouse" placeholder="选择仓库" style="width: 100%">
                  <el-option label="全部" value="" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="ABC分类">
                <el-select v-model="filterForm.abcClass" placeholder="选择ABC分类" style="width: 100%">
                  <el-option label="全部" value="" />
                  <el-option label="A类" value="A" />
                  <el-option label="B类" value="B" />
                  <el-option label="C类" value="C" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12" class="filter-actions">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="resetFilter">重置</el-button>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 操作按钮卡片 -->
      <el-card shadow="hover" class="action-card">
        <div class="action-buttons">
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon> 新增策略
          </el-button>
          <el-button type="success" @click="batchEdit">
            <el-icon><Edit /></el-icon> 批量编辑
          </el-button>
          <el-button type="warning" @click="batchDelete">
            <el-icon><Delete /></el-icon> 批量删除
          </el-button>
          <el-upload
            class="upload-btn"
            action=""
            :auto-upload="false"
            :on-change="handleImportChange"
            :show-file-list="false"
            accept=".xlsx,.xls"
          >
            <el-button>
              <el-icon><Upload /></el-icon> 批量导入
            </el-button>
          </el-upload>
          <el-button @click="exportPolicies">
            <el-icon><Download /></el-icon> 导出策略
          </el-button>
          <el-button @click="autoCalculate">
            <el-icon><Setting /></el-icon> 自动计算
          </el-button>
          <el-button @click="refreshPolicies">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </el-card>

      <!-- 策略表格卡片 -->
      <el-card shadow="hover" class="policy-card">
        <template #header>
          <div class="card-header">
            <span>库存策略列表</span>
            <span class="policy-count">共 {{ filteredPolicies.length }} 条记录</span>
          </div>
        </template>

        <el-table
          :data="paginatedPolicies"
          style="width: 100%"
          @selection-change="handleSelectionChange"
          @row-click="handleRowClick"
          row-key="id"
          v-loading="loading"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="策略ID" width="120" />
          <el-table-column prop="materialCode" label="物料编码" width="150" />
          <el-table-column prop="materialName" label="物料名称" min-width="200" />
          <el-table-column prop="warehouseName" label="仓库" width="120" />
          <el-table-column prop="abcClass" label="ABC分类" width="100">
            <template #default="scope">
              <el-tag :type="getAbcTagType(scope.row.abcClass)">
                {{ scope.row.abcClass }}类
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="safetyStock" label="安全库存" width="120" align="right" />
          <el-table-column prop="rop" label="再订货点" width="120" align="right" />
          <el-table-column prop="eoq" label="经济订货批量" width="150" align="right" />
          <el-table-column prop="maxStock" label="最大库存" width="120" align="right" />
          <el-table-column prop="serviceLevel" label="服务水平" width="120" align="right">
            <template #default="scope">
              {{ (scope.row.serviceLevel * 100).toFixed(0) }}%
            </template>
          </el-table-column>
          <el-table-column prop="creator" label="创建人" width="100" />
          <el-table-column prop="updateTime" label="更新时间" width="180" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="editPolicy(scope.row)">
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="deletePolicy(scope.row)">
                删除
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
            :total="total"
          />
        </div>
      </el-card>
    </div>

    <!-- 策略编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="60%" center>
      <el-form
        ref="policyFormRef"
        :model="currentPolicy"
        :rules="policyRules"
        label-position="top"
        label-width="120px"
        class="policy-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物料编码" prop="materialCode" required>
              <el-select v-model="currentPolicy.materialCode" placeholder="选择物料编码" style="width: 100%">
                <el-option
                  v-for="material in materials"
                  :key="material.code"
                  :label="`${material.code} - ${material.name}`"
                  :value="material.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="currentPolicy.warehouseId" placeholder="选择仓库" style="width: 100%">
                <el-option
                  v-for="warehouse in warehouses"
                  :key="warehouse.id"
                  :label="warehouse.name"
                  :value="warehouse.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="ABC分类" prop="abcClass" required>
              <el-select v-model="currentPolicy.abcClass" placeholder="选择ABC分类" style="width: 100%">
                <el-option label="A类" value="A" />
                <el-option label="B类" value="B" />
                <el-option label="C类" value="C" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="安全库存" prop="safetyStock" required>
              <el-input-number
                v-model="currentPolicy.safetyStock"
                :min="0"
                :precision="2"
                placeholder="输入安全库存"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="再订货点" prop="rop" required>
              <el-input-number
                v-model="currentPolicy.rop"
                :min="0"
                :precision="2"
                placeholder="输入再订货点"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="经济订货批量" prop="eoq" required>
              <el-input-number
                v-model="currentPolicy.eoq"
                :min="0"
                :precision="2"
                placeholder="输入经济订货批量"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大库存" prop="maxStock" required>
              <el-input-number
                v-model="currentPolicy.maxStock"
                :min="0"
                :precision="2"
                placeholder="输入最大库存"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="服务水平" prop="serviceLevel" required>
              <el-slider
                v-model="currentPolicy.serviceLevel"
                :min="0.8"
                :max="0.99"
                :step="0.01"
                :marks="{ 0.8: '80%', 0.9: '90%', 0.99: '99%' }"
              />
              <div class="service-level-value">{{ (currentPolicy.serviceLevel * 100).toFixed(0) }}%</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePolicy">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Upload, Download, Refresh, Setting } from '@element-plus/icons-vue';
import { unwrapListResponse } from '@/api';
import { forecastApi } from '@/api/scm';
import { basicDataApi } from '@/api/erp/basic-data';
import { warehouseApi } from '@/api/wms';

// 筛选表单数据
const filterForm = reactive({
  materialCode: '',
  materialName: '',
  warehouse: '',
  abcClass: ''
});

// 库存策略数据
const inventoryPolicies = ref<any[]>([]);

// 分页数据
const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(false);

// 选中的行
const selectedRows = ref<any[]>([]);

// 编辑对话框
const dialogVisible = ref(false);
const dialogTitle = ref('新增策略');
const policyFormRef = ref<any>(null);

// 物料和仓库数据
const materials = ref<any[]>([]);

const warehouses = ref<any[]>([{ id: 'ALL', name: '全部仓库' }]);

const loadMaterials = async () => {
  try {
    const res: any = await basicDataApi.getMaterials({ page: 1, size: 200 });
    const list = unwrapListResponse<any>(res);
    materials.value = (list || []).map((m: any) => ({
      code: m.materialCode ?? m.code ?? m.id ?? '',
      name: m.materialName ?? m.name ?? ''
    })).filter((m: any) => m.code);
  } catch (e) {
    materials.value = [];
  }
};

const loadWarehouses = async () => {
  try {
    const res: any = await warehouseApi.getList({ page: 1, size: 200 });
    const list = unwrapListResponse<any>(res);
    warehouses.value = [{ id: 'ALL', name: '全部仓库' }].concat(
      (list || []).map((w: any) => ({
        id: String(w.id ?? w.warehouseId ?? w.warehouseCode ?? ''),
        name: w.warehouseName ?? w.name ?? w.warehouseCode ?? ''
      })).filter((w: any) => w.id && w.name)
    );
  } catch (e) {
    warehouses.value = [{ id: 'ALL', name: '全部仓库' }];
  }
};

// 当前编辑的策略
const currentPolicy = reactive({
  id: '',
  materialCode: '',
  materialName: '',
  warehouseId: 'ALL',
  warehouseName: '全部仓库',
  abcClass: 'A',
  safetyStock: 0,
  rop: 0,
  eoq: 0,
  maxStock: 0,
  serviceLevel: 0.95,
  creator: 'admin'
});

// 表单验证规则
const policyRules = reactive({
  materialCode: [{ required: true, message: '请选择物料编码', trigger: 'blur' }],
  abcClass: [{ required: true, message: '请选择ABC分类', trigger: 'blur' }],
  safetyStock: [
    { required: true, message: '请输入安全库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '安全库存必须大于等于0', trigger: 'blur' }
  ],
  rop: [
    { required: true, message: '请输入再订货点', trigger: 'blur' },
    { type: 'number', min: 0, message: '再订货点必须大于等于0', trigger: 'blur' }
  ],
  eoq: [
    { required: true, message: '请输入经济订货批量', trigger: 'blur' },
    { type: 'number', min: 0, message: '经济订货批量必须大于等于0', trigger: 'blur' }
  ],
  maxStock: [
    { required: true, message: '请输入最大库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '最大库存必须大于等于0', trigger: 'blur' }
  ],
  serviceLevel: [
    { required: true, message: '请设置服务水平', trigger: 'change' },
    { type: 'number', min: 0.8, max: 0.99, message: '服务水平必须在80%-99%之间', trigger: 'change' }
  ]
});

// 根据筛选条件过滤结果
const filteredPolicies = computed(() => {
  return inventoryPolicies.value.filter(item => {
    // 物料编码过滤
    if (filterForm.materialCode && !String(item.materialCode ?? '').includes(filterForm.materialCode)) {
      return false;
    }
    // 物料名称过滤
    if (filterForm.materialName && !String(item.materialName ?? '').includes(filterForm.materialName)) {
      return false;
    }
    // 仓库过滤
    if (filterForm.warehouse && !String(item.warehouseId ?? '').includes(filterForm.warehouse)) {
      return false;
    }
    // ABC分类过滤
    if (filterForm.abcClass && item.abcClass !== filterForm.abcClass) {
      return false;
    }
    return true;
  });
});

// 分页结果
const paginatedPolicies = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  return filteredPolicies.value.slice(startIndex, endIndex);
});

const total = computed(() => {
  return filteredPolicies.value.length;
});

const loadPolicies = async () => {
  loading.value = true;
  try {
    const res: any = await forecastApi.getStrategies({ page: 1, size: 2000 });
    const records = unwrapListResponse<any>(res);
    inventoryPolicies.value = records.map((s: any) => ({
      id: s.id,
      materialCode: s.materialCode,
      materialName: s.materialName,
      warehouseId: 'ALL',
      warehouseName: '全部仓库',
      abcClass: s.abcClass || 'A',
      safetyStock: Number(s.safetyStock || 0),
      rop: Number(s.reorderPoint || 0),
      eoq: Number(s.eoq || 0),
      maxStock: Number(s.maxStock || 0),
      serviceLevel: Number(s.serviceLevelTarget || 0.95),
      creator: 'SYSTEM',
      updateTime: s.updateTime ? String(s.updateTime).replace('T', ' ').slice(0, 19) : ''
    }));

  } catch (e) {
    inventoryPolicies.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadMaterials();
  loadWarehouses();
  loadPolicies();
});

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  loadPolicies().finally(() => {
    ElMessage.info('搜索完成');
  });
};

// 重置筛选条件
const resetFilter = () => {
  Object.assign(filterForm, {
    materialCode: '',
    materialName: '',
    warehouse: '',
    abcClass: ''
  });
  currentPage.value = 1;
  loadPolicies().finally(() => {
    ElMessage.info('筛选条件已重置');
  });
};

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
};

// 处理当前页变化
const handleCurrentChange = (current: number) => {
  currentPage.value = current;
};

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection;
};

// 处理行点击
const handleRowClick = (row: any) => {
  // 可以添加行点击逻辑
};

// 打开新增对话框
const openAddDialog = () => {
  dialogTitle.value = '新增策略';
  Object.assign(currentPolicy, {
    id: '',
    materialCode: '',
    materialName: '',
    warehouseId: 'ALL',
    warehouseName: '全部仓库',
    abcClass: 'A',
    safetyStock: 0,
    rop: 0,
    eoq: 0,
    maxStock: 0,
    serviceLevel: 0.95
  });
  dialogVisible.value = true;
};

// 编辑策略
const editPolicy = (row: any) => {
  dialogTitle.value = '编辑策略';
  Object.assign(currentPolicy, { ...row });
  dialogVisible.value = true;
};

// 保存策略
const savePolicy = async () => {
  if (!policyFormRef.value) return;
  
  await policyFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      const material = materials.value.find(m => m.code === currentPolicy.materialCode);
      if (material) {
        currentPolicy.materialName = material.name;
      }
      const payload = {
        materialCode: currentPolicy.materialCode,
        materialName: currentPolicy.materialName,
        abcClass: currentPolicy.abcClass,
        safetyStock: currentPolicy.safetyStock,
        reorderPoint: currentPolicy.rop,
        eoq: currentPolicy.eoq,
        maxStock: currentPolicy.maxStock,
        serviceLevelTarget: currentPolicy.serviceLevel
      };

      loading.value = true;
      try {
        if (currentPolicy.id) {
          await forecastApi.updateStrategy(Number(currentPolicy.id), payload);
          ElMessage.success('策略已更新');
        } else {
          await forecastApi.createStrategy(payload);
          ElMessage.success('策略已新增');
        }
        dialogVisible.value = false;
        await loadPolicies();
      } catch (e) {
        ElMessage.error('保存失败');
      } finally {
        loading.value = false;
      }
    } else {
      ElMessage.error('表单验证失败，请检查输入');
      return false;
    }
  });
};

// 删除策略
const deletePolicy = (row: any) => {
  ElMessageBox.confirm('确定要删除该策略吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    loading.value = true;
    forecastApi.deleteStrategy(Number(row.id)).then(() => {
      ElMessage.success('策略已删除');
      loadPolicies();
    }).catch(() => {
      ElMessage.error('删除失败');
    }).finally(() => {
      loading.value = false;
    });
  }).catch(() => {
    // 取消删除
  });
};

// 批量编辑
const batchEdit = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要编辑的策略');
    return;
  }
  ElMessage.info('批量编辑功能已触发');
};

// 批量删除
const batchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的策略');
    return;
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 条策略吗？`, '批量删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const idsToDelete = selectedRows.value.map(row => row.id);
    inventoryPolicies.value = inventoryPolicies.value.filter(item => !idsToDelete.includes(item.id));
    selectedRows.value = [];
    ElMessage.success(`已删除 ${idsToDelete.length} 条策略`);
  }).catch(() => {
    // 取消删除
  });
};

// 处理导入文件变化
const handleImportChange = (file: any) => {
  const reader = new FileReader();
  reader.onload = (e) => {
    ElMessage.success('文件已读取，实际项目中会解析Excel文件并导入数据');
  };
  reader.readAsArrayBuffer(file.raw);
};

// 导出策略
const exportPolicies = () => {
  ElMessage.success('导出功能已触发');
  // 实际项目中，这里会调用导出API
};

// 自动计算
const autoCalculate = () => {
  loading.value = true;
  forecastApi.calculateStrategies().then(() => {
    ElMessage.success('已触发自动计算');
    return loadPolicies();
  }).catch(() => {
    ElMessage.error('自动计算失败');
  }).finally(() => {
    loading.value = false;
  });
};

// 刷新策略
const refreshPolicies = () => {
  loadPolicies().finally(() => {
    ElMessage.success('策略已刷新');
  });
};

// 获取ABC分类标签类型
const getAbcTagType = (abcClass: string): string => {
  switch (abcClass) {
    case 'A':
      return 'danger';
    case 'B':
      return 'warning';
    case 'C':
      return 'success';
    default:
      return 'info';
  }
};
</script>

<style scoped>
.inventory-policy-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
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

.filter-card,
.action-card,
.policy-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  width: 100%;
}

.filter-actions {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.upload-btn {
  display: inline-block;
}

.policy-count {
  font-size: 14px;
  color: #606266;
  margin-left: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.service-level-value {
  text-align: center;
  margin-top: 10px;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .inventory-policy-container {
    padding: 10px;
  }
  
  .filter-form {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .filter-actions {
    margin-top: 10px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>

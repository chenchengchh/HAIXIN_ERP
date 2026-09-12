<template>
  <WmsPageLayout title="条码规则管理" class="barcode-rule-setting-container">
    <template #actions>
      <el-button type="primary" @click="handleAddBarcodeRule">
        <el-icon-plus /> 新建条码规则
      </el-button>
    </template>

    <template #filters>
      <el-form label-width="80px" inline>
        <el-form-item label="关键词">
          <el-input
            placeholder="编码/名称"
            v-model="searchKeyword"
            clearable
            @keyup.enter="handleSearch"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="selectedStatus"
            placeholder="全部"
            clearable
            style="width: 150px"
          >
            <el-option label="启用" value="1"></el-option>
            <el-option label="禁用" value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleResetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </template>

    <el-card class="barcode-rule-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h3>条码规则列表</h3>
          <span class="list-count">共 {{ total }} 条记录</span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="barcodeRules"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        stripe
        :empty-text="loading ? '加载中...' : '暂无数据'"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="ruleCode" label="规则编码" width="120"></el-table-column>
        <el-table-column prop="ruleName" label="规则名称" width="180"></el-table-column>
        <el-table-column prop="ruleType" label="规则类型" width="120">
          <template #default="scope">
            <el-tag
              :type="getRuleTypeColor(scope.row.ruleType)"
            >
              {{ getRuleTypeName(scope.row.ruleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ruleFormat" label="规则格式" min-width="200">
          <template #default="scope">
            <code>{{ scope.row.ruleFormat }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag
              :type="scope.row.status === '1' ? 'success' : 'danger'"
            >
              {{ scope.row.status === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180"></el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleEditBarcodeRule(scope.row)"
              :icon="EditPen"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDeleteBarcodeRule(scope.row)"
              :icon="Delete"
            >
              删除
            </el-button>
            <el-switch
              v-model="scope.row.status"
              :active-value="'1'"
              :inactive-value="'0'"
              @change="handleStatusChange(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 条码规则详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleCloseDialog"
    >
      <el-form
        ref="barcodeRuleFormRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input
            v-model="formData.ruleCode"
            placeholder="请输入规则编码"
            :disabled="dialogMode === 'edit'"
          ></el-input>
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input
            v-model="formData.ruleName"
            placeholder="请输入规则名称"
          ></el-input>
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select
            v-model="formData.ruleType"
            placeholder="请选择规则类型"
          >
            <el-option label="仓库码" value="warehouse"></el-option>
            <el-option label="库位码" value="location"></el-option>
            <el-option label="物料码" value="material"></el-option>
            <el-option label="批次码" value="batch"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="规则格式" prop="ruleFormat">
          <el-input
            v-model="formData.ruleFormat"
            placeholder="请输入规则格式，例如：WH{code}-{date}"
            type="textarea"
            :rows="3"
          ></el-input>
          <div class="rule-format-hint">
            <strong>格式说明：</strong>
            <ul>
              <li>使用{code}表示自动生成的序列号</li>
              <li>使用{date}表示当前日期，格式：YYYYMMDD</li>
              <li>使用{time}表示当前时间，格式：HHmmss</li>
              <li>使用自定义前缀或后缀，例如：WH-</li>
            </ul>
          </div>
        </el-form-item>
        <el-form-item label="规则描述" prop="ruleDesc">
          <el-input
            v-model="formData.ruleDesc"
            placeholder="请输入规则描述"
            type="textarea"
            :rows="3"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">取消</el-button>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </WmsPageLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus as ElIconPlus, EditPen, Delete } from '@element-plus/icons-vue';
import { barcodeRuleApi } from '@/api/wms';
import WmsPageLayout from '../components/WmsPageLayout.vue';

// 状态管理
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('新建条码规则');
const dialogMode = ref<'add' | 'edit'>('add');
const barcodeRuleFormRef = ref<InstanceType<typeof import('element-plus').ElForm>>();

// 搜索参数
const searchKeyword = ref('');

// 状态筛选
const selectedStatus = ref<string>('');

// 分页参数
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 选中的条码规则
const selectedBarcodeRules = ref<BarcodeRule[]>([]);

// 条码规则数据类型
interface BarcodeRule {
  id: number;
  ruleCode: string;
  ruleName: string;
  ruleType: string;
  ruleFormat: string;
  ruleDesc?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

// 条码规则列表数据
const barcodeRules = ref<BarcodeRule[]>([]);
const editingId = ref<number | null>(null);

// 获取条码规则列表数据
const getBarcodeRuleList = async () => {
  try {
    loading.value = true;

    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    };
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (selectedStatus.value) params.status = selectedStatus.value;
    const res = await barcodeRuleApi.getList(params);
    const pageData = res.data || {};
    const list = (pageData.list || []) as any[];
    barcodeRules.value = list.map((r: any) => ({
      ...r,
      ruleDesc: r?.ruleDesc ?? r?.description ?? ''
    })) as any;
    total.value = Number(pageData.total ?? 0);
  } catch (error) {
    console.error('获取条码规则列表失败:', error);
    ElMessage.error('获取条码规则列表失败');
  } finally {
    loading.value = false;
  }
};

// 表单数据
const formData = reactive({
  ruleCode: '',
  ruleName: '',
  ruleType: 'warehouse',
  ruleFormat: '',
  ruleDesc: ''
});

// 表单验证规则
const formRules = reactive({
  ruleCode: [
    { required: true, message: '请输入规则编码', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  ruleType: [
    { required: true, message: '请选择规则类型', trigger: 'blur' }
  ],
  ruleFormat: [
    { required: true, message: '请输入规则格式', trigger: 'blur' },
    { min: 3, max: 100, message: '长度在 3 到 100 个字符', trigger: 'blur' }
  ]
});

// 处理选中变化
const handleSelectionChange = (selection: BarcodeRule[]) => {
  selectedBarcodeRules.value = selection;
};

// 处理搜索
const handleSearch = () => {
  // 重置分页
  currentPage.value = 1;
  getBarcodeRuleList();
};

const handleResetSearch = () => {
  searchKeyword.value = '';
  selectedStatus.value = '';
  handleSearch();
};

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  getBarcodeRuleList();
};

// 处理当前页变化
const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  getBarcodeRuleList();
};

// 获取状态名称
const getStatusName = (status: string) => {
  return status === '1' ? '启用' : '禁用';
};

// 处理添加条码规则
const handleAddBarcodeRule = () => {
  dialogMode.value = 'add';
  dialogTitle.value = '新建条码规则';
  editingId.value = null;
  // 重置表单
  Object.assign(formData, {
    ruleCode: '',
    ruleName: '',
    ruleType: 'warehouse',
    ruleFormat: '',
    ruleDesc: ''
  });
  if (barcodeRuleFormRef.value) {
    barcodeRuleFormRef.value.resetFields();
  }
  dialogVisible.value = true;
};

// 处理编辑条码规则
const handleEditBarcodeRule = (barcodeRule: BarcodeRule) => {
  dialogMode.value = 'edit';
  dialogTitle.value = '编辑条码规则';
  editingId.value = barcodeRule.id;
  // 填充表单数据
  Object.assign(formData, {
    ruleCode: barcodeRule.ruleCode,
    ruleName: barcodeRule.ruleName,
    ruleType: barcodeRule.ruleType,
    ruleFormat: barcodeRule.ruleFormat,
    ruleDesc: barcodeRule.ruleDesc
  });
  dialogVisible.value = true;
};

// 处理删除条码规则
const handleDeleteBarcodeRule = (barcodeRule: BarcodeRule) => {
  ElMessageBox.confirm(
    `确定要删除条码规则【${barcodeRule.ruleName}】吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      loading.value = true;
      barcodeRuleApi
        .delete(String(barcodeRule.id))
        .then(() => {
          ElMessage.success('删除成功');
          getBarcodeRuleList();
        })
        .catch((e: any) => {
          ElMessage.error(e?.message || '删除失败');
        })
        .finally(() => {
          loading.value = false;
        });
    })
    .catch(() => {
      // 取消删除
    });
};

// 处理状态变化
const handleStatusChange = (barcodeRule: BarcodeRule) => {
  const old = barcodeRule.status === '1' ? '0' : '1';
  barcodeRuleApi
    .updateStatus(String(barcodeRule.id), barcodeRule.status)
    .then(() => {
      ElMessage.success(`条码规则【${barcodeRule.ruleName}】状态已更新为${barcodeRule.status === '1' ? '启用' : '禁用'}`);
    })
    .catch((e: any) => {
      barcodeRule.status = old;
      ElMessage.error(e?.message || '状态更新失败');
    });
};

// 处理表单提交
const handleSubmit = async () => {
  if (!barcodeRuleFormRef.value) return;
  
  try {
    await barcodeRuleFormRef.value.validate();
    
    loading.value = true;

    const payload: any = {
      ruleCode: formData.ruleCode,
      ruleName: formData.ruleName,
      ruleType: formData.ruleType,
      ruleFormat: formData.ruleFormat,
      description: formData.ruleDesc
    };

    if (dialogMode.value === 'add') {
      await barcodeRuleApi.create(payload);
      ElMessage.success('新建条码规则成功');
    } else {
      if (editingId.value == null) {
        throw new Error('缺少条码规则ID');
      }
      await barcodeRuleApi.update(String(editingId.value), payload);
      ElMessage.success('编辑条码规则成功');
    }

    dialogVisible.value = false;
    await getBarcodeRuleList();
  } catch (error) {
    console.error('表单验证失败:', error);
    ElMessage.error((error as any)?.message || '提交失败');
  } finally {
    loading.value = false;
  }
};

// 处理关闭对话框
const handleCloseDialog = () => {
  dialogVisible.value = false;
  if (barcodeRuleFormRef.value) {
    barcodeRuleFormRef.value.resetFields();
  }
};

// 获取规则类型颜色
const getRuleTypeColor = (ruleType: string) => {
  const colorMap: Record<string, string> = {
    'warehouse': 'success',
    'location': 'warning',
    'material': 'info',
    'batch': 'primary'
  };
  return colorMap[ruleType] || 'default';
};

// 获取规则类型名称
const getRuleTypeName = (ruleType: string) => {
  const nameMap: Record<string, string> = {
    'warehouse': '仓库码',
    'location': '库位码',
    'material': '物料码',
    'batch': '批次码'
  };
  return nameMap[ruleType] || '未知';
};

// 组件挂载时初始化数据
onMounted(() => {
  getBarcodeRuleList();
});
</script>

<style scoped>
.barcode-rule-setting-container {
  padding: 0;
}

.barcode-rule-card {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 规则格式提示 */
.rule-format-hint {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
}

.rule-format-hint strong {
  color: #303133;
}

.rule-format-hint ul {
  margin: 5px 0 0 20px;
  padding: 0;
  color: #606266;
}

.rule-format-hint li {
  margin: 3px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .barcode-rule-setting-container {
    padding: 0;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .card-header h3 {
    font-size: 16px;
  }
  
  .search-filter .el-input {
    width: 100% !important;
  }
  
  .rule-format-hint {
    font-size: 11px;
  }
}
</style>

<template>
  <WmsPageLayout title="物料类型管理" class="material-type-setting-container">
    <template #actions>
      <el-button type="primary" @click="handleAddMaterialType">
        <el-icon-plus /> 新建物料类型
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

    <el-card class="material-type-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h3>物料类型列表</h3>
          <span class="list-count">共 {{ total }} 条记录</span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="materialTypes"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        stripe
        :empty-text="loading ? '加载中...' : '暂无数据'"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="typeCode" label="类型编码" width="120"></el-table-column>
        <el-table-column prop="typeName" label="类型名称" width="180"></el-table-column>
        <el-table-column prop="typeDesc" label="类型描述" min-width="200"></el-table-column>
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
              @click="handleEditMaterialType(scope.row)"
              :icon="EditPen"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDeleteMaterialType(scope.row)"
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
    
    <!-- 物料类型详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :before-close="handleCloseDialog"
    >
      <el-form
        ref="materialTypeFormRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="类型编码" prop="typeCode">
          <el-input
            v-model="formData.typeCode"
            placeholder="请输入类型编码"
            :disabled="dialogMode === 'edit'"
          ></el-input>
        </el-form-item>
        <el-form-item label="类型名称" prop="typeName">
          <el-input
            v-model="formData.typeName"
            placeholder="请输入类型名称"
          ></el-input>
        </el-form-item>
        <el-form-item label="类型描述" prop="typeDesc">
          <el-input
            v-model="formData.typeDesc"
            placeholder="请输入类型描述"
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
import { materialTypeApi } from '@/api/wms';
import WmsPageLayout from '../components/WmsPageLayout.vue';

// 状态管理
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('新建物料类型');
const dialogMode = ref<'add' | 'edit'>('add');
const materialTypeFormRef = ref<InstanceType<typeof import('element-plus').ElForm>>();

// 搜索参数
const searchKeyword = ref('');

// 状态筛选
const selectedStatus = ref<string>('');

// 分页参数
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 选中的物料类型
const selectedMaterialTypes = ref<MaterialType[]>([]);

// 物料类型数据类型
interface MaterialType {
  id: number;
  typeCode: string;
  typeName: string;
  typeDesc?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

// 物料类型列表数据
const materialTypes = ref<MaterialType[]>([]);
const editingId = ref<number | null>(null);

// 获取物料类型列表数据
const getMaterialTypeList = async () => {
  try {
    loading.value = true;

    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    };
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (selectedStatus.value) params.status = selectedStatus.value;
    const res = await materialTypeApi.getList(params);
    const pageData = res.data || {};
    const list = (pageData.list || []) as any[];
    materialTypes.value = list.map((t: any) => ({
      ...t,
      typeDesc: t?.typeDesc ?? t?.description ?? ''
    })) as any;
    total.value = Number(pageData.total ?? 0);
  } catch (error) {
    console.error('获取物料类型列表失败:', error);
    ElMessage.error('获取物料类型列表失败');
  } finally {
    loading.value = false;
  }
};

// 表单数据
const formData = reactive({
  typeCode: '',
  typeName: '',
  typeDesc: ''
});

// 表单验证规则
const formRules = reactive({
  typeCode: [
    { required: true, message: '请输入类型编码', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  typeName: [
    { required: true, message: '请输入类型名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
});

// 处理选中变化
const handleSelectionChange = (selection: MaterialType[]) => {
  selectedMaterialTypes.value = selection;
};

// 处理搜索
const handleSearch = () => {
  // 重置分页
  currentPage.value = 1;
  getMaterialTypeList();
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
  getMaterialTypeList();
};

// 处理当前页变化
const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  getMaterialTypeList();
};

// 获取状态名称
const getStatusName = (status: string) => {
  return status === '1' ? '启用' : '禁用';
};

// 处理添加物料类型
const handleAddMaterialType = () => {
  dialogMode.value = 'add';
  dialogTitle.value = '新建物料类型';
  editingId.value = null;
  // 重置表单
  Object.assign(formData, {
    typeCode: '',
    typeName: '',
    typeDesc: ''
  });
  if (materialTypeFormRef.value) {
    materialTypeFormRef.value.resetFields();
  }
  dialogVisible.value = true;
};

// 处理编辑物料类型
const handleEditMaterialType = (materialType: MaterialType) => {
  dialogMode.value = 'edit';
  dialogTitle.value = '编辑物料类型';
  editingId.value = materialType.id;
  // 填充表单数据
  Object.assign(formData, {
    typeCode: materialType.typeCode,
    typeName: materialType.typeName,
    typeDesc: materialType.typeDesc
  });
  dialogVisible.value = true;
};

// 处理删除物料类型
const handleDeleteMaterialType = (materialType: MaterialType) => {
  ElMessageBox.confirm(
    `确定要删除物料类型【${materialType.typeName}】吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      loading.value = true;
      materialTypeApi
        .delete(String(materialType.id))
        .then(() => {
          ElMessage.success('删除成功');
          getMaterialTypeList();
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
const handleStatusChange = (materialType: MaterialType) => {
  const old = materialType.status === '1' ? '0' : '1';
  materialTypeApi
    .updateStatus(String(materialType.id), materialType.status)
    .then(() => {
      ElMessage.success(`物料类型【${materialType.typeName}】状态已更新为${materialType.status === '1' ? '启用' : '禁用'}`);
    })
    .catch((e: any) => {
      materialType.status = old;
      ElMessage.error(e?.message || '状态更新失败');
    });
};

// 处理表单提交
const handleSubmit = async () => {
  if (!materialTypeFormRef.value) return;
  
  try {
    await materialTypeFormRef.value.validate();
    
    loading.value = true;

    const payload: any = {
      typeCode: formData.typeCode,
      typeName: formData.typeName,
      description: formData.typeDesc
    };

    if (dialogMode.value === 'add') {
      await materialTypeApi.create(payload);
      ElMessage.success('新建物料类型成功');
    } else {
      if (editingId.value == null) {
        throw new Error('缺少物料类型ID');
      }
      await materialTypeApi.update(String(editingId.value), payload);
      ElMessage.success('编辑物料类型成功');
    }

    dialogVisible.value = false;
    await getMaterialTypeList();
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
  if (materialTypeFormRef.value) {
    materialTypeFormRef.value.resetFields();
  }
};

// 组件挂载时初始化数据
onMounted(() => {
  getMaterialTypeList();
});
</script>

<style scoped>
.material-type-setting-container {
  padding: 0;
}

.material-type-card {
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

/* 响应式设计 */
@media (max-width: 768px) {
  .material-type-setting-container {
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
  
}
</style>

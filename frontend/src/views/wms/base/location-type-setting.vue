<template>
  <WmsPageLayout title="库位类型管理" class="location-type-setting-container">
    <template #actions>
      <el-button type="primary" @click="handleAddLocationType">
        <el-icon-plus /> 新建库位类型
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

    <el-card class="location-type-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h3>库位类型列表</h3>
          <span class="list-count">共 {{ total }} 条记录</span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="locationTypes"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        stripe
        :empty-text="loading ? '加载中...' : '暂无数据'"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="typeCode" label="类型编码" width="120"></el-table-column>
        <el-table-column prop="typeName" label="类型名称" width="180"></el-table-column>
        <el-table-column prop="typeDesc" label="类型描述" min-width="200"></el-table-column>
        <el-table-column prop="maxWeight" label="最大承重(kg)" width="120">
          <template #default="scope">{{ scope.row.maxWeight }} kg</template>
        </el-table-column>
        <el-table-column prop="mixFlag" label="是否允许混放" width="120">
          <template #default="scope">
            <el-switch v-model="scope.row.mixFlag" disabled></el-switch>
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
              @click="handleEditLocationType(scope.row)"
              :icon="EditPen"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDeleteLocationType(scope.row)"
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
    
    <!-- 库位类型详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :before-close="handleCloseDialog"
    >
      <el-form
        ref="locationTypeFormRef"
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
        <el-form-item label="最大承重" prop="maxWeight">
          <el-input-number
            v-model="formData.maxWeight"
            :min="0"
            :step="10"
            placeholder="请输入最大承重"
            style="width: 100%"
          >
            <template #append>kg</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="是否允许混放" prop="mixFlag">
          <el-switch v-model="formData.mixFlag"></el-switch>
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
import { locationTypeApi } from '@/api/wms';
import WmsPageLayout from '../components/WmsPageLayout.vue';

// 状态管理
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('新建库位类型');
const dialogMode = ref<'add' | 'edit'>('add');
const locationTypeFormRef = ref<InstanceType<typeof import('element-plus').ElForm>>();

// 搜索参数
const searchKeyword = ref('');

// 分页参数
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 选中的库位类型
const selectedLocationTypes = ref<LocationType[]>([]);

// 库位类型数据类型
interface LocationType {
  id: number;
  typeCode: string;
  typeName: string;
  typeDesc?: string;
  maxWeight: number;
  mixFlag: boolean;
  status: string;
  createdAt: string;
  updatedAt: string;
}

// 状态筛选
const selectedStatus = ref<string>('');

// 库位类型列表数据
const locationTypes = ref<LocationType[]>([]);
const editingId = ref<number | null>(null);

// 获取库位类型列表数据
const getLocationTypeList = async () => {
  try {
    loading.value = true;

    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    };
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (selectedStatus.value) params.status = selectedStatus.value;
    const res = await locationTypeApi.getList(params);
    const pageData = res.data || {};
    const list = (pageData.list || []) as any[];
    locationTypes.value = list.map((t: any) => ({
      ...t,
      typeDesc: t?.typeDesc ?? t?.description ?? ''
    })) as any;
    total.value = Number(pageData.total ?? 0);
  } catch (error) {
    console.error('获取库位类型列表失败:', error);
    ElMessage.error('获取库位类型列表失败');
  } finally {
    loading.value = false;
  }
};

// 表单数据
const formData = reactive({
  typeCode: '',
  typeName: '',
  typeDesc: '',
  maxWeight: 0,
  mixFlag: false
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
  ],
  maxWeight: [
    { required: true, message: '请输入最大承重', trigger: 'blur' },
    { type: 'number', min: 0, message: '最大承重必须大于等于 0', trigger: 'blur' }
  ]
});

// 处理选中变化
const handleSelectionChange = (selection: LocationType[]) => {
  selectedLocationTypes.value = selection;
};

// 处理搜索
const handleSearch = () => {
  // 重置分页
  currentPage.value = 1;
  getLocationTypeList();
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
  getLocationTypeList();
};

// 处理当前页变化
const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  getLocationTypeList();
};

// 获取状态名称
const getStatusName = (status: string) => {
  return status === '1' ? '启用' : '禁用';
};

// 处理添加库位类型
const handleAddLocationType = () => {
  dialogMode.value = 'add';
  dialogTitle.value = '新建库位类型';
  editingId.value = null;
  // 重置表单
  Object.assign(formData, {
    typeCode: '',
    typeName: '',
    typeDesc: '',
    maxWeight: 0,
    mixFlag: false
  });
  if (locationTypeFormRef.value) {
    locationTypeFormRef.value.resetFields();
  }
  dialogVisible.value = true;
};

// 处理编辑库位类型
const handleEditLocationType = (locationType: LocationType) => {
  dialogMode.value = 'edit';
  dialogTitle.value = '编辑库位类型';
  editingId.value = locationType.id;
  // 填充表单数据
  Object.assign(formData, {
    typeCode: locationType.typeCode,
    typeName: locationType.typeName,
    typeDesc: locationType.typeDesc,
    maxWeight: locationType.maxWeight,
    mixFlag: locationType.mixFlag
  });
  dialogVisible.value = true;
};

// 处理删除库位类型
const handleDeleteLocationType = (locationType: LocationType) => {
  ElMessageBox.confirm(
    `确定要删除库位类型【${locationType.typeName}】吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      loading.value = true;
      locationTypeApi
        .delete(String(locationType.id))
        .then(() => {
          ElMessage.success('删除成功');
          getLocationTypeList();
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
const handleStatusChange = (locationType: LocationType) => {
  const old = locationType.status === '1' ? '0' : '1';
  locationTypeApi
    .updateStatus(String(locationType.id), locationType.status)
    .then(() => {
      ElMessage.success(`库位类型【${locationType.typeName}】状态已更新为${locationType.status === '1' ? '启用' : '禁用'}`);
    })
    .catch((e: any) => {
      locationType.status = old;
      ElMessage.error(e?.message || '状态更新失败');
    });
};

// 处理表单提交
const handleSubmit = async () => {
  if (!locationTypeFormRef.value) return;
  
  try {
    await locationTypeFormRef.value.validate();
    
    loading.value = true;

    const payload: any = {
      typeCode: formData.typeCode,
      typeName: formData.typeName,
      typeDesc: formData.typeDesc,
      maxWeight: formData.maxWeight,
      mixFlag: formData.mixFlag
    };

    if (dialogMode.value === 'add') {
      await locationTypeApi.create(payload);
      ElMessage.success('新建库位类型成功');
    } else {
      if (editingId.value == null) {
        throw new Error('缺少库位类型ID');
      }
      await locationTypeApi.update(String(editingId.value), payload);
      ElMessage.success('编辑库位类型成功');
    }

    dialogVisible.value = false;
    await getLocationTypeList();
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
  if (locationTypeFormRef.value) {
    locationTypeFormRef.value.resetFields();
  }
};

// 组件挂载时初始化数据
onMounted(() => {
  getLocationTypeList();
});
</script>

<style scoped>
.location-type-setting-container {
  padding: 0;
}

.location-type-card {
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
  .location-type-setting-container {
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

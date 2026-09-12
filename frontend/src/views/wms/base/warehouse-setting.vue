<template>
  <WmsPageLayout title="仓库管理" class="warehouse-setting-container">
    <template #actions>
      <el-button type="primary" @click="handleAddWarehouse">
        <el-icon-plus /> 新建仓库
      </el-button>
    </template>

    <template #filters>
      <el-form label-width="80px" inline>
        <el-form-item label="关键词">
          <el-input
            placeholder="仓库编码/名称"
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

    <el-card class="warehouse-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h3>仓库列表</h3>
          <span class="list-count">共 {{ total }} 条记录</span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="warehouses"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        stripe
        empty-text="暂无数据"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="warehouseCode" label="仓库编码" width="120"></el-table-column>
        <el-table-column prop="warehouseName" label="仓库名称" width="180"></el-table-column>
        <el-table-column prop="address" label="仓库地址" min-width="200"></el-table-column>
        <el-table-column prop="manager" label="仓库负责人" width="120"></el-table-column>
        <el-table-column prop="phone" label="联系电话" width="150"></el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag
              :type="scope.row.status === '1' ? 'success' : 'danger'"
            >
              {{ getStatusName(scope.row.status) }}
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
              @click="handleEditWarehouse(scope.row)"
              :icon="EditPen"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDeleteWarehouse(scope.row)"
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
          background
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 仓库详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleCloseDialog"
    >
      <el-form
        ref="warehouseFormRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="仓库编码" prop="warehouseCode">
          <el-input
            v-model="formData.warehouseCode"
            placeholder="请输入仓库编码"
            :disabled="dialogMode === 'edit'"
          ></el-input>
        </el-form-item>
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input
            v-model="formData.warehouseName"
            placeholder="请输入仓库名称"
          ></el-input>
        </el-form-item>
        <el-form-item label="仓库地址" prop="address">
          <el-input
            v-model="formData.address"
            placeholder="请输入仓库地址"
            type="textarea"
            :rows="3"
          ></el-input>
        </el-form-item>
        <el-form-item label="仓库负责人" prop="manager">
          <el-input
            v-model="formData.manager"
            placeholder="请输入仓库负责人"
          ></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入联系电话"
          ></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="formData.email"
            placeholder="请输入邮箱"
          ></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            placeholder="请输入备注"
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
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus as ElIconPlus, EditPen, Delete } from '@element-plus/icons-vue';
import { warehouseApi } from '@/api/wms';
import WmsPageLayout from '../components/WmsPageLayout.vue';

// 状态管理
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('新建仓库');
const dialogMode = ref<'add' | 'edit'>('add');
const warehouseFormRef = ref<InstanceType<typeof import('element-plus').ElForm>>();

// 分页参数
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 搜索和筛选参数
const searchKeyword = ref('');
const selectedStatus = ref('');

// 选中的仓库
const selectedWarehouses = ref<Warehouse[]>([]);

// 仓库数据类型
interface Warehouse {
  id: number;
  warehouseCode: string;
  warehouseName: string;
  address: string;
  manager: string;
  phone: string;
  email?: string;
  status: string;
  remark?: string;
  createdAt: string;
  updatedAt: string;
}

// 仓库列表数据
const warehouses = ref<Warehouse[]>([]);
const allWarehouses = ref<Warehouse[]>([]);
const editingId = ref<number | null>(null);

// 表单数据
const formData = reactive({
  warehouseCode: '',
  warehouseName: '',
  address: '',
  manager: '',
  phone: '',
  email: '',
  remark: ''
});

// 表单验证规则
const formRules = reactive({
  warehouseCode: [
    { required: true, message: '请输入仓库编码', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  warehouseName: [
    { required: true, message: '请输入仓库名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入仓库地址', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ],
  manager: [
    { required: true, message: '请输入仓库负责人', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
});

// 获取仓库列表数据
const getWarehouseList = async () => {
  try {
    loading.value = true;

    const res = await warehouseApi.getList({ page: 0, size: 1000 });
    const pageData = res.data || {};
    const list = (pageData.content || pageData.list || pageData.records || []) as any[];

    allWarehouses.value = list.map((w: any) => ({
      id: Number(w?.id ?? 0),
      warehouseCode: String(w?.warehouseCode ?? ''),
      warehouseName: String(w?.warehouseName ?? ''),
      address: String(w?.address ?? ''),
      manager: String(w?.manager ?? ''),
      phone: String(w?.contact ?? ''),
      email: '',
      status: String(w?.status ?? '1'),
      remark: '',
      createdAt: String(w?.createdTime ?? '').replace('T', ' '),
      updatedAt: String(w?.updatedTime ?? '').replace('T', ' ')
    }));

    let filteredData = [...allWarehouses.value];
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase();
      filteredData = filteredData.filter(warehouse =>
        warehouse.warehouseCode.toLowerCase().includes(keyword) ||
        warehouse.warehouseName.toLowerCase().includes(keyword)
      );
    }
    if (selectedStatus.value) {
      filteredData = filteredData.filter(warehouse => warehouse.status === selectedStatus.value);
    }

    total.value = filteredData.length;
    const startIndex = (currentPage.value - 1) * pageSize.value;
    const endIndex = startIndex + pageSize.value;
    warehouses.value = filteredData.slice(startIndex, endIndex);
  } catch (error) {
    console.error('获取仓库列表失败:', error);
    ElMessage.error('获取仓库列表失败');
  } finally {
    loading.value = false;
  }
};

// 处理选中变化
const handleSelectionChange = (selection: Warehouse[]) => {
  selectedWarehouses.value = selection;
};

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  getWarehouseList();
};

// 处理当前页变化
const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  getWarehouseList();
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  getWarehouseList();
};

const handleResetSearch = () => {
  searchKeyword.value = '';
  selectedStatus.value = '';
  handleSearch();
};

// 获取状态名称
const getStatusName = (status: string) => {
  return status === '1' ? '启用' : '禁用';
};

// 处理添加仓库
const handleAddWarehouse = () => {
  dialogMode.value = 'add';
  dialogTitle.value = '新建仓库';
  editingId.value = null;
  // 重置表单
  Object.assign(formData, {
    warehouseCode: '',
    warehouseName: '',
    address: '',
    manager: '',
    phone: '',
    email: '',
    remark: ''
  });
  if (warehouseFormRef.value) {
    warehouseFormRef.value.resetFields();
  }
  dialogVisible.value = true;
};

// 处理编辑仓库
const handleEditWarehouse = (warehouse: Warehouse) => {
  dialogMode.value = 'edit';
  dialogTitle.value = '编辑仓库';
  editingId.value = warehouse.id;
  // 填充表单数据
  Object.assign(formData, {
    warehouseCode: warehouse.warehouseCode,
    warehouseName: warehouse.warehouseName,
    address: warehouse.address,
    manager: warehouse.manager,
    phone: warehouse.phone,
    email: warehouse.email,
    remark: warehouse.remark
  });
  dialogVisible.value = true;
};

// 处理删除仓库
const handleDeleteWarehouse = (warehouse: Warehouse) => {
  ElMessageBox.confirm(
    `确定要删除仓库【${warehouse.warehouseName}】吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      loading.value = true;
      warehouseApi
        .delete(String(warehouse.id))
        .then(() => {
          ElMessage.success('删除成功');
          getWarehouseList();
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
const handleStatusChange = (warehouse: Warehouse) => {
  const old = warehouse.status === '1' ? '0' : '1';
  loading.value = true;
  warehouseApi
    .update(String(warehouse.id), { status: Number(warehouse.status) } as any)
    .then(() => {
      ElMessage.success(`仓库【${warehouse.warehouseName}】状态已更新为${getStatusName(warehouse.status)}`);
    })
    .catch((e: any) => {
      warehouse.status = old;
      ElMessage.error(e?.message || '状态更新失败');
    })
    .finally(() => {
      loading.value = false;
    });
};

// 处理表单提交
const handleSubmit = async () => {
  if (!warehouseFormRef.value) return;
  
  try {
    await warehouseFormRef.value.validate();
    
    loading.value = true;
    const payload: any = {
      warehouseCode: formData.warehouseCode,
      warehouseName: formData.warehouseName,
      address: formData.address,
      manager: formData.manager,
      contact: formData.phone
    };

    if (dialogMode.value === 'add') {
      await warehouseApi.create(payload);
      ElMessage.success('新建仓库成功');
    } else {
      if (editingId.value == null) {
        throw new Error('缺少仓库ID');
      }
      await warehouseApi.update(String(editingId.value), payload);
      ElMessage.success('编辑仓库成功');
    }

    dialogVisible.value = false;
    await getWarehouseList();
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
  if (warehouseFormRef.value) {
    warehouseFormRef.value.resetFields();
  }
};

// 组件挂载时初始化数据
onMounted(() => {
  console.log('仓库设置页面初始化');
  // 调用API获取仓库列表数据
  getWarehouseList();
});
</script>

<style scoped>
.warehouse-setting-container {
  padding: 0;
}

.warehouse-card {
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
  font-weight: 600;
  color: #303133;
}

/* 优化表格样式 */
:deep(.el-table) {
  border-radius: 6px;
  overflow: hidden;
}

:deep(.el-table__header-wrapper) {
  background-color: #fafafa;
}

:deep(.el-table__header th) {
  font-weight: 600;
  background-color: #fafafa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-table__body tr:hover > td) {
  background-color: #f5f7fa;
}

:deep(.el-table__body tr.el-table__row--striped) {
  background-color: #fafafa;
}

:deep(.el-table__body tr.el-table__row--striped:hover > td) {
  background-color: #f0f2f5;
}

/* 优化分页容器 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 16px 0 0;
}

:deep(.el-pagination) {
  justify-content: flex-end;
}

:deep(.el-pagination__total) {
  margin-right: 16px;
  color: #606266;
}

/* 优化对话框样式 */
:deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

:deep(.el-dialog__header) {
  background-color: #fafafa;
  border-bottom: 1px solid #ebeef5;
  padding: 20px 24px;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

:deep(.el-dialog__body) {
  padding: 24px;
  font-size: 14px;
}

:deep(.el-dialog__footer) {
  background-color: #fafafa;
  border-top: 1px solid #ebeef5;
  padding: 16px 24px;
}

/* 优化表单样式 */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  border-radius: 4px;
  border-color: #dcdfe6;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover) {
  border-color: #c6e2ff;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .warehouse-setting-container {
    padding: 0 20px;
  }
}

@media (max-width: 768px) {
  .warehouse-setting-container {
    padding: 0 12px;
  }
  
  :deep(.warehouse-card .el-card__body) {
    padding: 16px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .card-header h3 {
    font-size: 16px;
  }
  
  .search-filter {
    padding: 12px;
    margin-bottom: 16px;
  }
  
  .search-filter .el-input,
  .search-filter .el-select {
    width: 100%;
  }
  
  .pagination-container {
    margin-top: 16px;
    padding: 12px 0 0;
  }
  
  :deep(.el-pagination) {
    font-size: 13px;
  }
  
  :deep(.el-pagination__total) {
    margin-right: 8px;
  }
  
  :deep(.el-dialog) {
    margin: 10px;
    border-radius: 6px;
  }
  
  :deep(.el-dialog__header) {
    padding: 16px 20px;
  }
  
  :deep(.el-dialog__body) {
    padding: 20px;
  }
  
  :deep(.el-dialog__footer) {
    padding: 12px 20px;
  }
}

@media (max-width: 480px) {
  .warehouse-setting-container {
    padding: 0 8px;
  }
  
  :deep(.warehouse-card .el-card__body) {
    padding: 12px;
  }
  
  .search-filter {
    padding: 8px;
    gap: 8px;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
  
  :deep(.el-form-item__label) {
    font-size: 13px;
  }
}
</style>

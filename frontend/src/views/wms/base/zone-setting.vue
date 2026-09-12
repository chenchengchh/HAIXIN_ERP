<template>
  <WmsPageLayout title="库区管理" class="zone-setting-container">
    <template #actions>
      <el-button type="primary" @click="handleAddZone">
        <el-icon-plus /> 新建库区
      </el-button>
    </template>

    <template #filters>
      <el-form label-width="80px" inline>
        <el-form-item label="关键词">
          <el-input
            placeholder="库区编码/名称"
            v-model="searchKeyword"
            clearable
            @keyup.enter="handleSearch"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select
            v-model="selectedWarehouse"
            placeholder="全部"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="warehouse in warehouses"
              :key="warehouse.value"
              :label="warehouse.label"
              :value="warehouse.value"
            ></el-option>
          </el-select>
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

    <el-card class="zone-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h3>库区列表</h3>
          <span class="list-count">共 {{ total }} 条记录</span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="zones"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        stripe
        empty-text="暂无数据"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="zoneCode" label="库区编码" width="120"></el-table-column>
        <el-table-column prop="zoneName" label="库区名称" width="180"></el-table-column>
        <el-table-column prop="warehouseName" label="所属仓库" width="180"></el-table-column>
        <el-table-column prop="zoneType" label="库区类型" width="120">
          <template #default="scope">
            <el-tag
              :type="getZoneTypeColor(scope.row.zoneType)"
            >
              {{ getZoneTypeName(scope.row.zoneType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200"></el-table-column>
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
              @click="handleEditZone(scope.row)"
              :icon="EditPen"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDeleteZone(scope.row)"
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
    
    <!-- 库区详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleCloseDialog"
    >
      <el-form
        ref="zoneFormRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="所属仓库" prop="warehouseId">
          <el-select
            v-model="formData.warehouseId"
            placeholder="请选择所属仓库"
          >
            <el-option
              v-for="warehouse in warehouses"
              :key="warehouse.value"
              :label="warehouse.label"
              :value="warehouse.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="库区编码" prop="zoneCode">
          <el-input
            v-model="formData.zoneCode"
            placeholder="请输入库区编码"
            :disabled="dialogMode === 'edit'"
          ></el-input>
        </el-form-item>
        <el-form-item label="库区名称" prop="zoneName">
          <el-input
            v-model="formData.zoneName"
            placeholder="请输入库区名称"
          ></el-input>
        </el-form-item>
        <el-form-item label="库区类型" prop="zoneType">
          <el-select
            v-model="formData.zoneType"
            placeholder="请选择库区类型"
          >
            <el-option label="存储区" value="1"></el-option>
            <el-option label="拣货区" value="2"></el-option>
            <el-option label="暂存区" value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            placeholder="请输入库区描述"
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
import { warehouseApi, warehouseAreaApi } from '@/api/wms';
import WmsPageLayout from '../components/WmsPageLayout.vue';

// 状态管理
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('新建库区');
const dialogMode = ref<'add' | 'edit'>('add');
const zoneFormRef = ref<InstanceType<typeof import('element-plus').ElForm>>();

// 搜索和筛选
const searchKeyword = ref('');
const selectedWarehouse = ref('');
const selectedStatus = ref('');

// 分页参数
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 选中的库区
const selectedZones = ref<Zone[]>([]);

// 仓库列表（用于选择）
const warehouses = ref<{ label: string; value: string }[]>([]);

// 库区数据类型
interface Zone {
  id: number;
  zoneCode: string;
  zoneName: string;
  warehouseId: string;
  warehouseName: string;
  zoneType: string;
  description?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

// 库区列表数据
const zones = ref<Zone[]>([]);
const editingId = ref<number | null>(null);

const loadWarehouses = async () => {
  try {
    const res = await warehouseApi.getList({ page: 0, size: 1000 });
    const pageData = res.data || {};
    const list = (pageData.content || pageData.list || pageData.records || []) as any[];
    warehouses.value = list
      .map(w => ({
        label: String(w?.warehouseName ?? ''),
        value: String(w?.warehouseCode ?? '')
      }))
      .filter(v => v.value);
  } catch (e) {
    warehouses.value = [];
  }
};

// 获取库区列表数据
const getZoneList = async () => {
  try {
    loading.value = true;

    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    };
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (selectedWarehouse.value) params.warehouseCode = selectedWarehouse.value;
    if (selectedStatus.value) params.status = selectedStatus.value;

    const res = await warehouseAreaApi.getList(params);
    const pageData = res.data || {};
    zones.value = (pageData.list || []) as any[];
    total.value = Number(pageData.total ?? 0);
  } catch (error) {
    console.error('获取库区列表失败:', error);
    ElMessage.error('获取库区列表失败');
  } finally {
    loading.value = false;
  }
};

// 表单数据
const formData = reactive({
  warehouseId: '',
  zoneCode: '',
  zoneName: '',
  zoneType: '1',
  description: ''
});

// 表单验证规则
const formRules = reactive({
  warehouseId: [
    { required: true, message: '请选择所属仓库', trigger: 'blur' }
  ],
  zoneCode: [
    { required: true, message: '请输入库区编码', trigger: 'blur' },
    { min: 1, max: 10, message: '长度在 1 到 10 个字符', trigger: 'blur' }
  ],
  zoneName: [
    { required: true, message: '请输入库区名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  zoneType: [
    { required: true, message: '请选择库区类型', trigger: 'blur' }
  ]
});

// 处理选中变化
const handleSelectionChange = (selection: Zone[]) => {
  selectedZones.value = selection;
};

// 处理搜索
const handleSearch = () => {
  // 重置分页
  currentPage.value = 1;
  getZoneList();
};

const handleResetSearch = () => {
  searchKeyword.value = '';
  selectedWarehouse.value = '';
  selectedStatus.value = '';
  handleSearch();
};

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  getZoneList();
};

// 处理当前页变化
const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  getZoneList();
};

// 处理添加库区
const handleAddZone = () => {
  dialogMode.value = 'add';
  dialogTitle.value = '新建库区';
  editingId.value = null;
  // 重置表单
  Object.assign(formData, {
    warehouseId: '',
    zoneCode: '',
    zoneName: '',
    zoneType: '1',
    description: ''
  });
  if (zoneFormRef.value) {
    zoneFormRef.value.resetFields();
  }
  dialogVisible.value = true;
};

// 处理编辑库区
const handleEditZone = (zone: Zone) => {
  dialogMode.value = 'edit';
  dialogTitle.value = '编辑库区';
  editingId.value = zone.id;
  // 填充表单数据
  Object.assign(formData, {
    warehouseId: zone.warehouseId,
    zoneCode: zone.zoneCode,
    zoneName: zone.zoneName,
    zoneType: zone.zoneType,
    description: zone.description
  });
  dialogVisible.value = true;
};

// 处理删除库区
const handleDeleteZone = (zone: Zone) => {
  ElMessageBox.confirm(
    `确定要删除库区【${zone.zoneName}】吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      loading.value = true;
      warehouseAreaApi
        .delete(String(zone.id))
        .then(() => {
          ElMessage.success('删除成功');
          getZoneList();
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
const handleStatusChange = (zone: Zone) => {
  const old = zone.status === '1' ? '0' : '1';
  warehouseAreaApi
    .updateStatus(String(zone.id), zone.status)
    .then(() => {
      ElMessage.success(`库区【${zone.zoneName}】状态已更新为${zone.status === '1' ? '启用' : '禁用'}`);
    })
    .catch((e: any) => {
      zone.status = old;
      ElMessage.error(e?.message || '状态更新失败');
    });
};

// 处理表单提交
const handleSubmit = async () => {
  if (!zoneFormRef.value) return;
  
  try {
    await zoneFormRef.value.validate();
    
    loading.value = true;
    const payload: any = {
      warehouseCode: formData.warehouseId,
      zoneCode: formData.zoneCode,
      zoneName: formData.zoneName,
      zoneType: formData.zoneType,
      description: formData.description
    };

    if (dialogMode.value === 'add') {
      await warehouseAreaApi.create(payload);
      ElMessage.success('新建库区成功');
    } else {
      if (editingId.value == null) {
        throw new Error('缺少库区ID');
      }
      await warehouseAreaApi.update(String(editingId.value), payload);
      ElMessage.success('编辑库区成功');
    }

    dialogVisible.value = false;
    await getZoneList();
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
  if (zoneFormRef.value) {
    zoneFormRef.value.resetFields();
  }
};

// 获取库区类型颜色
const getZoneTypeColor = (zoneType: string) => {
  const colorMap: Record<string, string> = {
    '1': 'success',
    '2': 'warning',
    '3': 'info'
  };
  return colorMap[zoneType] || 'default';
};

// 获取库区类型名称
const getZoneTypeName = (zoneType: string) => {
  const nameMap: Record<string, string> = {
    '1': '存储区',
    '2': '拣货区',
    '3': '暂存区'
  };
  return nameMap[zoneType] || '未知';
};

// 组件挂载时初始化数据
onMounted(() => {
  loadWarehouses();
  getZoneList();
});
</script>

<style scoped>
.zone-setting-container {
  padding: 0;
}

.zone-card {
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
  .zone-setting-container {
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

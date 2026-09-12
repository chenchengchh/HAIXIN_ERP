<template>
  <div class="asn-list-container">
    <el-card class="operation-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>ASN列表</span>
          <el-button type="success" @click="handleCreate">新增ASN</el-button>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            placeholder="搜索ASN单号"
            v-model="searchForm.asnNo"
            clearable
          ></el-input>
        </el-col>
        <el-col :span="6">
          <el-select
            placeholder="选择供应商"
            v-model="searchForm.supplierId"
            clearable
          >
            <el-option
              v-for="supplier in suppliers"
              :key="supplier.id"
              :label="supplier.name"
              :value="supplier.id"
            ></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select
            placeholder="选择状态"
            v-model="searchForm.status"
            clearable
          >
            <el-option label="待确认" value="CREATED"></el-option>
            <el-option label="收货中" value="RECEIVING"></el-option>
            <el-option label="部分收货" value="PARTIAL_RECEIVED"></el-option>
            <el-option label="已完成" value="RECEIVED"></el-option>
            <el-option label="已取消" value="CANCELLED"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-col>
        <el-col :span="24" style="margin-top: 10px;">
          <el-row :gutter="10" justify="end">
            <el-col>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </el-col>
            <el-col>
              <el-button @click="handleReset">重置</el-button>
            </el-col>
          </el-row>
        </el-col>
      </el-row>
    </el-card>

    <!-- ASN列表 -->
    <el-card class="list-card" shadow="hover">
      <el-table
        v-loading="loading"
        :data="asnList"
        style="width: 100%"
      >
        <el-table-column prop="asnNo" label="ASN单号" width="180"></el-table-column>
        <el-table-column prop="deliveryNoteNo" label="关联订单" width="150"></el-table-column>
        <el-table-column prop="supplierName" label="供应商" width="150"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag
              :type="{
                'CREATED': 'info',
                'RECEIVING': 'warning',
                'PARTIAL_RECEIVED': 'warning',
                'RECEIVED': 'success',
                'CANCELLED': 'danger'
              }[scope.row.status as string] || 'info'"
            >
              {{ {
                'CREATED': '待确认',
                'RECEIVING': '收货中',
                'PARTIAL_RECEIVED': '部分收货',
                'RECEIVED': '已完成',
                'CANCELLED': '已取消'
              }[scope.row.status as string] || scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expectedArrivalDate" label="预计到货时间" width="180"></el-table-column>
        <el-table-column prop="actualArrivalDate" label="实际到货时间" width="180"></el-table-column>
        <el-table-column prop="totalQty" label="总数量" width="100"></el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleView(scope.row)"
              v-if="scope.row.status !== 'CANCELLED'"
            >
              查看
            </el-button>
            <el-button
              size="small"
              type="success"
              @click="handleConfirm(scope.row)"
              v-if="scope.row.status === 'CREATED'"
            >
              确认到货
            </el-button>
            <el-button
              size="small"
              @click="handleEdit(scope.row)"
              v-if="scope.row.status === 'CREATED'"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row)"
              v-if="scope.row.status === 'CREATED'"
            >
              删除
            </el-button>
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

    <!-- ASN详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :before-close="handleDialogClose"
    >
      <el-form :model="asnForm" label-width="100px">
        <el-divider>基本信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="ASN单号" prop="asnNo">
              <el-input v-model="asnForm.asnNo" :disabled="dialogType !== 'create'" placeholder="请输入ASN单号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联订单" prop="deliveryNoteNo">
              <el-input v-model="asnForm.deliveryNoteNo" placeholder="请输入关联订单号"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="asnForm.supplierId" placeholder="请选择供应商">
                <el-option
                  v-for="supplier in suppliers"
                  :key="supplier.id"
                  :label="supplier.name"
                  :value="supplier.id"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计到货时间" prop="expectedArrivalDate">
              <el-date-picker
                v-model="asnForm.expectedArrivalDate"
                type="datetime"
                placeholder="请选择预计到货时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="实际到货时间" prop="actualArrivalDate">
              <el-date-picker
                v-model="asnForm.actualArrivalDate"
                type="datetime"
                placeholder="请选择实际到货时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>物料明细</el-divider>
        <el-form-item label="物料列表" prop="items">
          <el-table :data="asnForm.items" border style="width: 100%">
            <el-table-column prop="materialCode" label="物料编码" width="150">
              <template #default="scope">
                <el-input v-model="scope.row.materialCode" placeholder="请输入物料编码"></el-input>
              </template>
            </el-table-column>
            <el-table-column prop="materialName" label="物料名称" width="200">
              <template #default="scope">
                <el-input v-model="scope.row.materialName" placeholder="请输入物料名称"></el-input>
              </template>
            </el-table-column>
            <el-table-column prop="expectedQuantity" label="数量" width="100">
              <template #default="scope">
                <el-input-number v-model="scope.row.expectedQuantity" :min="1" :step="1" placeholder="请输入数量"></el-input-number>
              </template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="80">
              <template #default="scope">
                <el-input v-model="scope.row.unit" placeholder="请输入单位"></el-input>
              </template>
            </el-table-column>
            <el-table-column prop="batchNo" label="批次号" width="150">
              <template #default="scope">
                <el-input v-model="scope.row.batchNo" placeholder="请输入批次号"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="scope">
                <el-button
                  type="danger"
                  size="small"
                  @click="handleDeleteItem(scope.$index)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="add-item-btn">
            <el-button type="primary" @click="handleAddItem">添加物料</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { unwrapPageResponse } from '@/api';
import { asnApi } from '@/api/wms';
import { supplierApi } from '@/api/srm';

const route = useRoute();

// 搜索表单
const searchForm = reactive({
  asnNo: '',
  supplierId: '',
  status: '',
  dateRange: [] as string[]
});

// 供应商数据
const suppliers = ref<any[]>([]);

// ASN数据
interface AsnItem {
  materialCode: string;
  materialName: string;
  expectedQuantity: number;
  unit: string;
  batchNo: string;
}

interface Asn {
  id: number;
  asnNo: string;
  deliveryNoteNo: string;
  supplierId: number;
  supplierCode: string;
  supplierName: string;
  status: string;
  expectedArrivalDate: string;
  actualArrivalDate: string;
  totalQty: number;
  createdTime: string;
  items: AsnItem[];
}

const asnList = ref<Asn[]>([]);

// 分页数据
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(false);

// 对话框数据
const dialogVisible = ref(false);
const dialogType = ref<'create' | 'edit' | 'view'>('create');
const dialogTitle = computed(() => {
  if (dialogType.value === 'create') return '新增ASN';
  if (dialogType.value === 'edit') return '编辑ASN';
  return '查看ASN';
});

// ASN表单数据
const asnForm = reactive<Asn>({
  id: 0,
  asnNo: '',
  deliveryNoteNo: '',
  supplierId: undefined as any,
  supplierCode: '',
  supplierName: '',
  status: 'CREATED',
  expectedArrivalDate: '',
  actualArrivalDate: '',
  totalQty: 0,
  createdTime: '',
  items: [{
    materialCode: '',
    materialName: '',
    expectedQuantity: 1,
    unit: '',
    batchNo: ''
  }]
});

// 获取供应商列表
const fetchSuppliers = async () => {
  try {
    const res = await supplierApi.getSupplierList({ page: 0, size: 1000 });
    suppliers.value = res.data.list.map((item: any) => ({
      id: item.id,
      code: item.supplierCode || item.code || '',
      name: item.supplierName
    }));
  } catch (error) {
    console.error('获取供应商列表失败', error);
  }
};

// 获取ASN列表
const fetchAsnList = async () => {
  loading.value = true;
  try {
    const params = {
      // 后端统一1基页码
      page: Math.max(currentPage.value, 1),
      size: pageSize.value,
      asnNo: searchForm.asnNo,
      // 后端按供应商名称模糊筛选，根据选中的供应商id取名称
      supplierName: suppliers.value.find(s => String(s.id) === String(searchForm.supplierId))?.name || undefined,
      status: searchForm.status,
      startTime: searchForm.dateRange?.[0] ? searchForm.dateRange[0] + ' 00:00:00' : undefined,
      endTime: searchForm.dateRange?.[1] ? searchForm.dateRange[1] + ' 23:59:59' : undefined
    };
    const res = await asnApi.getList(params);
    const page = unwrapPageResponse(res);
    // 后端未返回totalQty字段，根据明细行预计数量汇总
    asnList.value = (page.list || []).map((asn: any) => ({
      ...asn,
      totalQty: Array.isArray(asn?.items)
        ? asn.items.reduce((sum: number, it: any) => sum + Number(it?.expectedQuantity ?? it?.quantity ?? 0), 0)
        : 0
    }));
    total.value = page.total;
  } catch (error) {
    console.error('获取ASN列表失败', error);
    ElMessage.error('获取ASN列表失败');
  } finally {
    loading.value = false;
  }
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchAsnList();
};

// 处理重置
const handleReset = () => {
  searchForm.asnNo = '';
  searchForm.supplierId = '';
  searchForm.status = '';
  searchForm.dateRange = [];
  handleSearch();
};

// 处理新增ASN
const handleCreate = () => {
  dialogType.value = 'create';
  resetAsnForm();
  dialogVisible.value = true;
};

// 将后端ASN详情填充到表单（字段名已对齐，仅需补默认供应商选中与明细默认值）
const fillAsnForm = (data: any) => {
  resetAsnForm();
  if (!data) return;
  asnForm.id = data.id ?? 0;
  asnForm.asnNo = data.asnNo ?? '';
  asnForm.deliveryNoteNo = data.deliveryNoteNo ?? '';
  asnForm.supplierCode = data.supplierCode ?? '';
  asnForm.supplierName = data.supplierName ?? '';
  // 根据供应商编码或名称回显下拉选中项
  const matched = suppliers.value.find(s => s.code === asnForm.supplierCode || s.name === asnForm.supplierName);
  asnForm.supplierId = matched ? matched.id : undefined as any;
  asnForm.status = data.status ?? 'CREATED';
  asnForm.expectedArrivalDate = data.expectedArrivalDate ?? '';
  asnForm.actualArrivalDate = data.actualArrivalDate ?? '';
  asnForm.items = Array.isArray(data.items) && data.items.length > 0
    ? data.items.map((it: any) => ({
        materialCode: it.materialCode ?? '',
        materialName: it.materialName ?? '',
        expectedQuantity: Number(it.expectedQuantity ?? it.quantity ?? 1),
        unit: it.unit ?? '',
        batchNo: it.batchNo ?? ''
      }))
    : [{ materialCode: '', materialName: '', expectedQuantity: 1, unit: '', batchNo: '' }];
};

// 处理查看ASN
const handleView = async (row: Asn) => {
  dialogType.value = 'view';
  try {
    const res = await asnApi.getDetail(row.id);
    fillAsnForm(res.data);
    dialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取ASN详情失败');
  }
};

// 处理编辑ASN
const handleEdit = async (row: Asn) => {
  dialogType.value = 'edit';
  try {
    const res = await asnApi.getDetail(row.id);
    fillAsnForm(res.data);
    dialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取ASN详情失败');
  }
};

// 处理删除ASN
const handleDelete = (row: Asn) => {
  ElMessageBox.confirm('确定要删除该ASN吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await asnApi.delete(row.id);
      ElMessage.success('删除成功');
      fetchAsnList();
    } catch (error) {
      ElMessage.error('删除失败');
    }
  });
};

// 处理确认到货
const handleConfirm = (row: Asn) => {
  ElMessageBox.confirm(`确定要确认到货单号 ${row.asnNo} 吗？`, '确认收货', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await asnApi.confirmArrival(row.id);
      ElMessage.success('确认到货成功');
      fetchAsnList();
    } catch (error) {
      ElMessage.error('确认到货失败');
    }
  });
};

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchAsnList();
};

// 处理当前页变化
const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  fetchAsnList();
};

// 处理对话框关闭
const handleDialogClose = () => {
  dialogVisible.value = false;
};

// 重置ASN表单
const resetAsnForm = () => {
  asnForm.id = 0;
  asnForm.asnNo = '';
  asnForm.deliveryNoteNo = '';
  asnForm.supplierId = undefined as any;
  asnForm.supplierCode = '';
  asnForm.supplierName = '';
  asnForm.status = 'CREATED';
  asnForm.expectedArrivalDate = '';
  asnForm.actualArrivalDate = '';
  asnForm.totalQty = 0;
  asnForm.createdTime = '';
  asnForm.items = [{
    materialCode: '',
    materialName: '',
    expectedQuantity: 1,
    unit: '',
    batchNo: ''
  }];
};

// 添加物料
const handleAddItem = () => {
  asnForm.items.push({
    materialCode: '',
    materialName: '',
    expectedQuantity: 1,
    unit: '',
    batchNo: ''
  });
};

// 删除物料
const handleDeleteItem = (index: number) => {
  if (asnForm.items.length <= 1) {
    return;
  }
  asnForm.items.splice(index, 1);
};

// 保存ASN
const handleSave = async () => {
  // 查找供应商编码与名称（后端实体存储supplierCode/supplierName）
  const supplier = suppliers.value.find(s => s.id === asnForm.supplierId);
  if (supplier) {
    asnForm.supplierCode = supplier.code;
    asnForm.supplierName = supplier.name;
  }

  // 仅提交后端实体支持的字段
  const payload = {
    asnNo: asnForm.asnNo,
    deliveryNoteNo: asnForm.deliveryNoteNo,
    supplierCode: asnForm.supplierCode,
    supplierName: asnForm.supplierName,
    expectedArrivalDate: asnForm.expectedArrivalDate || null,
    actualArrivalDate: asnForm.actualArrivalDate || null,
    items: asnForm.items.map(it => ({
      materialCode: it.materialCode,
      materialName: it.materialName,
      expectedQuantity: it.expectedQuantity,
      unit: it.unit,
      batchNo: it.batchNo
    }))
  };

  try {
    if (dialogType.value === 'create') {
      await asnApi.create(payload);
      ElMessage.success('创建成功');
    } else if (dialogType.value === 'edit') {
      await asnApi.update(asnForm.id, payload);
      ElMessage.success('更新成功');
    }
    dialogVisible.value = false;
    fetchAsnList();
  } catch (error) {
    ElMessage.error('保存失败');
  }
};

// 组件挂载时初始化数据
onMounted(() => {
  fetchSuppliers();
  fetchAsnList();
  // 支持从收货作业页跳转携带 action=create 直接打开创建对话框
  if (route.query.action === 'create') {
    handleCreate();
  } else if (route.query.action === 'edit' && route.query.id) {
    // 支持跳转携带 action=edit&id=xxx 直接打开编辑对话框
    handleEdit({ id: Number(route.query.id) } as any);
  }
});

defineExpose({
  openCreate: handleCreate
});
</script>

<style scoped>
.asn-list-container {
  padding: 0;
}

.operation-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.add-item-btn {
  margin-top: 10px;
  text-align: right;
}
</style>

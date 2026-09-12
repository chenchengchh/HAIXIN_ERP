<template>
  <div class="receive-job-page">
    <el-card class="operation-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>查询条件</span>
          <div class="header-actions">
            <el-button type="primary" @click="createReceiveJob">创建收货作业</el-button>
            <el-button @click="refreshList">刷新列表</el-button>
          </div>
        </div>
      </template>

      <el-form :model="searchForm" label-width="80px" inline>
        <el-form-item label="作业单号">
          <el-input v-model="searchForm.jobNo" placeholder="输入作业单号" clearable></el-input>
        </el-form-item>
        <el-form-item label="ASN单号">
          <el-input v-model="searchForm.asnNo" placeholder="输入ASN单号" clearable></el-input>
        </el-form-item>
        <el-form-item label="PO单号">
          <el-input v-model="searchForm.poNo" placeholder="输入PO单号" clearable></el-input>
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="searchForm.supplierId" placeholder="选择供应商" clearable>
            <el-option
              v-for="supplier in suppliers"
              :key="supplier.id"
              :label="supplier.name"
              :value="supplier.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouseId" placeholder="选择仓库" clearable>
            <el-option
              v-for="warehouse in warehouses"
              :key="warehouse.id"
              :label="warehouse.name"
              :value="warehouse.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="待收货" value="pending"></el-option>
            <el-option label="收货中" value="receiving"></el-option>
            <el-option label="待质检" value="waiting_inspection"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.createTimeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            clearable
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchReceiveJobs">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 列表区域 -->
    <div class="list-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <h3>收货作业列表</h3>
            <span class="list-count">共 {{ receiveJobs.length }} 条记录</span>
          </div>
        </template>
        
        <el-table :data="receiveJobs" stripe style="width: 100%" v-loading="loading">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="jobNo" label="作业单号" width="180" sortable></el-table-column>
          <el-table-column prop="asnNo" label="ASN单号" width="150" sortable></el-table-column>
          <el-table-column prop="poNo" label="PO单号" width="150" sortable></el-table-column>
          <el-table-column prop="supplierName" label="供应商" width="180"></el-table-column>
          <el-table-column prop="warehouseName" label="仓库" width="120"></el-table-column>
          <el-table-column prop="totalQuantity" label="总数量" width="100" sortable></el-table-column>
          <el-table-column prop="receivedQuantity" label="已收数量" width="100" sortable></el-table-column>
          <el-table-column prop="status" label="状态" width="120" sortable>
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="160" sortable></el-table-column>
          <el-table-column prop="createdBy" label="创建人" width="120"></el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewReceiveJobDetail(scope.row)">
                详情
              </el-button>
              <el-button size="small" @click="startReceiveJob(scope.row)" :disabled="scope.row.status !== 'pending'">
                开始收货
              </el-button>
              <el-dropdown>
                <el-button size="small">
                  更多 <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="editReceiveJob(scope.row)" :disabled="scope.row.status !== 'pending'">编辑</el-dropdown-item>
                    <el-dropdown-item @click="cancelReceiveJob(scope.row)" :disabled="scope.row.status === 'completed' || scope.row.status === 'cancelled'">取消</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </el-card>
    </div>
    
    <!-- 收货作业详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentReceiveJob.jobNo ? '收货作业详情' : '创建收货作业'"
      width="900px"
      :before-close="handleDialogClose"
    >
      <div v-if="detailDialogVisible" class="detail-dialog-content">
        <!-- 基本信息 -->
        <el-card class="detail-card">
          <template #header>
            <h4>基本信息</h4>
          </template>
          <el-form :model="currentReceiveJob" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="作业单号">
                  <el-input v-model="currentReceiveJob.jobNo" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="ASN单号">
                  <el-input v-model="currentReceiveJob.asnNo" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="PO单号">
                  <el-input v-model="currentReceiveJob.poNo" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="供应商">
                  <el-input v-model="currentReceiveJob.supplierName" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="仓库">
                  <el-input v-model="currentReceiveJob.warehouseName" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态">
                  <el-tag :type="getStatusTagType(currentReceiveJob.status)">
                    {{ getStatusText(currentReceiveJob.status) }}
                  </el-tag>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
        
        <!-- 收货明细 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <h4>收货明细</h4>
              <div class="detail-actions">
                <el-button type="primary" size="small" @click="startScanning" v-if="currentReceiveJob.status === 'receiving'">
                  开始扫描
                </el-button>
              </div>
            </div>
          </template>
          
          <el-table :data="receiveJobItems" border style="width: 100%">
            <el-table-column prop="materialCode" label="物料代码" width="120"></el-table-column>
            <el-table-column prop="materialName" label="物料名称" width="180"></el-table-column>
            <el-table-column prop="specification" label="规格" width="150"></el-table-column>
            <el-table-column prop="unit" label="单位" width="80"></el-table-column>
            <el-table-column prop="expectedQuantity" label="预计数量" width="100"></el-table-column>
            <el-table-column prop="receivedQuantity" label="已收数量" width="100"></el-table-column>
            <el-table-column prop="inspectionStatus" label="质检状态" width="120">
              <template #default="scope">
                <el-tag :type="getInspectionStatusTagType(scope.row.inspectionStatus)">
                  {{ getInspectionStatusText(scope.row.inspectionStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="storageLocation" label="存储库位" width="120"></el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button size="small" @click="viewInspectionResult(scope.row)" :disabled="!scope.row.inspectionStatus">
                  质检结果
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
        
        <!-- 扫描区域 -->
        <el-card class="detail-card" v-if="showScanArea">
          <template #header>
            <h4>扫码收货</h4>
          </template>
          
          <div class="scan-section">
            <div class="scan-input-area">
              <el-input
                v-model="scanCode"
                placeholder="请扫描物料或托盘条码"
                @input="handleScanInput"
                autofocus
                clearable
              >
                <template #prepend>
                  扫描条码
                </template>
              </el-input>
            </div>
            
            <div class="scan-result-area" v-if="scanResults.length > 0">
              <h5>扫描记录</h5>
              <el-table :data="scanResults" style="width: 100%" max-height="200">
                <el-table-column prop="scanTime" label="扫描时间" width="180"></el-table-column>
                <el-table-column prop="barcode" label="条码" width="180"></el-table-column>
                <el-table-column prop="materialInfo" label="物料信息"></el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">
                      {{ scope.row.status === 'success' ? '成功' : '失败' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-card>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="completeReceiveJob" v-if="currentReceiveJob.status === 'receiving'">
            完成收货
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 开始收货对话框 -->
    <el-dialog
      v-model="startJobDialogVisible"
      title="开始收货"
      width="600px"
    >
      <div v-if="selectedReceiveJob">
        <el-form :model="startJobInfo" label-width="120px">
          <el-form-item label="作业单号">
            <el-input v-model="selectedReceiveJob.jobNo" disabled></el-input>
          </el-form-item>
          <el-form-item label="ASN单号">
            <el-input v-model="selectedReceiveJob.asnNo" disabled></el-input>
          </el-form-item>
          <el-form-item label="开始时间">
            <el-date-picker
              v-model="startJobInfo.startTime"
              type="datetime"
              placeholder="选择开始时间"
              style="width: 100%"
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="备注">
            <el-input
              v-model="startJobInfo.remark"
              type="textarea"
              :rows="3"
              placeholder="输入备注信息"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="startJobDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStartJob">开始收货</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ArrowDown } from '@element-plus/icons-vue';
import { useWmsInboundStore } from '@/stores/wms/inbound';
import { useRouter } from 'vue-router';
import { unwrapPageResponse } from '@/api';
import { asnApi, warehouseApi } from '@/api/wms';
import { supplierApi } from '@/api/srm';
import { ElMessage, ElMessageBox } from 'element-plus';
// 移除未定义的类型导入

const inboundStore = useWmsInboundStore();
const router = useRouter();

// 定义类型
interface ReceiveJob {
  id: string;
  jobNo: string;
  asnNo: string;
  poNo: string;
  supplierId: string;
  supplierName: string;
  warehouseId: string;
  warehouseName: string;
  status: string;
  totalQuantity: number;
  receivedQuantity: number;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
}

interface ReceiveJobItem {
  id: string;
  jobId: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  specification: string;
  unit: string;
  expectedQuantity: number;
  receivedQuantity: number;
  inspectionStatus: string;
  storageLocation: string;
  createdAt: string;
  updatedAt: string;
}

interface ScanResult {
  scanTime: string;
  barcode: string;
  materialInfo: string;
  status: string;
}

// 页面状态
const loading = ref(false);
const receiveJobs = ref<ReceiveJob[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(20);

// 搜索表单
const searchForm = reactive({
  jobNo: '',
  asnNo: '',
  poNo: '',
  supplierId: '',
  warehouseId: '',
  status: '',
  createTimeRange: [] as string[]
});

// 供应商和仓库数据
const suppliers = ref([
  { id: '1', name: '供应商A' },
  { id: '2', name: '供应商B' },
  { id: '3', name: '供应商C' }
]);

const warehouses = ref([
  { id: '1', name: '仓库A' },
  { id: '2', name: '仓库B' },
  { id: '3', name: '仓库C' }
]);

// 对话框状态
const detailDialogVisible = ref(false);
const currentReceiveJob = reactive({
  id: '',
  jobNo: '',
  asnNo: '',
  poNo: '',
  supplierId: '',
  supplierName: '',
  warehouseId: '',
  warehouseName: '',
  status: 'pending',
  totalQuantity: 0,
  receivedQuantity: 0,
  createdAt: '',
  createdBy: '',
  updatedAt: '',
  updatedBy: ''
});

const receiveJobItems = ref<ReceiveJobItem[]>([]);

// 扫描区域状态
const showScanArea = ref(false);
const scanCode = ref('');
const scanResults = ref<ScanResult[]>([]);

// 开始收货对话框
const startJobDialogVisible = ref(false);
const selectedReceiveJob = ref<ReceiveJob | null>(null);
const startJobInfo = reactive({
  startTime: new Date().toISOString().slice(0, 16),
  remark: ''
});

// 状态映射
const statusMap = {
  pending: '待收货',
  receiving: '收货中',
  waiting_inspection: '待质检',
  completed: '已完成',
  cancelled: '已取消'
};

const statusTagTypeMap = {
  pending: 'info',
  receiving: 'warning',
  waiting_inspection: 'primary',
  completed: 'success',
  cancelled: 'danger'
};

const inspectionStatusMap = {
  '': '未质检',
  pending: '待质检',
  passed: '质检通过',
  failed: '质检失败',
  waived: '免检'
};

const inspectionStatusTagTypeMap = {
  '': 'info',
  pending: 'warning',
  passed: 'success',
  failed: 'danger',
  waived: 'primary'
};

// 方法
const getStatusText = (status: string) => {
  return statusMap[status as keyof typeof statusMap] || status;
};

const getStatusTagType = (status: string) => {
  return statusTagTypeMap[status as keyof typeof statusTagTypeMap] || 'info';
};

const getInspectionStatusText = (status: string) => {
  return inspectionStatusMap[status as keyof typeof inspectionStatusMap] || status;
};

const getInspectionStatusTagType = (status: string) => {
  return inspectionStatusTagTypeMap[status as keyof typeof inspectionStatusTagTypeMap] || 'info';
};

const fetchReceiveJobs = async () => {
  loading.value = true;
  try {
    const params = {
      // 后端统一1基页码
      page: Math.max(currentPage.value, 1),
      size: pageSize.value
    } as any;
    // 作业单号即ASN单号，二者任一输入均按ASN单号模糊筛选
    if (searchForm.jobNo) params.asnNo = searchForm.jobNo;
    if (searchForm.asnNo) params.asnNo = searchForm.asnNo;
    // PO单号对应ASN的送货单号
    if (searchForm.poNo) params.deliveryNoteNo = searchForm.poNo;
    // 供应商下拉值为id，后端按名称模糊筛选
    if (searchForm.supplierId) {
      const sup = suppliers.value.find(s => String(s.id) === String(searchForm.supplierId));
      if (sup?.name) params.supplierName = sup.name;
    }
    // 仓库下拉值为warehouseCode
    if (searchForm.warehouseId) params.warehouseCode = searchForm.warehouseId;
    // 收货作业UI状态映射为后端ASN状态枚举（可多值，逗号分隔）
    if (searchForm.status) {
      const statusMap: Record<string, string> = {
        pending: 'CREATED',
        receiving: 'RECEIVING,PARTIAL_RECEIVED',
        waiting_inspection: 'PARTIAL_RECEIVED',
        completed: 'RECEIVED',
        cancelled: 'CANCELLED'
      };
      params.status = statusMap[searchForm.status] || searchForm.status;
    }
    if (searchForm.createTimeRange?.[0]) params.startTime = searchForm.createTimeRange[0] + ' 00:00:00';
    if (searchForm.createTimeRange?.[1]) params.endTime = searchForm.createTimeRange[1] + ' 23:59:59';
    const res = await asnApi.getList(params);
    const page = unwrapPageResponse(res);
    const content = page.list;
    receiveJobs.value = content.map((asn: any) => {
      const items: any[] = Array.isArray(asn?.items) ? asn.items : [];
      const totalQty = items.reduce((sum, it) => sum + Number(it?.expectedQuantity ?? it?.quantity ?? 0), 0);
      const receivedQty = items.reduce((sum, it) => sum + Number(it?.receivedQuantity ?? 0), 0);
      const status = String(asn?.status || '').toUpperCase();
      const uiStatus =
        status === 'CREATED' ? 'pending'
          : status === 'RECEIVING' || status === 'PARTIAL_RECEIVED' ? 'receiving'
            : status === 'RECEIVED' ? 'completed'
              : status === 'CANCELLED' ? 'cancelled'
                : 'pending';
      return {
        id: String(asn?.id ?? ''),
        jobNo: String(asn?.asnNo ?? ''),
        asnNo: String(asn?.asnNo ?? ''),
        poNo: String(asn?.deliveryNoteNo ?? ''),
        supplierId: '',
        supplierName: String(asn?.supplierName ?? ''),
        warehouseId: String(asn?.warehouseId ?? ''),
        warehouseName: String(asn?.warehouseName ?? ''),
        status: uiStatus,
        totalQuantity: totalQty,
        receivedQuantity: receivedQty,
        createdAt: String(asn?.createdTime ?? ''),
        createdBy: 'system',
        updatedAt: String(asn?.updatedTime ?? ''),
        updatedBy: 'system'
      } as ReceiveJob;
    });
    total.value = page.total || receiveJobs.value.length;
  } catch (error) {
    console.error('获取收货作业列表失败:', error);
    ElMessage.error('获取收货作业列表失败');
  } finally {
    loading.value = false;
  }
};

const searchReceiveJobs = () => {
  currentPage.value = 1;
  fetchReceiveJobs();
};

const resetSearch = () => {
  Object.assign(searchForm, {
    jobNo: '',
    asnNo: '',
    poNo: '',
    supplierId: '',
    warehouseId: '',
    status: '',
    createTimeRange: []
  });
  fetchReceiveJobs();
};

const refreshList = () => {
  fetchReceiveJobs();
};

const createReceiveJob = () => {
  router.push({
    name: 'wms-inbound',
    query: { tab: 'asn-list', action: 'create' }
  });
};

const viewReceiveJobDetail = (job: ReceiveJob) => {
  loadReceiveJobDetail(job.id);
};

const editReceiveJob = (job: ReceiveJob) => {
  // 跳转至入库管理ASN列表并打开对应ASN的编辑对话框
  router.push({
    name: 'wms-inbound',
    query: { tab: 'asn-list', action: 'edit', id: job.id }
  });
};

const startReceiveJob = (job: ReceiveJob) => {
  selectedReceiveJob.value = job;
  startJobInfo.startTime = new Date().toISOString().slice(0, 16);
  startJobInfo.remark = '';
  startJobDialogVisible.value = true;
};

const submitStartJob = () => {
  if (!selectedReceiveJob.value) {
    return;
  }
  asnApi.start(selectedReceiveJob.value.id).then(() => {
    ElMessage.success('开始收货成功');
    startJobDialogVisible.value = false;
    fetchReceiveJobs();
  }).catch(() => {
    ElMessage.error('开始收货失败');
  });
};

const cancelReceiveJob = (job: ReceiveJob) => {
  ElMessageBox.confirm('确定要取消该收货作业吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await asnApi.cancel(job.id);
      ElMessage.success('已取消');
      fetchReceiveJobs();
    } catch {
      ElMessage.error('取消失败');
    }
  });
};

const handleScanInput = (value: string) => {
  if (value) {
    const asnId = currentReceiveJob.id;
    if (!asnId) {
      return;
    }
    asnApi.scan(asnId, value).then((res: any) => {
      // api包装层已解包，res.data即为ASN payload
      const payload = res?.data ?? {};
      const items: any[] = Array.isArray(payload?.items) ? payload.items : [];
      receiveJobItems.value = items.map((it: any) => ({
        id: String(it?.id ?? ''),
        jobId: String(payload?.id ?? ''),
        materialId: String(it?.materialCode ?? ''),
        materialCode: String(it?.materialCode ?? ''),
        materialName: String(it?.materialName ?? ''),
        specification: '',
        unit: String(it?.unit ?? ''),
        expectedQuantity: Number(it?.expectedQuantity ?? 0),
        receivedQuantity: Number(it?.receivedQuantity ?? 0),
        inspectionStatus: '',
        storageLocation: '',
        createdAt: '',
        updatedAt: ''
      }));
      scanResults.value.unshift({
        scanTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
        barcode: value,
        materialInfo: `物料 - ${value}`,
        status: 'success'
      });
      scanCode.value = '';
    }).catch(() => {
      scanResults.value.unshift({
        scanTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
        barcode: value,
        materialInfo: `物料 - ${value}`,
        status: 'fail'
      });
      scanCode.value = '';
    });
  }
};
// 处理开始扫描
const startScanning = () => {
  // 显示扫描区域
  showScanArea.value = true;
  // 清空之前的扫描结果
  scanResults.value = [];
  console.log('开始扫描');
};

// 处理完成收货
const completeReceiveJob = () => {
  const asnId = currentReceiveJob.id;
  if (!asnId) {
    return;
  }
  asnApi.complete(asnId).then(() => {
    ElMessage.success('收货完成');
    detailDialogVisible.value = false;
    fetchReceiveJobs();
  }).catch(() => {
    ElMessage.error('收货完成失败');
  });
};

const viewInspectionResult = (item: ReceiveJobItem) => {
  // 这里应该跳转到质检结果页面或打开质检结果对话框
  console.log('查看质检结果:', item);
};

const handleDialogClose = () => {
  // 对话框关闭前的处理
  console.log('对话框关闭');
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchReceiveJobs();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchReceiveJobs();
};

// 生命周期
onMounted(() => {
  supplierApi.getSupplierList({ page: 0, size: 1000 }).then(res => {
    const list = Array.isArray(res.data?.list) ? res.data.list : [];
    suppliers.value = list.map((item: any) => ({
      id: String(item.id ?? ''),
      name: String(item.supplierName ?? '')
    }));
  }).catch(() => {});

  warehouseApi.getList({ page: 0, size: 1000 }).then((res: any) => {
    const list = unwrapPageResponse(res).list;
    // 下拉值使用warehouseCode，与后端ASN筛选参数一致
    warehouses.value = list.map((w: any) => ({
      id: String(w.warehouseCode ?? w.id ?? ''),
      name: String(w.warehouseName ?? w.warehouseCode ?? '')
    }));
  }).catch(() => {});

  fetchReceiveJobs();
});

const loadReceiveJobDetail = async (id: string) => {
  try {
    const res = await asnApi.getDetail(id);
    // api包装层已解包，res.data即为ASN payload
    const payload = res?.data ?? {};
    const status = String(payload?.status || '').toUpperCase();
    const uiStatus =
      status === 'CREATED' ? 'pending'
        : status === 'RECEIVING' || status === 'PARTIAL_RECEIVED' ? 'receiving'
          : status === 'RECEIVED' ? 'completed'
            : status === 'CANCELLED' ? 'cancelled'
              : 'pending';
    Object.assign(currentReceiveJob, {
      id: String(payload?.id ?? ''),
      jobNo: String(payload?.asnNo ?? ''),
      asnNo: String(payload?.asnNo ?? ''),
      poNo: String(payload?.deliveryNoteNo ?? ''),
      supplierId: '',
      supplierName: String(payload?.supplierName ?? ''),
      warehouseId: String(payload?.warehouseId ?? ''),
      warehouseName: String(payload?.warehouseName ?? ''),
      status: uiStatus,
      totalQuantity: 0,
      receivedQuantity: 0,
      createdAt: String(payload?.createdTime ?? ''),
      createdBy: 'system',
      updatedAt: String(payload?.updatedTime ?? ''),
      updatedBy: 'system'
    });
    const items: any[] = Array.isArray(payload?.items) ? payload.items : [];
    receiveJobItems.value = items.map((it: any) => ({
      id: String(it?.id ?? ''),
      jobId: String(payload?.id ?? ''),
      materialId: String(it?.materialCode ?? ''),
      materialCode: String(it?.materialCode ?? ''),
      materialName: String(it?.materialName ?? ''),
      specification: '',
      unit: String(it?.unit ?? ''),
      expectedQuantity: Number(it?.expectedQuantity ?? 0),
      receivedQuantity: Number(it?.receivedQuantity ?? 0),
      inspectionStatus: '',
      storageLocation: '',
      createdAt: '',
      updatedAt: ''
    }));
    showScanArea.value = uiStatus === 'receiving';
    scanResults.value = [];
    detailDialogVisible.value = true;
  } catch {
    ElMessage.error('获取收货作业详情失败');
  }
};
</script>

<style scoped>
.receive-job-page {
  padding: 0;
}

.operation-card {
  margin-bottom: 16px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.list-section {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.card-header h4 {
  margin: 0;
  font-size: 14px;
  color: #303133;
}

.list-count {
  font-size: 14px;
  color: #606266;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-dialog-content {
  max-height: 600px;
  overflow-y: auto;
}

.detail-card {
  margin-bottom: 16px;
}

.detail-actions {
  display: flex;
  gap: 8px;
}

.scan-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.scan-input-area {
  display: flex;
  justify-content: center;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
}

.scan-input-area .el-input {
  width: 500px;
}

.scan-result-area {
  max-height: 300px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.scan-result-area h5 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #303133;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>

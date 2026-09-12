<template>
  <div class="data-collection-view">
    <div class="page-header">
      <h2>数据采集</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/scada">SCADA系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/scada/data-collection">数据采集</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/scada/data-collection#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-tabs v-model="activeTab" class="function-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="实时数据采集" name="real-time" />
      <el-tab-pane label="多协议支持" name="protocols" />
      <el-tab-pane label="数据预处理" name="preprocessing" />
      <el-tab-pane label="数据存储" name="storage" />
    </el-tabs>
    
    <!-- 实时数据采集 -->
    <div v-if="activeTab === 'real-time'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>实时数据采集</span>
            <div>
              <el-button type="success" size="small" style="margin-right: 8px" @click="visionVisible = true">仪表拍照抄表</el-button>
              <el-button type="primary" size="small" @click="handleAddPoint">添加采集点</el-button>
            </div>
          </div>
        </template>
        <div class="sub-card-content">
          <el-table :data="collectionPoints" style="width: 100%" height="300">
            <el-table-column prop="id" label="采集点ID" width="100" />
            <el-table-column prop="tagCode" label="点位编码" width="150" />
            <el-table-column prop="deviceName" label="设备名称" width="120" />
            <el-table-column prop="protocol" label="协议类型" width="100">
              <template #default="scope">
                <el-tag>{{ scope.row.protocol }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="value" label="当前值" width="100" />
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'online' ? 'success' : 'danger'">
                  {{ scope.row.status === 'online' ? '在线' : '离线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleEditPoint(scope.row)">编辑</el-button>
                <el-button size="small" @click="handleDeletePoint(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
    
    <!-- 添加/编辑采集点对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑采集点' : '添加采集点'"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form ref="pointFormRef" :model="formData" label-width="100px" class="point-form">
        <el-form-item label="点位编码" prop="tagCode" :rules="[{ required: true, message: '请输入点位编码', trigger: 'blur' }]">
          <el-input v-model="formData.tagCode" placeholder="请输入点位编码" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName" :rules="[{ required: true, message: '请输入设备名称', trigger: 'blur' }]">
          <el-input v-model="formData.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="协议类型" prop="protocol" :rules="[{ required: true, message: '请选择协议类型', trigger: 'change' }]">
          <el-select v-model="formData.protocol" placeholder="请选择协议类型">
            <el-option label="Modbus" value="Modbus" />
            <el-option label="OPC UA" value="OPC UA" />
            <el-option label="MQTT" value="MQTT" />
            <el-option label="S7" value="S7" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位" prop="unit" :rules="[{ required: true, message: '请输入单位', trigger: 'blur' }]">
          <el-input v-model="formData.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="初始值" prop="value">
          <el-input-number v-model="formData.value" :min="0" :step="0.1" placeholder="请输入初始值" />
        </el-form-item>
        <el-form-item label="状态" prop="status" :rules="[{ required: true, message: '请选择状态', trigger: 'change' }]">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSavePoint" :loading="saveLoading">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- S11 仪表视觉识别对话框（拍照/粘贴文本 → 规则结构化 → 预填抄表表单） -->
    <DocVisionDialog
      v-model="visionVisible"
      doc-type="gauge"
      title="仪表拍照抄表"
      @confirm="handleVisionConfirm"
    />

    <!-- 抄表确认对话框（CONFIRM 级：人工核对后落库，quality=manual） -->
    <el-dialog v-model="manualReadVisible" title="人工抄表确认" width="480px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="采集点位" required>
          <el-select v-model="manualReadForm.tagCode" placeholder="选择点位" style="width: 100%" filterable>
            <el-option
              v-for="p in collectionPoints"
              :key="p.tagCode"
              :label="`${p.tagCode}（${p.deviceName || ''}）`"
              :value="p.tagCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="仪表读数" required>
          <el-input-number v-model="manualReadForm.value" :precision="3" style="width: 100%" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="manualReadForm.unit" placeholder="如 °C / bar" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="manualReadVisible = false">取消</el-button>
          <el-button type="primary" :loading="manualReadSaving" @click="handleManualReadSave">确认抄表</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 多协议支持 -->
    <div v-if="activeTab === 'protocols'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>多协议配置</span>
            <el-button type="primary" size="small" @click="handleAddProtocol">添加协议</el-button>
          </div>
        </template>
        <div class="sub-card-content">
          <el-table :data="protocols" style="width: 100%" height="300">
            <el-table-column prop="id" label="协议ID" width="100" />
            <el-table-column prop="name" label="协议名称" width="120" />
            <el-table-column prop="type" label="协议类型" width="100">
              <template #default="scope">
                <el-tag :type="getProtocolTypeColor(scope.row.type)">{{ scope.row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="deviceCount" label="设备数量" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'running' ? 'success' : 'danger'">
                  {{ scope.row.status === 'running' ? '运行中' : '已停止' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleConfigProtocol(scope.row)">配置</el-button>
                <el-button size="small" @click="handleToggleProtocol(scope.row)">{{ scope.row.status === 'running' ? '停止' : '启动' }}</el-button>
                <el-button size="small" type="danger" @click="handleDeleteProtocol(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
    
    <!-- 添加/编辑协议对话框 -->
    <el-dialog
      v-model="protocolDialogVisible"
      :title="isEditProtocol ? '编辑协议' : '添加协议'"
      width="500px"
      @close="handleProtocolDialogClose"
    >
      <el-form ref="protocolFormRef" :model="protocolFormData" label-width="120px" class="protocol-form">
        <el-form-item label="协议名称" prop="name" :rules="[{ required: true, message: '请输入协议名称', trigger: 'blur' }]">
          <el-input v-model="protocolFormData.name" placeholder="请输入协议名称" />
        </el-form-item>
        <el-form-item label="协议类型" prop="type" :rules="[{ required: true, message: '请选择协议类型', trigger: 'change' }]">
          <el-select v-model="protocolFormData.type" placeholder="请选择协议类型">
            <el-option label="Modbus" value="Modbus" />
            <el-option label="OPC UA" value="OPC UA" />
            <el-option label="MQTT" value="MQTT" />
            <el-option label="S7" value="S7" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备数量" prop="deviceCount">
          <el-input-number v-model="protocolFormData.deviceCount" :min="0" placeholder="请输入设备数量" />
        </el-form-item>
        <el-form-item label="初始状态" prop="status" :rules="[{ required: true, message: '请选择初始状态', trigger: 'change' }]">
          <el-select v-model="protocolFormData.status" placeholder="请选择初始状态">
            <el-option label="运行中" value="running" />
            <el-option label="已停止" value="stopped" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="protocolDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveProtocol" :loading="protocolSaveLoading">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 协议配置对话框 -->
    <el-dialog
      v-model="configDialogVisible"
      title="协议配置"
      width="600px"
      @close="handleConfigDialogClose"
    >
      <div v-if="selectedProtocol" class="protocol-config-content">
        <h4>{{ selectedProtocol.name }} - {{ selectedProtocol.type }}</h4>
        <el-descriptions :column="2" border class="protocol-config">
          <el-descriptions-item label="协议ID">{{ selectedProtocol.id }}</el-descriptions-item>
          <el-descriptions-item label="设备数量">{{ selectedProtocol.deviceCount }} 台</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="selectedProtocol.status === 'running' ? 'success' : 'danger'">
              {{ selectedProtocol.status === 'running' ? '运行中' : '已停止' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="协议类型">{{ selectedProtocol.type }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="config-section">
          <h5>连接配置</h5>
          <el-form :model="protocolConfigData" label-width="120px" class="config-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="主机地址">
                  <el-input v-model="protocolConfigData.host" placeholder="输入主机地址" />
                </el-form-item>
                <el-form-item label="端口">
                  <el-input-number v-model="protocolConfigData.port" :min="1" :max="65535" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="超时时间">
                  <el-input-number v-model="protocolConfigData.timeout" :min="100" :max="10000" :step="100" />
                  <span style="margin-left: 10px;">毫秒</span>
                </el-form-item>
                <el-form-item label="重试次数">
                  <el-input-number v-model="protocolConfigData.retryCount" :min="0" :max="10" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
        
        <div class="config-section">
          <h5>高级配置</h5>
          <el-form :model="protocolConfigData" label-width="120px" class="config-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="数据刷新间隔">
                  <el-input-number v-model="protocolConfigData.refreshInterval" :min="100" :max="60000" :step="100" />
                  <span style="margin-left: 10px;">毫秒</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="启用日志">
                  <el-switch v-model="protocolConfigData.enableLogging" active-text="开启" inactive-text="关闭" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="configDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleSaveConfig" :loading="configSaveLoading">保存配置</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 数据预处理 -->
    <div v-if="activeTab === 'preprocessing'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>数据预处理</span>
            <el-button type="primary" size="small" @click="savePreprocessSettings" :loading="preprocessSaveLoading">保存配置</el-button>
          </div>
        </template>
        <div class="sub-card-content">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="preprocess-item">
                <h4>量程转换</h4>
                <el-switch v-model="preprocessSettings.rangeConversion" active-text="开启" inactive-text="关闭" />
              </div>
            </el-col>
            <el-col :span="8">
              <div class="preprocess-item">
                <h4>死区过滤</h4>
                <el-switch v-model="preprocessSettings.deadbandFilter" active-text="开启" inactive-text="关闭" />
                <el-input-number v-model="preprocessSettings.deadbandValue" :min="0" :max="100" :step="0.1" style="margin-left: 10px;" />
              </div>
            </el-col>
            <el-col :span="8">
              <div class="preprocess-item">
                <h4>数据校验</h4>
                <el-switch v-model="preprocessSettings.dataValidation" active-text="开启" inactive-text="关闭" />
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>
    
    <!-- 数据存储 -->
    <div v-if="activeTab === 'storage'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>数据存储</span>
            <el-button type="primary" size="small" @click="saveStorageConfig">保存配置</el-button>
          </div>
        </template>
        <div class="sub-card-content">
          <el-tabs v-model="storageActiveTab" class="storage-tabs">
            <!-- 存储类型 -->
            <el-tab-pane label="存储类型" name="type">
              <div class="storage-config-item">
                <h4>选择存储类型</h4>
                <el-radio-group v-model="storageConfig.storageType" class="storage-type-group">
                  <el-radio-button value="mysql">MySQL</el-radio-button>
                  <el-radio-button value="influxdb">InfluxDB</el-radio-button>
                  <el-radio-button value="timescaledb">TimescaleDB</el-radio-button>
                  <el-radio-button value="mongodb">MongoDB</el-radio-button>
                </el-radio-group>
              </div>
            </el-tab-pane>
            
            <!-- 连接配置 -->
            <el-tab-pane label="连接配置" name="connection">
              <el-form :model="storageConfig" label-width="120px" class="storage-form">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="主机地址">
                      <el-input v-model="storageConfig.host" placeholder="输入主机地址" />
                    </el-form-item>
                    <el-form-item label="端口">
                      <el-input-number v-model="storageConfig.port" :min="1" :max="65535" />
                    </el-form-item>
                    <el-form-item label="数据库名称">
                      <el-input v-model="storageConfig.database" placeholder="输入数据库名称" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="用户名">
                      <el-input v-model="storageConfig.username" placeholder="输入用户名" />
                    </el-form-item>
                    <el-form-item label="密码">
                      <el-input v-model="storageConfig.password" type="password" placeholder="输入密码" />
                    </el-form-item>
                    <el-form-item label="连接状态">
                      <el-tag :type="storageConfig.connectionStatus === 'connected' ? 'success' : 'danger'">
                        {{ storageConfig.connectionStatus === 'connected' ? '已连接' : '未连接' }}
                      </el-tag>
                      <el-button size="small" type="primary" @click="testConnection" style="margin-left: 10px;">
                        测试连接
                      </el-button>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-tab-pane>
            
            <!-- 存储策略 -->
            <el-tab-pane label="存储策略" name="policy">
              <el-form :model="storageConfig" label-width="120px" class="storage-form">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="实时数据保留期">
                      <el-input-number v-model="storageConfig.realtimeRetention" :min="1" />
                      <span style="margin-left: 10px;">天</span>
                    </el-form-item>
                    <el-form-item label="历史数据采样周期">
                      <el-select v-model="storageConfig.historicalSampling" placeholder="选择采样周期">
                        <el-option label="1分钟" value="1m" />
                        <el-option label="5分钟" value="5m" />
                        <el-option label="15分钟" value="15m" />
                        <el-option label="1小时" value="1h" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="数据压缩级别">
                      <el-select v-model="storageConfig.compressionLevel" placeholder="选择压缩级别">
                        <el-option label="无压缩" value="none" />
                        <el-option label="低压缩" value="low" />
                        <el-option label="中压缩" value="medium" />
                        <el-option label="高压缩" value="high" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="备份策略">
                      <el-select v-model="storageConfig.backupPolicy" placeholder="选择备份策略">
                        <el-option label="每天" value="daily" />
                        <el-option label="每周" value="weekly" />
                        <el-option label="每月" value="monthly" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-tab-pane>
            
            <!-- 存储状态 -->
            <el-tab-pane label="存储状态" name="status">
              <el-descriptions :column="2" border class="storage-status">
                <el-descriptions-item label="存储类型">{{ getStorageTypeName() }}</el-descriptions-item>
                <el-descriptions-item label="连接状态">
                  <el-tag :type="storageConfig.connectionStatus === 'connected' ? 'success' : 'danger'">
                    {{ storageConfig.connectionStatus === 'connected' ? '已连接' : '未连接' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="已存储数据量">{{ storageConfig.storedData }} MB</el-descriptions-item>
                <el-descriptions-item label="每秒写入次数">{{ storageConfig.writesPerSecond }} 次</el-descriptions-item>
                <el-descriptions-item label="存储效率">{{ storageConfig.storageEfficiency }} %</el-descriptions-item>
                <el-descriptions-item label="最后写入时间">{{ storageConfig.lastWriteTime }}</el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { unwrapListResponse, unwrapResponseData } from '@/api'
import {
  scadaCollectPointApi,
  scadaProtocolApi,
  scadaPreprocessApi,
  scadaRealtimeApi,
  scadaStorageApi,
  type ScadaCollectPoint,
  type ScadaPreprocessSetting,
  type ScadaProtocol,
  type ScadaStoragePolicy
} from '@/api/scada'
import DocVisionDialog from '@/components/ai/DocVisionDialog.vue'

// 路由实例
const router = useRouter()
const route = useRoute()

// 激活的标签页
const activeTab = ref('real-time')

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'real-time': '实时数据采集',
  'protocols': '多协议支持',
  'preprocessing': '数据预处理',
  'storage': '数据存储'
}

// 实时数据采集
const collectionPoints = ref<ScadaCollectPoint[]>([])

// S11 仪表视觉识别与人工抄表状态
const visionVisible = ref(false)
const manualReadVisible = ref(false)
const manualReadSaving = ref(false)
const manualReadForm = ref<{ tagCode: string; value: number | undefined; unit: string }>({
  tagCode: '',
  value: undefined,
  unit: ''
})

/**
 * 视觉识别确认：预填抄表表单并弹出人工确认框（CONFIRM 级）
 * @param fields 识别提取字段 {tagCode, reading, unit}
 */
const handleVisionConfirm = (fields: Record<string, any>) => {
  manualReadForm.value = {
    tagCode: String(fields.tagCode || ''),
    value: fields.reading != null ? Number(fields.reading) : undefined,
    unit: String(fields.unit || '')
  }
  manualReadVisible.value = true
}

/** 人工确认抄表：走 SCADA 人工补录端点落库（quality=manual），成功后刷新点位列表 */
const handleManualReadSave = async () => {
  if (!manualReadForm.value.tagCode) {
    ElMessage.warning('请选择采集点位')
    return
  }
  if (manualReadForm.value.value == null) {
    ElMessage.warning('请输入仪表读数')
    return
  }
  manualReadSaving.value = true
  try {
    await scadaRealtimeApi.createManualReading({
      tagCode: manualReadForm.value.tagCode,
      value: manualReadForm.value.value,
      unit: manualReadForm.value.unit || undefined
    })
    ElMessage.success('抄表记录已保存')
    manualReadVisible.value = false
    await loadCollectionPoints()
  } catch (e) {
    ElMessage.error('抄表保存失败，请稍后重试')
  } finally {
    manualReadSaving.value = false
  }
}

// 添加/编辑采集点对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const saveLoading = ref(false)
const pointFormRef = ref()

// 表单数据
const formData = ref({
  id: 0,
  tagCode: '',
  deviceName: '',
  protocol: '',
  unit: '',
  value: 0,
  status: 'online'
})

// 重置表单数据
const resetForm = () => {
  formData.value = {
    id: 0,
    tagCode: '',
    deviceName: '',
    protocol: '',
    unit: '',
    value: 0,
    status: 'online'
  }
  if (pointFormRef.value) {
    pointFormRef.value.resetFields()
  }
}

// 打开添加采集点对话框
const handleAddPoint = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 打开编辑采集点对话框
const handleEditPoint = (point: any) => {
  isEdit.value = true
  formData.value = { ...point }
  dialogVisible.value = true
}

// 保存采集点
const handleSavePoint = async () => {
  if (!pointFormRef.value) return
  
  try {
    // 表单验证
    await pointFormRef.value.validate()
    
    saveLoading.value = true

    const payload: ScadaCollectPoint = { ...formData.value }
    if (isEdit.value) {
      await scadaCollectPointApi.update(formData.value.id, payload)
      ElMessage.success('采集点编辑成功')
    } else {
      await scadaCollectPointApi.create(payload)
      ElMessage.success('采集点添加成功')
    }

    await loadCollectionPoints()
    
    dialogVisible.value = false
  } catch (error) {
    console.error('保存采集点失败:', error)
    if (error instanceof Error) {
      ElMessage.error(`保存失败: ${error.message}`)
    } else {
      ElMessage.error('保存采集点失败')
    }
  } finally {
    saveLoading.value = false
  }
}

// 处理对话框关闭
const handleDialogClose = () => {
  resetForm()
}

// 多协议支持
const protocols = ref<ScadaProtocol[]>([])

// 协议对话框相关
const protocolDialogVisible = ref(false)
const isEditProtocol = ref(false)
const protocolSaveLoading = ref(false)
const protocolFormRef = ref()
const protocolFormData = ref({
  id: 0,
  name: '',
  type: '',
  deviceCount: 0,
  status: 'running'
})

// 协议配置对话框相关
const configDialogVisible = ref(false)
const selectedProtocol = ref<any>(null)
const protocolConfigData = ref({
  host: 'localhost',
  port: 502,
  timeout: 3000,
  retryCount: 3,
  refreshInterval: 1000,
  enableLogging: true
})
const configSaveLoading = ref(false)

// 数据预处理
const preprocessSettings = ref<ScadaPreprocessSetting>({
  rangeConversion: true,
  deadbandFilter: true,
  deadbandValue: 0.5,
  dataValidation: true
})
const preprocessSaveLoading = ref(false)

// 数据存储配置
const storageActiveTab = ref('type')
const storageConfig = ref<ScadaStoragePolicy>({
  storageType: 'influxdb',
  host: 'localhost',
  port: 8086,
  database: 'scada_data',
  username: 'admin',
  password: 'password',
  connectionStatus: 'connected',
  realtimeRetention: 7,
  historicalSampling: '5m',
  compressionLevel: 'medium',
  backupPolicy: 'daily',
  storedData: 1250,
  writesPerSecond: 150,
  storageEfficiency: 85,
  lastWriteTime: '2025-12-31 11:30:00'
})

const extractListData = (response: any) => {
  return unwrapListResponse<any>(response)
}

const loadCollectionPoints = async () => {
  const response = await scadaCollectPointApi.list()
  collectionPoints.value = extractListData(response)
}

const loadProtocols = async () => {
  const response = await scadaProtocolApi.list()
  protocols.value = extractListData(response)
}

const loadPreprocessSettings = async () => {
  const response = await scadaPreprocessApi.get()
  const data = unwrapResponseData<any>(response)
  if (data) {
    preprocessSettings.value = { ...preprocessSettings.value, ...data }
  }
}

// 保存数据预处理配置
const savePreprocessSettings = async () => {
  preprocessSaveLoading.value = true
  try {
    await scadaPreprocessApi.save(preprocessSettings.value)
    ElMessage.success('预处理配置保存成功')
    await loadPreprocessSettings()
  } catch (error: any) {
    console.error('保存预处理配置失败:', error)
    ElMessage.error('保存预处理配置失败')
  } finally {
    preprocessSaveLoading.value = false
  }
}

const loadStoragePolicy = async () => {
  const response = await scadaStorageApi.get()
  const data = unwrapResponseData<any>(response)
  if (data) {
    storageConfig.value = { ...storageConfig.value, ...data }
  }
}

// 获取协议类型颜色
const getProtocolTypeColor = (type: string) => {
  switch (type) {
    case 'Modbus': return 'primary'
    case 'OPC UA': return 'success'
    case 'MQTT': return 'warning'
    default: return 'info'
  }
}

// 获取存储类型名称
const getStorageTypeName = () => {
  const typeMap: Record<string, string> = {
    mysql: 'MySQL',
    influxdb: 'InfluxDB',
    timescaledb: 'TimescaleDB',
    mongodb: 'MongoDB'
  }
  return typeMap[storageConfig.value.storageType] || storageConfig.value.storageType
}

// 从路由获取标签页状态
const getActiveTabFromRoute = () => {
  const tabMap: Record<string, string> = {
    'real-time': 'real-time',
    'protocols': 'protocols',
    'preprocessing': 'preprocessing',
    'storage': 'storage'
  }
  const tabName = route.params.tab || 'real-time'
  return tabMap[tabName as string] || 'real-time'
}

// 组件挂载时，从路由获取标签页状态
onMounted(() => {
  activeTab.value = getActiveTabFromRoute()
  Promise.allSettled([
    loadCollectionPoints(),
    loadProtocols(),
    loadPreprocessSettings(),
    loadStoragePolicy()
  ]).catch(() => undefined)
})

// 标签页切换事件
const handleTabChange = (tabName: string) => {
  console.log('切换标签页:', tabName)
  // 更新路由，保持标签页状态
  router.push({
    path: `/home/scada/data-collection/${tabName}`
  })
}



const handleDeletePoint = async (point: any) => {
  try {
    ElMessage({ message: `正在删除采集点 ${point.tagCode}...`, type: 'info' })

    await scadaCollectPointApi.remove(point.id)
    await loadCollectionPoints()
    ElMessage.success('采集点删除成功')
  } catch (error) {
    console.error('删除采集点失败:', error)
    ElMessage.error('采集点删除失败')
  }
}

// 重置协议配置
const resetProtocolConfig = () => {
  protocolConfigData.value = {
    host: 'localhost',
    port: 502,
    timeout: 3000,
    retryCount: 3,
    refreshInterval: 1000,
    enableLogging: true
  }
}

// 配置协议
const handleConfigProtocol = (protocol: any) => {
  selectedProtocol.value = protocol
  
  // 根据协议类型设置默认端口
  resetProtocolConfig()
  if (protocol.type === 'Modbus') {
    protocolConfigData.value.port = 502
  } else if (protocol.type === 'OPC UA') {
    protocolConfigData.value.port = 4840
  } else if (protocol.type === 'MQTT') {
    protocolConfigData.value.port = 1883
  } else if (protocol.type === 'S7') {
    protocolConfigData.value.port = 102
  }
  
  configDialogVisible.value = true
}

// 重置协议表单
const resetProtocolForm = () => {
  protocolFormData.value = {
    id: 0,
    name: '',
    type: '',
    deviceCount: 0,
    status: 'running'
  }
  if (protocolFormRef.value) {
    protocolFormRef.value.resetFields()
  }
}

// 打开添加协议对话框
const handleAddProtocol = () => {
  isEditProtocol.value = false
  resetProtocolForm()
  protocolDialogVisible.value = true
}

// 切换协议状态
const handleToggleProtocol = async (protocol: any) => {
  const nextStatus = protocol.status === 'running' ? 'stopped' : 'running'
  const enabled = nextStatus === 'running'
  try {
    await scadaProtocolApi.toggle(protocol.id, enabled)
    protocol.status = nextStatus
    ElMessage.success('协议状态更新成功')
  } catch (error) {
    console.error('切换协议状态失败:', error)
    ElMessage.error('协议状态更新失败')
  }
}

// 保存协议
const handleSaveProtocol = async () => {
  if (!protocolFormRef.value) return
  
  try {
    // 表单验证
    await protocolFormRef.value.validate()
    
    protocolSaveLoading.value = true

    const payload: ScadaProtocol = { ...protocolFormData.value }
    if (isEditProtocol.value) {
      await scadaProtocolApi.update(protocolFormData.value.id, payload)
      ElMessage.success('协议编辑成功')
    } else {
      await scadaProtocolApi.create(payload)
      ElMessage.success('协议添加成功')
    }

    await loadProtocols()
    
    protocolDialogVisible.value = false
  } catch (error) {
    console.error('保存协议失败:', error)
    if (error instanceof Error) {
      ElMessage.error(`保存失败: ${error.message}`)
    } else {
      ElMessage.error('保存协议失败')
    }
  } finally {
    protocolSaveLoading.value = false
  }
}

// 协议对话框关闭事件
const handleProtocolDialogClose = () => {
  resetProtocolForm()
}

// 保存协议配置
const handleSaveConfig = async () => {
  if (!selectedProtocol.value) return
  
  try {
    configSaveLoading.value = true

    await scadaProtocolApi.updateConfig(selectedProtocol.value.id, protocolConfigData.value)
    ElMessage.success('协议配置保存成功')
    configDialogVisible.value = false
  } catch (error) {
    console.error('保存协议配置失败:', error)
    ElMessage.error('保存协议配置失败')
  } finally {
    configSaveLoading.value = false
  }
}

// 配置对话框关闭事件
const handleConfigDialogClose = () => {
  selectedProtocol.value = null
  resetProtocolConfig()
}

// 删除协议
const handleDeleteProtocol = async (protocol: any) => {
  try {
    ElMessage({ message: `正在删除协议 ${protocol.name}...`, type: 'info' })

    await scadaProtocolApi.remove(protocol.id)
    await loadProtocols()
    ElMessage.success('协议删除成功')
  } catch (error) {
    console.error('删除协议失败:', error)
    ElMessage.error('协议删除失败')
  }
}

// 保存存储配置
const saveStorageConfig = async () => {
  try {
    await scadaStorageApi.save(storageConfig.value)
    ElMessage.success('存储配置保存成功')
  } catch (error) {
    console.error('保存存储配置失败:', error)
    ElMessage.error('存储配置保存失败')
  }
}

// 测试连接
const testConnection = async () => {
  try {
    const response = await scadaStorageApi.testConnection(storageConfig.value)
    const data = unwrapResponseData<any>(response)
    if (data?.connectionStatus) {
      storageConfig.value.connectionStatus = data.connectionStatus
    } else {
      storageConfig.value.connectionStatus = 'connected'
    }
    ElMessage.success('连接测试成功')
  } catch (error) {
    console.error('测试连接失败:', error)
    storageConfig.value.connectionStatus = 'disconnected'
    ElMessage.error('连接测试失败')
  }
}
</script>

<style scoped>
.data-collection-view {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color-light);
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.8rem;
}

/* 覆盖默认的h2样式，确保只影响页面标题 */
h2 {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.sub-card {
  margin-bottom: 20px;
}

.sub-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1rem;
}

.sub-card-content {
  padding: 10px 0;
}

/* 数据预处理 */
.preprocess-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.preprocess-item h4 {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

/* 数据存储 */
.storage-tabs {
  margin-top: 20px;
}

.storage-config-item {
  margin-bottom: 20px;
}

.storage-config-item h4 {
  margin-bottom: 16px;
  font-size: 16px;
  color: #303133;
}

.storage-type-group {
  margin-bottom: 20px;
}

.storage-form {
  margin-top: 20px;
}

.storage-status {
  margin-top: 20px;
}

.storage-status .el-descriptions-item__content {
  font-weight: bold;
}

.storage-status .el-descriptions-item__label {
  background-color: #f5f7fa;
}
</style>

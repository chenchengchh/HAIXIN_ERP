<template>
  <div class="location-print-container">
    <!-- 操作栏 -->
    <el-card class="operation-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-select
            v-model="selectedZone"
            placeholder="选择库区"
            clearable
            @change="handleZoneChange"
          >
            <el-option
              v-for="zone in zones"
              :key="zone.value"
              :label="zone.label"
              :value="zone.value"
            ></el-option>
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-select
            v-model="selectedLocationType"
            placeholder="选择库位类型"
            clearable
          >
            <el-option
              v-for="t in locationTypes"
              :key="t.value"
              :label="t.label"
              :value="t.value"
            ></el-option>
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-select
            v-model="printTemplate"
            placeholder="选择打印模板"
            @change="handleTemplateChange"
          >
            <el-option label="标准模板" value="standard"></el-option>
            <el-option label="紧凑模板" value="compact"></el-option>
            <el-option label="自定义模板" value="custom"></el-option>
          </el-select>
        </el-col>
      </el-row>
    </el-card>

    <!-- 库位列表 -->
    <el-card class="list-card">
      <div class="list-header">
        <div class="title">可选库位</div>
        <el-row :gutter="10">
          <el-col>
            <el-button type="primary" @click="selectAll">全选</el-button>
          </el-col>
          <el-col>
            <el-button @click="selectNone">清空</el-button>
          </el-col>
          <el-col>
            <el-button type="success" @click="selectVisible">选择可见</el-button>
          </el-col>
        </el-row>
      </div>
      <el-table
        v-loading="loading"
        :data="locations"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="locationCode" label="库位码" width="120"></el-table-column>
        <el-table-column prop="zoneCode" label="库区" width="80"></el-table-column>
        <el-table-column prop="type" label="类型" width="80">
          <template #default="scope">
            <el-tag
                  :type="getLocationTypeColor(scope.row.type as string)"
                >
                  {{ getLocationTypeName(scope.row.type as string) }}
                </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag
                  :type="getLocationStatusColor(scope.row.status as string)"
                >
                  {{ getLocationStatusName(scope.row.status as string) }}
                </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxWeight" label="最大承重" width="100">
          <template #default="scope">{{ scope.row.maxWeight }} kg</template>
        </el-table-column>
        <el-table-column prop="mixFlag" label="是否混放" width="100">
          <template #default="scope">
            <el-switch v-model="scope.row.mixFlag" disabled></el-switch>
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

    <!-- 打印预览 -->
    <el-card class="preview-card">
      <div class="preview-header">
        <div class="title">打印预览</div>
          <el-row :gutter="10">
            <el-col>
              <el-select v-model="pdfOrientation" placeholder="PDF方向" size="small">
                <el-option label="横向" value="landscape"></el-option>
                <el-option label="纵向" value="portrait"></el-option>
              </el-select>
            </el-col>
            <el-col>
              <el-button type="primary" @click="handlePrint" :disabled="selectedLocations.length === 0">
                打印库位码
              </el-button>
            </el-col>
            <el-col>
              <el-button @click="handleExportPDF" :disabled="selectedLocations.length === 0">
                导出PDF
              </el-button>
            </el-col>
            <el-col>
              <el-button @click="handleExportExcel" :disabled="selectedLocations.length === 0">
                导出Excel
              </el-button>
            </el-col>
          </el-row>
        </div>
      <div class="preview-content">
        <div
          v-for="(location, index) in selectedLocations"
          :key="location.id"
          class="label-preview"
          :class="printTemplate"
        >
          <div class="label-header">{{ warehouseName }}</div>
          <div class="label-code">
            <div class="code-text">{{ location.locationCode }}</div>
            <div class="code-barcode">
              <svg :width="150" :height="50" xmlns="http://www.w3.org/2000/svg">
                <!-- 简单的条码实现，可以替换为更复杂的条码生成逻辑 -->
                <g transform="translate(10, 10)">
                  <!-- 条码起始符 -->
                  <rect x="0" y="0" width="2" height="30" fill="black" />
                  <rect x="4" y="0" width="2" height="30" fill="black" />
                  <rect x="8" y="0" width="2" height="30" fill="black" />
                  
                  <!-- 条码数据区域 -->
                  <g v-for="(char, i) in location.locationCode" :key="i" :transform="`translate(${12 + i * 10}, 0)`">
                    <!-- 使用简单的线条表示条码，实际应用中应该使用专业的条码生成算法 -->
                    <rect 
                      x="0" 
                      y="0" 
                      :width="2" 
                      :height="char.charCodeAt(0) % 2 === 0 ? 30 : 20" 
                      fill="black" 
                    />
                    <rect 
                      x="4" 
                      y="0" 
                      :width="2" 
                      :height="char.charCodeAt(0) % 3 === 0 ? 30 : 25" 
                      fill="black" 
                    />
                  </g>
                  
                  <!-- 条码结束符 -->
                  <rect :x="12 + location.locationCode.length * 10" y="0" width="2" height="30" fill="black" />
                  <rect :x="16 + location.locationCode.length * 10" y="0" width="2" height="30" fill="black" />
                  <rect :x="20 + location.locationCode.length * 10" y="0" width="2" height="30" fill="black" />
                </g>
              </svg>
            </div>
          </div>
          <div class="label-info">
            <div>库区: {{ location.zoneCode }}</div>
            <div>类型: {{ getLocationTypeName(location.type) }}</div>
            <div>承重: {{ location.maxWeight }} kg</div>
            <div>混放: {{ location.mixFlag ? '是' : '否' }}</div>
          </div>
          <div class="label-footer">{{ new Date().toLocaleDateString() }}</div>
        </div>
        <div v-if="selectedLocations.length === 0" class="empty-preview">
          请选择要打印的库位
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
// 使用正确的ES模块导入方式
import { jsPDF } from 'jspdf';
import html2canvas from 'html2canvas';
import { warehouseAreaApi, locationApi, locationTypeApi } from '@/api/wms';

const zones = ref<{ label: string; value: string }[]>([]);
const locationTypes = ref<{ label: string; value: string; mixFlag: boolean; maxWeight: number }[]>([]);
const locationTypeMetaByCode = ref<Record<string, { label: string; mixFlag: boolean; maxWeight: number }>>({});

// 选择的库区
const selectedZone = ref('');

// 选择的库位类型
const selectedLocationType = ref('');

// 打印模板
const printTemplate = ref('standard');

// 仓库名称
const warehouseName = computed(() => selectedLocations.value[0]?.warehouseName || locations.value[0]?.warehouseName || '');

// 库位数据
interface Location {
  id: number;
  locationCode: string;
  zoneCode: string;
  type: string;
  status: string;
  mixFlag: boolean;
  maxWeight: number;
  warehouseName?: string;
}

// 库位数据
const locations = ref<Location[]>([]);

// 分页数据
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(false);

const fetchZones = async () => {
  const res = await warehouseAreaApi.getList({ page: 1, size: 1000 });
  const pageData = res.data || {};
  const list = (pageData.list || []) as any[];
  zones.value = list
    .map((z: any) => ({
      label: String(z?.zoneName ?? z?.zoneCode ?? ''),
      value: String(z?.zoneCode ?? '')
    }))
    .filter(v => v.value);
};

const fetchLocationTypes = async () => {
  const res = await locationTypeApi.getList({ page: 1, size: 1000 });
  const pageData = res.data || {};
  const list = (pageData.list || []) as any[];
  const meta: Record<string, { label: string; mixFlag: boolean; maxWeight: number }> = {};
  locationTypes.value = list
    .map((t: any) => {
      const code = String(t?.typeCode ?? '');
      const label = String(t?.typeName ?? code);
      const mixFlag = Boolean(t?.mixFlag ?? false);
      const maxWeight = Number(t?.maxWeight ?? 0);
      if (code) {
        meta[code] = { label, mixFlag, maxWeight };
      }
      return { label, value: code, mixFlag, maxWeight };
    })
    .filter(v => v.value);
  locationTypeMetaByCode.value = meta;
};

// 获取库位列表数据
const getLocationList = async () => {
  try {
    loading.value = true;

    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    };
    if (selectedZone.value) params.zoneCode = selectedZone.value;
    if (selectedLocationType.value) params.locationTypeCode = selectedLocationType.value;
    const res = await locationApi.getList(params);
    const pageData = res.data || {};
    const list = (pageData.list || []) as any[];
    const meta = locationTypeMetaByCode.value;
    locations.value = list.map((l: any) => {
      const type = String(l?.locationTypeCode ?? '');
      const m = meta[type];
      return {
        id: Number(l?.id ?? 0),
        locationCode: String(l?.locationCode ?? ''),
        zoneCode: String(l?.zoneCode ?? ''),
        type,
        status: String(l?.status ?? '1'),
        mixFlag: Boolean(m?.mixFlag ?? false),
        maxWeight: Number(m?.maxWeight ?? 0),
        warehouseName: String(l?.warehouseName ?? '')
      } as Location;
    });
    total.value = Number(pageData.total ?? 0);
  } catch (error) {
    console.error('获取库位列表失败:', error);
    ElMessage.error((error as any)?.message || '获取库位列表失败');
  } finally {
    loading.value = false;
  }
};

// 选中的库位
const selectedLocations = ref<Location[]>([]);

// PDF方向
const pdfOrientation = ref<'portrait' | 'landscape'>('portrait');

// 处理库区变化
const handleZoneChange = () => {
  currentPage.value = 1;
  getLocationList();
};

// 处理模板变化
const handleTemplateChange = () => {
  console.log('选择模板:', printTemplate.value);
};

// 处理全选
const selectAll = () => {
  loading.value = true;
  const params: any = {
    page: 1,
    size: 1000
  };
  if (selectedZone.value) params.zoneCode = selectedZone.value;
  if (selectedLocationType.value) params.locationTypeCode = selectedLocationType.value;
  locationApi
    .getList(params)
    .then(res => {
      const pageData = res.data || {};
      const list = (pageData.list || []) as any[];
      const meta = locationTypeMetaByCode.value;
      selectedLocations.value = list.map((l: any) => {
        const type = String(l?.locationTypeCode ?? '');
        const m = meta[type];
        return {
          id: Number(l?.id ?? 0),
          locationCode: String(l?.locationCode ?? ''),
          zoneCode: String(l?.zoneCode ?? ''),
          type,
          status: String(l?.status ?? '1'),
          mixFlag: Boolean(m?.mixFlag ?? false),
          maxWeight: Number(m?.maxWeight ?? 0),
          warehouseName: String(l?.warehouseName ?? '')
        } as Location;
      });
    })
    .catch((e: any) => {
      ElMessage.error(e?.message || '全选失败');
    })
    .finally(() => {
      loading.value = false;
    });
};

// 处理清空
const selectNone = () => {
  selectedLocations.value = [];
};

// 处理选择可见
const selectVisible = () => {
  // 获取当前页可见的库位
  const visibleLocations = [...locations.value];
  
  // 添加当前页可见的库位到选中列表
  visibleLocations.forEach(location => {
    if (!selectedLocations.value.find(item => item.id === location.id)) {
      selectedLocations.value.push(location);
    }
  });
  
  console.log('选择可见库位:', visibleLocations.length, '个');
};

// 处理选择变化
const handleSelectionChange = (selection: Location[]) => {
  selectedLocations.value = selection;
};

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  getLocationList();
};

// 处理当前页变化
const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  getLocationList();
};

// 处理打印
const handlePrint = () => {
  if (selectedLocations.value.length === 0) {
    console.warn('没有选中要打印的库位');
    return;
  }
  
  console.log('打印库位码:', selectedLocations.value);
  locationApi
    .printCodes({ codes: selectedLocations.value.map(v => v.locationCode) })
    .then(() => {
      ElMessage.success('打印任务已提交');
    })
    .catch((e: any) => {
      ElMessage.warning(e?.message || '打印任务提交失败');
    });
  
  // 创建打印内容
  const printContent = document.createElement('div');
  printContent.className = 'print-container';
  
  // 添加打印样式
  const printStyle = document.createElement('style');
  printStyle.textContent = `
    @media print {
      body {
        margin: 0;
        padding: 0;
        font-family: Arial, sans-serif;
      }
      .print-container {
        display: flex;
        flex-wrap: wrap;
        gap: 20px;
        padding: 20px;
      }
      .label-preview {
        border: 1px solid #e0e0e0;
        border-radius: 4px;
        padding: 10px;
        background-color: #ffffff;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        display: flex;
        flex-direction: column;
        align-items: center;
        break-inside: avoid;
      }
      .label-preview.standard {
        width: 200px;
        height: 150px;
      }
      .label-preview.compact {
        width: 150px;
        height: 100px;
        font-size: 12px;
      }
      .label-preview.custom {
        width: 250px;
        height: 180px;
      }
      .label-header {
        font-size: 14px;
        font-weight: bold;
        margin-bottom: 10px;
      }
      .label-code {
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-bottom: 10px;
        flex: 1;
        justify-content: center;
      }
      .code-text {
        font-size: 18px;
        font-weight: bold;
        margin-bottom: 5px;
      }
      .code-barcode {
        width: 150px;
        height: 50px;
        background-color: #f0f0f0;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 1px dashed #ccc;
        font-size: 12px;
        color: #999;
      }
      .label-info {
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-bottom: 10px;
        font-size: 12px;
      }
      .label-footer {
        font-size: 10px;
        color: #999;
      }
    }
  `;
  document.head.appendChild(printStyle);
  
  // 添加每个标签到打印内容
  selectedLocations.value.forEach((location, index) => {
    const labelDiv = document.createElement('div');
    labelDiv.className = `label-preview ${printTemplate.value}`;
    
    labelDiv.innerHTML = `
      <div class="label-header">${warehouseName}</div>
      <div class="label-code">
        <div class="code-text">${location.locationCode}</div>
        <div class="code-barcode">${location.locationCode}</div>
      </div>
      <div class="label-info">
        <div>库区: ${location.zoneCode}</div>
        <div>类型: ${location.type === '1' ? '存储' : location.type === '2' ? '拣货' : '暂存'}</div>
        <div>承重: ${location.maxWeight} kg</div>
        <div>混放: ${location.mixFlag ? '是' : '否'}</div>
      </div>
      <div class="label-footer">${new Date().toLocaleDateString()}</div>
    `;
    
    printContent.appendChild(labelDiv);
  });
  
  // 添加到文档
  document.body.appendChild(printContent);
  
  // 执行打印
  window.print();
  
  // 移除打印内容
  setTimeout(() => {
    document.body.removeChild(printContent);
    document.head.removeChild(printStyle);
  }, 1000);
  
  // 保存打印记录
  savePrintRecord();
};

// 保存打印记录
const savePrintRecord = () => {
  // 这里可以实现保存打印记录的逻辑
  console.log('保存打印记录');
};

// 处理PDF导出
const handleExportPDF = async () => {
  if (selectedLocations.value.length === 0) {
    console.warn('没有选中要导出的库位');
    return;
  }
  
  console.log('导出PDF:', selectedLocations.value);
  
  // 创建PDF文档
  const pdf = new jsPDF({
    orientation: pdfOrientation.value,
    unit: 'mm',
    format: 'a4'
  });
  
  // 获取页面宽度和高度
  const pageWidth = pdf.internal.pageSize.getWidth();
  const pageHeight = pdf.internal.pageSize.getHeight();
  
  // 创建一个临时容器来渲染所有标签
  const tempContainer = document.createElement('div');
  tempContainer.className = 'pdf-export-container';
  tempContainer.style.position = 'absolute';
  tempContainer.style.left = '-9999px';
  tempContainer.style.top = '-9999px';
  tempContainer.style.width = '210mm';
  tempContainer.style.background = 'white';
  tempContainer.style.padding = '10mm';
  tempContainer.style.boxSizing = 'border-box';
  
  // 添加样式
  tempContainer.style.fontFamily = 'Arial, sans-serif';
  tempContainer.style.display = 'flex';
  tempContainer.style.flexWrap = 'wrap';
  tempContainer.style.gap = '10mm';
  
  // 为每个标签创建DOM元素
  selectedLocations.value.forEach(location => {
    const labelDiv = document.createElement('div');
    labelDiv.className = `label-preview ${printTemplate.value}`;
    labelDiv.style.border = '1px solid #e0e0e0';
    labelDiv.style.borderRadius = '4px';
    labelDiv.style.padding = '10px';
    labelDiv.style.background = '#ffffff';
    labelDiv.style.boxShadow = '0 2px 8px rgba(0, 0, 0, 0.1)';
    labelDiv.style.display = 'flex';
    labelDiv.style.flexDirection = 'column';
    labelDiv.style.alignItems = 'center';
    
    // 根据模板设置尺寸
    switch (printTemplate.value) {
      case 'standard':
        labelDiv.style.width = '60mm';
        labelDiv.style.height = '45mm';
        break;
      case 'compact':
        labelDiv.style.width = '45mm';
        labelDiv.style.height = '30mm';
        labelDiv.style.fontSize = '12px';
        break;
      case 'custom':
        labelDiv.style.width = '75mm';
        labelDiv.style.height = '54mm';
        break;
    }
    
    labelDiv.innerHTML = `
      <div class="label-header">${warehouseName.value}</div>
      <div class="label-code">
        <div class="code-text">${location.locationCode}</div>
        <div class="code-barcode">
          <svg width="150" height="50" xmlns="http://www.w3.org/2000/svg">
            <g transform="translate(10, 10)">
              <!-- 条码起始符 -->
              <rect x="0" y="0" width="2" height="30" fill="black" />
              <rect x="4" y="0" width="2" height="30" fill="black" />
              <rect x="8" y="0" width="2" height="30" fill="black" />
              
              <!-- 条码数据区域 -->
              <g transform="translate(12, 0)">
                ${Array.from(location.locationCode).map((char, i) => `
                  <g transform="translate(${i * 10}, 0)">
                    <rect x="0" y="0" width="2" height="${char.charCodeAt(0) % 2 === 0 ? 30 : 20}" fill="black" />
                    <rect x="4" y="0" width="2" height="${char.charCodeAt(0) % 3 === 0 ? 30 : 25}" fill="black" />
                  </g>
                `).join('')}
              </g>
              
              <!-- 条码结束符 -->
              <rect x="${12 + location.locationCode.length * 10}" y="0" width="2" height="30" fill="black" />
              <rect x="${16 + location.locationCode.length * 10}" y="0" width="2" height="30" fill="black" />
              <rect x="${20 + location.locationCode.length * 10}" y="0" width="2" height="30" fill="black" />
            </g>
          </svg>
        </div>
      </div>
      <div class="label-info">
        <div>库区: ${location.zoneCode}</div>
        <div>类型: ${location.type === '1' ? '存储' : location.type === '2' ? '拣货' : '暂存'}</div>
        <div>承重: ${location.maxWeight} kg</div>
        <div>混放: ${location.mixFlag ? '是' : '否'}</div>
      </div>
      <div class="label-footer">${new Date().toLocaleDateString()}</div>
    `;
    
    tempContainer.appendChild(labelDiv);
  });
  
  document.body.appendChild(tempContainer);
  
  try {
    // 使用html2canvas将临时容器转换为canvas
    const canvas = await html2canvas(tempContainer, {
      scale: 2, // 提高分辨率
      useCORS: true,
      logging: false,
      backgroundColor: '#ffffff'
    });
    
    // 将canvas转换为图片
    const imgData = canvas.toDataURL('image/png');
    
    // 计算图片在PDF中的尺寸
    const imgWidth = pageWidth - 20; // 左右边距10mm
    const imgHeight = (canvas.height * imgWidth) / canvas.width;
    
    // 计算需要多少页
    let heightLeft = imgHeight;
    let position = 10; // 上边距10mm
    
    // 添加第一页
    pdf.addImage(imgData, 'PNG', 10, position, imgWidth, imgHeight);
    heightLeft -= pageHeight - 20; // 减去上下边距
    
    // 添加后续页面
    while (heightLeft >= 0) {
      position = heightLeft - imgHeight;
      pdf.addPage();
      pdf.addImage(imgData, 'PNG', 10, position, imgWidth, imgHeight);
      heightLeft -= pageHeight - 20;
    }
    
    // 保存PDF文件
    pdf.save(`库位码_${new Date().getTime()}.pdf`);
    
    // 保存导出记录
    savePrintRecord();
  } catch (error) {
    console.error('导出PDF失败:', error);
  } finally {
    // 移除临时容器
    document.body.removeChild(tempContainer);
  }
};

// 处理Excel导出
const handleExportExcel = () => {
  if (selectedLocations.value.length === 0) {
    console.warn('没有选中要导出的库位');
    return;
  }
  
  console.log('导出Excel:', selectedLocations.value);
  
  // 简单的Excel导出实现，可以使用xlsx库实现更复杂的功能
  const csvContent = "data:text/csv;charset=utf-8," 
    + "库位码,库区,类型,状态,是否混放,最大承重\n" 
    + selectedLocations.value.map(location => {
      const typeText = getLocationTypeName(location.type);
      const statusText = getLocationStatusName(location.status);
      return `${location.locationCode},${location.zoneCode},${typeText},${statusText},${location.mixFlag ? '是' : '否'},${location.maxWeight} kg`;
    }).join("\n");
  
  // 创建下载链接
  const encodedUri = encodeURI(csvContent);
  const link = document.createElement("a");
  link.setAttribute("href", encodedUri);
  link.setAttribute("download", `库位码_${new Date().getTime()}.csv`);
  document.body.appendChild(link);
  
  // 触发下载
  link.click();
  
  // 移除链接
  document.body.removeChild(link);
  
  // 保存导出记录
  savePrintRecord();
};

// 获取库位类型颜色
const getLocationTypeColor = (type: string) => {
  if (!type) return 'default';
  const order = locationTypes.value.findIndex(v => v.value === type);
  const palette = ['success', 'warning', 'info', 'primary'];
  return palette[(order >= 0 ? order : 0) % palette.length] || 'default';
};

// 获取库位类型名称
const getLocationTypeName = (type: string) => {
  return locationTypeMetaByCode.value[type]?.label || type || '未知';
};

// 获取库位状态颜色
const getLocationStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    '1': 'success',
    '0': 'danger'
  };
  return colorMap[status] || 'default';
};

// 获取库位状态名称
const getLocationStatusName = (status: string) => {
  const statusMap: Record<string, string> = {
    '1': '启用',
    '0': '禁用'
  };
  return statusMap[status] || '未知';
};

// 组件挂载时初始化数据
onMounted(() => {
  Promise.allSettled([fetchZones(), fetchLocationTypes()]).finally(() => {
    getLocationList();
  });
});
</script>

<style scoped>
.location-print-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.operation-card {
  margin-bottom: 20px;
}

.list-card {
  margin-bottom: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-header .title {
  font-size: 18px;
  font-weight: bold;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.preview-card {
  margin-bottom: 20px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.preview-header .title {
  font-size: 18px;
  font-weight: bold;
}

.preview-content {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.label-preview {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 10px;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.label-preview.standard {
  width: 200px;
  height: 150px;
}

.label-preview.compact {
  width: 150px;
  height: 100px;
  font-size: 12px;
}

.label-preview.custom {
  width: 250px;
  height: 180px;
}

.label-header {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 10px;
}

.label-code {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 10px;
  flex: 1;
  justify-content: center;
}

.code-text {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}

.code-barcode {
  width: 150px;
  height: 50px;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #ccc;
  font-size: 12px;
  color: #999;
}

.label-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 10px;
  font-size: 12px;
}

.label-footer {
  font-size: 10px;
  color: #999;
}

.empty-preview {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  border: 1px dashed #e0e0e0;
  color: #999;
  font-size: 16px;
}
</style>

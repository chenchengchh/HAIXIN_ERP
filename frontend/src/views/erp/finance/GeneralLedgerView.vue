<template>
  <div class="general-ledger-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>总账管理</span>
        </div>
      </template>
      
      <!-- 导出进度条 -->
      <el-progress
        v-if="exportProgress > 0 && exportProgress < 100"
        :percentage="exportProgress"
        :format="percentageFormat"
        :stroke-width="15"
        :color="['#67C23A', '#E6A23C', '#F56C6C']"
        class="export-progress"
      ></el-progress>
      
      <!-- 总账数据列表 -->
      <TableComponent
        :data="ledgerList"
        :columns="ledgerColumns"
        :total="ledgerTotal"
        :loading="ledgerLoading"
        :show-index="true"
        :show-selection="true"
        :show-action="false"
        :table-actions="tableActions"
        :batch-actions="[]"
        :filters="ledgerFilters"
        :show-filter="true"
        :virtual-scroll="true"
        :virtual-scroll-threshold="100"
        :optimize-rendering="true"
        :row-height="50"
        @search="handleLedgerSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <!-- 金额列自定义 -->
        <template #beginning_balance="{ row }">
          <span :class="{ 'negative': parseFloat(row.beginning_balance) < 0 }">
            {{ formatCurrency(row.beginning_balance) }}
          </span>
        </template>
        <template #debit_amount="{ row }">
          <span :class="{ 'negative': parseFloat(row.debit_amount) < 0 }">
            {{ formatCurrency(row.debit_amount) }}
          </span>
        </template>
        <template #credit_amount="{ row }">
          <span :class="{ 'negative': parseFloat(row.credit_amount) < 0 }">
            {{ formatCurrency(row.credit_amount) }}
          </span>
        </template>
        <template #ending_balance="{ row }">
          <span :class="{ 'negative': parseFloat(row.ending_balance) < 0 }">
            {{ formatCurrency(row.ending_balance) }}
          </span>
        </template>
      </TableComponent>
    </el-card>
    
    <!-- 打印预览对话框 -->
    <el-dialog
      v-model="printPreviewVisible"
      title="打印预览"
      width="90%"
      :before-close="handlePreviewClose"
    >
      <div class="print-preview-content">
        <h1>ERP系统总账</h1>
        <div class="print-info">
          打印日期: {{ new Date().toLocaleString() }}
        </div>
        <table class="preview-table">
          <thead>
            <tr>
              <th>科目编码</th>
              <th>科目名称</th>
              <th>科目类型</th>
              <th>期初余额</th>
              <th>本期借方发生额</th>
              <th>本期贷方发生额</th>
              <th>期末余额</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in previewData" :key="row.id">
              <td>{{ row.account_code }}</td>
              <td>{{ row.account_name }}</td>
              <td>{{ row.account_type }}</td>
              <td>{{ formatCurrency(row.beginning_balance) }}</td>
              <td>{{ formatCurrency(row.debit_amount) }}</td>
              <td>{{ formatCurrency(row.credit_amount) }}</td>
              <td>{{ formatCurrency(row.ending_balance) }}</td>
              <td>{{ row.status }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="printPreviewVisible = false">关闭</el-button>
          <el-button type="primary" @click="handlePrintFromPreview">打印</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 导出格式选择对话框 -->
    <el-dialog
      v-model="exportFormatVisible"
      title="选择导出格式"
      width="400px"
    >
      <div class="export-format-options">
        <el-radio-group v-model="selectedExportFormat" class="format-radio-group">
          <el-radio-button value="csv">CSV</el-radio-button>
          <el-radio-button value="excel">Excel</el-radio-button>
          <el-radio-button value="pdf">PDF</el-radio-button>
        </el-radio-group>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="exportFormatVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmExportFormat">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Document, RefreshLeft, DocumentChecked } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { TableComponent } from '../../../components/base'
import { unwrapPageResponse } from '../../../api'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import { logger } from '../../../utils/logger'
import * as XLSX from 'xlsx'
import { jsPDF } from 'jspdf'
import html2canvas from 'html2canvas'

// 响应式数据
// 总账列表
const ledgerList = ref<any[]>([])
const ledgerTotal = ref(0)
const ledgerLoading = ref(false)
const ledgerPage = ref(1)
const ledgerSize = ref(10)
const selectedRows = ref<any[]>([])

// 打印预览相关
const printPreviewVisible = ref(false)
const previewData = ref<any[]>([])

// 导出相关
const exportFormatVisible = ref(false)
const selectedExportFormat = ref('csv')
const exportProgress = ref(0)

// 进度条格式化函数
const percentageFormat = (percentage: number) => {
  return `处理中 ${percentage}%`
}

// 科目类型选项
const accountTypeOptions = [
  { label: '资产类', value: 'asset' },
  { label: '负债类', value: 'liability' },
  { label: '所有者权益类', value: 'equity' },
  { label: '成本类', value: 'cost' },
  { label: '损益类', value: 'income' },
  { label: '费用类', value: 'expense' }
]

// 总账筛选条件
const ledgerFilters = [
  { prop: 'account_code', label: '科目编码', type: 'input' as 'input', placeholder: '请输入科目编码' },
  { prop: 'account_name', label: '科目名称', type: 'input' as 'input', placeholder: '请输入科目名称' },
  { prop: 'account_type', label: '科目类型', type: 'select' as 'select', options: accountTypeOptions },
  { prop: 'date_range', label: '日期范围', type: 'daterange' as 'daterange' },
  { prop: 'status', label: '状态', type: 'select' as 'select', options: [
    { label: '启用', value: 1 },
    { label: '禁用', value: 0 }
  ]}
]

// 总账列配置
const ledgerColumns = [
  { prop: 'account_code', label: '科目编码', width: 150, sortable: true },
  { prop: 'account_name', label: '科目名称', minWidth: 200, sortable: true },
  { prop: 'account_type', label: '科目类型', width: 120, sortable: true },
  { prop: 'beginning_balance', label: '期初余额', width: 150, align: 'right', slotName: 'beginning_balance', sortable: true },
  { prop: 'debit_amount', label: '本期借方发生额', width: 180, align: 'right', slotName: 'debit_amount', sortable: true },
  { prop: 'credit_amount', label: '本期贷方发生额', width: 180, align: 'right', slotName: 'credit_amount', sortable: true },
  { prop: 'ending_balance', label: '期末余额', width: 150, align: 'right', slotName: 'ending_balance', sortable: true },
  { prop: 'status', label: '状态', width: 100, sortable: true }
]

// 表格操作按钮
const tableActions = [
  {
    key: 'print',
    text: '打印总账',
    type: 'success',
    icon: DocumentChecked,
    handler: () => {
      if (selectedRows.value.length > 0) {
        // 先显示预览
        previewData.value = selectedRows.value
        printPreviewVisible.value = true
      } else {
        ElMessage.warning('请选择要打印的总账数据')
      }
    }
  },
  {
    key: 'export',
    text: '导出总账',
    type: 'warning',
    icon: DocumentChecked,
    handler: () => {
      // 显示导出格式选择
      exportFormatVisible.value = true
    }
  },
  {
    key: 'refresh',
    text: '刷新',
    icon: RefreshLeft,
    handler: async () => {
      try {
        // 添加刷新前的加载状态
        ledgerLoading.value = true
        await handleLedgerSearch()
        // 刷新成功反馈
        ElMessage.success('数据刷新成功')
      } catch (error) {
        // 刷新失败反馈
        ElMessage.error('数据刷新失败，请稍后重试')
        ErrorHandler.handleApiError(error, false)
      } finally {
        ledgerLoading.value = false
      }
    }
  }
]

// 初始加载
onMounted(() => {
  handleLedgerSearch()
})

// 获取总账列表
const getLedgerList = async (params: any) => {
  ledgerLoading.value = true
  
  try {
    const result = await erpApi.finance.getGeneralLedger({
      page: params.page || ledgerPage.value,
      size: params.size || ledgerSize.value,
      ...params
    })
    const page = unwrapPageResponse<any>(result)
    ledgerList.value = page.list
    ledgerTotal.value = page.total || page.list.length || 0
    ledgerPage.value = params.page || ledgerPage.value
    ledgerSize.value = params.size || ledgerSize.value
  } catch (apiError) {
    ErrorHandler.handleApiError(apiError)
    ledgerList.value = []
    ledgerTotal.value = 0
  } finally {
    ledgerLoading.value = false
  }
}

// 搜索总账
const handleLedgerSearch = async (params: any = {}) => {
  // 保留当前的筛选条件和分页状态
  const currentParams = {
    page: ledgerPage.value,
    size: ledgerSize.value,
    ...params
  }
  return getLedgerList(currentParams)
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  ledgerSize.value = size
  getLedgerList({ page: ledgerPage.value, size })
}

// 页码改变
const handleCurrentChange = (page: number) => {
  ledgerPage.value = page
  getLedgerList({ page, size: ledgerSize.value })
}

// 选择行改变
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

// 格式化金额
const formatCurrency = (amount: string | number): string => {
  try {
    const num = parseFloat(amount as string)
    if (isNaN(num)) return '0.00'
    return num.toFixed(2)
  } catch (error) {
    logger.error('金额格式化错误:', error)
    return '0.00'
  }
}

// 打印预览关闭处理
const handlePreviewClose = () => {
  printPreviewVisible.value = false
}

// 从预览打印
const handlePrintFromPreview = async () => {
  try {
    ledgerLoading.value = true
    printPreviewVisible.value = false
    
    // 准备打印数据
    const printData = previewData.value
    

    
    // 创建打印窗口
    const printWindow = window.open('', '_blank', 'width=800,height=600')
    if (!printWindow) {
      ElMessage.error('无法打开打印窗口，请检查浏览器设置')
      return
    }
    
    // 构建打印内容
    const printContent = `
      <html>
      <head>
        <title>总账打印</title>
        <style>
          body {
            font-family: Arial, sans-serif;
            margin: 20px;
          }
          h1 {
            text-align: center;
            color: #333;
          }
          table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
          }
          th, td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: center;
          }
          th {
            background-color: #f2f2f2;
            font-weight: bold;
          }
          tr:nth-child(even) {
            background-color: #f9f9f9;
          }
          .print-info {
            text-align: right;
            margin-bottom: 20px;
            color: #666;
          }
        </style>
      </head>
      <body>
        <h1>ERP系统总账</h1>
        <div class="print-info">
          打印日期: ${new Date().toLocaleString()}
        </div>
        <table>
          <thead>
            <tr>
              <th>科目编码</th>
              <th>科目名称</th>
              <th>科目类型</th>
              <th>期初余额</th>
              <th>本期借方发生额</th>
              <th>本期贷方发生额</th>
              <th>期末余额</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            ${printData.map((row: any) => `
              <tr>
                <td>${row.account_code}</td>
                <td>${row.account_name}</td>
                <td>${row.account_type}</td>
                <td>${formatCurrency(row.beginning_balance)}</td>
                <td>${formatCurrency(row.debit_amount)}</td>
                <td>${formatCurrency(row.credit_amount)}</td>
                <td>${formatCurrency(row.ending_balance)}</td>
                <td>${row.status}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </body>
      </html>
    `
    
    // 写入打印内容并触发打印
    printWindow.document.write(printContent)
    printWindow.document.close()
    
    // 等待打印窗口加载完成后触发打印
    printWindow.onload = () => {
      printWindow.print()
      // 打印完成后关闭窗口
      printWindow.onafterprint = () => {
        printWindow.close()
        ledgerLoading.value = false
        ElMessage.success('总账打印完成')
      }
    }
    
  } catch (error) {
    ledgerLoading.value = false
    printPreviewVisible.value = false
    ElMessage.error('打印失败，请稍后重试')
    ErrorHandler.handleApiError(error, false)
  }
}

// 打印总账（保留原函数用于兼容）
const handlePrintLedger = async () => {
  // 先显示预览
  previewData.value = selectedRows.value.length > 0 ? selectedRows.value : ledgerList.value
  printPreviewVisible.value = true
}



// 更新进度条
const updateProgress = (progress: number) => {
  exportProgress.value = progress
}

// 导出为CSV
const exportToCSV = async (data: any[]) => {
  try {
    updateProgress(30)
    
    // 构建CSV内容
    const headers = ['科目编码', '科目名称', '科目类型', '期初余额', '本期借方发生额', '本期贷方发生额', '期末余额', '状态']
    const csvContent = [
      headers.join(','), // 表头
      ...data.map((row: any) => [
        row.account_code,
        `"${row.account_name}"`, // 处理包含逗号的字符串
        `"${row.account_type}"`,
        row.beginning_balance,
        row.debit_amount,
        row.credit_amount,
        row.ending_balance,
        `"${row.status}"`
      ].join(','))
    ].join('\n')
    
    updateProgress(60)
    
    // 创建Blob对象
    const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
    
    updateProgress(90)
    
    // 创建下载链接
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `总账_${new Date().toISOString().slice(0, 10)}.csv`)
    link.style.visibility = 'hidden'
    
    // 添加到文档并触发下载
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    // 释放URL对象
    URL.revokeObjectURL(url)
    
    return true
  } catch (error) {
    logger.error('导出CSV失败:', error)
    return false
  }
}

// 导出为Excel
const exportToExcel = async (data: any[]) => {
  try {
    updateProgress(30)
    
    // 准备Excel数据
    const headers = ['科目编码', '科目名称', '科目类型', '期初余额', '本期借方发生额', '本期贷方发生额', '期末余额', '状态']
    const worksheetData = [
      headers,
      ...data.map((row: any) => [
        row.account_code,
        row.account_name,
        row.account_type,
        parseFloat(row.beginning_balance),
        parseFloat(row.debit_amount),
        parseFloat(row.credit_amount),
        parseFloat(row.ending_balance),
        row.status
      ])
    ]
    
    updateProgress(60)
    
    // 创建工作簿和工作表
    const worksheet = XLSX.utils.aoa_to_sheet(worksheetData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '总账数据')
    
    updateProgress(90)
    
    // 导出文件
    XLSX.writeFile(workbook, `总账_${new Date().toISOString().slice(0, 10)}.xlsx`)
    
    return true
  } catch (error) {
    logger.error('导出Excel失败:', error)
    return false
  }
}

// 导出为PDF
const exportToPDF = async (data: any[]) => {
  try {
    updateProgress(30)
    
    // 创建临时元素用于生成PDF
    const tempElement = document.createElement('div')
    tempElement.innerHTML = `
      <div style="font-family: Arial, sans-serif; margin: 20px; padding: 20px; background: white;">
        <h1 style="text-align: center; color: #333;">ERP系统总账</h1>
        <div style="text-align: right; margin-bottom: 20px; color: #666;">
          打印日期: ${new Date().toLocaleString()}
        </div>
        <table style="width: 100%; border-collapse: collapse; margin-top: 20px;">
          <thead>
            <tr>
              <th style="border: 1px solid #ddd; padding: 8px; text-align: center; background-color: #f2f2f2; font-weight: bold;">科目编码</th>
              <th style="border: 1px solid #ddd; padding: 8px; text-align: center; background-color: #f2f2f2; font-weight: bold;">科目名称</th>
              <th style="border: 1px solid #ddd; padding: 8px; text-align: center; background-color: #f2f2f2; font-weight: bold;">科目类型</th>
              <th style="border: 1px solid #ddd; padding: 8px; text-align: center; background-color: #f2f2f2; font-weight: bold;">期初余额</th>
              <th style="border: 1px solid #ddd; padding: 8px; text-align: center; background-color: #f2f2f2; font-weight: bold;">本期借方发生额</th>
              <th style="border: 1px solid #ddd; padding: 8px; text-align: center; background-color: #f2f2f2; font-weight: bold;">本期贷方发生额</th>
              <th style="border: 1px solid #ddd; padding: 8px; text-align: center; background-color: #f2f2f2; font-weight: bold;">期末余额</th>
              <th style="border: 1px solid #ddd; padding: 8px; text-align: center; background-color: #f2f2f2; font-weight: bold;">状态</th>
            </tr>
          </thead>
          <tbody>
            ${data.map((row: any) => `
              <tr>
                <td style="border: 1px solid #ddd; padding: 8px; text-align: center;">${row.account_code}</td>
                <td style="border: 1px solid #ddd; padding: 8px; text-align: center;">${row.account_name}</td>
                <td style="border: 1px solid #ddd; padding: 8px; text-align: center;">${row.account_type}</td>
                <td style="border: 1px solid #ddd; padding: 8px; text-align: center;">${formatCurrency(row.beginning_balance)}</td>
                <td style="border: 1px solid #ddd; padding: 8px; text-align: center;">${formatCurrency(row.debit_amount)}</td>
                <td style="border: 1px solid #ddd; padding: 8px; text-align: center;">${formatCurrency(row.credit_amount)}</td>
                <td style="border: 1px solid #ddd; padding: 8px; text-align: center;">${formatCurrency(row.ending_balance)}</td>
                <td style="border: 1px solid #ddd; padding: 8px; text-align: center;">${row.status}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `
    tempElement.style.position = 'absolute'
    tempElement.style.left = '-9999px'
    document.body.appendChild(tempElement)
    
    updateProgress(50)
    
    // 使用html2canvas生成canvas
    const canvas = await html2canvas(tempElement, {
      scale: 2, // 提高分辨率
      useCORS: true,
      logging: false
    })
    
    updateProgress(70)
    
    // 移除临时元素
    document.body.removeChild(tempElement)
    
    // 创建PDF文档
    const pdf = new jsPDF({
      orientation: 'portrait',
      unit: 'mm',
      format: 'a4'
    })
    
    // 计算页面尺寸
    const imgWidth = 210 // A4宽度
    const imgHeight = canvas.height * imgWidth / canvas.width
    let heightLeft = imgHeight
    let position = 0
    
    // 添加第一页
    pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= 297 // A4高度
    
    // 如果内容超过一页，添加更多页面
    while (heightLeft >= 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= 297
    }
    
    updateProgress(90)
    
    // 保存PDF
    pdf.save(`总账_${new Date().toISOString().slice(0, 10)}.pdf`)
    
    return true
  } catch (error) {
    logger.error('导出PDF失败:', error)
    return false
  }
}

// 处理导出格式选择
const confirmExportFormat = async () => {
  try {
    ledgerLoading.value = true
    exportFormatVisible.value = false
    exportProgress.value = 0
    
    // 准备导出数据
    const exportData = selectedRows.value.length > 0 ? selectedRows.value : ledgerList.value
    

    
    let success = false
    
    // 根据选择的格式导出
    switch (selectedExportFormat.value) {
      case 'csv':
        success = await exportToCSV(exportData)
        break
      case 'excel':
        success = await exportToExcel(exportData)
        break
      case 'pdf':
        success = await exportToPDF(exportData)
        break
      default:
        ElMessage.error('不支持的导出格式')
        return
    }
    
    if (success) {
      ElMessage.success('总账导出完成')
    } else {
      ElMessage.error('导出失败，请稍后重试')
    }
    
  } catch (error) {
    ElMessage.error('导出失败，请稍后重试')
    ErrorHandler.handleApiError(error, false)
  } finally {
    ledgerLoading.value = false
    exportProgress.value = 0 // 重置进度条
  }
}

// 导出总账（保留原函数用于兼容）
const handleExportLedger = async () => {
  // 显示导出格式选择
  exportFormatVisible.value = true
}
</script>

<style scoped lang="scss">
.general-ledger-view {
  padding: 20px;
  
  .module-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .card-header {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 20px;
      font-weight: bold;
      color: #1890ff;
      
      .data-source-tag {
        margin-left: auto;
        font-size: 12px;
      }
    }
  }
  
  // 金额样式
  :deep(.negative) {
    color: #f56c6c;
  }
  
  // 表格样式优化
  :deep(.el-table) {
    .el-table__header-wrapper {
      position: sticky;
      top: 0;
      z-index: 10;
    }
    
    .el-table__body-wrapper {
      overflow-y: auto;
      max-height: calc(100vh - 300px);
    }
    
    // 排序图标颜色
    .el-table__sort-indicator {
      color: #1890ff;
    }
    
    // 表头样式
    th.el-table__cell {
      background-color: #f5f7fa;
      font-weight: bold;
    }
    
    // 行样式
    tr.el-table__row:hover {
      background-color: #f0f9eb;
    }
  }
  
  // 筛选区域样式
  :deep(.filter-form) {
    margin-bottom: 20px;
    padding: 16px;
    background-color: #f5f7fa;
    border-radius: 8px;
    border: 1px solid #e4e7ed;
  }
  
  // 操作按钮样式
  :deep(.table-actions) {
    margin-bottom: 20px;
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
}

// 导出进度条样式
.export-progress {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f0f9eb;
  border-radius: 8px;
  border: 1px solid #c3e6cb;
}

// 打印预览样式
.print-preview-content {
  max-height: 60vh;
  overflow-y: auto;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
  
  h1 {
    text-align: center;
    color: #333;
    margin-bottom: 20px;
  }
  
  .print-info {
    text-align: right;
    margin-bottom: 20px;
    color: #666;
    font-size: 14px;
  }
  
  .preview-table {
    width: 100%;
    border-collapse: collapse;
    background-color: white;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    
    th,
    td {
      border: 1px solid #ddd;
      padding: 8px;
      text-align: center;
    }
    
    th {
      background-color: #f2f2f2;
      font-weight: bold;
      color: #333;
    }
    
    tr:nth-child(even) {
      background-color: #f9f9f9;
    }
    
    tr:hover {
      background-color: #f5f5f5;
    }
  }
}

// 导出格式选择样式
.export-format-options {
  padding: 20px 0;
  
  .format-radio-group {
    width: 100%;
    display: flex;
    justify-content: center;
    gap: 10px;
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .general-ledger-view {
    padding: 16px;
    
    :deep(.el-table) {
      .el-table__body-wrapper {
        max-height: calc(100vh - 250px);
      }
    }
  }
}

@media (max-width: 768px) {
  .general-ledger-view {
    padding: 12px;
    
    .module-card {
      .card-header {
        font-size: 18px;
      }
    }
    
    :deep(.el-table) {
      .el-table__body-wrapper {
        max-height: calc(100vh - 200px);
      }
      
      th.el-table__cell,
      td.el-table__cell {
        padding: 8px;
        font-size: 12px;
      }
    }
    
    :deep(.filter-form) {
      padding: 12px;
    }
    
    :deep(.table-actions) {
      flex-direction: column;
      gap: 8px;
    }
    
    // 响应式预览表格
    .preview-table {
      font-size: 12px;
      
      th,
      td {
        padding: 6px;
      }
    }
  }
}
</style>

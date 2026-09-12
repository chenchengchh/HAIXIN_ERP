<template>
  <div class="table-component">
    <el-card shadow="hover" class="table-card">
      <!-- 筛选条件区域 -->
      <div 
        v-if="showFilter" 
        class="filter-section" 
        ref="filterSection"
        v-show="isFilterVisible"
      >
        <el-form :model="searchForm" :inline="true" label-position="top" class="filter-form">
          <el-form-item v-for="(filter, index) in filters" :key="index" :label="filter.label">
            <!-- 文本输入框 -->
            <el-input
              v-if="filter.type === 'input'"
              v-model="searchForm[filter.prop]"
              :placeholder="filter.placeholder || `请输入${filter.label}`"
              clearable
              @keyup.enter="handleSearch"
            >
              <!-- 高级搜索选项 -->
              <template #append>
                <el-dropdown @command="(command: string) => handleAdvancedSearch(filter.prop, command)">
                  <el-button class="append-btn">
                    <el-icon><Search /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="contains">包含</el-dropdown-item>
                      <el-dropdown-item command="startWith">开头是</el-dropdown-item>
                      <el-dropdown-item command="endWith">结尾是</el-dropdown-item>
                      <el-dropdown-item command="equal">等于</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-input>
            
            <!-- 下拉选择框 -->
            <el-select
              v-else-if="filter.type === 'select'"
              v-model="searchForm[filter.prop]"
              :placeholder="filter.placeholder || `请选择${filter.label}`"
              clearable
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="option in filter.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
            
            <!-- 日期选择器 -->
            <el-date-picker
              v-else-if="filter.type === 'date'"
              v-model="searchForm[filter.prop]"
              type="date"
              :placeholder="filter.placeholder || `请选择${filter.label}`"
              clearable
              style="width: 100%"
            />
            
            <!-- 日期范围选择器 -->
            <el-date-picker
              v-else-if="filter.type === 'daterange'"
              v-model="searchForm[filter.prop]"
              type="daterange"
              :placeholder="filter.placeholder || `请选择${filter.label}`"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              clearable
              style="width: 100%"
            />
            
            <!-- 数字范围选择器 -->
            <el-input-number
              v-else-if="filter.type === 'number'"
              v-model="searchForm[filter.prop]"
              :placeholder="filter.placeholder || `请输入${filter.label}`"
              :min="filter.min || 0"
              :max="filter.max || Infinity"
              :step="filter.step || 1"
              style="width: 100%"
            />
          </el-form-item>
          
          <!-- 操作按钮 -->
          <el-form-item class="filter-actions">
            <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 查询</el-button>
            <el-button @click="handleReset"><el-icon><Refresh /></el-icon> 重置</el-button>
            <el-button v-if="showExport" type="success" @click="handleExport"><el-icon><Download /></el-icon> 导出</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 表格操作区域 -->
      <div class="table-action-section">
        <div class="action-left">
          <!-- 批量操作按钮 -->
          <el-dropdown v-if="showSelection && selectedRows.length > 0 && batchActionsToShow.length > 0" trigger="click" @command="handleBatchOperation">
            <el-button type="primary" plain>
              批量操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="item in batchActionsToShow"
                  :key="item.command"
                  :command="item.command"
                >
                  {{ item.text }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <el-button
            v-for="action in tableActions"
            :key="action.key"
            :type="action.type || 'default'"
            :size="action.size || 'default'"
            @click="action.handler"
            :icon="action.icon"
          >
            {{ action.text }}
          </el-button>
        </div>
        <div class="action-right">
          <el-tooltip content="刷新" placement="top">
             <el-button circle :icon="Refresh" @click="handleSearch" />
          </el-tooltip>
          <el-button
            type="primary"
            link
            v-if="showFilter"
            @click="toggleFilter"
          >
            <el-icon><Filter /></el-icon>
            {{ isFilterVisible ? '收起筛选' : '展开筛选' }}
          </el-button>
        </div>
      </div>
      
      <!-- 表格主体 -->
      <div class="table-wrapper">
        <el-table
          v-loading="loading"
          :data="safeData"
          :height="tableHeight || '600px'"
          border
          stripe
          @selection-change="handleSelectionChange"
          @row-click="handleRowClick"
          :row-class-name="getRowClassName"
          highlight-current-row
          :virtual-scroll="Boolean(props.virtualScroll) && safeData.length > 0"
          :virtual-scroll-threshold="Number(props.virtualScrollThreshold || 100)"
          :optimize-rendering="Boolean(props.optimizeRendering)"
          :cell-lazy-load="Boolean(props.cellLazyLoad)"
          :auto-width="Boolean(props.autoWidth)"
          :row-height="Number(props.rowHeight || 50)"
          :empty-text="!loading && safeData.length === 0 ? '暂无数据' : ''"
          header-cell-class-name="table-header-cell"
        >
          <!-- 多选框列 -->
          <el-table-column
            v-if="showSelection"
            type="selection"
            width="50"
            fixed="left"
            align="center"
          />
          
          <!-- 序号列 -->
          <el-table-column
            v-if="showIndex"
            type="index"
            label="序号"
            width="60"
            fixed="left"
            align="center"
            :index="indexMethod"
          />
          
          <!-- 虚拟滚动行号列 -->
          <el-table-column
            v-if="props.virtualScroll && props.showRowNumber"
            type="index"
            label="行号"
            width="60"
            fixed="left"
            align="center"
            :index="indexMethod"
          />
          
          <!-- 自定义列 -->
          <el-table-column
            v-for="column in columns"
            :key="column.prop"
            :prop="column.prop"
            :label="column.label"
            :width="column.width"
            :min-width="column.minWidth"
            :fixed="column.fixed"
            :sortable="column.sortable"
            :sort-orders="column.sortOrders || ['ascending', 'descending', null]"
            :align="column.align || 'center'"
            :header-align="column.headerAlign || 'center'"
            show-overflow-tooltip
          >
            <!-- 自定义模板 -->
            <template #default="scope">
              <slot v-if="column.slotName" :name="column.slotName" :row="scope.row" :index="scope.$index" />
              <!-- 状态标签 -->
              <el-tag
                v-else-if="column.type === 'status'"
                :type="getColumnStatusType(column, scope.row)"
                size="small"
                effect="light"
              >
                {{ getColumnStatusLabel(column, scope.row) }}
              </el-tag>
              <!-- 日期格式化 -->
              <span v-else-if="column.type === 'date'">
                {{ formatDate(scope.row[column.prop], column.format || 'YYYY-MM-DD') }}
              </span>
              <!-- 日期时间格式化 -->
              <span v-else-if="column.type === 'datetime'">
                {{ formatDate(scope.row[column.prop], column.format || 'YYYY-MM-DD HH:mm:ss') }}
              </span>
              <!-- 金额格式化 -->
              <span v-else-if="column.type === 'money'" class="money-cell">
                {{ formatMoney(scope.row[column.prop], column.precision || 2) }}
              </span>
              <!-- 默认显示 -->
              <span v-else>{{ scope.row[column.prop] }}</span>
            </template>
          </el-table-column>
          
          <!-- 操作列 -->
          <el-table-column
            v-if="showAction || (actions || []).length > 0"
            label="操作"
            width="180"
            fixed="right"
            align="center"
            header-align="center"
          >
            <template #default="scope">
              <div class="action-buttons">
                <slot v-if="actionSlotName" :name="actionSlotName" :row="scope.row" :index="scope.$index" />
                <template v-else>
                  <el-button
                    v-for="(action, index) in actions"
                    :key="index"
                    :type="action.type || 'primary'"
                    :size="action.size || 'small'"
                    :link="action.link !== false" 
                    :disabled="action.disabled && action.disabled(scope.row)"
                    @click="action.handler && action.handler(scope.row, scope.$index)"
                  >
                    <el-icon v-if="action.icon"><component :is="action.icon" /></el-icon>
                    {{ action.text }}
                  </el-button>
                </template>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页区域 -->
      <div class="pagination-section">
        <div class="pagination-info">
          <span v-if="showSelection">已选择 <b>{{ selectedRows.length }}</b> 项</span>
        </div>
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="pagination.pageSizes || [10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            background
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Filter, Search, Refresh, Download, ArrowDown } from '@element-plus/icons-vue'
import { logger } from '../../utils/logger'
// import { ElMessage } from 'element-plus'

// 组件属性定义
interface TableColumn {
  prop: string
  label: string
  width?: number | string
  minWidth?: number | string
  fixed?: boolean | string
  sortable?: boolean
  sortOrders?: string[]
  align?: string
  headerAlign?: string
  type?: 'status' | 'date' | 'datetime' | 'money'
  format?: string
  precision?: number
  statusMap?: Record<string, { label: string; type: string }>
  slotName?: string
}

interface TableAction {
  key: string
  text: string
  type?: string
  size?: string
  icon?: any
  handler: () => void
}

interface Action {
  text: string
  type?: string
  size?: string
  link?: boolean
  icon?: any
  handler?: (row: any, index: number) => void
  disabled?: (row: any) => boolean
}

interface Filter {
  prop: string
  label: string
  type: 'input' | 'select' | 'date' | 'daterange' | 'number'
  placeholder?: string
  options?: Array<{ value: any; label: string }>
  min?: number
  max?: number
  step?: number
}

interface Pagination {
  currentPage: number
  pageSize: number
  pageSizes?: number[]
}

interface BatchAction {
  command: string
  text: string
}

// 组件属性
const props = defineProps<{
  // 表格数据
  data: Array<any>
  // 表格列配置
  columns: Array<TableColumn>
  // 总数
  total: number
  // 加载状态
  loading?: boolean
  // 是否显示序号列
  showIndex?: boolean
  // 是否显示选择列
  showSelection?: boolean
  // 是否显示操作列
  showAction?: boolean
  // 操作列插槽名称
  actionSlotName?: string
  // 行操作按钮配置
  actions?: Array<Action>
  // 表格操作按钮配置
  tableActions?: Array<TableAction>
  batchActions?: Array<BatchAction>
  // 筛选条件配置
  filters?: Array<Filter>
  // 是否显示筛选区域
  showFilter?: boolean
  // 是否显示导出按钮
  showExport?: boolean
  // 表格高度
  tableHeight?: number | string
  // 初始筛选条件
  initialFilter?: Record<string, any>
  // 每页大小选项
  pageSizes?: number[]
  // ------------------------ 大数据优化相关属性 ------------------------
  // 是否启用虚拟滚动
  virtualScroll?: boolean
  // 虚拟滚动阈值
  virtualScrollThreshold?: number
  // 是否优化渲染（减少不必要的计算）
  optimizeRendering?: boolean
  // 单元格内容是否懒加载
  cellLazyLoad?: boolean
  // 列宽是否自动计算
  autoWidth?: boolean
  // 是否显示行号（独立于序号列，用于虚拟滚动场景）
  showRowNumber?: boolean
  // 行高（用于虚拟滚动计算）
  rowHeight?: number
}>()

// 组件事件
const emit = defineEmits<{
  // 搜索事件
  search: [params: any]
  // 分页大小改变事件
  sizeChange: [size: number]
  // 页码改变事件
  currentChange: [currentPage: number]
  // 选择行事件
  selectionChange: [selectedRows: Array<any>]
  // 导出事件
  export: [params: any]
  // 批量操作事件
  batchOperation: [operation: string, selectedRows: Array<any>]
  // 高级搜索事件
  advancedSearch: [searchConfig: any]
  // 行点击事件
  rowClick: [row: any, event: any]
}>()

// 响应式数据
// 搜索表单
const searchForm = reactive<Record<string, any>>({})
// 高级搜索配置，保存每个字段的搜索方式
const advancedSearchConfig = reactive<Record<string, string>>({})
// 选中行
const selectedRows = ref<Array<any>>([])
// 当前选中行
const currentRow = ref<any>(null)
// 分页配置
const pagination = reactive<Pagination>({
  currentPage: 1,
  pageSize: 10,
  pageSizes: props.pageSizes || [10, 20, 50, 100]
})
// 筛选区域显隐
const isFilterVisible = ref(true)
// 筛选条件本地存储键名
const filterStorageKey = ref('')

// 安全数据，确保总是数组类型，避免TypeError: data2 is not iterable错误
const safeData = computed(() => {
  return Array.isArray(props.data) ? props.data : []
})

const batchActionsToShow = computed(() => {
  if (props.batchActions !== undefined) return props.batchActions
  return [
    { command: 'batch-delete', text: '批量删除' },
    { command: 'batch-approve', text: '批量审核' },
    { command: 'batch-reject', text: '批量拒绝' },
    { command: 'batch-export', text: '批量导出' }
  ]
})

// 初始化
onMounted(() => {
  // 生成唯一的存储键，基于过滤器配置
  if (props.filters && props.filters.length > 0) {
    const filterProps = props.filters.map(f => f.prop).join('_')
    filterStorageKey.value = `table_filter_${filterProps}`
    
    // 从本地存储加载筛选区域显隐状态
    const savedFilterVisible = localStorage.getItem(filterStorageKey.value + '_visible')
    if (savedFilterVisible !== null) {
      isFilterVisible.value = savedFilterVisible === 'true'
    }
    
    // 从本地存储加载筛选环境
    const savedFilters = localStorage.getItem(filterStorageKey.value)
    let parsedFilters: Record<string, any> = {}
    if (savedFilters) {
      try {
        parsedFilters = JSON.parse(savedFilters)
      } catch (error) {
        logger.error('解析保存的筛选条件失败:', error)
      }
    }

    // 初始化搜索表单
    props.filters.forEach(filter => {
      // 优先使用本地存储，其次使用初始值
      let defaultValue = parsedFilters[filter.prop] !== undefined 
        ? parsedFilters[filter.prop] 
        : (props.initialFilter?.[filter.prop] !== undefined ? props.initialFilter[filter.prop] : '')
      
      // 特殊处理日期范围，必须是数组
      if (filter.type === 'daterange' && !Array.isArray(defaultValue)) {
        defaultValue = []
      }
      
      searchForm[filter.prop] = defaultValue
    })
  }
})


// 序号生成方法
const indexMethod = (index: number) => {
  return (pagination.currentPage - 1) * pagination.pageSize + index + 1
}

// 处理搜索
const handleSearch = () => {
  // 保存筛选条件到本地存储
  if (filterStorageKey.value) {
    localStorage.setItem(filterStorageKey.value, JSON.stringify(searchForm))
  }
  
  emit('search', {
    ...searchForm,
    page: pagination.currentPage,
    size: pagination.pageSize,
    advancedSearch: { ...advancedSearchConfig }
  })
}

// 处理重置
const handleReset = () => {
  // 重置搜索表单
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  // 重置高级搜索配置
  Object.keys(advancedSearchConfig).forEach(key => {
    delete advancedSearchConfig[key]
  })
  // 重置页码
  pagination.currentPage = 1
  // 执行搜索
  handleSearch()
}

// 处理导出
const handleExport = () => {
  emit('export', searchForm)
}

// 切换筛选区域显隐
const toggleFilter = () => {
  isFilterVisible.value = !isFilterVisible.value
  
  // 保存筛选区域显隐状态到本地存储
  if (filterStorageKey.value) {
    localStorage.setItem(filterStorageKey.value + '_visible', isFilterVisible.value.toString())
  }
}

// 高级搜索处理
const handleAdvancedSearch = (field: string, command: string) => {
  // 保存当前字段的搜索方式
  advancedSearchConfig[field] = command
  // 触发高级搜索事件
  emit('advancedSearch', {
    field,
    searchMethod: command,
    searchForm: { ...searchForm },
    advancedSearchConfig: { ...advancedSearchConfig }
  })
  // 执行搜索
  handleSearch()
}

// 批量操作处理
const handleBatchOperation = (command: string) => {
  if (selectedRows.value.length === 0) {
    // 使用el-message提示用户选择数据
    import('element-plus').then(({ ElMessage }) => {
      ElMessage.warning('请选择要操作的数据')
    })
    return
  }
  
  // 根据操作类型显示不同的确认消息
  let confirmMessage = ''
  switch (command) {
    case 'batch-delete':
      confirmMessage = `确定要删除选中的${selectedRows.value.length}条数据吗？此操作不可恢复。`
      break
    case 'batch-approve':
      confirmMessage = `确定要审核通过选中的${selectedRows.value.length}条数据吗？`
      break
    case 'batch-reject':
      confirmMessage = `确定要拒绝选中的${selectedRows.value.length}条数据吗？`
      break
    case 'batch-export':
      confirmMessage = `确定要导出选中的${selectedRows.value.length}条数据吗？`
      break
    default:
      confirmMessage = `确定要对选中的${selectedRows.value.length}条数据执行此操作吗？`
  }
  
  // 使用el-confirm显示确认对话框
  import('element-plus').then(({ ElMessageBox }) => {
    ElMessageBox.confirm(confirmMessage, '操作确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      // 触发批量操作事件
      emit('batchOperation', command, selectedRows.value)
    }).catch(() => {
      // 用户取消操作
      import('element-plus').then(({ ElMessage }) => {
        ElMessage.info('已取消操作')
      })
    })
  })
}

// 处理分页大小改变
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  emit('sizeChange', size)
  emit('search', {
    ...searchForm,
    page: pagination.currentPage,
    size: pagination.pageSize
  })
}

// 处理页码改变
const handleCurrentChange = (currentPage: number) => {
  pagination.currentPage = currentPage
  emit('currentChange', currentPage)
  emit('search', {
    ...searchForm,
    page: pagination.currentPage,
    size: pagination.pageSize
  })
}

// 处理行选择
const handleSelectionChange = (rows: Array<any>) => {
  selectedRows.value = rows
  emit('selectionChange', rows)
}

// 处理行点击
const handleRowClick = (row: any, event: any) => {
  currentRow.value = row
  emit('rowClick', row, event)
}

// 获取行类名
const getRowClassName = ({ row }: { row: any }) => {
  return currentRow.value && currentRow.value.id === row.id ? 'table-row-highlight' : ''
}

// 获取列状态类型
const getColumnStatusType = (column: TableColumn, row: any) => {
  const statusValue = row[column.prop]
  if (column.statusMap && column.statusMap[statusValue]) {
    return column.statusMap[statusValue].type
  }
  return ''
}

// 获取列状态标签
const getColumnStatusLabel = (column: TableColumn, row: any) => {
  const statusValue = row[column.prop]
  if (column.statusMap && column.statusMap[statusValue]) {
    return column.statusMap[statusValue].label
  }
  return statusValue
}

// 日期格式化
const formatDate = (date: any, format: string) => {
  if (!date) return ''
  try {
    const d = new Date(date)
    if (isNaN(d.getTime())) return ''
    
    // 简单的格式化实现，实际项目中可以使用dayjs或date-fns
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    const seconds = String(d.getSeconds()).padStart(2, '0')
    
    return format
      .replace('YYYY', String(year))
      .replace('MM', month)
      .replace('DD', day)
      .replace('HH', hours)
      .replace('mm', minutes)
      .replace('ss', seconds)
  } catch (error) {
    return date
  }
}

// 金额格式化
const formatMoney = (money: any, precision: number) => {
  if (money === undefined || money === null) return ''
  return Number(money).toFixed(precision)
}
</script>

<style scoped lang="scss">
.table-component {
  width: 100%;
  
  .table-card {
    :deep(.el-card__body) {
      padding: 20px;
    }
  }

  .filter-section {
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    
    .filter-form {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      
      .el-form-item {
        margin-bottom: 10px;
        margin-right: 0;
        
        &.filter-actions {
          margin-left: auto;
        }
      }
    }
  }
  
  .table-action-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .action-left {
      display: flex;
      gap: 10px;
    }
    
    .action-right {
      display: flex;
      gap: 10px;
    }
  }
  
  .table-wrapper {
    margin-bottom: 20px;
    
    :deep(.el-table) {
      // 优化表格头部样式
      th.table-header-cell {
        background-color: var(--el-fill-color-light);
        color: var(--el-text-color-primary);
        font-weight: 600;
        height: 50px;
      }
      
      // 优化选中行样式
      .el-table__row.table-row-highlight {
        background-color: var(--el-color-primary-light-9);
      }
    }
    
    .action-buttons {
      display: flex;
      justify-content: center;
      gap: 8px;
      flex-wrap: wrap;
    }
    
    .money-cell {
      font-family: monospace;
    }
  }
  
  .pagination-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 20px;
    
    .pagination-info {
      color: var(--el-text-color-secondary);
      font-size: 14px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .table-component {
    .filter-section {
      .filter-form {
        flex-direction: column;
        
        .el-form-item {
          width: 100%;
          margin-right: 0;
          
          :deep(.el-form-item__content) {
            width: 100%;
          }
          
          &.filter-actions {
            margin-left: 0;
            justify-content: flex-end;
          }
        }
      }
    }
    
    .table-action-section {
      flex-direction: column;
      align-items: flex-start;
      gap: 10px;
      
      .action-right {
        align-self: flex-end;
      }
    }
    
    .pagination-section {
      flex-direction: column;
      gap: 10px;
      align-items: flex-end;
    }
  }
}
</style>

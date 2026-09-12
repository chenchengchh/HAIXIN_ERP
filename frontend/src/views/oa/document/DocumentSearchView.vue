<template>
  <div class="document-search-view">
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-input
        v-model="searchQuery"
        placeholder="输入关键词搜索文档"
        clearable
        @keyup.enter="handleSearch"
        size="large"
      >
        <template #append>
          <el-button type="primary" size="large" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </template>
      </el-input>
      
      <!-- 搜索条件 -->
      <div class="search-conditions">
        <el-collapse v-model="activeConditions">
          <el-collapse-item title="高级搜索" name="advanced">
            <el-form :model="advancedSearchParams" label-width="80px" size="small">
              <div class="form-row">
                <el-form-item label="文档类型">
                  <el-select v-model="advancedSearchParams.fileType" placeholder="选择文档类型">
                    <el-option label="全部" value="" />
                    <el-option label="PDF" value="pdf" />
                    <el-option label="Word" value="doc" />
                    <el-option label="Excel" value="xls" />
                    <el-option label="PPT" value="ppt" />
                    <el-option label="图片" value="jpg" />
                    <el-option label="压缩包" value="zip" />
                  </el-select>
                </el-form-item>
                <el-form-item label="所属分类">
                  <el-select v-model="advancedSearchParams.categoryId" placeholder="选择分类">
                    <el-option label="全部" value="" />
                    <el-option v-for="category in categories" :key="category.id" :label="category.categoryName" :value="category.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="上传人">
                  <el-input v-model="advancedSearchParams.uploaderName" placeholder="输入上传人" />
                </el-form-item>
              </div>
              <div class="form-row">
                <el-form-item label="上传时间">
                  <el-date-picker
                    v-model="advancedSearchParams.uploadTime"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    size="small"
                  />
                </el-form-item>
                <el-form-item label="文件大小">
                  <el-input-number
                    v-model="advancedSearchParams.minSize"
                    placeholder="最小(MB)"
                    :min="0"
                    size="small"
                  />
                  <span class="size-separator">-</span>
                  <el-input-number
                    v-model="advancedSearchParams.maxSize"
                    placeholder="最大(MB)"
                    :min="0"
                    size="small"
                  />
                </el-form-item>
              </div>
              <div class="form-actions">
                <el-button type="primary" size="small" @click="handleSearch">搜索</el-button>
                <el-button size="small" @click="resetAdvancedSearch">重置</el-button>
              </div>
            </el-form>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
    
    <!-- 搜索结果 -->
    <div class="search-results" v-if="showResults">
      <div class="results-header">
        <span class="results-count">找到 <span class="count">{{ searchResults.length }}</span> 个结果</span>
        <div class="sort-options">
          <span class="sort-label">排序：</span>
          <el-select v-model="sortBy" placeholder="选择排序方式" size="small">
            <el-option label="相关度" value="relevance" />
            <el-option label="上传时间（最新）" value="uploadTimeDesc" />
            <el-option label="上传时间（最早）" value="uploadTimeAsc" />
            <el-option label="文件大小（从大到小）" value="fileSizeDesc" />
            <el-option label="文件大小（从小到大）" value="fileSizeAsc" />
          </el-select>
        </div>
      </div>
      
      <!-- 搜索结果列表 -->
      <div class="results-list">
        <el-card class="result-card" v-for="result in searchResults" :key="result.id" shadow="hover">
          <div class="card-content">
            <div class="result-header">
              <h3>
                <el-link type="primary" @click="viewDocument(result)">{{ result.documentName }}</el-link>
              </h3>
              <div class="result-meta">
                <el-tag size="small">{{ result.fileType }}</el-tag>
                <span class="file-size">{{ formatFileSize(result.fileSize) }}</span>
                <span class="upload-time">{{ result.uploadTime }}</span>
              </div>
            </div>
            
            <div class="result-body">
              <div class="result-preview">
                <el-icon class="preview-icon"><Document /></el-icon>
                <div class="preview-content">
                  <p>{{ result.previewContent }}</p>
                </div>
              </div>
              
              <div class="result-info">
                <div class="info-item">
                  <el-icon><User /></el-icon>
                  <span>上传人：{{ result.uploaderName }}</span>
                </div>
                <div class="info-item">
                  <el-icon><Folder /></el-icon>
                  <span>分类：{{ result.categoryName }}</span>
                </div>
                <div class="info-item">
                  <el-icon><View /></el-icon>
                  <span>浏览次数：{{ result.viewCount }}</span>
                </div>
                <div class="info-item">
                  <el-icon><Download /></el-icon>
                  <span>下载次数：{{ result.downloadCount }}</span>
                </div>
              </div>
              
              <div class="result-tags">
                <el-tag size="small" v-for="tag in result.tags" :key="tag" type="info">{{ tag }}</el-tag>
              </div>
            </div>
            
            <div class="result-footer">
              <el-button type="primary" size="small" @click="viewDocument(result)">
                <el-icon><View /></el-icon>
                预览
              </el-button>
              <el-button type="success" size="small" @click="downloadDocument(result)">
                <el-icon><Download /></el-icon>
                下载
              </el-button>
              <el-button type="info" size="small" @click="copyLink(result)">
                <el-icon><Link /></el-icon>
                复制链接
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 搜索建议 -->
    <div class="search-suggestions" v-if="showSuggestions && searchSuggestions.length > 0">
      <h4>搜索建议</h4>
      <div class="suggestions-list">
        <el-tag
          v-for="suggestion in searchSuggestions"
          :key="suggestion"
          type="info"
          effect="plain"
          @click="selectSuggestion(suggestion)"
        >
          {{ suggestion }}
        </el-tag>
      </div>
    </div>
    
    <!-- 搜索历史 -->
    <div class="search-history" v-if="showHistory && searchHistory.length > 0">
      <div class="history-header">
        <h4>搜索历史</h4>
        <el-button link @click="clearHistory">
          <el-icon><Delete /></el-icon>
          清空历史
        </el-button>
      </div>
      <div class="history-list">
        <el-tag
          v-for="history in searchHistory"
          :key="history"
          type="warning"
          effect="plain"
          @click="selectSuggestion(history)"
        >
          {{ history }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Document, User, Folder, View, Download, Link, Delete } from '@element-plus/icons-vue'

// 搜索查询
const searchQuery = ref('')

// 搜索条件
const activeConditions = ref(['advanced'])

// 高级搜索参数
const advancedSearchParams = reactive({
  fileType: '',
  categoryId: '',
  uploaderName: '',
  uploadTime: [],
  minSize: 0,
  maxSize: 0
})

// 分类列表
const categories = ref([
  { id: 1, categoryName: '技术文档' },
  { id: 2, categoryName: '产品文档' },
  { id: 3, categoryName: '运营文档' },
  { id: 4, categoryName: '财务文档' },
  { id: 5, categoryName: '人事文档' }
])

// 搜索结果
const searchResults = ref([
  {
    id: 1,
    documentName: '产品需求文档',
    fileType: 'pdf',
    fileSize: 1024 * 1024 * 2.5,
    uploadTime: '2025-12-01 10:00',
    uploaderName: '张三',
    categoryName: '产品文档',
    tags: ['产品', '需求', '文档'],
    previewContent: '这是一份产品需求文档，包含了产品的功能需求、非功能需求、用户故事等内容。文档详细描述了产品的核心功能和设计方案...',
    viewCount: 25,
    downloadCount: 15
  },
  {
    id: 2,
    documentName: '技术设计文档',
    fileType: 'doc',
    fileSize: 1024 * 1024 * 1.8,
    uploadTime: '2025-12-02 14:20',
    uploaderName: '李四',
    categoryName: '技术文档',
    tags: ['技术', '设计', '文档'],
    previewContent: '这是一份技术设计文档，包含了系统架构设计、数据库设计、接口设计等内容。文档详细描述了系统的技术实现方案...',
    viewCount: 32,
    downloadCount: 23
  },
  {
    id: 3,
    documentName: '财务报表',
    fileType: 'xls',
    fileSize: 1024 * 1024 * 0.8,
    uploadTime: '2025-12-03 16:45',
    uploaderName: '王五',
    categoryName: '财务文档',
    tags: ['财务', '报表', '2025'],
    previewContent: '这是一份2025年的财务报表，包含了公司的收入、支出、利润等财务数据。报表详细展示了公司的财务状况...',
    viewCount: 18,
    downloadCount: 8
  }
])

// 分页参数
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 排序方式
const sortBy = ref('relevance')

// 搜索建议
const searchSuggestions = ref(['产品需求', '技术设计', '财务报表', '运营计划', '员工手册'])

// 搜索历史
const searchHistory = ref(['产品需求', '技术设计', '财务报表'])

// 显示结果
const showResults = ref(false)
// 显示建议
const showSuggestions = ref(false)
// 显示历史
const showHistory = ref(true)

// 计算总条数
pagination.total = searchResults.value.length

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (size < 1024) {
    return size + ' B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB'
  } else {
    return (size / (1024 * 1024)).toFixed(2) + ' MB'
  }
}

// 处理搜索
const handleSearch = () => {
  console.log('搜索关键词:', searchQuery.value)
  console.log('高级搜索参数:', advancedSearchParams)
  // 这里应该调用API搜索文档
  showResults.value = true
  showSuggestions.value = false
  // 添加到搜索历史
  if (searchQuery.value && !searchHistory.value.includes(searchQuery.value)) {
    searchHistory.value.unshift(searchQuery.value)
    if (searchHistory.value.length > 10) {
      searchHistory.value.pop()
    }
  }
}

// 重置高级搜索
const resetAdvancedSearch = () => {
  Object.assign(advancedSearchParams, {
    fileType: '',
    categoryId: '',
    uploaderName: '',
    uploadTime: [],
    minSize: 0,
    maxSize: 0
  })
}

// 选择建议
const selectSuggestion = (suggestion: string) => {
  searchQuery.value = suggestion
  handleSearch()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  // 这里应该调用API获取数据
}

// 页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  // 这里应该调用API获取数据
}

// 查看文档
const viewDocument = (document: any) => {
  console.log('查看文档:', document)
  // 这里应该跳转到查看文档页面
}

// 下载文档
const downloadDocument = (document: any) => {
  console.log('下载文档:', document)
  // 这里应该调用API下载文档
}

// 复制链接
const copyLink = (document: any) => {
  console.log('复制链接:', document)
  // 这里应该复制文档链接到剪贴板
  ElMessage.success('链接复制成功')
}

// 清空搜索历史
const clearHistory = () => {
  searchHistory.value = []
  showHistory.value = false
  ElMessage.success('搜索历史已清空')
}
</script>

<style scoped>
.document-search-view {
  padding: 20px;
  box-sizing: border-box;
}

.search-section {
  margin-bottom: 20px;
}

.search-conditions {
  margin-top: 16px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.size-separator {
  margin: 0 8px;
  color: #909399;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}

.search-results {
  margin-bottom: 20px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.results-count {
  font-size: 16px;
  font-weight: bold;
}

.count {
  color: #409eff;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-label {
  color: #606266;
}

.results-list {
  margin-bottom: 20px;
}

.result-card {
  margin-bottom: 16px;
}

.card-content {
  padding: 10px 0;
}

.result-header {
  margin-bottom: 16px;
}

.result-header h3 {
  margin: 0 0 8px 0;
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #909399;
}

.file-size,
.upload-time {
  font-size: 12px;
}

.result-body {
  margin-bottom: 16px;
}

.result-preview {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background-color: #fafafa;
  border-radius: 4px;
}

.preview-icon {
  font-size: 24px;
  color: #409eff;
  align-self: flex-start;
}

.preview-content {
  flex: 1;
}

.preview-content p {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.result-info {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
}

.result-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.result-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.search-suggestions,
.search-history {
  margin-bottom: 20px;
}

.suggestions-list,
.history-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-header h4 {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .document-search-view {
    padding: 12px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .results-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .result-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .result-footer {
    flex-wrap: wrap;
  }
  
  .result-footer .el-button {
    flex: 1;
    min-width: 80px;
  }
}
</style>
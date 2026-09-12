<template>
  <div class="version-management-view">
    <!-- 搜索和筛选区域 -->
    <div class="search-filter">
      <el-form :inline="true" :model="searchParams" class="filter-form">
        <el-form-item label="文档名称">
          <el-input v-model="searchParams.documentName" placeholder="输入文档名称" size="small" />
        </el-form-item>
        <el-form-item label="文件类型">
          <el-select v-model="searchParams.fileType" placeholder="选择文件类型" size="small">
            <el-option label="全部" value="" />
            <el-option label="PDF" value="pdf" />
            <el-option label="Word" value="doc" />
            <el-option label="Excel" value="xls" />
            <el-option label="PPT" value="ppt" />
            <el-option label="图片" value="jpg" />
            <el-option label="压缩包" value="zip" />
          </el-select>
        </el-form-item>
        <el-form-item label="上传人">
          <el-input v-model="searchParams.uploaderName" placeholder="输入上传人姓名" size="small" />
        </el-form-item>
        <el-form-item label="版本时间">
          <el-date-picker
            v-model="searchParams.versionTime"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            size="small"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search" size="small">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="reset" size="small">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 版本列表 -->
    <div class="version-list">
      <el-card class="version-card" v-for="document in versionList" :key="document.id" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>{{ document.documentName }}</span>
            <el-tag size="small">{{ document.fileType }}</el-tag>
          </div>
        </template>
        <div class="card-content">
          <div class="document-info">
            <div class="info-item">
              <el-icon><Document /></el-icon>
              <span>文档ID: {{ document.id }}</span>
            </div>
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>上传人: {{ document.uploaderName }}</span>
            </div>
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <span>创建时间: {{ document.uploadTime }}</span>
            </div>
            <div class="info-item">
              <el-icon><DocumentCopy /></el-icon>
              <span>总版本数: {{ document.versionCount }}</span>
            </div>
          </div>
          
          <!-- 版本历史 -->
          <div class="version-history">
            <el-table :data="document.versions" stripe style="width: 100%" size="small">
              <el-table-column prop="versionNo" label="版本号" width="100" align="center">
                <template #default="scope">
                  <el-tag :type="scope.row.isCurrent ? 'success' : 'info'" size="small">
                    {{ scope.row.versionNo }}
                    <span v-if="scope.row.isCurrent"> (当前版本)</span>
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="changeLog" label="变更说明" min-width="200" />
              <el-table-column prop="fileSize" label="文件大小" width="120" align="center">
                <template #default="scope">
                  {{ formatFileSize(scope.row.fileSize) }}
                </template>
              </el-table-column>
              <el-table-column prop="uploaderName" label="上传人" width="120" align="center" />
              <el-table-column prop="createTime" label="上传时间" width="180" align="center" />
              <el-table-column label="操作" width="200" align="center">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="downloadVersion(scope.row)">
                    <el-icon><Download /></el-icon>
                    下载
                  </el-button>
                  <el-button type="success" size="small" @click="restoreVersion(scope.row)" :disabled="scope.row.isCurrent">
                    <el-icon><RefreshRight /></el-icon>
                    恢复
                  </el-button>
                  <el-button type="danger" size="small" @click="deleteVersion(scope.row)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
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
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Document, User, Clock, DocumentCopy, Download, RefreshRight, Delete } from '@element-plus/icons-vue'

// 搜索参数
const searchParams = reactive({
  documentName: '',
  fileType: '',
  uploaderName: '',
  versionTime: []
})

// 分页参数
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 版本列表数据
const versionList = ref([
  {
    id: 1,
    documentName: '产品需求文档',
    fileType: 'pdf',
    uploadTime: '2025-12-01 10:00',
    uploaderName: '张三',
    versionCount: 3,
    versions: [
      {
        id: 1,
        documentId: 1,
        versionNo: 3.0,
        storagePath: '/documents/product/20251210/',
        fileSize: 1024 * 1024 * 3.2,
        uploaderId: 1,
        uploaderName: '张三',
        changeLog: '更新了产品功能需求',
        createTime: '2025-12-10 14:30',
        isCurrent: true
      },
      {
        id: 2,
        documentId: 1,
        versionNo: 2.0,
        storagePath: '/documents/product/20251205/',
        fileSize: 1024 * 1024 * 2.8,
        uploaderId: 1,
        uploaderName: '张三',
        changeLog: '调整了产品架构设计',
        createTime: '2025-12-05 09:15',
        isCurrent: false
      },
      {
        id: 3,
        documentId: 1,
        versionNo: 1.0,
        storagePath: '/documents/product/20251201/',
        fileSize: 1024 * 1024 * 2.5,
        uploaderId: 1,
        uploaderName: '张三',
        changeLog: '初始版本',
        createTime: '2025-12-01 10:00',
        isCurrent: false
      }
    ]
  },
  {
    id: 2,
    documentName: '技术设计文档',
    fileType: 'doc',
    uploadTime: '2025-12-02 14:20',
    uploaderName: '李四',
    versionCount: 2,
    versions: [
      {
        id: 4,
        documentId: 2,
        versionNo: 2.0,
        storagePath: '/documents/tech/20251208/',
        fileSize: 1024 * 1024 * 2.2,
        uploaderId: 2,
        uploaderName: '李四',
        changeLog: '优化了技术实现方案',
        createTime: '2025-12-08 16:45',
        isCurrent: true
      },
      {
        id: 5,
        documentId: 2,
        versionNo: 1.0,
        storagePath: '/documents/tech/20251202/',
        fileSize: 1024 * 1024 * 1.8,
        uploaderId: 2,
        uploaderName: '李四',
        changeLog: '初始版本',
        createTime: '2025-12-02 14:20',
        isCurrent: false
      }
    ]
  },
  {
    id: 5,
    documentName: '员工手册',
    fileType: 'pdf',
    uploadTime: '2025-12-05 09:00',
    uploaderName: '孙七',
    versionCount: 3,
    versions: [
      {
        id: 10,
        documentId: 5,
        versionNo: 3.0,
        storagePath: '/documents/hr/20251215/',
        fileSize: 1024 * 1024 * 4.1,
        uploaderId: 5,
        uploaderName: '孙七',
        changeLog: '更新了公司制度',
        createTime: '2025-12-15 10:30',
        isCurrent: true
      },
      {
        id: 11,
        documentId: 5,
        versionNo: 2.0,
        storagePath: '/documents/hr/20251210/',
        fileSize: 1024 * 1024 * 3.9,
        uploaderId: 5,
        uploaderName: '孙七',
        changeLog: '调整了福利政策',
        createTime: '2025-12-10 14:20',
        isCurrent: false
      },
      {
        id: 12,
        documentId: 5,
        versionNo: 1.0,
        storagePath: '/documents/hr/20251205/',
        fileSize: 1024 * 1024 * 3.7,
        uploaderId: 5,
        uploaderName: '孙七',
        changeLog: '初始版本',
        createTime: '2025-12-05 09:00',
        isCurrent: false
      }
    ]
  }
])

// 计算总条数
pagination.total = versionList.value.length

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

// 搜索
const search = () => {
  console.log('搜索参数:', searchParams)
  // 这里应该调用API进行搜索
}

// 重置
const reset = () => {
  Object.assign(searchParams, {
    documentName: '',
    fileType: '',
    uploaderName: '',
    versionTime: []
  })
  // 这里应该调用API获取默认数据
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

// 下载版本
const downloadVersion = (version: any) => {
  console.log('下载版本:', version)
  // 这里应该调用API下载版本
}

// 恢复版本
const restoreVersion = (version: any) => {
  console.log('恢复版本:', version)
  // 这里应该调用API恢复版本
  ElMessage.success('版本恢复成功')
}

// 删除版本
const deleteVersion = (version: any) => {
  console.log('删除版本:', version)
  // 这里应该调用API删除版本
  ElMessage.success('版本删除成功')
}
</script>

<style scoped>
.version-management-view {
  padding: 20px;
  box-sizing: border-box;
}

.search-filter {
  margin-bottom: 20px;
}

.version-list {
  margin-bottom: 20px;
}

.version-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.card-content {
  padding: 10px 0;
}

.document-info {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
}

.version-history {
  background-color: #fafafa;
  padding: 10px;
  border-radius: 4px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .version-management-view {
    padding: 12px;
  }
  
  .search-filter .filter-form {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .document-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .version-history {
    padding: 5px;
    overflow-x: auto;
  }
}
</style>
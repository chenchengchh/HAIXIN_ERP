<template>
  <div class="cad-viewer">
    <div class="cad-header">
      <div class="cad-title">
        <h3>{{ fileName }}</h3>
        <div class="cad-meta">
          <span>{{ fileType.toUpperCase() }} | {{ fileSize }}</span>
        </div>
      </div>
      <div class="cad-actions">
        <el-button type="primary" size="small" @click="zoomIn">
          <el-icon><ZoomIn /></el-icon> 放大
        </el-button>
        <el-button type="primary" size="small" @click="zoomOut">
          <el-icon><ZoomOut /></el-icon> 缩小
        </el-button>
        <el-button type="primary" size="small" @click="zoomFit">
          <el-icon><FullScreen /></el-icon> 适应窗口
        </el-button>
        <el-button type="primary" size="small" @click="rotate">
          <el-icon><RefreshRight /></el-icon> 旋转
        </el-button>
        <el-button type="primary" size="small" @click="download">
          <el-icon><Download /></el-icon> 下载
        </el-button>
      </div>
    </div>

    <div class="cad-content">
      <div class="cad-view-area">
        <div v-if="fileType === 'dwg'" class="dwg-viewer">
          <div class="viewer-placeholder">
            <el-empty description="DWG图纸预览功能开发中，当前使用图片替代显示" />
            <img src="/placeholder-dwg.png" alt="DWG预览" class="placeholder-image" />
          </div>
        </div>
        <div v-else-if="['jpg', 'jpeg', 'png', 'gif'].includes(fileType)" class="image-viewer">
          <img :src="filePath" :alt="fileName" class="cad-image" />
        </div>
        <div v-else-if="fileType === 'pdf'" class="pdf-viewer">
          <div class="viewer-placeholder">
            <el-empty description="PDF文档预览功能开发中，当前使用图片替代显示" />
            <img src="/placeholder-pdf.png" alt="PDF预览" class="placeholder-image" />
          </div>
        </div>
        <div v-else class="other-viewer">
          <div class="viewer-placeholder">
            <el-empty description="不支持的文件类型" />
          </div>
        </div>
      </div>

      <div class="cad-sidebar">
        <div class="sidebar-section">
          <h4>图层管理</h4>
          <el-checkbox-group v-model="visibleLayers">
            <el-checkbox v-for="layer in layers" :key="layer.id" :label="layer.id">
              {{ layer.name }}
            </el-checkbox>
          </el-checkbox-group>
        </div>

        <div class="sidebar-section">
          <h4>测量工具</h4>
          <div class="measurement-tools">
            <el-button type="primary" size="small" @click="toggleMeasurementTool('distance')">
              <el-icon><CirclePlus /></el-icon> 距离测量
            </el-button>
            <el-button type="primary" size="small" @click="toggleMeasurementTool('area')">
              <el-icon><CirclePlus /></el-icon> 面积测量
            </el-button>
            <el-button type="primary" size="small" @click="toggleMeasurementTool('angle')">
              <el-icon><CirclePlus /></el-icon> 角度测量
            </el-button>
          </div>
        </div>

        <div class="sidebar-section">
          <h4>属性信息</h4>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="文件名称">{{ fileName }}</el-descriptions-item>
            <el-descriptions-item label="文件类型">{{ fileType.toUpperCase() }}</el-descriptions-item>
            <el-descriptions-item label="文件大小">{{ fileSize }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ createTime }}</el-descriptions-item>
            <el-descriptions-item label="修改时间">{{ modifyTime }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ version }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ZoomIn, ZoomOut, FullScreen, RefreshRight, Download, CirclePlus } from '@element-plus/icons-vue'

// 定义组件属性
const props = defineProps<{
  fileName?: string
  filePath?: string
  fileType?: string
  fileSize?: string
  createTime?: string
  modifyTime?: string
  version?: string
}>()

// 定义组件事件
const emit = defineEmits<{
  (e: 'zoom-in'): void
  (e: 'zoom-out'): void
  (e: 'zoom-fit'): void
  (e: 'rotate'): void
  (e: 'download'): void
  (e: 'toggle-layer', layerId: string, visible: boolean): void
  (e: 'toggle-measurement', tool: string): void
}>()

// 文件名
const fileName = ref(props.fileName || '未命名文件')

// 文件类型
const fileType = ref(props.fileType || 'dwg')

// 文件大小
const fileSize = ref(props.fileSize || '0.0MB')

// 文件路径
const filePath = ref(props.filePath || '/placeholder-image.png')

// 创建时间
const createTime = ref(props.createTime || '2024-01-01')

// 修改时间
const modifyTime = ref(props.modifyTime || '2024-01-01')

// 版本
const version = ref(props.version || 'V1.0')

// 图层列表
const layers = ref([
  { id: '1', name: '中心线', visible: true },
  { id: '2', name: '轮廓线', visible: true },
  { id: '3', name: '尺寸标注', visible: true },
  { id: '4', name: '文字说明', visible: true },
  { id: '5', name: '辅助线', visible: false }
])

// 可见图层
const visibleLayers = ref(['1', '2', '3', '4'])

// 放大
const zoomIn = () => {
  emit('zoom-in')
}

// 缩小
const zoomOut = () => {
  emit('zoom-out')
}

// 适应窗口
const zoomFit = () => {
  emit('zoom-fit')
}

// 旋转
const rotate = () => {
  emit('rotate')
}

// 下载
const download = () => {
  emit('download')
}

// 切换测量工具
const toggleMeasurementTool = (tool: string) => {
  emit('toggle-measurement', tool)
}
</script>

<style scoped>
.cad-viewer {
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  background-color: white;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.cad-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.cad-title h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #333;
}

.cad-meta {
  font-size: 14px;
  color: #666;
}

.cad-actions {
  display: flex;
  gap: 8px;
}

.cad-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.cad-view-area {
  flex: 1;
  overflow: auto;
  background-color: #f9fafb;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.cad-sidebar {
  width: 280px;
  background-color: white;
  border-left: 1px solid #e5e7eb;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.viewer-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
}

.placeholder-image {
  max-width: 80%;
  max-height: 60%;
  margin-top: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.cad-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.sidebar-section {
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.sidebar-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #333;
}

.measurement-tools {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .cad-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .cad-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .cad-sidebar {
    width: 240px;
  }
}

@media (max-width: 768px) {
  .cad-content {
    flex-direction: column;
  }

  .cad-sidebar {
    width: 100%;
    height: 200px;
    border-left: none;
    border-top: 1px solid #e5e7eb;
  }
}
</style>
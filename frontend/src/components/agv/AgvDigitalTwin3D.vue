<template>
  <div class="agv-digital-twin-3d">
    <!-- 3D数字孪生场景容器 -->
    <div ref="chartRef" class="twin-canvas"></div>
    <!-- 图例说明 -->
    <div class="twin-legend">
      <span class="legend-item"><i class="dot running"></i>运行中</span>
      <span class="legend-item"><i class="dot idle"></i>空闲</span>
      <span class="legend-item"><i class="dot charging"></i>充电中</span>
      <span class="legend-item"><i class="dot fault"></i>故障</span>
      <span class="legend-item"><i class="dot obstacle"></i>障碍物</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import 'echarts-gl'

// AGV实时数据最小结构（兼容后端AGV类型与视图层映射类型）
interface AgvLike {
  code: string
  status: string
  position?: string
  batteryLevel?: number
  speed?: number
  direction?: string
  [key: string]: any
}

// 任务数据最小结构
interface TaskLike {
  taskNo?: string
  agvCode?: string
  startPoint?: string
  endPoint?: string
  status?: string
  [key: string]: any
}

// 障碍物数据结构（父组件可传入真实障碍物坐标）
interface ObstacleItem {
  id?: string
  x: number
  y: number
  width?: number
  height?: number
  description?: string
}

// 组件入参：实时AGV列表、任务列表、障碍物列表
const props = defineProps<{
  agvs?: AgvLike[]
  tasks?: TaskLike[]
  obstacles?: ObstacleItem[]
}>()

// 3D场景画布引用
const chartRef = ref<HTMLDivElement | null>(null)
// echarts实例
let chartInstance: echarts.ECharts | null = null

// 场景尺寸配置（仓库平面坐标范围，单位：米）
const SCENE_WIDTH = 100
const SCENE_DEPTH = 60
const SHELF_HEIGHT = 6

/**
 * 仓库区域布局配置（A/B/C/D四个存储区 + 充电区）
 * 每个区域用一组货架立方体表示，坐标为区域中心点
 */
const areaLayout: { name: string; center: [number, number]; color: string }[] = [
  { name: 'A区', center: [20, 15], color: '#1e5b8a' },
  { name: 'B区', center: [80, 15], color: '#1e6e6e' },
  { name: 'C区', center: [20, 45], color: '#5a4a8a' },
  { name: 'D区', center: [80, 45], color: '#6e5a2e' }
]

/**
 * 将后端位置字符串（如 "A区-01"）解析为3D平面坐标
 * 解析失败时按AGV编号哈希分散到场景中，保证所有小车可见
 * @param position 后端返回的位置描述字符串
 * @param seed 用于哈希分散的编号种子
 * @returns [x, y] 平面坐标
 */
const parsePosition = (position: string | undefined, seed: string): [number, number] => {
  if (position) {
    // 匹配 "X区-NN" 格式的位置编码
    const match = position.match(/([A-D])\D*(\d+)/i)
    if (match && match[1] && match[2] !== undefined) {
      const areaLetter = match[1].toUpperCase()
      const area = areaLayout.find(a => a.name.startsWith(areaLetter))
      const index = parseInt(match[2], 10) || 1
      if (area) {
        // 在区域中心附近按序号偏移，形成货架位散布效果
        const offsetX = ((index - 1) % 4) * 5 - 7.5
        const offsetY = Math.floor((index - 1) / 4) * 5 - 2.5
        return [area.center[0] + offsetX, area.center[1] + offsetY]
      }
    }
  }
  // 无法解析时按编号哈希生成稳定坐标
  let hash = 0
  for (let i = 0; i < seed.length; i++) {
    hash = (hash * 31 + seed.charCodeAt(i)) % 997
  }
  return [10 + (hash % 80), 10 + ((hash * 7) % 40)]
}

/**
 * 根据AGV状态获取对应的颜色
 * @param status AGV运行状态
 * @returns 十六进制颜色值
 */
const getStatusColor = (status: string): string => {
  switch (status) {
    case 'running': return '#00e676'
    case 'idle': return '#9ea7b3'
    case 'charging': return '#ffab00'
    case 'fault': return '#ff1744'
    case 'maintenance': return '#b388ff'
    default: return '#9ea7b3'
  }
}

/**
 * 获取状态中文名称，用于tooltip展示
 * @param status AGV运行状态
 * @returns 中文状态名
 */
const getStatusText = (status: string): string => {
  switch (status) {
    case 'running': return '运行中'
    case 'idle': return '空闲'
    case 'charging': return '充电中'
    case 'fault': return '故障'
    case 'maintenance': return '维护中'
    default: return status
  }
}

/**
 * 生成仓库货架立方体数据
 * 每个区域生成两排货架，用scatter3D大立方体呈现
 * @returns 货架散点数据数组
 */
const buildShelfData = () => {
  const data: any[] = []
  areaLayout.forEach(area => {
    // 每区两排货架，每排4个货位立方体
    for (let row = 0; row < 2; row++) {
      for (let i = 0; i < 4; i++) {
        data.push({
          name: area.name,
          value: [area.center[0] - 9 + i * 6, area.center[1] - 4 + row * 8, SHELF_HEIGHT / 2],
          itemStyle: { color: area.color, opacity: 0.85 }
        })
      }
    }
  })
  return data
}

/**
 * 生成区域名称标签数据，悬浮于货架上方
 * @returns 区域标签散点数据
 */
const buildAreaLabelData = () => {
  return areaLayout.map(area => ({
    name: area.name,
    value: [area.center[0], area.center[1], SHELF_HEIGHT + 2],
    label: {
      show: true,
      formatter: area.name,
      color: '#00e5ff',
      fontSize: 16,
      fontWeight: 'bold'
    },
    symbolSize: 0.1,
    itemStyle: { opacity: 0 }
  }))
}

/**
 * 生成AGV小车3D散点数据（彩色球体，带编号标签）
 * @returns AGV散点数据数组
 */
const buildAgvData = () => {
  return (props.agvs || []).map(agv => {
    const [x, y] = parsePosition(agv.position, agv.code || agv.id || '')
    return {
      name: agv.code,
      value: [x, y, 1.2],
      agvInfo: agv,
      itemStyle: { color: getStatusColor(agv.status) },
      label: {
        show: true,
        formatter: agv.code,
        color: '#ffffff',
        fontSize: 11,
        distance: 6
      }
    }
  })
}

/**
 * 生成AGV任务路径起终点标记数据
 * 注意：echarts-gl 的 lines3D 系列不支持 cartesian3D（grid3D）坐标系
 * （其 layout 仅处理 globe/geo3D/mapbox3D/maptalks3D），在 grid3D 下
 * getItemLayout 返回 undefined 导致 _updateLines 崩溃。
 * 改用 scatter3D 在路径起点/终点绘制标记点，路径信息在面板中展示。
 * @returns scatter3D数据数组（起点绿、终点红）
 */
const buildPathMarkers = () => {
  const markers: any[] = []
  const agvList = props.agvs || []
  ;(props.tasks || [])
    .filter(t => t && (t.status === 'running' || t.status === 'assigned' || t.status === 'paused'))
    .forEach(task => {
      const start = parsePosition(task.startPoint, task.taskNo || 's')
      const end = parsePosition(task.endPoint, task.taskNo || 'e')
      // 起点标记（绿色大圆）
      markers.push({
        name: `起点-${task.taskNo}`,
        value: [start[0], start[1], 0.3],
        itemStyle: { color: '#00e676', opacity: 0.9 },
        symbolSize: 14,
        label: { show: true, formatter: '起', color: '#fff', fontSize: 9 }
      })
      // 终点标记（红色大圆）
      markers.push({
        name: `终点-${task.taskNo}`,
        value: [end[0], end[1], 0.3],
        itemStyle: { color: '#ff1744', opacity: 0.9 },
        symbolSize: 14,
        label: { show: true, formatter: '终', color: '#fff', fontSize: 9 }
      })
      // AGV当前位置标记（青色，若已分配）
      const agv = agvList.find(a => a && a.code === task.agvCode)
      if (agv) {
        const [ax, ay] = parsePosition(agv.position, agv.code || '')
        markers.push({
          name: `AGV-${task.taskNo}`,
          value: [ax, ay, 0.3],
          itemStyle: { color: '#00e5ff', opacity: 0.9 },
          symbolSize: 10,
          label: { show: true, formatter: '车', color: '#fff', fontSize: 9 }
        })
      }
    })
  return markers
}

/**
 * 生成障碍物立方体数据
 * 优先使用父组件传入的真实障碍物，否则使用默认演示障碍物
 * @returns 障碍物散点数据数组
 */
const buildObstacleData = () => {
  const list: ObstacleItem[] = (props.obstacles && props.obstacles.length)
    ? props.obstacles
    : [
        { x: 45, y: 20, description: '障碍物1' },
        { x: 62, y: 40, description: '障碍物2' },
        { x: 50, y: 50, description: '障碍物3' }
      ]
  return list.map(o => ({
    name: o.description || '障碍物',
    value: [o.x, o.y, 2],
    itemStyle: { color: '#ff1744', opacity: 0.9 }
  }))
}

/**
 * 构建3D数字孪生完整配置项
 * @returns echarts option配置对象
 */
const buildOption = () => {
  return {
    // 深色科技风背景
    backgroundColor: '#0a1929',
    // tooltip展示AGV详情
    tooltip: {
      backgroundColor: 'rgba(10, 25, 41, 0.9)',
      borderColor: '#00e5ff',
      textStyle: { color: '#ffffff' },
      formatter: (params: any) => {
        if (params.seriesName === 'AGV实时位置' && params.data?.agvInfo) {
          const agv = params.data.agvInfo as AgvLike
          return [
            `<b>AGV编号：${agv.code}</b>`,
            `状态：${getStatusText(agv.status)}`,
            `速度：${agv.speed ?? 0} m/s`,
            `电量：${agv.batteryLevel ?? 0}%`,
            `位置：${agv.position || '未知'}`,
            `方向：${agv.direction || '-'}`
          ].join('<br/>')
        }
        return params.name
      }
    },
    xAxis3D: {
      name: 'X/m',
      type: 'value',
      min: 0,
      max: SCENE_WIDTH,
      axisLine: { lineStyle: { color: '#1c4966' } },
      axisLabel: { color: '#5b8aa8' },
      splitLine: { lineStyle: { color: 'rgba(0, 229, 255, 0.15)' } }
    },
    yAxis3D: {
      name: 'Y/m',
      type: 'value',
      min: 0,
      max: SCENE_DEPTH,
      axisLine: { lineStyle: { color: '#1c4966' } },
      axisLabel: { color: '#5b8aa8' },
      splitLine: { lineStyle: { color: 'rgba(0, 229, 255, 0.15)' } }
    },
    zAxis3D: {
      name: 'Z/m',
      type: 'value',
      min: 0,
      max: 15,
      axisLine: { lineStyle: { color: '#1c4966' } },
      axisLabel: { color: '#5b8aa8' },
      splitLine: { show: false }
    },
    grid3D: {
      boxWidth: 180,
      boxDepth: 110,
      boxHeight: 30,
      // 地面网格青色科技感配色
      axisPointer: { show: false },
      splitLine: { lineStyle: { color: 'rgba(0, 229, 255, 0.2)' } },
      plane: {
        show: true,
        color: [
          { color: '#0d2137', opacity: 0.9 },
          { color: '#0a1929', opacity: 0.95 }
        ]
      },
      light: {
        main: { intensity: 1.2, shadow: true, alpha: 40, beta: 30 },
        ambient: { intensity: 0.4 }
      },
      viewControl: {
        // 默认视角与交互（旋转/缩放）
        alpha: 25,
        beta: 45,
        distance: 260,
        autoRotate: false
      }
    },
    series: [
      // 仓库货架立方体
      {
        name: '仓库货架',
        type: 'scatter3D',
        symbol: 'rect',
        symbolSize: 12,
        data: buildShelfData()
      },
      // 区域名称标签
      {
        name: '区域标签',
        type: 'scatter3D',
        symbolSize: 0.1,
        data: buildAreaLabelData()
      },
      // AGV实时位置球体
      {
        name: 'AGV实时位置',
        type: 'scatter3D',
        symbolSize: 10,
        data: buildAgvData(),
        itemStyle: { borderColor: '#ffffff', borderWidth: 1 }
      },
      // 障碍物立方体
      {
        name: '障碍物',
        type: 'scatter3D',
        symbol: 'diamond',
        symbolSize: 8,
        data: buildObstacleData()
      },
      // AGV任务路径起终点标记（lines3D不支持grid3D坐标系，改用scatter3D标记）
      {
        name: '任务路径标记',
        type: 'scatter3D',
        symbolSize: 14,
        data: buildPathMarkers()
      }
    ]
  }
}

// 容器尺寸观察器，用于Tab切换时容器从隐藏变可见后自动resize
let resizeObserver: ResizeObserver | null = null

/**
 * 渲染或更新3D场景
 * 数据变化时调用setOption重渲染，保留当前视角
 */
const renderChart = () => {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption(buildOption(), { notMerge: true })
}

/**
 * 监听容器尺寸变化，自适应调整3D画布
 * 解决Tab切换时容器由隐藏(display:none)变可见导致echarts尺寸为0或默认100x100的问题
 */
const handleResize = () => {
  if (chartInstance && chartRef.value) {
    const w = chartRef.value.clientWidth
    const h = chartRef.value.clientHeight
    // 容器有有效尺寸时才resize，避免初始化阶段无效调用
    if (w > 0 && h > 0) {
      chartInstance.resize({ width: w, height: h })
    }
  }
}

onMounted(() => {
  renderChart()
  // 使用ResizeObserver监听容器自身尺寸变化（Tab切换/父级布局变化均能捕获）
  if (chartRef.value && typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(() => {
      handleResize()
    })
    resizeObserver.observe(chartRef.value)
  }
  // 兼容窗口尺寸变化
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  resizeObserver?.disconnect()
  resizeObserver = null
  chartInstance?.dispose()
  chartInstance = null
})

// 监听props数据变化，自动重渲染3D场景（父组件定时刷新数据时生效）
watch(
  () => [props.agvs, props.tasks, props.obstacles],
  () => {
    renderChart()
  },
  { deep: true }
)
</script>

<style scoped>
/* 3D数字孪生组件整体容器 */
.agv-digital-twin-3d {
  position: relative;
  width: 100%;
  height: 500px;
  background-color: #0a1929;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(0, 229, 255, 0.25);
}

/* 3D画布 */
.twin-canvas {
  width: 100%;
  height: 100%;
}

/* 状态图例 */
.twin-legend {
  position: absolute;
  left: 16px;
  bottom: 12px;
  display: flex;
  gap: 16px;
  padding: 6px 12px;
  background: rgba(10, 25, 41, 0.75);
  border: 1px solid rgba(0, 229, 255, 0.3);
  border-radius: 6px;
  color: #cfe8f5;
  font-size: 12px;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.dot.running { background-color: #00e676; }
.dot.idle { background-color: #9ea7b3; }
.dot.charging { background-color: #ffab00; }
.dot.fault { background-color: #ff1744; }
.dot.obstacle { background-color: #ff1744; border-radius: 2px; }
</style>

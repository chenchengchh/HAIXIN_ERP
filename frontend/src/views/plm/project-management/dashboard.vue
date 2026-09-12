<template>
  <div class="project-dashboard-view">
    <h3>项目概览</h3>
    
    <!-- 项目统计卡片 -->
    <div class="stats-cards">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ totalProjects }}</div>
          <div class="stat-label">总项目数</div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ executingProjects }}</div>
          <div class="stat-label">执行中项目</div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ completedProjects }}</div>
          <div class="stat-label">已完成项目</div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ delayedProjects }}</div>
          <div class="stat-label">延迟项目</div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ planningProjects }}</div>
          <div class="stat-label">规划中项目</div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ suspendedProjects }}</div>
          <div class="stat-label">已暂停项目</div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ cancelledProjects }}</div>
          <div class="stat-label">已取消项目</div>
        </div>
      </el-card>
    </div>
    
    <!-- 项目筛选和搜索 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="项目搜索">
          <el-input
            v-model="filterForm.searchKeyword"
            placeholder="请输入项目名称或编码"
            clearable
            @input="handleSearch"
            prefix-icon="Search"
          ></el-input>
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select
            v-model="filterForm.status"
            placeholder="请选择状态"
            clearable
            @change="handleFilter"
          >
            <el-option label="所有状态" value=""></el-option>
            <el-option label="规划中" value="planning"></el-option>
            <el-option label="执行中" value="in-progress"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="延迟" value="delayed"></el-option>
            <el-option label="已暂停" value="suspended"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="项目类型">
          <el-select
            v-model="filterForm.type"
            placeholder="请选择类型"
            clearable
            @change="handleFilter"
          >
            <el-option label="所有类型" value=""></el-option>
            <el-option label="新产品研发(NPI)" value="NPI"></el-option>
            <el-option label="产品改进" value="改进"></el-option>
            <el-option label="技术研发" value="研发"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleFilter"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 项目列表 -->
    <el-card shadow="hover" class="project-list-card">
      <template #header>
        <div class="card-header">
          <span>项目列表</span>
          <el-button type="primary" size="small" @click="showAddProjectDialog">新增项目</el-button>
        </div>
      </template>
      <div class="project-list">
        <el-table :data="projects" style="width: 100%">
          <el-table-column prop="code" label="项目编码" min-width="120"></el-table-column>
          <el-table-column prop="name" label="项目名称" min-width="200"></el-table-column>
          <el-table-column prop="type" label="项目类型" min-width="100"></el-table-column>
          <el-table-column prop="manager" label="项目经理" min-width="120"></el-table-column>
          <el-table-column prop="startDate" label="开始日期" min-width="120"></el-table-column>
          <el-table-column prop="endDate" label="结束日期" min-width="120"></el-table-column>
          <el-table-column prop="progress" label="进度" min-width="120">
            <template #default="scope">
              <el-progress :percentage="scope.row.progress" :stroke-width="10"></el-progress>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="100">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">{{ statusMap[scope.row.status] || scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="200" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewProject(scope.row.id)">查看</el-button>
              <el-button size="small" @click="editProject(scope.row.id)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDeleteProject(scope.row.id)">删除</el-button>
              <el-dropdown>
                <el-button size="small">
                  更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="viewGantt(scope.row.id)">甘特图</el-dropdown-item>
                    <el-dropdown-item @click="viewResource(scope.row.id)">资源负载</el-dropdown-item>
                    <el-dropdown-item>项目报告</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalProjects"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </div>
    </el-card>
    
    <!-- 编辑项目对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑项目"
      width="600px"
      @close="resetEditProjectForm"
    >
      <el-form
        ref="editProjectFormRef"
        :model="editProjectForm"
        :rules="editProjectRules"
        label-width="120px"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input
            v-model="editProjectForm.name"
            placeholder="请输入项目名称"
            maxlength="100"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="项目编码" prop="code">
          <el-input
            v-model="editProjectForm.code"
            placeholder="请输入项目编码"
            maxlength="50"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="项目经理" prop="manager">
          <el-input
            v-model="editProjectForm.manager"
            placeholder="请输入项目经理"
            maxlength="50"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="项目类型" prop="type">
          <el-select
            v-model="editProjectForm.type"
            placeholder="请选择项目类型"
          >
            <el-option label="新产品研发(NPI)" value="NPI"></el-option>
            <el-option label="产品改进" value="改进"></el-option>
            <el-option label="技术研发" value="研发"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="editProjectForm.startDate"
            type="date"
            placeholder="请选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="editProjectForm.endDate"
            type="date"
            placeholder="请选择结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="项目进度" prop="progress">
          <el-slider
            v-model="editProjectForm.progress"
            :min="0"
            :max="100"
            show-input
          ></el-slider>
        </el-form-item>
        <el-form-item label="项目状态" prop="status">
          <el-select
            v-model="editProjectForm.status"
            placeholder="请选择项目状态"
          >
            <el-option label="规划中" value="planning"></el-option>
            <el-option label="执行中" value="in-progress"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="延迟" value="delayed"></el-option>
            <el-option label="已暂停" value="suspended"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetEditProjectForm">取消</el-button>
          <el-button type="primary" :loading="editProjectLoading" @click="handleEditProject">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 新增项目对话框 -->
    <el-dialog
      v-model="addProjectDialogVisible"
      title="新增项目"
      width="600px"
      @close="resetAddProjectForm"
    >
      <el-form
        ref="addProjectFormRef"
        :model="addProjectForm"
        :rules="addProjectRules"
        label-width="120px"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input
            v-model="addProjectForm.name"
            placeholder="请输入项目名称"
            maxlength="100"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="项目编码" prop="code">
          <el-input
            v-model="addProjectForm.code"
            placeholder="请输入项目编码"
            maxlength="50"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="项目经理" prop="manager">
          <el-input
            v-model="addProjectForm.manager"
            placeholder="请输入项目经理"
            maxlength="50"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="项目类型" prop="type">
          <el-select
            v-model="addProjectForm.type"
            placeholder="请选择项目类型"
          >
            <el-option label="新产品研发(NPI)" value="NPI"></el-option>
            <el-option label="产品改进" value="改进"></el-option>
            <el-option label="技术研发" value="研发"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="addProjectForm.startDate"
            type="date"
            placeholder="请选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="addProjectForm.endDate"
            type="date"
            placeholder="请选择结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="初始进度" prop="progress">
          <el-slider
            v-model="addProjectForm.progress"
            :min="0"
            :max="100"
            show-input
          ></el-slider>
        </el-form-item>
        <el-form-item label="初始状态" prop="status">
          <el-select
            v-model="addProjectForm.status"
            placeholder="请选择初始状态"
          >
            <el-option label="规划中" value="planning"></el-option>
            <el-option label="执行中" value="in-progress"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="已暂停" value="suspended"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetAddProjectForm">取消</el-button>
          <el-button type="primary" :loading="addProjectLoading" @click="handleAddProject">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useProjectManagementStore } from '@/stores/plm/projectManagement'

// 初始化路由
const router = useRouter()

// 初始化项目管理状态
const projectStore = useProjectManagementStore()

// 从状态管理获取项目列表
const projects = computed(() => projectStore.filteredProjects)

// 统计数据计算
const totalProjects = computed(() => projectStore.projects.length)
const executingProjects = computed(() => projectStore.projects.filter(p => p.status === 'in-progress').length)
const completedProjects = computed(() => projectStore.projects.filter(p => p.status === 'completed').length)
const delayedProjects = computed(() => projectStore.projects.filter(p => p.status === 'delayed').length)
const planningProjects = computed(() => projectStore.projects.filter(p => p.status === 'planning').length)
const suspendedProjects = computed(() => projectStore.projects.filter(p => p.status === 'suspended').length)
const cancelledProjects = computed(() => projectStore.projects.filter(p => p.status === 'cancelled').length)

// 筛选表单
const filterForm = ref({
  searchKeyword: '',
  status: '',
  type: '',
  dateRange: null as [string, string] | null
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)

// 英文状态到中文状态的映射
const statusMap: Record<string, string> = {
  'planning': '规划中',
  'in-progress': '执行中',
  'completed': '已完成',
  'delayed': '延迟',
  'suspended': '已暂停',
  'cancelled': '已取消'
}

// 状态标签类型映射
const getStatusTagType = (status: string) => {
  const tagTypeMap: Record<string, string> = {
    'planning': 'info',
    'in-progress': 'info',
    'completed': 'success',
    'delayed': 'danger',
    'suspended': 'warning',
    'cancelled': 'danger'
  }
  return tagTypeMap[status] || 'info'
}

// 查看项目详情
const viewProject = (id: string) => {
  projectStore.setSelectedProjectId(id)
  router.push(`/home/plm/project-management/project-detail/${id}`)
}

// 编辑项目对话框
const editDialogVisible = ref(false)
const editProjectFormRef = ref<any>(null)
const editProjectLoading = ref(false)

// 编辑项目表单数据
const editProjectForm = ref({
  name: '',
  code: '',
  manager: '',
  type: '',
  startDate: '',
  endDate: '',
  progress: 0,
  status: 'planning'
})

// 编辑项目表单验证规则
const editProjectRules = ref({
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '项目名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入项目编码', trigger: 'blur' },
    { min: 2, max: 50, message: '项目编码长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  manager: [
    { required: true, message: '请输入项目经理', trigger: 'blur' },
    { min: 2, max: 50, message: '项目经理长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择项目类型', trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择项目状态', trigger: 'change' }
  ]
})

// 显示编辑项目对话框
const editProject = async (id: string) => {
  projectStore.setSelectedProjectId(id)
  const project = projectStore.getProjectById(id)
  if (project) {
    editProjectForm.value = {
      name: project.name,
      code: project.code,
      manager: project.manager,
      type: project.type,
      startDate: project.startDate,
      endDate: project.endDate,
      progress: project.progress,
      status: project.status
    }
    editDialogVisible.value = true
  }
}

// 重置编辑项目表单
const resetEditProjectForm = () => {
  if (editProjectFormRef.value) {
    editProjectFormRef.value.resetFields()
  }
  editProjectForm.value = {
    name: '',
    code: '',
    manager: '',
    type: '',
    startDate: '',
    endDate: '',
    progress: 0,
    status: 'planning'
  }
  editDialogVisible.value = false
}

// 处理编辑项目
const handleEditProject = async () => {
  if (!editProjectFormRef.value) return
  
  try {
    // 表单验证
    await editProjectFormRef.value.validate()
    
    editProjectLoading.value = true
    
    // 提交项目数据
    const updatedProject = await projectStore.updateProject(projectStore.selectedProjectId!, editProjectForm.value)
    
    if (updatedProject) {
      ElMessage.success('项目更新成功')
      editDialogVisible.value = false
      resetEditProjectForm()
    } else {
      ElMessage.error('项目更新失败')
    }
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('表单验证失败，请检查输入')
    }
  } finally {
    editProjectLoading.value = false
  }
}

// 处理删除项目
const handleDeleteProject = async (id: string) => {
  try {
    // 确认删除
    await ElMessageBox.confirm(
      '确定要删除该项目吗？此操作不可撤销。',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 执行删除操作
    const success = await projectStore.deleteProject(id)
    
    if (success) {
      ElMessage.success('项目删除成功')
    } else {
      ElMessage.error('项目删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除操作失败')
    }
  }
}

// 查看甘特图
const viewGantt = (id: string) => {
  projectStore.setSelectedProjectId(id)
  router.push(`/home/plm/project-management/gantt-view`)
}

// 查看资源负载
const viewResource = (id: string) => {
  projectStore.setSelectedProjectId(id)
  router.push(`/home/plm/project-management/resource`)
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  projectStore.setPagination(currentPage.value, size)
}

// 当前页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  projectStore.setPagination(page, pageSize.value)
}

// 处理搜索
const handleSearch = () => {
  projectStore.setSearchKeyword(filterForm.value.searchKeyword)
}

// 处理筛选
const handleFilter = () => {
  projectStore.setStatusFilter(filterForm.value.status)
}

// 重置筛选条件
const resetFilters = () => {
  filterForm.value = {
    searchKeyword: '',
    status: '',
    type: '',
    dateRange: null
  }
  projectStore.resetFilters()
}

// 初始化加载项目数据
onMounted(async () => {
  await projectStore.fetchProjects()
})

// 新增项目对话框
const addProjectDialogVisible = ref(false)
const addProjectFormRef = ref<any>(null)
const addProjectLoading = ref(false)

// 新增项目表单数据
const addProjectForm = ref({
  name: '',
  code: '',
  manager: '',
  type: '',
  startDate: '',
  endDate: '',
  progress: 0,
  status: 'planning'
})

// 新增项目表单验证规则
const addProjectRules = ref({
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '项目名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入项目编码', trigger: 'blur' },
    { min: 2, max: 50, message: '项目编码长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  manager: [
    { required: true, message: '请输入项目经理', trigger: 'blur' },
    { min: 2, max: 50, message: '项目经理长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择项目类型', trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择初始状态', trigger: 'change' }
  ]
})

// 显示新增项目对话框
const showAddProjectDialog = () => {
  addProjectDialogVisible.value = true
}

// 重置新增项目表单
const resetAddProjectForm = () => {
  if (addProjectFormRef.value) {
    addProjectFormRef.value.resetFields()
  }
  addProjectForm.value = {
    name: '',
    code: '',
    manager: '',
    type: '',
    startDate: '',
    endDate: '',
    progress: 0,
    status: 'planning'
  }
  addProjectDialogVisible.value = false
}

// 处理新增项目
const handleAddProject = async () => {
  if (!addProjectFormRef.value) return
  
  try {
    // 表单验证
    await addProjectFormRef.value.validate()
    
    addProjectLoading.value = true
    
    // 提交项目数据
    const newProject = await projectStore.addProject(addProjectForm.value)
    
    if (newProject) {
      ElMessage.success('项目新增成功')
      addProjectDialogVisible.value = false
      resetAddProjectForm()
    } else {
      ElMessage.error('项目新增失败')
    }
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('表单验证失败，请检查输入')
    }
  } finally {
    addProjectLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.project-dashboard-view {
  padding: 20px;
  height: 100%;
  overflow: auto;

  h3 {
    margin-bottom: 20px;
    color: #333;
    font-size: 1.3rem;
  }

  /* 统计卡片 */
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 20px;
  }

  .stat-card {
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
    }
  }

  .stat-content {
    text-align: center;
    padding: 16px 0;
  }

  .stat-number {
    font-size: 2rem;
    font-weight: bold;
    color: #1890ff;
  }

  .stat-label {
    font-size: 0.9rem;
    color: #666;
    margin-top: 8px;
  }

  /* 筛选卡片 */
  .filter-card {
    margin-bottom: 20px;
    
    .el-form {
      width: 100%;
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;
    }
    
    .el-form-item {
      margin-right: 0;
      margin-bottom: 0;
    }
  }

  /* 项目列表卡片 */
  .project-list-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .project-list {
    padding: 16px 0;
  }

  /* 分页 */
  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  /* 响应式设计 */
  @media (max-width: 1024px) {
    padding: 16px;

    .stats-cards {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  @media (max-width: 768px) {
    padding: 12px;

    h3 {
      font-size: 1.1rem;
      margin-bottom: 16px;
    }

    .stats-cards {
      grid-template-columns: 1fr;
      gap: 12px;
    }

    .stat-number {
      font-size: 1.5rem;
    }
  }
}
</style>

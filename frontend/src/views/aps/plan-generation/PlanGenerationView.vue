<template>
  <div class="plan-generation-view">
    <div class="page-header">
      <h2>计划生成</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/aps' }">APS系统</el-breadcrumb-item>
        <el-breadcrumb-item>计划生成</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 统计卡片 -->
    <div class="statistics-cards" style="margin-bottom: 20px;">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="生产计划总数" :value="statistics.totalPlans">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em;"><Document /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="执行中计划" :value="statistics.activePlans">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em;"><CircleCheck /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="排程结果数" :value="statistics.totalSchedules">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em;"><List /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="已完成计划" :value="statistics.completedPlans">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em;"><Select /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 计划生成模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
        <el-tab-pane label="主生产计划" name="mps">
          <div class="tab-content">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>主生产计划管理</span>
                  <div style="display: flex; gap: 10px;">
                    <el-button type="primary" size="small" @click="handleCreate">创建主生产计划</el-button>
                    <el-button 
                      type="danger" 
                      size="small" 
                      :disabled="selectedPlans.length === 0"
                      @click="handleBatchDelete"
                    >
                      批量删除({{ selectedPlans.length }})
                    </el-button>
                  </div>
                </div>
              </template>
              <!-- 搜索栏 -->
              <div class="search-bar" style="margin-bottom: 16px;">
                <el-input
                  v-model="searchForm.mps.keyword"
                  placeholder="请输入计划编号或计划名称搜索"
                  style="width: 300px; margin-right: 10px;"
                  clearable
                  @keyup.enter="handleSearch('mps')"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-select
                  v-model="searchForm.mps.status"
                  placeholder="选择状态"
                  style="width: 150px; margin-right: 10px;"
                  clearable
                >
                  <el-option label="草稿" value="草稿" />
                  <el-option label="已排程" value="SCHEDULED" />
                  <el-option label="已下达" value="RELEASED" />
                  <el-option label="执行中" value="IN_PROGRESS" />
                  <el-option label="已完成" value="COMPLETED" />
                  <el-option label="已取消" value="CANCELLED" />
                </el-select>
                <el-button type="primary" @click="handleSearch('mps')">搜索</el-button>
                <el-button @click="handleResetSearch('mps')">重置</el-button>
              </div>
              <el-table 
                :data="productionPlans" 
                stripe 
                style="width: 100%" 
                v-loading="loading.mps"
                @selection-change="handleSelectionChange"
              >
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="ID" width="80" fixed="left" />
                <el-table-column prop="planCode" label="计划编号" width="150" show-overflow-tooltip />
                <el-table-column prop="planName" label="计划名称" min-width="150" show-overflow-tooltip />
                <el-table-column prop="startDate" label="开始日期" width="120">
                  <template #default="scope">
                    {{ scope.row.startDate ? formatDate(scope.row.startDate) : '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="endDate" label="结束日期" width="120">
                  <template #default="scope">
                    {{ scope.row.endDate ? formatDate(scope.row.endDate) : '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100" fixed="right">
                  <template #default="scope">
                    <el-tag
                      :type="getStatusTagType(scope.row.status)"
                      size="small"
                    >
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="280" fixed="right">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="handleView(scope.row)">查看</el-button>
                    <el-button type="warning" size="small" @click="handleEdit(scope.row)">编辑</el-button>
                    <el-button 
                      v-if="scope.row.status === 'draft' || scope.row.status === '草稿'"
                      type="success" 
                      size="small" 
                      @click="handleSubmitApproval(scope.row)"
                    >
                      提交审批
                    </el-button>
                    <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <!-- 分页 -->
              <el-pagination
                v-model:current-page="pagination.mps.currentPage"
                v-model:page-size="pagination.mps.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="pagination.mps.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange('mps')"
                @current-change="handleCurrentChange('mps')"
                style="margin-top: 20px; justify-content: flex-end;"
              />
            </el-card>
          </div>
        </el-tab-pane>
        <el-tab-pane label="详细排程计划" name="detail-schedule">
          <div class="tab-content">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>详细排程计划</span>
                  <div style="display: flex; gap: 10px;">
                    <el-button type="primary" size="small" @click="handleGenerateSchedule">生成排程计划</el-button>
                    <el-button 
                      type="danger" 
                      size="small" 
                      :disabled="selectedSchedules.length === 0"
                      @click="handleBatchDeleteSchedule"
                    >
                      批量删除({{ selectedSchedules.length }})
                    </el-button>
                  </div>
                </div>
              </template>
              <!-- 搜索栏 -->
              <div class="search-bar" style="margin-bottom: 16px;">
                <el-input
                  v-model="searchForm['detail-schedule'].keyword"
                  placeholder="请输入计划编号搜索"
                  style="width: 300px; margin-right: 10px;"
                  clearable
                  @keyup.enter="handleSearch('detail-schedule')"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-select
                  v-model="searchForm['detail-schedule'].algorithm"
                  placeholder="选择算法"
                  style="width: 150px; margin-right: 10px;"
                  clearable
                >
                  <el-option label="优先级调度" value="priority" />
                  <el-option label="最早交期优先" value="edd" />
                  <el-option label="关键路径法" value="cpm" />
                  <el-option label="遗传算法" value="genetic" />
                  <el-option label="模拟退火" value="simulatedAnnealing" />
                </el-select>
                <el-button type="primary" @click="handleSearch('detail-schedule')">搜索</el-button>
                <el-button @click="handleResetSearch('detail-schedule')">重置</el-button>
              </div>
              <el-table 
                :data="scheduleResults" 
                stripe 
                style="width: 100%" 
                v-loading="loading['detail-schedule']"
                @selection-change="handleScheduleSelectionChange"
                empty-text="暂无数据"
              >
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="ID" width="80" fixed="left" />
                <el-table-column prop="planId" label="计划ID" width="100" />
                <el-table-column prop="planCode" label="计划编号" width="150" show-overflow-tooltip />
                <el-table-column prop="algorithm" label="使用算法" width="120">
                  <template #default="scope">
                    <el-tag type="info" size="small">{{ scope.row.algorithm }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createdTime" label="生成时间" width="180">
                  <template #default="scope">
                    {{ scope.row.createdTime || '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100" fixed="right">
                  <template #default="scope">
                    <el-tag
                      :type="getScheduleStatusTagType(scope.row.status)"
                      size="small"
                    >
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="handleViewSchedule(scope.row)">查看</el-button>
                    <el-button type="danger" size="small" @click="handleDeleteSchedule(scope.row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <!-- 分页 -->
              <el-pagination
                v-model:current-page="pagination['detail-schedule'].currentPage"
                v-model:page-size="pagination['detail-schedule'].pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="pagination['detail-schedule'].total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange('detail-schedule')"
                @current-change="handleCurrentChange('detail-schedule')"
                style="margin-top: 20px; justify-content: flex-end;"
              />
            </el-card>
          </div>
        </el-tab-pane>
        <el-tab-pane label="多周期计划" name="multi-period">
          <div class="tab-content">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>多周期计划</span>
                  <el-button type="primary" size="small" @click="handleGenerateMultiPeriod">生成多周期计划</el-button>
                </div>
              </template>
              <el-table :data="multiPeriodPlans" stripe style="width: 100%" v-loading="loading['multi-period']">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="planNo" label="计划编号" min-width="160" show-overflow-tooltip />
                <el-table-column prop="periodTypeText" label="周期类型" width="100" />
                <el-table-column prop="periodCount" label="周期数" width="80" />
                <el-table-column prop="startDate" label="开始日期" width="110" />
                <el-table-column prop="endDate" label="结束日期" width="110" />
                <el-table-column prop="status" label="状态" width="90">
                  <template #default="scope">
                    <el-tag size="small" type="info">{{ scope.row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="handleViewMultiPeriod(scope.row)">查看</el-button>
                    <el-button type="danger" size="small" @click="handleDeleteMultiPeriod(scope.row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 生产计划创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="planFormRef"
        :model="planForm"
        :rules="planFormRules"
        label-width="120px"
      >
        <el-form-item label="计划编号" prop="planNo">
          <el-input v-model="planForm.planNo" placeholder="请输入计划编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="计划名称" prop="planName">
          <el-input v-model="planForm.planName" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="计划类型" prop="planType">
          <el-select v-model="planForm.planType" placeholder="请选择计划类型" style="width: 100%">
            <el-option label="主生产计划" :value="0" />
            <el-option label="物料需求计划" :value="1" />
            <el-option label="能力需求计划" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划周期" prop="planCycle">
          <el-select v-model="planForm.planCycle" placeholder="请选择计划周期" style="width: 100%">
            <el-option label="日" :value="0" />
            <el-option label="周" :value="1" />
            <el-option label="月" :value="2" />
            <el-option label="季度" :value="3" />
            <el-option label="年" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="planForm.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="planForm.endDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="计划描述" prop="description">
          <el-input
            v-model="planForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入计划描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitPlan" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 生成排程对话框 -->
    <el-dialog
      v-model="scheduleDialogVisible"
      title="生成排程计划"
      width="700px"
      :close-on-click-modal="false"
      @close="handleScheduleDialogClose"
    >
      <el-form
        ref="scheduleFormRef"
        :model="scheduleForm"
        :rules="scheduleFormRules"
        label-width="120px"
      >
        <el-form-item label="生产计划" prop="planId">
          <el-select
            v-model="scheduleForm.planId"
            placeholder="请选择生产计划"
            style="width: 100%"
            filterable
            @change="handlePlanChange"
          >
            <el-option
              v-for="plan in productionPlans"
              :key="plan.id"
              :label="`${plan.planName} (${plan.planCode})`"
              :value="plan.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排程算法" prop="algorithm">
          <el-select
            v-model="scheduleForm.algorithm"
            placeholder="请选择排程算法"
            style="width: 100%"
            @change="handleAlgorithmChange"
          >
            <el-option label="优先级调度" value="priority" />
            <el-option label="最早交期优先" value="edd" />
            <el-option label="关键路径法" value="cpm" />
            <el-option label="遗传算法" value="genetic" />
            <el-option label="模拟退火" value="simulatedAnnealing" />
          </el-select>
        </el-form-item>
        <el-form-item label="算法参数" v-if="algorithmParams.length > 0">
          <el-form :model="scheduleForm.params" label-width="100px">
            <el-form-item
              v-for="param in algorithmParams"
              :key="param.key"
              :label="param.label"
            >
              <el-input-number
                v-if="param.type === 'number'"
                v-model="scheduleForm.params[param.key]"
                :min="param.min"
                :max="param.max"
                :step="param.step || 1"
                style="width: 100%"
              />
              <el-input
                v-else
                v-model="scheduleForm.params[param.key]"
                :placeholder="`请输入${param.label}`"
                style="width: 100%"
              />
            </el-form-item>
          </el-form>
        </el-form-item>
        <el-form-item label="优化目标" prop="objectives">
          <el-checkbox-group v-model="scheduleForm.objectives">
            <el-checkbox label="最小化总完工时间">最小化总完工时间</el-checkbox>
            <el-checkbox label="最小化延期订单数量">最小化延期订单数量</el-checkbox>
            <el-checkbox label="最大化资源利用率">最大化资源利用率</el-checkbox>
            <el-checkbox label="平衡资源负荷">平衡资源负荷</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="scheduleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitSchedule" :loading="scheduleSubmitting">生成排程</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 生产计划详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="生产计划详情"
      width="800px"
    >
      <el-skeleton :loading="detailLoading" animated>
        <template #template>
          <el-skeleton-item variant="h3" style="width: 30%" />
          <el-skeleton-item variant="text" style="width: 60%; margin-top: 16px;" />
          <el-skeleton-item variant="text" style="width: 40%; margin-top: 16px;" />
        </template>
        <template #default>
          <div v-if="detailData" class="detail-content">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="计划ID">{{ detailData.id }}</el-descriptions-item>
              <el-descriptions-item label="计划编号">{{ detailData.planNo || detailData.planCode }}</el-descriptions-item>
              <el-descriptions-item label="计划名称">{{ detailData.planName }}</el-descriptions-item>
              <el-descriptions-item label="计划类型">
                {{ detailData.planType === 0 ? '主生产计划' : detailData.planType === 1 ? '物料需求计划' : '能力需求计划' }}
              </el-descriptions-item>
              <el-descriptions-item label="计划周期">
                {{ detailData.planCycle === 0 ? '日' : detailData.planCycle === 1 ? '周' : detailData.planCycle === 2 ? '月' : detailData.planCycle === 3 ? '季度' : '年' }}
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="detailData.status === '已发布' || detailData.status === '执行中' ? 'success' : detailData.status === '已完成' ? 'info' : 'warning'">
                  {{ detailData.status }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="开始日期">{{ detailData.startDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="结束日期">{{ detailData.endDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间" :span="2">
                {{ detailData.createdAt ? new Date(detailData.createdAt).toLocaleString() : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="描述" :span="2">
                {{ detailData.description || '无' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </template>
      </el-skeleton>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 排程详情对话框（包含甘特图） -->
    <el-dialog
      v-model="scheduleDetailDialogVisible"
      title="排程详情"
      width="90%"
      top="5vh"
      class="schedule-detail-dialog"
    >
      <el-skeleton :loading="scheduleDetailLoading" animated>
        <template #template>
          <el-skeleton-item variant="h3" style="width: 30%" />
          <el-skeleton-item variant="text" style="width: 60%; margin-top: 16px;" />
        </template>
        <template #default>
          <div v-if="scheduleDetailData" class="schedule-detail-content">
            <!-- 基本信息 -->
            <el-card shadow="never" style="margin-bottom: 20px;">
              <template #header>
                <span>基本信息</span>
              </template>
              <el-descriptions :column="3" border>
                <el-descriptions-item label="排程ID">{{ scheduleDetailData.id }}</el-descriptions-item>
                <el-descriptions-item label="计划ID">{{ scheduleDetailData.planId }}</el-descriptions-item>
                <el-descriptions-item label="排程编号">{{ scheduleDetailData.scheduleNo || scheduleDetailData.planCode }}</el-descriptions-item>
                <el-descriptions-item label="使用算法">{{ scheduleDetailData.algorithm || '优先级调度' }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="scheduleDetailData.status === '已完成' ? 'success' : scheduleDetailData.status === '执行中' ? 'warning' : 'info'">
                    {{ scheduleDetailData.status }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="生成时间">{{ scheduleDetailData.createdTime || (scheduleDetailData.createdAt ? new Date(scheduleDetailData.createdAt).toLocaleString() : '-') }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
            
            <!-- 甘特图 -->
            <el-card shadow="never" v-if="ganttTasks.length > 0">
              <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <span>排程甘特图</span>
                  <el-button type="primary" size="small" @click="exportScheduleData">导出排程</el-button>
                </div>
              </template>
              <div style="height: 500px;">
                <el-alert type="info" :closable="false" style="margin-bottom: 10px;">
                  <template #default>
                    <p>提示：如需完整交互式甘特图，请使用排程可视化页面查看</p>
                  </template>
                </el-alert>
                <el-table :data="ganttTasks" stripe style="width: 100%">
                  <el-table-column prop="text" label="任务名称" />
                  <el-table-column prop="start_date" label="开始时间" width="180">
                    <template #default="scope">
                      {{ scope.row.start_date ? new Date(scope.row.start_date).toLocaleString() : '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="end_date" label="结束时间" width="180">
                    <template #default="scope">
                      {{ scope.row.end_date ? new Date(scope.row.end_date).toLocaleString() : '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="duration" label="工期(天)" width="100">
                    <template #default="scope">
                      {{ scope.row.duration || '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="resource_name" label="资源" width="150" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag size="small">{{ scope.row.status || '-' }}</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-card>
            <el-empty v-else description="暂无甘特图数据" />
          </div>
        </template>
      </el-skeleton>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="scheduleDetailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="viewFullGantt" v-if="scheduleDetailData">查看完整甘特图</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 生成多周期计划对话框 -->
    <el-dialog
      v-model="multiPeriodDialogVisible"
      title="生成多周期计划"
      width="700px"
      :close-on-click-modal="false"
      @close="handleMultiPeriodDialogClose"
    >
      <el-form
        ref="multiPeriodFormRef"
        :model="multiPeriodForm"
        :rules="multiPeriodFormRules"
        label-width="120px"
      >
        <el-form-item label="生产计划" prop="planId">
          <el-select
            v-model="multiPeriodForm.planId"
            placeholder="请选择生产计划"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="plan in productionPlans"
              :key="plan.id"
              :label="`${plan.planName} (${plan.planCode})`"
              :value="plan.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排程算法" prop="algorithm">
          <el-select
            v-model="multiPeriodForm.algorithm"
            placeholder="请选择排程算法"
            style="width: 100%"
          >
            <el-option label="优先级调度" value="priority" />
            <el-option label="最早交期优先" value="edd" />
            <el-option label="关键路径法" value="cpm" />
            <el-option label="遗传算法" value="genetic" />
            <el-option label="模拟退火" value="simulatedAnnealing" />
          </el-select>
        </el-form-item>
        <el-form-item label="周期类型" prop="periodType">
          <el-select
            v-model="multiPeriodForm.periodType"
            placeholder="请选择周期类型"
            style="width: 100%"
          >
            <el-option label="日" value="daily" />
            <el-option label="周" value="weekly" />
            <el-option label="月" value="monthly" />
            <el-option label="季度" value="quarterly" />
          </el-select>
        </el-form-item>
        <el-form-item label="周期数量" prop="periodCount">
          <el-input-number
            v-model="multiPeriodForm.periodCount"
            :min="1"
            :max="12"
            :step="1"
            placeholder="请输入周期数量"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="multiPeriodForm.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="优化目标" prop="objectives">
          <el-checkbox-group v-model="multiPeriodForm.objectives">
            <el-checkbox label="最小化总完工时间">最小化总完工时间</el-checkbox>
            <el-checkbox label="最小化延期订单数量">最小化延期订单数量</el-checkbox>
            <el-checkbox label="最大化资源利用率">最大化资源利用率</el-checkbox>
            <el-checkbox label="平衡资源负荷">平衡资源负荷</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="multiPeriodDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitMultiPeriod" :loading="multiPeriodSubmitting">生成多周期计划</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 多周期计划详情对话框 -->
    <el-dialog
      v-model="multiPeriodDetailVisible"
      title="多周期计划详情"
      width="800px"
    >
      <el-skeleton :loading="multiPeriodDetailLoading" animated>
        <template #default>
          <div v-if="multiPeriodDetail">
            <el-descriptions :column="3" border style="margin-bottom: 16px;">
              <el-descriptions-item label="计划编号">{{ multiPeriodDetail.planNo }}</el-descriptions-item>
              <el-descriptions-item label="计划名称" :span="2">{{ multiPeriodDetail.planName }}</el-descriptions-item>
              <el-descriptions-item label="源计划">{{ multiPeriodDetail.sourcePlanNo || multiPeriodDetail.sourcePlanId }}</el-descriptions-item>
              <el-descriptions-item label="周期类型">{{ periodTypeMap[multiPeriodDetail.periodType] || multiPeriodDetail.periodType }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag size="small" type="info">{{ multiPeriodDetail.status }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="开始日期">{{ multiPeriodDetail.startDate }}</el-descriptions-item>
              <el-descriptions-item label="结束日期">{{ multiPeriodDetail.endDate }}</el-descriptions-item>
              <el-descriptions-item label="算法">{{ multiPeriodDetail.algorithm || '-' }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="3">{{ multiPeriodDetail.remark || '无' }}</el-descriptions-item>
            </el-descriptions>
            <el-table :data="multiPeriodItems" stripe border>
              <el-table-column prop="periodIndex" label="周期" width="70" />
              <el-table-column prop="periodStart" label="开始日期" width="120" />
              <el-table-column prop="periodEnd" label="结束日期" width="120" />
              <el-table-column prop="quantity" label="分配数量" width="120">
                <template #default="scope">{{ scope.row.quantity ?? '-' }}</template>
              </el-table-column>
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag size="small" type="info">{{ scope.row.status }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-skeleton>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="multiPeriodDetailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Document, CircleCheck, List, Select } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { unwrapListResponse, unwrapPageResponse, unwrapResponseData } from '../../../api'
import { DataTransformer } from '../../../utils/data-transformer'
// 导入API服务
import { apsApi } from '../../../api/aps'
import { useAuthStore } from '../../../stores/auth'

const ProductionPlanAPI = apsApi.ProductionPlanAPI
const ScheduleResultAPI = apsApi.ScheduleResultAPI
const AlgorithmParamAPI = apsApi.AlgorithmParamAPI
const MultiPeriodPlanAPI = apsApi.MultiPeriodPlanAPI

// 路由
const router = useRouter()

// 当前登录用户（提交审批时作为发起人）
const authStore = useAuthStore()

// 选中的项目
const selectedPlans = ref<any[]>([])
const selectedSchedules = ref<any[]>([])

// 活跃标签
const activeTab = ref<string>('mps')

// 数据列表
const productionPlans = ref<any[]>([])
const scheduleResults = ref<any[]>([])
const multiPeriodPlans = ref<any[]>([])

// 加载状态
const loading = ref<{ [key: string]: boolean }>({
  mps: false,
  'detail-schedule': false,
  'multi-period': false
})

// 分页信息
const pagination = reactive<{ [key: string]: any }>({
  mps: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  'detail-schedule': {
    currentPage: 1,
    pageSize: 10,
    total: 0
  }
})

// 搜索表单
const searchForm = reactive<{ [key: string]: any }>({
  mps: {
    keyword: '',
    status: ''
  },
  'detail-schedule': {
    keyword: '',
    algorithm: ''
  }
})

// 统计数据
const statistics = computed(() => {
  const totalPlans = productionPlans.value.length
  const activePlans = productionPlans.value.filter((p: any) =>
    ['执行中', '已发布', '已排程', '已下达'].includes(p.status)
  ).length
  // 排程结果总数取分页total（未加载时回退当前列表长度）
  const totalSchedules = pagination['detail-schedule'].total || scheduleResults.value.length
  const completedPlans = productionPlans.value.filter((p: any) =>
    p.status === '已完成'
  ).length

  return {
    totalPlans,
    activePlans,
    totalSchedules,
    completedPlans
  }
})

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('创建生产计划')
const submitting = ref(false)
const planFormRef = ref<FormInstance>()

// 表单数据
const planForm = reactive({
  id: undefined as number | undefined,
  planNo: '',
  planName: '',
  planType: 0,
  planCycle: 1,
  startDate: '',
  endDate: '',
  description: ''
})

// 表单验证规则
const planFormRules: FormRules = {
  planNo: [
    { required: true, message: '请输入计划编号', trigger: 'blur' },
    { min: 2, max: 50, message: '计划编号长度应在2到50个字符之间', trigger: 'blur' }
  ],
  planName: [
    { required: true, message: '请输入计划名称', trigger: 'blur' },
    { min: 2, max: 100, message: '计划名称长度应在2到100个字符之间', trigger: 'blur' }
  ],
  planType: [
    { required: true, message: '请选择计划类型', trigger: 'change' }
  ],
  planCycle: [
    { required: true, message: '请选择计划周期', trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (planForm.startDate && value && new Date(value) < new Date(planForm.startDate)) {
          callback(new Error('结束日期不能早于开始日期'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const getResponseMessage = (response: any, fallback: string) => {
  const normalized = DataTransformer.normalizeResponse(response)
  return normalized?.msg || normalized?.message || fallback
}

// 获取生产计划数据
const fetchProductionPlans = async () => {
  loading.value.mps = true
  try {
    // 准备查询参数
    const params: any = {
      page: pagination.mps.currentPage,
      size: pagination.mps.pageSize
    }
    
    // 添加搜索条件
    if (searchForm.mps.keyword) {
      params.keyword = searchForm.mps.keyword
    }
    
    if (searchForm.mps.status !== '') {
      params.status = searchForm.mps.status
    }
    
    // 调用API获取生产计划列表
    const response = await ProductionPlanAPI.getProductionPlans(params)
    const page = unwrapPageResponse<any>(response)
    productionPlans.value = page.list.map((plan: any) => ({
      ...plan,
      planCode: plan.planNo || plan.id,
      // 后端实体字段为startTime/endTime（LocalDateTime），映射为前端展示用startDate/endDate
      startDate: plan.startTime || plan.startDate || '',
      endDate: plan.endTime || plan.endDate || '',
      status: getStatusText(plan.status)
    }))
    pagination.mps.total = page.total
  } catch (error: any) {
    // 改进错误处理，显示更友好的错误信息
    const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '服务器内部错误，请稍后重试'
    console.error('获取生产计划失败:', errorMsg)
    ElMessage.error(errorMsg)
    productionPlans.value = []
    pagination.mps.total = 0
  } finally {
    loading.value.mps = false
  }
}

// 获取状态文本：兼容后端字符串状态（草稿/SCHEDULED/RELEASED等）与历史数字状态
const getStatusText = (status: string | number) => {
  if (typeof status === 'string' && status) {
    const textMap: { [key: string]: string } = {
      'DRAFT': '草稿',
      '草稿': '草稿',
      'SUBMITTED': '审批中',
      'APPROVED': '审批通过',
      'REJECTED': '审批拒绝',
      'SCHEDULED': '已排程',
      'RELEASED': '已下达',
      'IN_PROGRESS': '执行中',
      'COMPLETED': '已完成',
      'CANCELLED': '已取消'
    }
    return textMap[status.toUpperCase()] ?? textMap[status] ?? status
  }
  const statusMap: { [key: number]: string } = {
    0: '草稿',
    1: '已发布',
    2: '执行中',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status as number] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const typeMap: { [key: string]: string } = {
    '草稿': 'info',
    '审批中': 'warning',
    '审批通过': 'success',
    '审批拒绝': 'danger',
    '已发布': 'warning',
    '已排程': 'primary',
    '已下达': 'warning',
    '执行中': 'success',
    '已完成': '',
    '已取消': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取排程状态标签类型
const getScheduleStatusTagType = (status: string) => {
  const typeMap: { [key: string]: string } = {
    '待执行': 'info',
    '执行中': 'warning',
    '已完成': 'success',
    '已取消': 'danger'
  }
  return typeMap[status] || 'info'
}

// 格式化日期
const formatDate = (date: string | Date) => {
  if (!date) return '-'
  try {
    const d = typeof date === 'string' ? new Date(date) : date
    return d.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  } catch (error) {
    return date.toString()
  }
}

// 获取排程结果数据
const fetchScheduleResults = async () => {
  loading.value['detail-schedule'] = true
  try {
    // 准备查询参数
    const params: any = {
      page: pagination['detail-schedule'].currentPage,
      size: pagination['detail-schedule'].pageSize
    }
    
    // 添加搜索条件
    if (searchForm['detail-schedule'].keyword) {
      params.planCode = searchForm['detail-schedule'].keyword
    }
    
    if (searchForm['detail-schedule'].algorithm) {
      params.algorithm = searchForm['detail-schedule'].algorithm
    }
    
    // 调用API获取排程结果列表
    const response = await ScheduleResultAPI.getScheduleResults(params)
    const page = unwrapPageResponse<any>(response)

    // 建立计划id->计划编号映射（排程结果实体仅含planId，展示时需要planNo）
    const planNoMap = await buildPlanNoMap()

    scheduleResults.value = page.list.map((result: any) => ({
      ...result,
      planCode: planNoMap.get(result.planId) || result.planId || result.id,
      algorithm: result.algorithm || '优先级调度',
      // 后端字段为createdTime，且status为字符串(RUNNING/COMPLETED/FAILED)
      createdTime: result.createdTime ? new Date(result.createdTime).toLocaleString() : '',
      status: getScheduleStatusText(result.status)
    }))
    pagination['detail-schedule'].total = page.total
  } catch (error: any) {
    // 改进错误处理，显示更友好的错误信息
    let errorMsg = '服务器内部错误，请稍后重试'
    
    if (error.response) {
      // 服务器返回错误响应
      errorMsg = error.response.statusText || '服务器内部错误，请稍后重试'
    } else if (error.request) {
      // 请求发送但没有收到响应
      errorMsg = '服务器无响应，请检查网络连接'
    } else {
      // 请求配置错误
      errorMsg = error.message || '请求配置错误'
    }
    
    ElMessage.error(errorMsg)
    scheduleResults.value = []
    pagination['detail-schedule'].total = 0
  } finally {
    loading.value['detail-schedule'] = false
  }
}

// 计划id->计划编号缓存，避免每次列表刷新重复请求
let planNoMapCache: Map<number, string> | null = null

/**
 * 构建计划id到计划编号planNo的映射（带缓存）
 */
const buildPlanNoMap = async (): Promise<Map<number, string>> => {
  if (planNoMapCache) return planNoMapCache
  const map = new Map<number, string>()
  try {
    const res = await ProductionPlanAPI.getProductionPlans()
    unwrapListResponse<any>(res).forEach((p: any) => {
      if (p?.id != null) map.set(p.id, p.planNo || String(p.id))
    })
  } catch {
    // 映射构建失败时回退显示planId
  }
  planNoMapCache = map
  return map
}

// 获取排程状态文本：兼容后端字符串状态(RUNNING/COMPLETED/FAILED)与历史数字状态
const getScheduleStatusText = (status: string | number) => {
  if (typeof status === 'string' && status) {
    const textMap: { [key: string]: string } = {
      'RUNNING': '执行中',
      'COMPLETED': '已完成',
      'FAILED': '失败',
      'PENDING': '待执行',
      'SCHEDULED': '已排程',
      'RELEASED': '已下达',
      'CANCELLED': '已取消'
    }
    return textMap[status.toUpperCase()] ?? status
  }
  const statusMap: { [key: number]: string } = {
    0: '待执行',
    1: '执行中',
    2: '已完成',
    3: '已取消'
  }
  return statusMap[status as number] || '未知'
}

/**
 * 周期类型中英文映射（后端存储daily/weekly/monthly/quarterly）
 */
const periodTypeMap: { [key: string]: string } = {
  daily: '日计划',
  weekly: '周计划',
  monthly: '月计划',
  quarterly: '季度计划'
}

// 获取多周期计划数据（真实API）
const fetchMultiPeriodPlans = async () => {
  loading.value['multi-period'] = true
  try {
    const response = await MultiPeriodPlanAPI.getMultiPeriodPlans()
    const list = unwrapListResponse<any>(response)
    multiPeriodPlans.value = list.map((plan: any) => ({
      ...plan,
      periodTypeText: periodTypeMap[plan.periodType] || plan.periodType
    }))
  } catch (error: any) {
    // 改进错误处理，显示更友好的错误信息
    const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '服务器内部错误，请稍后重试'
    console.error('获取多周期计划失败:', errorMsg)
    ElMessage.error(errorMsg)
    multiPeriodPlans.value = []
  } finally {
    loading.value['multi-period'] = false
  }
}

// 根据当前活跃标签获取对应数据
// 该函数已在文件末尾重新实现，包含调试日志


// 处理创建
const handleCreate = () => {
  isEdit.value = false
  dialogTitle.value = '创建生产计划'
  resetPlanForm()
  dialogVisible.value = true
}

// 详情对话框相关
const detailDialogVisible = ref(false)
const detailData = ref<any>(null)
const detailLoading = ref(false)

// 处理查看
const handleView = async (row: any) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  try {
      const response = await ProductionPlanAPI.getProductionPlanById(row.id)
      detailData.value = unwrapResponseData<any>(response) || row
  } catch (error: any) {
    console.error('获取详情失败:', error)
    ElMessage.error(error.message || '获取详情失败')
    detailData.value = row
  } finally {
    detailLoading.value = false
  }
}

// 处理编辑
const handleEdit = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑生产计划'
  // 填充表单数据
  planForm.id = row.id
  planForm.planNo = row.planNo || row.planCode || ''
  planForm.planName = row.planName || ''
  planForm.planType = row.planType ?? 0
  planForm.planCycle = row.planCycle ?? 1
  planForm.startDate = row.startDate || ''
  planForm.endDate = row.endDate || ''
  planForm.description = row.description || ''
  dialogVisible.value = true
}

// 提交审批
const handleSubmitApproval = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要提交生产计划"${row.planName || row.planNo}"进行审批吗？`,
      '提交审批',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用提交审批API（携带当前登录用户作为发起人，OA实例initiator非空约束）
    const userInfo = authStore.userInfo as any
    const response = await ProductionPlanAPI.submitPlanForApproval(
      row.id,
      Number(userInfo?.id) || undefined,
      userInfo?.name || userInfo?.username || undefined
    )
    const result = unwrapResponseData<any>(response)
    
    if (result) {
      ElMessage.success('生产计划已提交审批')
      // 刷新数据
      fetchProductionPlans()
    } else {
      ElMessage.error('提交审批失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('提交审批失败:', error)
      ElMessage.error(error.message || '提交审批失败')
    }
  }
}

// 重置表单
const resetPlanForm = () => {
  planForm.id = undefined
  planForm.planNo = ''
  planForm.planName = ''
  planForm.planType = 0
  planForm.planCycle = 1
  planForm.startDate = ''
  planForm.endDate = ''
  planForm.description = ''
  planFormRef.value?.clearValidate()
}

// 对话框关闭
const handleDialogClose = () => {
  resetPlanForm()
  // 清除表单验证状态
  planFormRef.value?.resetFields()
}

// 提交表单
const handleSubmitPlan = async () => {
  if (!planFormRef.value) return
  
  await planFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      
      try {
        const submitData = {
          planNo: planForm.planNo,
          planName: planForm.planName,
          planType: planForm.planType,
          planCycle: planForm.planCycle,
          startDate: planForm.startDate,
          endDate: planForm.endDate,
          description: planForm.description,
          status: 0 // 草稿状态
        }
        
        let response: any
        if (isEdit.value && planForm.id) {
          // 更新生产计划
          response = await ProductionPlanAPI.updateProductionPlan(planForm.id, submitData)
        } else {
          // 创建生产计划
          response = await ProductionPlanAPI.createProductionPlan(submitData)
        }
        
        const responseData = DataTransformer.normalizeResponse(response)
        if (DataTransformer.isSuccessCode(responseData?.code)) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
          dialogVisible.value = false
          // 刷新数据
          fetchProductionPlans()
        } else {
          ElMessage.error(getResponseMessage(responseData, isEdit.value ? '更新失败' : '创建失败'))
        }
      } catch (error: any) {
        console.error('提交失败:', error)
        ElMessage.error(error.message || '提交失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 分页处理
const fetchDataByTabName = (tab: string) => {
  console.log('调试 - fetchDataByTabName被调用，tab:', tab)
  switch (tab) {
    case 'mps':
      fetchProductionPlans()
      break
    case 'detail-schedule':
      fetchScheduleResults()
      break
    case 'multi-period':
      fetchMultiPeriodPlans()
      break
    default:
      console.warn('调试 - 未知的标签:', tab)
  }
}

const handleSizeChange = (tab: string) => {
  fetchDataByTabName(tab)
}

const handleCurrentChange = (tab: string) => {
  fetchDataByTabName(tab)
}

// 搜索处理
const handleSearch = (tab: string) => {
  // 重置到第一页
  if (tab === 'mps') {
    pagination.mps.currentPage = 1
    fetchProductionPlans()
  } else if (tab === 'detail-schedule') {
    pagination['detail-schedule'].currentPage = 1
    fetchScheduleResults()
  }
}

// 重置搜索
const handleResetSearch = (tab: string) => {
  if (tab === 'mps') {
    searchForm.mps.keyword = ''
    searchForm.mps.status = ''
    pagination.mps.currentPage = 1
    fetchProductionPlans()
  } else if (tab === 'detail-schedule') {
    searchForm['detail-schedule'].keyword = ''
    searchForm['detail-schedule'].algorithm = ''
    pagination['detail-schedule'].currentPage = 1
    fetchScheduleResults()
  }
}

/**
 * 查看完整甘特图：跳转排程可视化页面，通过query传递计划ID自动定位
 */
const viewFullGantt = () => {
  if (scheduleDetailData.value) {
    const planId = scheduleDetailData.value.planId
    scheduleDetailDialogVisible.value = false
    router.push({
      name: 'aps-schedule-visualization',
      query: planId ? { planId: String(planId) } : {}
    })
  }
}

/**
 * 导出排程数据：将当前排程详情的甘特任务导出为CSV文件（前端生成，无需后端接口）
 */
const exportScheduleData = () => {
  if (!scheduleDetailData.value || ganttTasks.value.length === 0) {
    ElMessage.warning('暂无可导出的排程数据')
    return
  }
  // CSV表头（加BOM保证Excel中文不乱码）
  const headers = ['任务ID', '任务名称', '开始时间', '结束时间', '工期(小时)', '资源', '数量', '状态']
  const rows = ganttTasks.value.map((task: any) => [
    task.id ?? '',
    task.text ?? '',
    task.start_date ? new Date(task.start_date).toLocaleString() : '',
    task.end_date ? new Date(task.end_date).toLocaleString() : '',
    task.duration ?? '',
    task.resource_name ?? '',
    task.quantity ?? '',
    task.status === 'in_progress' ? '已下达' : '已排程'
  ])
  const csvContent = '﻿' + [headers, ...rows]
    .map(row => row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))
    .join('\r\n')
  // 生成Blob并触发浏览器下载
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  const scheduleNo = scheduleDetailData.value.scheduleNo || `schedule-${scheduleDetailData.value.id}`
  link.download = `排程数据_${scheduleNo}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
  ElMessage.success('排程数据导出成功')
}

// 选择变化处理
const handleSelectionChange = (selection: any[]) => {
  selectedPlans.value = selection
}

const handleScheduleSelectionChange = (selection: any[]) => {
  selectedSchedules.value = selection
}

// 批量删除生产计划
const handleBatchDelete = async () => {
  if (selectedPlans.value.length === 0) {
    ElMessage.warning('请选择要删除的计划')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedPlans.value.length} 个生产计划吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 逐个删除
    let successCount = 0
    let failCount = 0
    
    for (const plan of selectedPlans.value) {
        try {
          const response = await ProductionPlanAPI.deleteProductionPlan(plan.id)
          const responseData = DataTransformer.normalizeResponse(response)
          if (DataTransformer.isSuccessCode(responseData?.code)) {
            successCount++
          } else {
            failCount++
          }
        } catch (error: any) {
          console.error('删除计划失败:', error)
          failCount++
        }
      }
    
    if (successCount > 0) {
      ElMessage.success({
        message: `成功删除 ${successCount} 个计划`,
        duration: 2000
      })
    }
    if (failCount > 0) {
      ElMessage.warning({
        message: `删除失败 ${failCount} 个计划`,
        duration: 3000
      })
    }
    
    // 清空选择并刷新列表
    selectedPlans.value = []
    await fetchProductionPlans()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

// 批量删除排程
const handleBatchDeleteSchedule = async () => {
  if (selectedSchedules.value.length === 0) {
    ElMessage.warning('请选择要删除的排程')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedSchedules.value.length} 个排程结果吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 逐个删除
    let successCount = 0
    let failCount = 0
    
    for (const schedule of selectedSchedules.value) {
      try {
        const response = await ScheduleResultAPI.deleteScheduleResult(schedule.id)
        const responseData = DataTransformer.normalizeResponse(response)
        if (DataTransformer.isSuccessCode(responseData?.code)) {
          successCount++
        } else {
          failCount++
        }
      } catch (error: any) {
        console.error('删除排程失败:', error)
        failCount++
      }
    }
    
    if (successCount > 0) {
      ElMessage.success({
        message: `成功删除 ${successCount} 个排程`,
        duration: 2000
      })
    }
    if (failCount > 0) {
      ElMessage.warning({
        message: `删除失败 ${failCount} 个排程`,
        duration: 3000
      })
    }
    
    // 清空选择并刷新列表
    selectedSchedules.value = []
    await fetchScheduleResults()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

// 生成排程对话框相关
const scheduleDialogVisible = ref(false)
const scheduleSubmitting = ref(false)
const scheduleFormRef = ref<FormInstance>()
const algorithmParams = ref<any[]>([])

// 排程表单数据
const scheduleForm = reactive({
  planId: undefined as number | undefined,
  algorithm: '',
  params: {} as any,
  objectives: [] as string[]
})

// 排程表单验证规则
const scheduleFormRules: FormRules = {
  planId: [
    { required: true, message: '请选择生产计划', trigger: 'change' }
  ],
  algorithm: [
    { required: true, message: '请选择排程算法', trigger: 'change' }
  ],
  objectives: [
    { required: true, message: '请至少选择一个优化目标', trigger: 'change' }
  ]
}

// 处理生成排程
const handleGenerateSchedule = () => {
  scheduleDialogVisible.value = true
  resetScheduleForm()
  // 确保已加载生产计划列表
  if (productionPlans.value.length === 0) {
    fetchProductionPlans()
  }
}

// 重置排程表单
const resetScheduleForm = () => {
  scheduleForm.planId = undefined
  scheduleForm.algorithm = ''
  scheduleForm.params = {}
  scheduleForm.objectives = []
  algorithmParams.value = []
  scheduleFormRef.value?.clearValidate()
}

// 排程对话框关闭
const handleScheduleDialogClose = () => {
  resetScheduleForm()
}

// 计划选择变化
const handlePlanChange = (planId: number) => {
  // 可以加载计划相关的默认参数
}

// 算法选择变化
const handleAlgorithmChange = async (algorithm: string) => {
  if (!algorithm) return
  
  try {
    // 加载算法参数配置
    const response = await AlgorithmParamAPI.getParamsByAlgorithmName(algorithm)
    const responseData = DataTransformer.normalizeResponse(response)
    if (DataTransformer.isSuccessCode(responseData?.code)) {
      const params = unwrapListResponse<any>(response)
      algorithmParams.value = params.map((p: any) => ({
        key: p.paramName,
        label: p.paramName,
        type: 'number',
        min: 0,
        max: 1000
      }))
      
      // 加载默认参数值
      const defaultResponse = await AlgorithmParamAPI.getDefaultParams(algorithm)
      const defaultResponseData = DataTransformer.normalizeResponse(defaultResponse)
      if (DataTransformer.isSuccessCode(defaultResponseData?.code)) {
        scheduleForm.params = { ...(unwrapResponseData<any>(defaultResponse) || {}) }
      }
    } else {
      // 使用默认参数配置
      setDefaultAlgorithmParams(algorithm)
    }
  } catch (error) {
    console.error('加载算法参数失败:', error)
    setDefaultAlgorithmParams(algorithm)
  }
}

// 设置默认算法参数
const setDefaultAlgorithmParams = (algorithm: string) => {
  const defaultParams: { [key: string]: any[] } = {
    genetic: [
      { key: 'populationSize', label: '种群大小', type: 'number', min: 10, max: 200, step: 10 },
      { key: 'maxGenerations', label: '最大代数', type: 'number', min: 10, max: 1000, step: 10 },
      { key: 'crossoverRate', label: '交叉率', type: 'number', min: 0, max: 1, step: 0.1 },
      { key: 'mutationRate', label: '变异率', type: 'number', min: 0, max: 1, step: 0.1 }
    ],
    simulatedAnnealing: [
      { key: 'initialTemperature', label: '初始温度', type: 'number', min: 1, max: 1000 },
      { key: 'finalTemperature', label: '终止温度', type: 'number', min: 0, max: 1, step: 0.1 },
      { key: 'coolingRate', label: '冷却率', type: 'number', min: 0, max: 1, step: 0.01 },
      { key: 'maxIterations', label: '最大迭代次数', type: 'number', min: 100, max: 10000, step: 100 }
    ]
  }
  
  algorithmParams.value = defaultParams[algorithm] || []
  if (algorithmParams.value.length > 0) {
    scheduleForm.params = {}
    algorithmParams.value.forEach(param => {
      scheduleForm.params[param.key] = param.min || 0
    })
  }
}

// 提交排程生成
const handleSubmitSchedule = async () => {
  if (!scheduleFormRef.value) return
  
  await scheduleFormRef.value.validate(async (valid) => {
    if (valid) {
      if (!scheduleForm.planId) {
        ElMessage.warning('请选择生产计划')
        return
      }
      
      scheduleSubmitting.value = true
      
      const submitData = {
        planId: scheduleForm.planId,
        algorithm: scheduleForm.algorithm,
        params: scheduleForm.params,
        objectives: scheduleForm.objectives
      }
      
      // 显示进度提示
      const loadingMessage = ElMessage({
        message: '正在生成排程，请稍候...',
        type: 'info',
        duration: 0,
        showClose: false
      })
      
      try {
        // 调用API生成排程
        const response = await ScheduleResultAPI.generateSchedule(submitData)
        const responseData = DataTransformer.normalizeResponse(response)
        
        if (DataTransformer.isSuccessCode(responseData?.code)) {
          loadingMessage.close()
          ElMessage.success('排程生成成功')
          scheduleDialogVisible.value = false
          // 刷新排程结果列表
          fetchScheduleResults()
        } else {
          loadingMessage.close()
          ElMessage.error(getResponseMessage(responseData, '排程生成失败'))
        }
      } catch (error: any) {
        loadingMessage.close()
        console.error('生成排程失败:', error.message || error)
        ElMessage.error(error.message || '生成排程失败')
      } finally {
        scheduleSubmitting.value = false
      }
    }
  })
}

// 处理删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该生产计划吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    try {
      // 调用API删除生产计划
      const response = await ProductionPlanAPI.deleteProductionPlan(row.id)
      const responseData = DataTransformer.normalizeResponse(response)
      
      if (DataTransformer.isSuccessCode(responseData?.code)) {
        ElMessage.success('删除成功')
        // 重新获取数据
        fetchProductionPlans()
      } else {
        ElMessage.error(getResponseMessage(responseData, '删除失败'))
      }
    } catch (error: any) {
      console.error('删除计划失败:', error.message || error)
      ElMessage.error(error.message || '删除计划失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除计划失败:', error)
      ElMessage.error(error.message || '删除计划失败')
    }
  }
}

// 排程详情对话框相关
const scheduleDetailDialogVisible = ref(false)
const scheduleDetailData = ref<any>(null)
const scheduleDetailLoading = ref(false)
const ganttTasks = ref<any[]>([])
const ganttLinks = ref<any[]>([])

// 处理查看排程
const handleViewSchedule = async (row: any) => {
  scheduleDetailDialogVisible.value = true
  scheduleDetailLoading.value = true
  try {
    try {
      // 获取排程详情
      const response = await ScheduleResultAPI.getScheduleResultById(row.id)
      scheduleDetailData.value = unwrapResponseData<any>(response) || row
      
      // 获取甘特图数据（后端返回结构为 {tasks, links, scheduleNo, resultStatus}）
      const ganttResponse = await ScheduleResultAPI.getGanttData(row.id)
      const ganttData = unwrapResponseData<any>(ganttResponse)
      ganttTasks.value = Array.isArray(ganttData?.tasks) ? ganttData.tasks : []
      ganttLinks.value = Array.isArray(ganttData?.links) ? ganttData.links : []
    } catch (error: any) {
      console.error('获取详情失败:', error.message || error)
      ElMessage.error(error.message || '获取详情失败')
      scheduleDetailData.value = row
      ganttTasks.value = []
      ganttLinks.value = []
    } finally {
      scheduleDetailLoading.value = false
    }
  } catch (error: any) {
    console.error('获取详情失败:', error.message || error)
    ElMessage.error(error.message || '获取详情失败')
    scheduleDetailData.value = row
    scheduleDetailLoading.value = false
    ganttTasks.value = []
    ganttLinks.value = []
  }
}

// 处理删除排程
const handleDeleteSchedule = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该排程结果吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    try {
      // 调用API删除排程结果
      const response = await ScheduleResultAPI.deleteScheduleResult(row.id)
      const responseData = DataTransformer.normalizeResponse(response)
      
      if (DataTransformer.isSuccessCode(responseData?.code)) {
        ElMessage.success('删除成功')
        // 重新获取数据
        fetchScheduleResults()
      } else {
        ElMessage.error(getResponseMessage(responseData, '删除失败'))
      }
    } catch (error: any) {
      console.error('删除排程失败:', error.message || error)
      ElMessage.error(error.message || '删除排程失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除排程失败:', error)
      ElMessage.error(error.message || '删除排程失败')
    }
  }
}

// 多周期计划详情对话框状态
const multiPeriodDetailVisible = ref(false)
const multiPeriodDetailLoading = ref(false)
const multiPeriodDetail = ref<any>(null)
const multiPeriodItems = ref<any[]>([])

/**
 * 处理查看多周期计划：加载详情与周期片段明细
 */
const handleViewMultiPeriod = async (row: any) => {
  multiPeriodDetailVisible.value = true
  multiPeriodDetailLoading.value = true
  multiPeriodDetail.value = null
  multiPeriodItems.value = []
  try {
    const response = await MultiPeriodPlanAPI.getMultiPeriodPlanDetail(row.id)
    const data = unwrapResponseData<any>(response)
    multiPeriodDetail.value = data?.plan ?? row
    multiPeriodItems.value = Array.isArray(data?.items) ? data.items : []
  } catch (error: any) {
    console.error('获取多周期计划详情失败:', error.message || error)
    ElMessage.error(error.message || '获取详情失败')
    multiPeriodDetail.value = row
  } finally {
    multiPeriodDetailLoading.value = false
  }
}

// 多周期计划对话框相关
const multiPeriodDialogVisible = ref(false)
const multiPeriodSubmitting = ref(false)
const multiPeriodFormRef = ref<FormInstance>()

// 多周期计划表单数据
const multiPeriodForm = reactive({
  planId: undefined as number | undefined,
  algorithm: '',
  periodType: 'weekly',
  periodCount: 1,
  startDate: '',
  objectives: [] as string[]
})

// 多周期计划表单验证规则
const multiPeriodFormRules: FormRules = {
  planId: [
    { required: true, message: '请选择生产计划', trigger: 'change' }
  ],
  algorithm: [
    { required: true, message: '请选择排程算法', trigger: 'change' }
  ],
  periodType: [
    { required: true, message: '请选择周期类型', trigger: 'change' }
  ],
  periodCount: [
    { required: true, message: '请输入周期数量', trigger: 'change' },
    { type: 'number', min: 1, max: 12, message: '周期数量应在1到12之间', trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  objectives: [
    { required: true, message: '请至少选择一个优化目标', trigger: 'change' }
  ]
}

// 处理生成多周期计划
const handleGenerateMultiPeriod = () => {
  multiPeriodDialogVisible.value = true
  // 确保已加载生产计划列表
  if (productionPlans.value.length === 0) {
    fetchProductionPlans()
  }
}

// 重置多周期计划表单
const resetMultiPeriodForm = () => {
  multiPeriodForm.planId = undefined
  multiPeriodForm.algorithm = ''
  multiPeriodForm.periodType = 'weekly'
  multiPeriodForm.periodCount = 1
  multiPeriodForm.startDate = ''
  multiPeriodForm.objectives = []
  multiPeriodFormRef.value?.clearValidate()
}

// 多周期计划对话框关闭
const handleMultiPeriodDialogClose = () => {
  resetMultiPeriodForm()
}

// 提交生成多周期计划（真实API）
const handleSubmitMultiPeriod = async () => {
  if (!multiPeriodFormRef.value) return

  await multiPeriodFormRef.value.validate(async (valid) => {
    if (valid) {
      multiPeriodSubmitting.value = true

      const submitData = {
        planId: multiPeriodForm.planId!, // 表单验证已确保planId非空
        algorithm: multiPeriodForm.algorithm,
        periodType: multiPeriodForm.periodType,
        periodCount: multiPeriodForm.periodCount,
        startDate: multiPeriodForm.startDate,
        objectives: multiPeriodForm.objectives
      }

      // 显示进度提示
      const loadingMessage = ElMessage({
        message: '正在生成多周期计划，请稍候...',
        type: 'info',
        duration: 0,
        showClose: false
      })

      try {
        const response = await MultiPeriodPlanAPI.generateMultiPeriodPlan(submitData)
        const responseData = DataTransformer.normalizeResponse(response)
        loadingMessage.close()
        if (DataTransformer.isSuccessCode(responseData?.code)) {
          ElMessage.success('多周期计划生成成功')
          multiPeriodDialogVisible.value = false
          // 刷新多周期计划列表
          fetchMultiPeriodPlans()
        } else {
          ElMessage.error(responseData?.msg || responseData?.message || '生成失败')
        }
      } catch (error: any) {
        loadingMessage.close()
        console.error('生成多周期计划失败:', error)
        ElMessage.error(error.response?.data?.msg || error.message || '生成失败')
      } finally {
        multiPeriodSubmitting.value = false
      }
    }
  })
}

// 处理删除多周期计划（真实API，级联删除周期明细）
const handleDeleteMultiPeriod = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该多周期计划吗？其周期片段明细将一并删除。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await MultiPeriodPlanAPI.deleteMultiPeriodPlan(row.id)
    const responseData = DataTransformer.normalizeResponse(response)
    if (DataTransformer.isSuccessCode(responseData?.code)) {
      ElMessage.success('删除成功')
      await fetchMultiPeriodPlans()
    } else {
      ElMessage.error(responseData?.msg || responseData?.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除多周期计划失败:', error)
      ElMessage.error(error.response?.data?.msg || error.message || '删除失败')
    }
  }
}

// 组件挂载时获取默认标签数据，并预取排程结果总数用于统计卡片
onMounted(() => {
  fetchDataByTab(activeTab.value)
  prefetchScheduleResultCount()
})

/**
 * 预取排程结果总数：仅取分页total(size=1)，供首屏统计卡片展示，不影响各tab按需加载
 */
const prefetchScheduleResultCount = async () => {
  try {
    const response = await ScheduleResultAPI.getScheduleResults({ page: 1, size: 1 })
    const page = unwrapPageResponse<any>(response)
    pagination['detail-schedule'].total = page.total
  } catch {
    // 预取失败静默处理，切换tab时会重新加载
  }
}

// 监听标签切换，获取对应数据
const handleTabChange = (tab: string) => {
  fetchDataByTab(tab)
}

// 根据当前活跃标签获取对应数据
const fetchDataByTab = async (tab: string) => {
  switch (tab) {
    case 'mps':
      await fetchProductionPlans()
      break
    case 'detail-schedule':
      await fetchScheduleResults()
      break
    case 'multi-period':
      await fetchMultiPeriodPlans()
      break
  }
}
</script>

<style scoped>
.plan-generation-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.page-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 1.5rem;
}

.module-nav {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  min-height: calc(100% - 80px);
}

/* 标签内容样式 */
.tab-content {
  padding: 20px;
}

/* 卡片头部样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1.1rem;
}

.detail-content {
  padding: 10px 0;
}

.schedule-detail-dialog {
  .schedule-detail-content {
    max-height: 70vh;
    overflow-y: auto;
  }
}

.statistics-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-card :deep(.el-statistic__head) {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-card :deep(.el-statistic__number) {
  font-size: 24px;
  font-weight: bold;
}

.search-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .plan-generation-view {
    padding: 12px;
  }
  
  .page-header h2 {
    font-size: 1.2rem;
  }
  
  .module-nav {
    min-height: auto;
  }
  
  .tab-content {
    padding: 12px;
  }
  
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .schedule-detail-dialog {
    width: 95% !important;
  }
}
</style>

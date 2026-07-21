<template>
  <div class="schedule">
    <div class="page-header">
      <h2 class="page-title">赛程日历</h2>
      <div class="header-actions">
        <el-select v-model="filterStage" placeholder="筛选阶段" clearable style="width: 140px" @change="loadSchedule">
          <el-option label="小组赛" value="GROUP" />
          <el-option label="16强" value="ROUND_OF_16" />
          <el-option label="8强" value="QUARTER" />
          <el-option label="半决赛" value="SEMI" />
          <el-option label="决赛" value="FINAL" />
        </el-select>
      </div>
    </div>

    <el-card shadow="hover">
      <el-table :data="filteredSchedule" stripe v-loading="loading" height="650">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="stage" label="阶段" width="100">
          <template #default="{ row }">
            <el-tag :type="stageColor[row.stage]" size="small">{{ stageLabel[row.stage] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="group" label="小组" width="70" align="center">
          <template #default="{ row }">
            <span v-if="row.group">{{ row.group }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="主队" min-width="140" align="right">
          <template #default="{ row }">{{ row.homeTeamName }}</template>
        </el-table-column>
        <el-table-column label="比分" width="100" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 'COMPLETED'">
              <el-tag type="info" size="small">{{ row.homeGoals }} - {{ row.awayGoals }}</el-tag>
            </template>
            <template v-else>
              <el-tag type="warning" size="small" effect="plain">未赛</el-tag>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="客队" min-width="140">
          <template #default="{ row }">{{ row.awayTeamName }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'info'" size="small">
              {{ row.status === 'COMPLETED' ? '已完赛' : '待进行' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="schedule-stats">
        共 {{ filteredSchedule.length }} 场比赛 |
        已完赛 {{ filteredSchedule.filter(m => m.status === 'COMPLETED').length }} 场 |
        待进行 {{ filteredSchedule.filter(m => m.status !== 'COMPLETED').length }} 场
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'

const schedule = ref([])
const loading = ref(false)
const filterStage = ref('')

const stageLabel = {
  GROUP: '小组赛', ROUND_OF_16: '16强', QUARTER: '8强',
  SEMI: '半决赛', FINAL: '决赛', THIRD_PLACE: '季军赛'
}
const stageColor = {
  GROUP: 'primary', ROUND_OF_16: 'success', QUARTER: 'warning',
  SEMI: 'danger', FINAL: 'info', THIRD_PLACE: 'info'
}

const filteredSchedule = computed(() => {
  if (!filterStage.value) return schedule.value
  return schedule.value.filter(m => m.stage === filterStage.value)
})

onMounted(() => loadSchedule())

async function loadSchedule() {
  loading.value = true
  try {
    const res = await request.get('/dashboard/schedule')
    schedule.value = res.data
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 22px; color: #333; }
.header-actions { display: flex; gap: 10px; }
.schedule-stats {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
  color: #666;
  font-size: 14px;
}
</style>
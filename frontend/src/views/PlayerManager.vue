<template>
  <div class="player-manager">
    <div class="page-header">
      <h2 class="page-title">球员管理</h2>
      <div class="header-actions">
        <el-select v-model="filterTeam" placeholder="选择球队" clearable style="width: 160px" @change="loadPlayers">
          <el-option v-for="t in teams" :key="t.id" :label="t.flagEmoji + ' ' + t.name" :value="t.id" />
        </el-select>
        <el-select v-model="filterPosition" placeholder="选择位置" clearable style="width: 120px" @change="loadPlayers">
          <el-option label="门将 GK" value="GK" />
          <el-option label="后卫 DF" value="DF" />
          <el-option label="中场 MF" value="MF" />
          <el-option label="前锋 FW" value="FW" />
        </el-select>
        <el-button v-if="authStore.isAdmin" type="primary" icon="Plus" @click="openDialog(null)">新增球员</el-button>
      </div>
    </div>

    <el-card shadow="hover">
      <el-table :data="players" stripe v-loading="loading" height="600">
        <el-table-column prop="name" label="姓名" min-width="140" />
        <el-table-column label="球队" width="130">
          <template #default="{ row }">
            {{ getTeamName(row.teamId) }}
          </template>
        </el-table-column>
        <el-table-column prop="position" label="位置" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="positionColor[row.position]" size="small">{{ row.position }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="jerseyNumber" label="号码" width="70" align="center" />
        <el-table-column prop="age" label="年龄" width="70" align="center" />
        <el-table-column prop="height" label="身高" width="70" align="center">
          <template #default="{ row }">{{ row.height }}cm</template>
        </el-table-column>
        <el-table-column prop="rating" label="能力值" width="80" align="center" sortable>
          <template #default="{ row }">
            <span :style="{ color: row.rating >= 85 ? '#F56C6C' : row.rating >= 75 ? '#E6A23C' : '#67C23A', fontWeight: 'bold' }">
              {{ row.rating }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="goals" label="进球" width="70" align="center" />
        <el-table-column prop="assists" label="助攻" width="70" align="center" />
        <el-table-column label="操作" width="120" v-if="authStore.isAdmin">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="deletePlayer(row.id)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editPlayer ? '编辑球员' : '新增球员'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="球队">
          <el-select v-model="form.teamId" style="width: 100%">
            <el-option v-for="t in teams" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置">
          <el-select v-model="form.position" style="width: 100%">
            <el-option label="门将 GK" value="GK" />
            <el-option label="后卫 DF" value="DF" />
            <el-option label="中场 MF" value="MF" />
            <el-option label="前锋 FW" value="FW" />
          </el-select>
        </el-form-item>
        <el-form-item label="球衣号码"><el-input-number v-model="form.jerseyNumber" :min="1" :max="99" /></el-form-item>
        <el-form-item label="年龄"><el-input-number v-model="form.age" :min="16" :max="45" /></el-form-item>
        <el-form-item label="能力值"><el-slider v-model="form.rating" :min="40" :max="99" show-input /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePlayer">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const players = ref([])
const teams = ref([])
const loading = ref(false)
const filterTeam = ref(null)
const filterPosition = ref('')
const dialogVisible = ref(false)
const editPlayer = ref(null)

const positionColor = { GK: 'warning', DF: 'primary', MF: 'success', FW: 'danger' }

const form = ref({
  name: '', teamId: null, position: 'MF', jerseyNumber: 10,
  age: 25, height: 178, weight: 75, rating: 75
})

onMounted(async () => {
  const teamRes = await request.get('/teams')
  teams.value = teamRes.data
  loadPlayers()
})

async function loadPlayers() {
  loading.value = true
  try {
    const params = {}
    if (filterTeam.value) params.teamId = filterTeam.value
    if (filterPosition.value) params.position = filterPosition.value
    const res = await request.get('/players', { params })
    players.value = res.data
  } finally {
    loading.value = false
  }
}

function getTeamName(teamId) {
  const team = teams.value.find(t => t.id === teamId)
  return team ? team.flagEmoji + ' ' + team.name : '未知'
}

function openDialog(player) {
  if (!authStore.isAdmin) return
  editPlayer.value = player
  form.value = player ? { ...player } : { name: '', teamId: null, position: 'MF', jerseyNumber: 10, age: 25, height: 178, weight: 75, rating: 75 }
  dialogVisible.value = true
}

async function savePlayer() {
  try {
    if (editPlayer.value) {
      await request.put('/players/' + editPlayer.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/players', form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadPlayers()
  } catch (e) {}
}

async function deletePlayer(id) {
  await request.delete('/players/' + id)
  ElMessage.success('删除成功')
  loadPlayers()
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 10px; }
.page-title { font-size: 22px; color: #333; }
.header-actions { display: flex; gap: 10px; align-items: center; }
</style>
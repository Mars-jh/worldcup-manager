<template>
  <div class="team-manager">
    <div class="page-header">
      <h2 class="page-title">球队管理</h2>
      <div class="header-actions">
        <el-input v-model="searchText" placeholder="搜索球队..." prefix-icon="Search" clearable style="width: 200px" @input="handleSearch" />
        <el-select v-model="filterGroup" placeholder="筛选小组" clearable style="width: 120px" @change="loadTeams">
          <el-option v-for="g in 'ABCDEFGH'.split('')" :key="g" :label="g + ' 组'" :value="g" />
        </el-select>
        <el-button v-if="authStore.isAdmin" type="primary" icon="Plus" @click="openDialog(null)">新增球队</el-button>
      </div>
    </div>

    <el-card shadow="hover">
      <el-table :data="teams" stripe v-loading="loading" @row-click="openDialog">
        <el-table-column prop="flagEmoji" label="国旗" width="70" align="center" />
        <el-table-column prop="name" label="球队名称" min-width="120" />
        <el-table-column prop="code" label="代码" width="80" />
        <el-table-column prop="continent" label="大洲" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ continentLabel[row.continent] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="groupLetter" label="小组" width="70" align="center">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.groupLetter }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="coach" label="主教练" width="140" />
        <el-table-column prop="worldRanking" label="世界排名" width="90" align="center" sortable />
        <el-table-column label="操作" width="120" v-if="authStore.isAdmin">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click.stop="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该球队？" @confirm="deleteTeam(row.id)">
              <template #reference>
                <el-button link type="danger" size="small" @click.stop>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editTeam ? '编辑球队' : '新增球队'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="球队名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="代码"><el-input v-model="form.code" maxlength="3" /></el-form-item>
        <el-form-item label="大洲">
          <el-select v-model="form.continent" style="width: 100%">
            <el-option v-for="(label, key) in continentLabel" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="小组">
          <el-select v-model="form.groupLetter" style="width: 100%">
            <el-option v-for="g in 'ABCDEFGH'.split('')" :key="g" :label="g + ' 组'" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="主教练"><el-input v-model="form.coach" /></el-form-item>
        <el-form-item label="世界排名"><el-input-number v-model="form.worldRanking" :min="1" :max="200" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTeam">保存</el-button>
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
const teams = ref([])
const loading = ref(false)
const searchText = ref('')
const filterGroup = ref('')
const dialogVisible = ref(false)
const editTeam = ref(null)

const continentLabel = {
  EUROPE: '欧洲', SOUTH_AMERICA: '南美洲', NORTH_AMERICA: '北美洲',
  AFRICA: '非洲', ASIA: '亚洲', OCEANIA: '大洋洲'
}

const form = ref({
  name: '', code: '', continent: 'EUROPE', groupLetter: 'A',
  coach: '', worldRanking: 1
})

onMounted(() => loadTeams())

async function loadTeams() {
  loading.value = true
  try {
    const params = {}
    if (filterGroup.value) params.group = filterGroup.value
    const res = await request.get('/teams', { params })
    teams.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  if (searchText.value) {
    const res = await request.get('/teams', { params: { keyword: searchText.value } })
    teams.value = res.data
  } else {
    loadTeams()
  }
}

function openDialog(team) {
  if (!authStore.isAdmin) return
  editTeam.value = team
  if (team) {
    form.value = { ...team }
  } else {
    form.value = { name: '', code: '', continent: 'EUROPE', groupLetter: 'A', coach: '', worldRanking: 1 }
  }
  dialogVisible.value = true
}

async function saveTeam() {
  try {
    if (editTeam.value) {
      await request.put('/teams/' + editTeam.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/teams', form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadTeams()
  } catch (e) {}
}

async function deleteTeam(id) {
  await request.delete('/teams/' + id)
  ElMessage.success('删除成功')
  loadTeams()
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}
.page-title { font-size: 22px; color: #333; }
.header-actions { display: flex; gap: 10px; align-items: center; }
</style>
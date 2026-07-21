<template>
  <div class="group-stage">
    <div class="page-header">
      <h2 class="page-title">小组赛</h2>
      <div class="header-actions">
        <el-radio-group v-model="currentGroup" size="large" @change="loadGroupData">
          <el-radio-button v-for="g in groups" :key="g" :value="g">{{ g }} 组</el-radio-button>
        </el-radio-group>
        <el-button v-if="authStore.isAdmin" type="success" @click="generateKnockout">
          生成淘汰赛对阵
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 积分榜 -->
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">📊 {{ currentGroup }} 组积分榜</span>
          </template>
          <el-table :data="standings" stripe size="small" v-loading="loadingStandings">
            <el-table-column type="index" label="#" width="40" />
            <el-table-column prop="flagEmoji" label="" width="40" align="center" />
            <el-table-column prop="name" label="球队" min-width="100" />
            <el-table-column prop="played" label="赛" width="45" align="center" />
            <el-table-column prop="won" label="胜" width="45" align="center" />
            <el-table-column prop="drawn" label="平" width="45" align="center" />
            <el-table-column prop="lost" label="负" width="45" align="center" />
            <el-table-column label="净胜" width="55" align="center">
              <template #default="{ row }">
                <span :style="{ color: row.goalDifference > 0 ? '#67C23A' : row.goalDifference < 0 ? '#F56C6C' : '#999' }">
                  {{ row.goalDifference > 0 ? '+' : '' }}{{ row.goalDifference }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="goalsFor" label="进球" width="50" align="center" />
            <el-table-column prop="points" label="积分" width="55" align="center">
              <template #default="{ row }">
                <span style="font-weight: bold; color: #409EFF">{{ row.points }}</span>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top: 10px; color: #999; font-size: 12px">
            * 前两名晋级淘汰赛
          </div>
        </el-card>
      </el-col>

      <!-- 比赛列表 -->
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">⚽ {{ currentGroup }} 组赛程</span>
          </template>
          <el-table :data="matches" stripe size="small" v-loading="loadingMatches">
            <el-table-column label="主队" min-width="120" align="right">
              <template #default="{ row }">
                <span>{{ getTeamName(row.homeTeamId) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="比分" width="120" align="center">
              <template #default="{ row }">
                <template v-if="row.status === 'COMPLETED'">
                  <el-tag type="info" size="small" effect="plain">
                    {{ row.homeGoals }} - {{ row.awayGoals }}
                  </el-tag>
                </template>
                <template v-else>
                  <el-tag type="warning" size="small" effect="plain">未赛</el-tag>
                </template>
              </template>
            </el-table-column>
            <el-table-column label="客队" min-width="120">
              <template #default="{ row }">
                <span>{{ getTeamName(row.awayTeamId) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center" v-if="authStore.isOperator">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openScoreDialog(row)">
                  {{ row.status === 'COMPLETED' ? '修改' : '录入' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 比分录入弹窗 -->
    <el-dialog v-model="scoreDialogVisible" title="录入比分" width="400px">
      <div class="score-dialog-content">
        <div class="score-team">
          <div class="team-name">{{ getTeamName(scoreForm.homeTeamId) }}</div>
          <el-input-number v-model="scoreForm.homeGoals" :min="0" :max="20" size="large" />
        </div>
        <div class="score-vs">VS</div>
        <div class="score-team">
          <div class="team-name">{{ getTeamName(scoreForm.awayTeamId) }}</div>
          <el-input-number v-model="scoreForm.awayGoals" :min="0" :max="20" size="large" />
        </div>
      </div>
      <template #footer>
        <el-button @click="scoreDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitScore">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const authStore = useAuthStore()
const groups = 'ABCDEFGH'.split('')
const currentGroup = ref('A')
const standings = ref([])
const matches = ref([])
const teams = ref([])
const loadingStandings = ref(false)
const loadingMatches = ref(false)
const scoreDialogVisible = ref(false)
const scoreForm = ref({ matchId: null, homeTeamId: null, awayTeamId: null, homeGoals: 0, awayGoals: 0 })

onMounted(async () => {
  const teamRes = await request.get('/teams')
  teams.value = teamRes.data
  loadGroupData()
})

async function loadGroupData() {
  loadingStandings.value = true
  loadingMatches.value = true
  try {
    const [standRes, matchRes] = await Promise.all([
      request.get('/groups/' + currentGroup.value + '/standings'),
      request.get('/matches', { params: { group: currentGroup.value } })
    ])
    standings.value = standRes.data
    matches.value = matchRes.data
  } finally {
    loadingStandings.value = false
    loadingMatches.value = false
  }
}

function getTeamName(teamId) {
  if (!teamId) return '待定'
  const team = teams.value.find(t => t.id === teamId)
  return team ? team.flagEmoji + ' ' + team.name : '未知'
}

function openScoreDialog(match) {
  scoreForm.value = {
    matchId: match.id,
    homeTeamId: match.homeTeamId,
    awayTeamId: match.awayTeamId,
    homeGoals: match.homeGoals || 0,
    awayGoals: match.awayGoals || 0
  }
  scoreDialogVisible.value = true
}

async function submitScore() {
  try {
    await request.put('/matches/' + scoreForm.value.matchId + '/score', {
      homeGoals: scoreForm.value.homeGoals,
      awayGoals: scoreForm.value.awayGoals
    })
    ElMessage.success('比分已录入')
    scoreDialogVisible.value = false
    loadGroupData()
  } catch (e) {}
}

async function generateKnockout() {
  try {
    await ElMessageBox.confirm('确定生成淘汰赛对阵？这将根据各组前两名自动生成16强对阵。', '确认', { type: 'warning' })
    await request.post('/groups/generate-knockout')
    ElMessage.success('淘汰赛对阵已生成！')
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 10px; }
.page-title { font-size: 22px; color: #333; }
.header-actions { display: flex; gap: 10px; align-items: center; }
.card-title { font-weight: bold; font-size: 15px; }
.score-dialog-content { display: flex; align-items: center; justify-content: space-around; padding: 20px 0; }
.score-team { text-align: center; }
.team-name { font-weight: bold; margin-bottom: 15px; font-size: 16px; }
.score-vs { font-size: 24px; font-weight: bold; color: #999; }
</style>
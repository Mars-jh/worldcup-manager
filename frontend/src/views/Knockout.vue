<template>
  <div class="knockout">
    <div class="page-header">
      <h2 class="page-title">淘汰赛对阵</h2>
      <el-button type="primary" icon="Refresh" @click="loadBracket">刷新</el-button>
    </div>

    <div v-if="!hasBracket" class="empty-state">
      <el-empty description="淘汰赛对阵尚未生成">
        <el-button v-if="authStore.isAdmin" type="primary" @click="$router.push('/groups')">
          前往小组赛生成对阵
        </el-button>
      </el-empty>
    </div>

    <div v-else class="bracket-container">
      <!-- 16强 -->
      <div class="bracket-round">
        <div class="round-title">16 强</div>
        <div class="match-list">
          <div v-for="match in bracket.ROUND_OF_16" :key="match.id" class="match-card" @click="openScoreDialog(match)">
            <div class="match-team" :class="{ winner: isWinner(match, match.homeTeamId) }">
              <span>{{ getTeamName(match.homeTeamId) }}</span>
              <span class="score" v-if="match.status === 'COMPLETED'">{{ match.homeGoals }}</span>
            </div>
            <div class="match-team" :class="{ winner: isWinner(match, match.awayTeamId) }">
              <span>{{ getTeamName(match.awayTeamId) }}</span>
              <span class="score" v-if="match.status === 'COMPLETED'">{{ match.awayGoals }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 8强 -->
      <div class="bracket-round">
        <div class="round-title">8 强</div>
        <div class="match-list">
          <div v-for="match in bracket.QUARTER" :key="match.id" class="match-card" @click="openScoreDialog(match)">
            <div class="match-team" :class="{ winner: isWinner(match, match.homeTeamId) }">
              <span>{{ getTeamName(match.homeTeamId) }}</span>
              <span class="score" v-if="match.status === 'COMPLETED'">{{ match.homeGoals }}</span>
            </div>
            <div class="match-team" :class="{ winner: isWinner(match, match.awayTeamId) }">
              <span>{{ getTeamName(match.awayTeamId) }}</span>
              <span class="score" v-if="match.status === 'COMPLETED'">{{ match.awayGoals }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 4强 -->
      <div class="bracket-round">
        <div class="round-title">半决赛</div>
        <div class="match-list">
          <div v-for="match in bracket.SEMI" :key="match.id" class="match-card" @click="openScoreDialog(match)">
            <div class="match-team" :class="{ winner: isWinner(match, match.homeTeamId) }">
              <span>{{ getTeamName(match.homeTeamId) }}</span>
              <span class="score" v-if="match.status === 'COMPLETED'">{{ match.homeGoals }}</span>
            </div>
            <div class="match-team" :class="{ winner: isWinner(match, match.awayTeamId) }">
              <span>{{ getTeamName(match.awayTeamId) }}</span>
              <span class="score" v-if="match.status === 'COMPLETED'">{{ match.awayGoals }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 决赛 -->
      <div class="bracket-round final-round">
        <div class="round-title">🏆 决赛</div>
        <div class="match-list">
          <div v-for="match in bracket.FINAL" :key="match.id" class="match-card final-card" @click="openScoreDialog(match)">
            <div class="match-team" :class="{ winner: isWinner(match, match.homeTeamId) }">
              <span>{{ getTeamName(match.homeTeamId) }}</span>
              <span class="score" v-if="match.status === 'COMPLETED'">{{ match.homeGoals }}</span>
            </div>
            <div class="match-team" :class="{ winner: isWinner(match, match.awayTeamId) }">
              <span>{{ getTeamName(match.awayTeamId) }}</span>
              <span class="score" v-if="match.status === 'COMPLETED'">{{ match.awayGoals }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 比分弹窗 -->
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
        <el-button type="primary" @click="submitScore" :disabled="!authStore.isOperator">提交比分</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const bracket = ref({})
const teams = ref([])
const scoreDialogVisible = ref(false)
const scoreForm = ref({ matchId: null, homeTeamId: null, awayTeamId: null, homeGoals: 0, awayGoals: 0 })

const hasBracket = computed(() => {
  return bracket.value.ROUND_OF_16 && bracket.value.ROUND_OF_16.length > 0
})

onMounted(async () => {
  const teamRes = await request.get('/teams')
  teams.value = teamRes.data
  loadBracket()
})

async function loadBracket() {
  try {
    const res = await request.get('/knockout/bracket')
    bracket.value = res.data
  } catch (e) {
    console.error(e)
  }
}

function getTeamName(teamId) {
  if (!teamId) return '待定'
  const team = teams.value.find(t => t.id === teamId)
  return team ? team.flagEmoji + ' ' + team.name : '待定'
}

function isWinner(match, teamId) {
  if (match.status !== 'COMPLETED' || !teamId) return false
  if (match.homeGoals > match.awayGoals) return teamId === match.homeTeamId
  if (match.awayGoals > match.homeGoals) return teamId === match.awayTeamId
  return false
}

function openScoreDialog(match) {
  if (!authStore.isOperator) return
  if (!match.homeTeamId || !match.awayTeamId) {
    ElMessage.warning('对阵双方尚未确定')
    return
  }
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
    ElMessage.success('比分已录入，胜者已晋级')
    scoreDialogVisible.value = false
    loadBracket()
  } catch (e) {}
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 22px; color: #333; }
.empty-state { text-align: center; padding: 60px 0; }

.bracket-container {
  display: flex;
  gap: 30px;
  overflow-x: auto;
  padding: 20px 0;
}
.bracket-round {
  min-width: 200px;
  flex-shrink: 0;
}
.round-title {
  font-weight: bold;
  font-size: 16px;
  text-align: center;
  margin-bottom: 15px;
  color: #333;
  padding-bottom: 8px;
  border-bottom: 2px solid #409EFF;
}
.match-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.match-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
}
.match-card:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}
.match-team {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  font-size: 13px;
  border-bottom: 1px solid #f0f0f0;
}
.match-team:last-child { border-bottom: none; }
.match-team.winner {
  background: #f0f9ff;
  font-weight: bold;
  color: #409EFF;
}
.score {
  font-weight: bold;
  font-size: 14px;
  min-width: 20px;
  text-align: center;
}
.final-card {
  border: 2px solid #FFD700;
  box-shadow: 0 2px 12px rgba(255, 215, 0, 0.3);
}
.final-round .round-title {
  border-bottom-color: #FFD700;
}

.score-dialog-content { display: flex; align-items: center; justify-content: space-around; padding: 20px 0; }
.score-team { text-align: center; }
.team-name { font-weight: bold; margin-bottom: 15px; font-size: 16px; }
.score-vs { font-size: 24px; font-weight: bold; color: #999; }
</style>
<template>
  <div class="user-manager">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
    </div>

    <el-card shadow="hover">
      <el-table :data="users" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleColor[row.role]" size="small">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="editUser(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="resetPwd(row)">重置密码</el-button>
            <el-popconfirm title="确定删除该用户？" @confirm="deleteUser(row.id)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="400px">
      <el-form :model="editForm" label-width="70px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" style="width: 100%">
            <el-option label="管理员 ADMIN" value="ADMIN" />
            <el-option label="操作员 OPERATOR" value="OPERATOR" />
            <el-option label="观众 VIEWER" value="VIEWER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="pwdDialogVisible" title="重置密码" width="400px">
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input :value="pwdForm.username" disabled />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPwd">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const users = ref([])
const loading = ref(false)
const editDialogVisible = ref(false)
const pwdDialogVisible = ref(false)
const editingId = ref(null)

const roleColor = { ADMIN: 'danger', OPERATOR: 'warning', VIEWER: 'info' }

const editForm = ref({ username: '', email: '', role: 'VIEWER', enabled: true })
const pwdForm = ref({ userId: null, username: '', newPassword: '' })

onMounted(() => loadUsers())

async function loadUsers() {
  loading.value = true
  try {
    const res = await request.get('/users')
    users.value = res.data
  } finally {
    loading.value = false
  }
}

function editUser(user) {
  editingId.value = user.id
  editForm.value = { username: user.username, email: user.email, role: user.role, enabled: user.enabled }
  editDialogVisible.value = true
}

async function saveUser() {
  try {
    await request.put('/users/' + editingId.value, editForm.value)
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    loadUsers()
  } catch {
    ElMessage.error('保存用户失败')
  }
}

function resetPwd(user) {
  pwdForm.value = { userId: user.id, username: user.username, newPassword: '' }
  pwdDialogVisible.value = true
}

async function submitResetPwd() {
  if (pwdForm.value.newPassword.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  try {
    await request.put('/users/' + pwdForm.value.userId + '/reset-password', { newPassword: pwdForm.value.newPassword })
    ElMessage.success('密码已重置')
    pwdDialogVisible.value = false
  } catch {
    ElMessage.error('重置密码失败')
  }
}

async function deleteUser(id) {
  try {
    await request.delete('/users/' + id)
    ElMessage.success('删除成功')
    loadUsers()
  } catch {
    ElMessage.error('删除用户失败')
  }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 22px; color: #333; }
</style>

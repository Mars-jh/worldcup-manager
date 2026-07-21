<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>⚽ 2066 世界杯管理系统</h1>
        <p>World Cup Manager</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>没有账号？</span>
        <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
      </div>
      <div class="demo-accounts">
        <p>演示账号：</p>
        <div class="demo-list">
          <el-tag @click="fillDemo('admin', 'admin123')" class="demo-tag" type="danger" effect="plain">admin</el-tag>
          <el-tag @click="fillDemo('operator', '123456')" class="demo-tag" type="warning" effect="plain">operator</el-tag>
          <el-tag @click="fillDemo('viewer', 'viewer123')" class="demo-tag" type="info" effect="plain">viewer</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // 错误已在 axios 拦截器中处理
  } finally {
    loading.value = false
  }
}

function fillDemo(username, password) {
  form.username = username
  form.password = password
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}
.login-header {
  text-align: center;
  margin-bottom: 30px;
}
.login-header h1 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}
.login-header p {
  color: #999;
  font-size: 14px;
}
.login-footer {
  text-align: center;
  margin-top: 15px;
  color: #666;
}
.demo-accounts {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  text-align: center;
}
.demo-accounts p {
  color: #999;
  font-size: 13px;
  margin-bottom: 10px;
}
.demo-list {
  display: flex;
  gap: 10px;
  justify-content: center;
}
.demo-tag {
  cursor: pointer;
}
</style>
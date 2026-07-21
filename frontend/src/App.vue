<template>
  <router-view />
</template>

<script setup>
// App 根组件 - 仅作为路由出口
// 全局样式
import { onMounted } from 'vue'

onMounted(() => {
  document.title = '2066 世界杯管理系统'
})
</script>

<style>
/* 全局基础样式 */
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif;
  background: #f0f2f5;
  color: #333;
}
/* 滚动条美化 */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-thumb { background: #c0c4cc; border-radius: 3px; }
::-webkit-scrollbar-track { background: transparent; }
</style>
<template>
  <div class="user-info-container">
    <!-- 其他现有内容 -->
    
    <div class="theme-switcher">
      <h3>主题设置</h3>
      <el-radio-group v-model="currentTheme" @change="changeTheme">
        <el-radio label="default">默认主题</el-radio>
        <el-radio label="eye-care">护眼模式</el-radio>
        <el-radio label="dark">暗黑模式</el-radio>
      </el-radio-group>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserInfoIndexView',
  data() {
    return {
      currentTheme: 'default'
    }
  },
  created() {
    // 从 localStorage 获取保存的主题
    const savedTheme = localStorage.getItem('theme') || 'default'
    this.currentTheme = savedTheme
    this.applyTheme(savedTheme)
  },
  methods: {
    changeTheme(theme) {
      this.applyTheme(theme)
      // 保存主题设置到 localStorage
      localStorage.setItem('theme', theme)
    },
    applyTheme(theme) {
      // 移除所有主题
      document.documentElement.removeAttribute('data-theme')
      
      // 应用新主题
      if (theme !== 'default') {
        document.documentElement.setAttribute('data-theme', theme)
      }
    }
  }
}
</script>

<style>
@import '@/assets/styles/themes.css';

.user-info-container {
  background-color: var(--primary-bg);
  color: var(--text-color);
}

.theme-switcher {
  margin: 20px 0;
  padding: 15px;
  background-color: var(--secondary-bg);
  border-radius: 8px;
}
</style> 
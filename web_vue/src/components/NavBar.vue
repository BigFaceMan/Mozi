<template>
  <nav class="navbar d-flex flex-column sidebar">
    <div class="container-fluid d-flex flex-column align-items-start">
      <router-link class="navbar-brand" :to="{name: 'home'}">智能体训练与仿真平台</router-link>
      <ul class="navbar-nav w-100">
        <li class="nav-item">
          <router-link :class="route_name == 'envconduct_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'envconduct_index'}">场景管理</router-link>
        </li>
        <li class="nav-item">
          <router-link :class="route_name == 'modelconduct_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'modelconduct_index'}">方法管理</router-link>
        </li>
        <li class="nav-item">
          <router-link :class="route_name == 'modeltrain_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'modeltrain_index'}">模型管理</router-link>
        </li>
        <li class="nav-item">
          <router-link :class="route_name == 'pk_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'pk_index'}">对战调度</router-link>
        </li>
        <li class="nav-item">
          <router-link :class="route_name == 'dataAnalysis_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'dataAnalysis_index'}">数据分析</router-link>
        </li>
        <li class="nav-item">
          <router-link :class="route_name == 'gameNodes_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'gameNodes_index'}">计算节点</router-link>
        </li>
        <li class="nav-item">
          <router-link :class="route_name == 'engineNodes_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'engineNodes_index'}">引擎节点</router-link>
        </li>
        <li class="nav-item">
          <router-link :class="route_name == 'logConduct_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'logConduct_index'}">日志管理</router-link>
        </li>
        <li class="nav-item">
          <router-link :class="route_name == 'help_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'help_index'}">帮助</router-link>
        </li>
        <li class="nav-item" v-if="$store.state.user.urank==='1'">
          <router-link :class="route_name == 'useropt_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'useropt_index'}">用户管理</router-link>
        </li>
      </ul>
      <ul class="navbar-nav w-100 mt-auto" v-if="$store.state.user.is_login">
        <li class="nav-item dropdown">
          <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
            {{ $store.state.user.username }}
          </a>
          <ul class="dropdown-menu" aria-labelledby="navbarDropdown">
            <li>
                <router-link class="dropdown-item" :to="{name: 'userinfo_index'}">我的信息</router-link>
            </li>
            <li><hr class="dropdown-divider"></li>
            <li><a class="dropdown-item" href="#" @click="logout">退出</a></li>
          </ul>
        </li>
      </ul>
      <ul class="navbar-nav w-100" v-else-if="!$store.state.user.pulling_info">
        <li class="nav-item">
          <router-link class="nav-link" :to="{name: 'user_account_login' }">登录</router-link>
        </li>
        <li class="nav-item">
          <router-link class="nav-link" :to="{name: 'user_account_register'}">注册</router-link>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script>
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import { useStore } from 'vuex';

export default {
    setup() {
        const store = useStore();
        const route = useRoute();
        let route_name = computed(() => route.name)

        const logout = () => {
          store.dispatch("logout");
        }

        return {
            route_name,
            logout
        }
    }
}
</script>

<style scoped>
/* 让导航栏固定在左侧，并添加透明效果 */
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 220px; /* 侧边栏宽度 */
  height: 100vh;
  background: rgba(0, 0, 0, 0.6); /* 半透明黑色 */
  backdrop-filter: blur(10px); /* 添加模糊效果 */
  overflow-y: hidden;
  padding: 10px;
  transition: all 0.3s ease-in-out;
}

/* 让主页面右移，避免被导航栏遮挡 */
.main-content {
  margin-left: 250px;
  padding: 20px;
}

/* 调整导航栏标题，使其宽度与菜单项一致 */
.navbar-brand {
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  width: 100%;
  padding: 15px 0;
  color: white;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

/* 统一菜单项宽度和样式 */
.nav-item {
  width: 100%;
}

/* 让导航项变得更现代化 */
.nav-link {
  color: rgba(255, 255, 255, 0.8);
  padding: 12px 15px;
  border-radius: 8px;
  transition: background 0.3s, color 0.3s;
  text-align: center;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

/* 当前选中的导航项 */
.nav-link.active {
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
  font-weight: bold;
}

/* 下拉菜单优化 */
.dropdown-menu {
  background: rgba(0, 0, 0, 0.8);
  border: none;
}

.dropdown-item {
  color: white;
}

.dropdown-item:hover {
  background: rgba(255, 255, 255, 0.2);
}

</style>

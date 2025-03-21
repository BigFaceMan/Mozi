<!--
 * @Author: ssp
 * @Date: 2024-11-13 15:06:09
 * @LastEditTime: 2025-03-21 16:00:40
-->
<template>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
  <div class="container">
    <router-link class="navbar-brand" :to="{name: 'home'}">智能体训练与仿真平台</router-link>
    <div class="collapse navbar-collapse" id="navbarText">
      <ul class="navbar-nav me-auto mb-2 mb-lg-0">
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
          <router-link :class="route_name == 'logConduct_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'logConduct_index'}">日志管理</router-link>
        </li>
        <li class="nav-item">
          <router-link :class="route_name == 'gameNodes_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'gameNodes_index'}"> 计算节点</router-link>
        </li>
        <li class="nav-item">
          <router-link :class="route_name == 'engineNodes_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'engineNodes_index'}">引擎节点</router-link>
        </li>
        <li class="nav-item">
          <router-link :class="route_name == 'help_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'help_index'}"> 帮助</router-link>
        </li>
        <li class="nav-item" v-if="$store.state.user.urank==='1'">
          <router-link :class="route_name == 'useropt_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'useropt_index'}">用户管理</router-link>
        </li>
      </ul>
      <ul class="navbar-nav" v-if="$store.state.user.is_login">
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
      <ul class="navbar-nav" v-else-if="!$store.state.user.pulling_info">
        <li class="nav-item">
          <router-link class="nav-link" :to="{name: 'user_account_login' }" role="button">
            登录
          </router-link>
        </li>
        <li class="nav-item">
          <router-link class="nav-link" :to="{name: 'user_account_register'}" role="button">
            注册
          </router-link>
        </li>
      </ul>
    </div>
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

</style>
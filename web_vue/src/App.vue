<template>
  <div :style="backgroundStyle">
    <div v-if="$store.state.themeSet.useTopNav">
      <NavBarRow />
      <router-view></router-view>
    </div>
    
    <div v-else class="app-container">
      <NavBar />
      <div class="main-content">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script>
import NavBarRow from "./components/NavBarRow.vue";
import NavBar from "./components/NavBar.vue";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap";
import { computed } from "vue";
import { useStore } from "vuex";

export default {
  components: {
    NavBarRow,
    NavBar,
  },
  setup() {
    // 这里可以通过某个配置决定默认是顶部导航还是侧边栏
    const store = useStore();
 // 计算属性，根据导航模式返回不同的背景图片
    const backgroundStyle = computed(() => {
      return {
        backgroundImage: store.state.themeSet.useTopNav
          ? `url(${new URL('@/assets/images/background.png', import.meta.url)})`
          : `url(${new URL('@/assets/images/theme2.jpg', import.meta.url)})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
        height: "100vh", // 关键：确保背景容器高度填满视口
        width: "100vw", // 关键：确保背景横向填满
      };
    });

    return {
      store,
      backgroundStyle
    };
  },
};
</script>

<style>
.app-container {
  display: flex;
}

.main-content {
  flex: 1;
  padding: 20px;
  margin-left: 250px;
}

/* body {
  background-image: url("@/assets/images/background.png");
  background-size: cover;
} */

#app {
  background-color: var(--primary-bg);
  color: var(--text-color);
}

a {
  color: var(--link-color);
}
</style>

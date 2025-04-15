<!--
 * @Author: ssp 71054301+BigFaceMan@users.noreply.github.com
 * @Date: 2025-03-05 20:25:05
 * @LastEditors: ssp 71054301+BigFaceMan@users.noreply.github.com
 * @LastEditTime: 2025-04-14 10:23:59
 * @FilePath: \web_vue\src\App.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
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
    // 计算属性：动态获取背景图片
    const backgroundStyle = computed(() => {
      // 优先使用用户自定义背景
      const customBg = store.state.themeSet.customBackground;
      if (customBg) {
        console.log("App catch change : ", customBg)
        return {
          backgroundImage: `url(${customBg})`,
          backgroundSize: "cover",
          backgroundPosition: "center",
          backgroundRepeat: "no-repeat",
          minHeight: "100vh",
          width: "100vw",
        };
      }

      // 根据导航模式使用默认背景
      return {
        backgroundImage: store.state.themeSet.useTopNav
          ? `url(${new URL('@/assets/images/background.png', import.meta.url)})`
          : `url(${new URL('@/assets/images/theme2.jpg', import.meta.url)})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
        minHeight: "100vh",
        width: "100vw",
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

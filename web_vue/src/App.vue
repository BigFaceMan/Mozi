<!--
 * @Author: ssp
 * @Date: 2024-11-06 12:41:37
 * @LastEditTime: 2024-11-06 23:25:09
-->
<template>
  <NavBar />
  <router-view></router-view>
</template>

<script>
import NavBar from './components/NavBar.vue'
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap/dist/js/bootstrap"

export default {
  components: {
    NavBar
  },
  setup() {

  }
}
</script>

<style>
body {
  background-image: url("@/assets/images/background.png");
  background-size: cover;
}

#app {
  background-color: var(--primary-bg);
  color: var(--text-color);
}

a {
  color: var(--link-color);
}
</style>

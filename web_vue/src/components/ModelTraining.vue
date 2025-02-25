<template>
  <div class="container">
    <!-- 仅显示 TensorBoard 区域 -->
    <div v-if="isTraining" class="card mb-3">
      <div class="card-body">
        <!-- 使标题居中并美化 -->
        <h5 class="title">训练可视化</h5>
        <iframe src="http://localhost:6006" width="100%" height="800px" frameborder="0"></iframe>
      </div>
    </div>

    <!-- 开始训练和结束训练按钮 -->
   <div class="d-flex justify-content-center mb-3">
      <button class="btn btn-primary me-2" @click="startTraining" :disabled="isTraining">开始训练</button>
      <button class="btn btn-danger" @click="stopTraining" :disabled="!isTraining">结束训练</button>
    </div>
  </div>
</template>

<script>
import { onMounted, onUnmounted, computed } from 'vue';
import { useStore } from 'vuex';

export default {
  setup() {
    const store = useStore();
    const isTraining = computed(() => store.state.pk.trainingStatus === "running");
    const socketUrl = `ws://127.0.0.1:3000/websocket/${store.state.user.token}/`;

    let socket = null;

    // 开始训练
    const startTraining = () => {
      store.commit('updateTrainingStatus', 'running');
      store.dispatch('startTraining');
    };

    // 停止训练
    const stopTraining = () => {
      store.commit('updateTrainingStatus', 'stopped');
      store.dispatch('stopTraining');
    };

    // 页面挂载时连接 WebSocket
    onMounted(() => {
      socket = new WebSocket(socketUrl);
      console.log("create websocket");

      socket.onopen = () => {
        console.log("connected!");
        store.commit("updateSocket", socket);
      };

      socket.onmessage = (msg) => {
        const data = JSON.parse(msg.data);
        console.log("通信成功", data);
        if (data.error_message === "complete") {
          store.commit("updateTrainingStatus", "stopped")
        }
      };

      socket.onclose = () => {
        console.log("disconnected!");
      };
    });

    // 页面卸载时关闭 WebSocket
    onUnmounted(() => {
      if (!isTraining.value) { // 只有当训练状态为 false 时才执行关闭操作
        if (socket && socket.readyState !== WebSocket.CLOSED) {
          socket.close(); // 关闭 WebSocket 连接
          store.commit('updateTrainingData', true);
          console.log("WebSocket 连接已关闭");
        }
      }
    });

    return {
      startTraining,
      stopTraining,
      isTraining,
    };
  }
};
</script>

<style scoped>
.container {
  max-width: 1000px; /* 增加容器最大宽度 */
}

.card-body {
  font-size: 0.95rem;
}

.title {
  text-align: center; /* 将标题居中 */
  font-size: 1.5rem; /* 增加字体大小 */
  font-weight: bold; /* 加粗字体 */
  color: #333; /* 设置字体颜色 */
  margin-bottom: 20px; /* 增加底部间距 */
}

iframe {
  border: none;
}
</style>

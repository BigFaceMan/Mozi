<!--
 * @Author: ssp
 * @Date: 2024-11-11 15:54:25
 * @LastEditTime: 2024-11-12 20:11:53
-->
<template>
  <div class="container">
    <!-- 训练信息展示区域 -->
    <div v-if="trainingData">
      <h4>训练可视化</h4>
      <div class="card mb-3">
        <div class="card-body">
          <h5>实时训练监控</h5>
          <p>训练进度: {{ trainingData.progress }}%</p>
          <div class="progress mb-3">
            <div class="progress-bar" role="progressbar" :style="{ width: trainingData.progress + '%' }"
                 :aria-valuenow="trainingData.progress" aria-valuemin="0" aria-valuemax="100">
              {{ trainingData.progress }}%
            </div>
          </div>
          <p>模型损失: {{ trainingData.loss }}</p>
        </div>
      </div>

      <h4>训练资源管理</h4>

      <!-- 资源管理部分 -->
      <div class="row">
        <!-- 计算资源调度 -->
        <div class="col-md-6">
          <div class="card mb-3">
            <div class="card-body">
              <h5>计算资源调度</h5>
              <p>CPU 利用率</p>
              <canvas id="cpuChart" width="200" height="200"></canvas>

              <p>GPU 利用率</p>
              <canvas id="gpuChart" width="200" height="200"></canvas>
            </div>
          </div>
        </div>

        <!-- 存储资源优化和网络资源管理 -->
        <div class="col-md-6">
          <div class="card mb-3">
            <div class="card-body">
              <h5>存储资源优化</h5>
              <p>内存占用率</p>
              <canvas id="memoryChart" width="200" height="200"></canvas>

              <p>显存占用率</p>
              <canvas id="vramChart" width="200" height="200"></canvas>
            </div>
          </div>
        </div>

        <div class="col-md-12">
          <div class="card mb-3">
            <div class="card-body">
              <h5>网络资源管理</h5>
              <p>训练数据传输带宽: {{ trainingData.networkBandwidth }} MB/s</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 开始训练和结束训练按钮 -->
    <div class="mb-3">
      <button class="btn btn-primary me-2" @click="startTraining" :disabled="isTraining">开始训练</button>
      <button class="btn btn-danger" @click="stopTraining" :disabled="!isTraining">结束训练</button>
    </div>
  </div>
</template>

<script>
import { onMounted, onUnmounted, computed } from 'vue';
import { useStore } from 'vuex';
import Chart from 'chart.js/auto';

export default {
  setup() {
    const store = useStore();
    const trainingData = computed(() => store.state.pk.trainingData);
    const isTraining = computed(() => store.state.pk.trainingStatus === "running");
    const socketUrl = `ws://127.0.0.1:3000/websocket/${store.state.user.token}/`;

    let socket = null;
    let cpuChart = null;
    let gpuChart = null;
    let memoryChart = null;
    let vramChart = null;

    // 开始训练
    const startTraining = () => {
      store.commit('updateTrainingStatus', 'running');
      console.log("是否训练 : ", isTraining.value)
      store.dispatch('startTraining');
    };

    // 停止训练
    const stopTraining = () => {
      store.commit('updateTrainingStatus', 'stopped');
      store.dispatch('stopTraining');
    };

    // 初始化饼状图函数
    const initCharts = () => {
      const ctxCpu = document.getElementById('cpuChart').getContext('2d');
      const ctxGpu = document.getElementById('gpuChart').getContext('2d');
      const ctxMemory = document.getElementById('memoryChart').getContext('2d');
      const ctxVram = document.getElementById('vramChart').getContext('2d');

      cpuChart = new Chart(ctxCpu, {
        type: 'doughnut',
        data: {
          labels: ['使用率', '空闲'],
          datasets: [{
            data: [0, 100],
            backgroundColor: ['#007bff', '#e9ecef']
          }]
        },
        options: {
          responsive: false,
          maintainAspectRatio: true
        }
      });

      gpuChart = new Chart(ctxGpu, {
        type: 'doughnut',
        data: {
          labels: ['使用率', '空闲'],
          datasets: [{
            data: [0, 100],
            backgroundColor: ['#28a745', '#e9ecef']
          }]
        },
        options: {
          responsive: false,
          maintainAspectRatio: true
        }
      });

      memoryChart = new Chart(ctxMemory, {
        type: 'doughnut',
        data: {
          labels: ['使用率', '空闲'],
          datasets: [{
            data: [0, 100],
            backgroundColor: ['#ffc107', '#e9ecef']
          }]
        },
        options: {
          responsive: false,
          maintainAspectRatio: true
        }
      });

      vramChart = new Chart(ctxVram, {
        type: 'doughnut',
        data: {
          labels: ['使用率', '空闲'],
          datasets: [{
            data: [0, 100],
            backgroundColor: ['#dc3545', '#e9ecef']
          }]
        },
        options: {
          responsive: false,
          maintainAspectRatio: true
        }
      });
    };

    // 更新饼状图数据
    const updateCharts = () => {
      console.log("on update charts")
      if (cpuChart && trainingData.value) {
        cpuChart.data.datasets[0].data = [trainingData.value.cpuUsage, 100 - trainingData.value.cpuUsage];
        cpuChart.update();
      }
      if (gpuChart && trainingData.value) {
        gpuChart.data.datasets[0].data = [trainingData.value.gpuUsage, 100 - trainingData.value.gpuUsage];
        gpuChart.update();
      }
      if (memoryChart && trainingData.value) {
        memoryChart.data.datasets[0].data = [trainingData.value.memoryUsage, 100 - trainingData.value.memoryUsage];
        memoryChart.update();
      }
      if (vramChart && trainingData.value) {
        vramChart.data.datasets[0].data = [trainingData.value.vramUsage, 100 - trainingData.value.vramUsage];
        vramChart.update();
      }
    };

    onMounted(() => {
      socket = new WebSocket(socketUrl);
      console.log("create websocket");

      socket.onopen = () => {
        console.log("connected!");
        console.log("pre ", store.state.pk.socket)
        store.commit("updateSocket", socket);
        console.log("after ", store.state.pk.socket)
      };

      socket.onmessage = (msg) => {
        const data = JSON.parse(msg.data);
        console.log("通信成功", data);
        if (data.error_message === "complete") {
          store.commit("updateTrainingStatus", "stopped")
        }
        else {
          store.commit('updateTrainingData', {
            progress: data.progress || 0,
            loss: data.loss || "N/A",
            cpuUsage: data.cpu_usage || 0,
            gpuUsage: data.gpu_usage || 0,
            memoryUsage: data.memory_usage || 0,
            vramUsage: data.vram_usage || 0,
            networkBandwidth: data.network_bandwidth || 0,
          });
        }
        updateCharts();
      };

      socket.onclose = () => {
        console.log("disconnected!");
      };

      if (isTraining.value) {
        console.log("init and update Charts")
        // initCharts();
        updateCharts();
      }
      else {
        initCharts();
      }
    });


  onUnmounted(() => {
    if (!isTraining.value) { // 只有当训练状态为 false 时才执行关闭操作
      if (socket && socket.readyState !== WebSocket.CLOSED) {
        socket.close(); // 关闭 WebSocket 连接
        store.commit('updateTrainingData', true
        );
        console.log("WebSocket 连接已关闭");
      }
      // 销毁图表
      cpuChart && cpuChart.destroy();
      gpuChart && gpuChart.destroy();
      memoryChart && memoryChart.destroy();
      vramChart && vramChart.destroy();
      console.log("摧毁图表")
    }
  });


    return {
      startTraining,
      stopTraining,
      trainingData,
      isTraining,
    };
  }
};
</script>

<style scoped>
.container {
  max-width: 800px;
}
.card-body {
  font-size: 0.95rem;
}
h5 {
  margin-bottom: 10px;
}
.progress-bar {
  font-size: 0.85rem;
}
</style>

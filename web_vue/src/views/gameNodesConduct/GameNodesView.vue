<template>
  <div class="container mt-4">
    <div class="row">
      <!-- 遍历所有计算节点并生成卡片 -->
      <div class="col-md-4" v-for="(node, index) in gameNodes" :key="index">
        <div class="card shadow-sm mb-4">
          <div class="card-body">
            <h5 class="card-title">节点 {{ index + 1 }}</h5>
            
            <ul class="list-group list-group-flush">
              <li class="list-group-item"><strong>IP 地址:</strong> {{ node.ip }}</li>
              <li class="list-group-item"><strong>端口:</strong> {{ node.port }}</li>
              
              <!-- 显示内存大小和显存大小 -->
              <li class="list-group-item"><strong>内存大小:</strong> {{ node.memorySize }} GB</li>
              <li class="list-group-item"><strong>显存大小:</strong> {{ node.gpuMemorySize }} GB</li>
              <!-- CPU 使用率图 -->
              <li class="list-group-item">
                <strong>CPU 使用率:</strong>
                <div class="chart-container">
                  <PieChart :data="cpuData(node.cpuUsage)" />
                </div>
              </li>

              <!-- GPU 使用率图 -->
              <li class="list-group-item">
                <strong>GPU 使用率:</strong>
                <div class="chart-container">
                  <PieChart :data="gpuData(node.gpuUsage)" />
                </div>
              </li>

              <!-- 内存使用率图 -->
              <li class="list-group-item">
                <strong>内存使用率:</strong>
                <div class="chart-container">
                  <PieChart :data="memoryData(node.memoryUsage)" />
                </div>
              </li>

              <!-- 显存使用率图 -->
              <li class="list-group-item">
                <strong>显存使用率:</strong>
                <div class="chart-container">
                  <PieChart :data="gpuMemoryData(node.gpuMemoryUsage)" />
                </div>
              </li>

              
              <li class="list-group-item"><strong>网络使用率:</strong> {{ node.networkUsage }}%</li>
              <li class="list-group-item"><strong>更新时间:</strong> {{ node.updateTime }}</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useStore } from 'vuex';
import $ from 'jquery';
import { Pie } from 'vue-chartjs';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';

// 注册 Chart.js 插件
ChartJS.register(ArcElement, Tooltip, Legend);

// 注册图表
const PieChart = Pie;

// 模拟数据
const store = useStore();
const gameNodes = ref([]);

const fetchGameNodes = () => {
  $.ajax({
    url: "http://127.0.0.1:3000/games/get/all/",
    type: "get",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    success(resp) {
      resp.sort((a, b) => {
        if (b.gpuMemorySize !== a.gpuMemorySize) {
          return b.gpuMemorySize - a.gpuMemorySize; // GPU 显存越大越优先
        }
        if (a.memoryUsage !== b.memoryUsage) {
          return a.memoryUsage - b.memoryUsage; // 内存使用率越小越优先
        }
        return a.gpuUsage - b.gpuUsage; // GPU 使用率越小越优先
      });
      gameNodes.value = resp;
    }
  });
};

onMounted(fetchGameNodes);

// 数据处理函数，生成图表数据
const cpuData = (cpuUsage) => {
  return {
    labels: ['Used', 'Remaining'],
    datasets: [{
      data: [cpuUsage, 100 - cpuUsage],
      backgroundColor: ['#ff6384', '#36a2eb'],
      hoverBackgroundColor: ['#ff6384', '#36a2eb']
    }]
  };
};

const gpuData = (gpuUsage) => {
  return {
    labels: ['Used', 'Remaining'],
    datasets: [{
      data: [gpuUsage, 100 - gpuUsage],
      backgroundColor: ['#ff9f40', '#36a2eb'],
      hoverBackgroundColor: ['#ff9f40', '#36a2eb']
    }]
  };
};

const memoryData = (memoryUsage) => {
  return {
    labels: ['Used', 'Remaining'],
    datasets: [{
      data: [memoryUsage, 100 - memoryUsage],
      backgroundColor: ['#ffcd56', '#36a2eb'],
      hoverBackgroundColor: ['#ffcd56', '#36a2eb']
    }]
  };
};

const gpuMemoryData = (gpuMemoryUsage) => {
  return {
    labels: ['Used', 'Remaining'],
    datasets: [{
      data: [gpuMemoryUsage, 100 - gpuMemoryUsage],
      backgroundColor: ['#4bc0c0', '#36a2eb'],
      hoverBackgroundColor: ['#4bc0c0', '#36a2eb']
    }]
  };
};
</script>

<style scoped>
.card {
  border-radius: 10px;
  background-color: #f9f9f9;
}

.card-body {
  background-color: #fff;
  border-radius: 10px;
}

.card-title {
  font-size: 1.25rem;
  color: #333;
}

.list-group-item {
  font-size: 1rem;
  background-color: #f2f2f2;
  color: #555;
}

.chart-container {
  height: 120px;  /* 增加高度，防止图表被遮挡 */
  width: 120px;   /* 调整宽度 */
  margin-left: auto;
  margin-right: auto;
}

.card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transform: scale(1.02);
  transition: 0.3s ease;
}
</style>

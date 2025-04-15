<template>
  <div>
    <h2>训练指标折线图</h2>
    <div v-if="dataLoaded">
      <div v-for="(chartData, key) in chartsData" :key="key">
        <h3>{{ keyToLabel[key] || key }}</h3>
        <canvas :id="'chart-' + key" class="chart-canvas"></canvas>
      </div>
    </div>
    <div v-else>
      <p>数据加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, defineProps } from 'vue';
import { useStore } from 'vuex';
import {
  Chart,
  Title,
  Tooltip,
  Legend,
  LineElement,
  LineController,
  CategoryScale,
  LinearScale,
  PointElement
} from 'chart.js';

Chart.register(Title, Tooltip, Legend, LineElement, CategoryScale, LinearScale, PointElement, LineController);

const props = defineProps({
  training: {
    type: Object,
    required: true,
  },
});

const store = useStore();

// 中英文映射表
const keyToLabel = {
  accuracy: '精度',
  speed: '速度',
  stability: '稳定性',
  loss: '损失',
  reward: '奖励',
};

const dataLoaded = ref(false);
const chartsData = reactive({
  accuracy: null,
  speed: null,
  stability: null,
  loss: null,
  reward: null,
});

const chartOptions = {
  responsive: true,
  scales: {
    x: {
      title: {
        display: true,
        text: '时间',
      },
    },
    y: {
      title: {
        display: true,
        text: '值',
      },
    },
  },
};

let chartInstances = {};

const processData = (rawData) => {
  const steps = [];
  const accuracyData = [];
  const speedData = [];
  const stabilityData = [];
  const lossData = [];
  const rewardData = [];

  rawData.forEach(item => {
    steps.push(item.step);
    accuracyData.push(item.accuracy !== -1 ? item.accuracy : null);
    speedData.push(item.speed !== -1 ? item.speed : null);
    stabilityData.push(item.stability !== -1 ? item.stability : null);
    lossData.push(item.loss !== -1 ? item.loss : null);
    rewardData.push(item.reward !== -1 ? item.reward : null);
  });

  chartsData.accuracy = {
    labels: steps,
    datasets: [{
      label: '精度',
      data: accuracyData,
      borderColor: 'rgba(75, 192, 192, 1)',
      fill: false,
    }],
  };

  chartsData.speed = {
    labels: steps,
    datasets: [{
      label: '速度',
      data: speedData,
      borderColor: 'rgba(153, 102, 255, 1)',
      fill: false,
    }],
  };

  chartsData.stability = {
    labels: steps,
    datasets: [{
      label: '稳定性',
      data: stabilityData,
      borderColor: 'rgba(255, 159, 64, 1)',
      fill: false,
    }],
  };

  chartsData.loss = {
    labels: steps,
    datasets: [{
      label: '损失',
      data: lossData,
      borderColor: 'rgba(255, 99, 132, 1)',
      fill: false,
    }],
  };

  chartsData.reward = {
    labels: steps,
    datasets: [{
      label: '奖励',
      data: rewardData,
      borderColor: 'rgba(54, 162, 235, 1)',
      fill: false,
    }],
  };

  dataLoaded.value = true;
};

const renderChart = () => {
  for (const key in chartsData) {
    const canvas = document.getElementById('chart-' + key);
    if (canvas) {
      chartInstances[key]?.destroy();
      chartInstances[key] = new Chart(canvas, {
        type: 'line',
        data: chartsData[key],
        options: chartOptions,
      });
    } else {
      console.error(`找不到画布 chart-${key}`);
    }
  }
};

onMounted(() => {
  fetch(`http://127.0.0.1:3000/train/info/?trainId=${props.training.id}`, {
    method: 'GET',
    headers: {
      Authorization: 'Bearer ' + store.state.user.token,
    },
  })
    .then(res => res.json())
    .then(resp => {
      if (resp && resp.data) {
        processData(resp.data);
        nextTick(() => {
          renderChart();
        });
      } else {
        console.warn("返回数据格式异常：", resp);
      }
    })
    .catch(err => {
      console.error("获取训练数据失败：", err);
    });
});

onBeforeUnmount(() => {
  for (const key in chartInstances) {
    chartInstances[key]?.destroy();
  }
});
</script>

<style scoped>
h2 {
  text-align: center;
  font-size: 24px;
  margin-bottom: 30px;
  color: #2c3e50;
}

h3 {
  font-size: 18px;
  color: #34495e;
  margin: 20px 0 10px;
  text-align: left;
  padding-left: 10px;
  border-left: 4px solid #3498db;
}

.chart-canvas {
  max-width: 100%;
  width: 100%;
  height: 400px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin: 20px auto;
  display: block;
  padding: 20px;
  transition: transform 0.2s ease-in-out;
}

.chart-canvas:hover {
  transform: scale(1.01);
}

@media (max-width: 768px) {
  .chart-canvas {
    height: 300px;
    padding: 10px;
  }

  h2 {
    font-size: 20px;
  }

  h3 {
    font-size: 16px;
  }
}

p {
  text-align: center;
  color: #7f8c8d;
  font-size: 16px;
  margin-top: 40px;
}
</style>

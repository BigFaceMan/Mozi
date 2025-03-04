<template>
  <div class="container mt-4">
    <h2 class="mb-4">参数配置</h2>

    <!-- 输入训练名称 -->
    <div class="form-group mb-3">
      <label for="trainingName">模型名称:</label>
      <input
        v-model="form.trainingName"
        id="trainingName"
        type="text"
        class="form-control"
        placeholder="请输入训练名称"
      />
    </div>
    <!-- 选择场景 -->
    <div class="form-group mb-3">
      <label for="scene">选择场景</label>
      <select v-model="form.scene" id="scene" class="form-select">
        <option v-for="situation in situations" :key="situation.id" :value="situation.name">
          {{ situation.name }}
        </option>
      </select>
    </div>

    <!-- 选择模型 -->
    <div class="form-group mb-3">
      <label for="model">选择方法:</label>
      <select v-model="form.model" id="model" class="form-select">
        <option v-for="model in filteredModels" :key="model.id" :value="model.name">
          {{ model.name }}
        </option>
      </select>
    </div>

    <div class="form-group mb-3">
      <label for="model">选择计算节点:</label>
      <select id="gamenode" class="form-select" @change="selectGameNode">
        <option v-for="(gameNode, index) in gameNodes" :key="gameNode.ip + gameNode.port"
         :value="gameNode.ip+':'+ gameNode.port">
            <!-- {{ gameNode.ip }} - {{ gameNode.port }} -->
          {{ '节点 ' + (index + 1) }}
        </option>
      </select>
    </div>

    <!-- 选择 PyTorch 版本 -->
    <div class="form-group mb-3">
      <label for="pytorchVersion">选择 PyTorch 版本:</label>
      <select v-model="form.pytorchVersion" id="pytorchVersion" class="form-select">
        <option value="1.6">1.6</option>
        <option value="1.8">1.8</option>
        <option value="1.9">1.9</option>
        <option value="1.10">1.10</option>
      </select>
    </div>

    <!-- 训练轮次 -->
    <div class="form-group mb-3">
      <label for="trainIterations">训练轮次:</label>
      <input
        v-model="form.trainIterations"
        id="trainIterations"
        type="text"
        class="form-control"
        placeholder="5000"
      />
    </div>

    <!-- 学习率 -->
    <div class="form-group mb-3">
      <label for="learningRate">学习率:</label>
      <input
        v-model="form.learningRate"
        id="learningRate"
        type="text"
        class="form-control"
        placeholder="0.001"
      />
    </div>

    <!-- batch size -->
    <div class="form-group mb-3">
      <label for="batchSize">batch size:</label>
      <input
        v-model="form.batchSize"
        id="batchSize"
        type="text"
        class="form-control"
        placeholder="32"
      />
    </div>

    <!-- 选择评估指标 -->
    <div class="form-group mb-3">
      <label for="evaluationMetrics">选择评估指标:</label>
      <div>
        <label v-for="metric in evaluationMetrics" :key="metric" class="form-check-label me-3">
          <input
            type="checkbox"
            class="form-check-input"
            v-model="form.selectedMetrics"
            :value="metric"
          />
          {{ metric }}
        </label>
      </div>
    </div>

    <!-- 提交按钮 -->
    <button @click="saveConfig" class="btn btn-primary me-2">保存配置</button>
    <button @click="startTraining" class="btn btn-primary">开始训练</button>

    <!-- 显示训练进度 -->
    <div v-if="trainingStatus" class="mt-3">
      <p>{{ trainingStatus }}</p>
    </div>

    <!-- 输出保存的配置信息 -->
    <div v-if="showConfig" class="alert alert-success mt-3">
      <h5>保存的配置信息:</h5>
      <pre>{{ form }}</pre>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useStore } from 'vuex';
import $ from 'jquery';

const store = useStore();
const models = ref([]);
const gameNodes = ref([]);
const situations = ref([]);
const showConfig = ref(false);
const trainingStatus = ref(null); // 用来存储训练状态

// 从 Vuex 获取表单数据
const form = reactive({ ...store.state.train.form });
const evaluationMetrics = ref(['精度', '速度', '稳定性', '资源消耗'])

const filteredModels = computed(() => {
  return models.value.filter(models => models.situationselect == form.scene);
});

// 从后端获取模型列表
const fetchModels = () => {
  $.ajax({
    url: "http://localhost:3000/model/getlist/",
    type: "get",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    success(resp) {
      models.value = resp;
    }
  });
  $.ajax({
    url: "http://localhost:3000/situation/getlist/",
    type: "get",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    success(resp) {
      situations.value = resp;
    }
  });
  $.ajax({
    url: "http://localhost:3000/games/get/all/",
    type: "get",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    success(resp) {
      gameNodes.value = resp;
      // console.log(resp)
    }
  });
};
const selectGameNode = (event) => {
  const selectedValue = event.target.value; // 获取 "ip:port" 格式的字符串
  const [ip, port] = selectedValue.split(':'); // 拆分出 ip 和 port
  console.log('选择的 IP:', ip);
  console.log('选择的 Port:', port);
  form.ip = ip;
  form.port = port;
};
// 保存配置
const saveConfig = () => {
  // 保存配置到 Vuex
  store.dispatch('setForm', form);
  showConfig.value = true;

  // 将配置转换为 JSON 字符串
  const configJSON = JSON.stringify(form, null, 2); // 使用 2 个空格缩进，使 JSON 更易读

  // 创建一个 Blob 对象
  const blob = new Blob([configJSON], { type: 'application/json' });

  // 创建一个下载链接
  const url = URL.createObjectURL(blob);

  // 创建一个隐藏的 <a> 标签用于触发下载
  const a = document.createElement('a');
  a.href = url;
  a.download = `${form.trainingName}_config.json`; // 使用训练名称作为文件名
  document.body.appendChild(a);

  // 触发下载
  a.click();

  // 清理 URL 对象
  URL.revokeObjectURL(url);

  // 移除 <a> 标签
  document.body.removeChild(a);
};

const startTraining = () => {
  // 在点击开始训练时，先显示"正在为您分配计算资源"
  trainingStatus.value = "正在为您分配计算资源...";

  // 延时1.5秒后更新为“已为您分配 GPU 和 GPU”
  setTimeout(() => {
    trainingStatus.value = "已为您分配 GPU 和 GPU, 通过gRPC连接到服务器...";
  }, 1500);

  // 进行后端请求，开始训练
  $.ajax({
    url: "http://localhost:3000/train/add/",
    type: "post",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    data: {
      trainingName: form.trainingName,
      scene: form.scene,
      model: form.model,
      pytorchVersion: form.pytorchVersion,
      ip: form.ip,
      port: form.port,
      modelParams: JSON.stringify(form.modelParams),
    },
    success(resp) {
      console.log(resp);
      fetchModels();
    },
    error(resp) {
      console.log(resp);
    }
  });
};

// 页面加载时获取模型列表
onMounted(fetchModels);
</script>

<style scoped>
/* 可选自定义样式 */
</style>

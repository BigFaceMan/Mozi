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
        <option v-for="situation in situations" :key="situation.id" :value="situation.projectname">
          {{ situation.projectname }}
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

<!-- 选择奖励目标（按钮弹窗） -->
    <!-- 选择奖励目标 -->
  <!-- 选择奖励目标的模态框 -->
  <div v-if="showRewardModal" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <h4 class="modal-title">配置奖励目标</h4>
      <div class="modal-body">
        <div v-if="entities.length > 0" class="reward-list">
          <div v-for="entity in entities" :key="entity.id" class="reward-item">
            <label class="reward-label">
              <input type="checkbox" v-model="selectedRewards" :value="entity.id" class="form-check-input" />
              {{ entity.name }}
            </label>
            <input v-model="rewardWeights[entity.id]" type="number" class="form-control weight-input" placeholder="权重" min="1" />
          </div>
        </div>
        <p v-else class="text-muted text-center">请先选择场景</p>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" @click="closeModal">取消</button>
        <button class="btn btn-primary" @click="saveRewards">确定</button>
      </div>
    </div>
  </div>

    <div class="form-group mb-3">
      <label for="model">选择计算节点:</label>
      <select id="gamenode" class="form-select" @change="selectGameNode">
        <option v-for="(gameNode, index) in gameNodes" :key="gameNode.ip + gameNode.port"
         :value="gameNode.ip+':'+ gameNode.port">
			{{ '节点 ' + (index + 1) + (index === 0 ? ' ⭐最优节点⭐' : '') }}
            <!-- {{ gameNode.ip }} - {{ gameNode.port }} -->
        </option>
      </select>
    </div>
    <div class="form-group mb-3">
      <label for="enginenode">选择仿真引擎节点:</label>
      <div class="custom-select-container">
        <button type="button" class="form-select" @click="toggleDropdown">
          {{ selectedNodes.length ? selectedNodes.map(node => node.nodeName).join(', ') : '请选择节点' }}
        </button>
        <div v-if="dropdownVisible" class="dropdown-list">
          <ul>
            <li v-for="(engineNode, index) in engineNodes" :key="engineNode.nodeName">
              <label>
                <input type="checkbox" :value="engineNode" v-model="selectedNodes" />
                  {{ '节点 ' + (index + 1) }}
              </label>
            </li>
          </ul>
        </div>
      </div>
    </div>
	<!-- 选择训练设备 -->
	<div class="form-group mb-3">
	<label for="trainDevice">选择训练设备:</label>
	<select v-model="form.trainDevice" id="trainDevice" class="form-select">
		<option value="cpu">CPU</option>
		<option value="gpu">GPU</option>
	</select>
	</div>

  <!-- 选择 PyTorch 版本 -->
    <div class="form-group mb-3">
      <label for="pytorchVersion">选择 PyTorch 版本:</label>
      <select v-model="form.pytorchVersion" id="pytorchVersion" class="form-select">
        <option v-for="version in pytorchVersions" :key="version" :value="version">
          {{ version }}
        </option>
      </select>
    </div>

    <!-- <div class="form-group mb-3">
      <label for="pytorchVersion">选择 PyTorch 版本:</label>
      <select v-model="form.pytorchVersion" id="pytorchVersion" class="form-select">
        <option value="1.6">1.6</option>
        <option value="1.8">1.8</option>
        <option value="1.9">1.9</option>
        <option value="1.10">1.10</option>
      </select>
    </div> -->

    <!-- 训练轮次 -->
    <div class="form-group mb-3">
      <label for="trainIterations">训练时间:</label>
      <input
        v-model="form.trainTime"
        id="trainIterations"
        type="text"
        class="form-control"
        placeholder="1600"
      />
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
		<label v-for="metric in evaluationMetrics" :key="metric.value" class="form-check-label me-3">
		<input
			type="checkbox"
			class="form-check-input"
			v-model="form.selectedMetrics"
			:value="metric.value"
		/>
		{{ metric.label }}
		</label>
	</div>
	</div>


  <div class="d-flex align-items-center gap-3 mt-3">
    <button class="btn btn-secondary" @click="showRewardModal = true">配置奖励目标</button>
    <button @click="saveConfig" class="btn btn-primary">保存配置</button>
    <button @click="startTraining" class="btn btn-primary">启动训练</button>
  </div>
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
import { ref, reactive, onMounted, computed, watch} from 'vue';
import { useStore } from 'vuex';
import $ from 'jquery';

const store = useStore();
const models = ref([]);
const gameNodes = ref([]);
const engineNodes = ref([]);
const situations = ref([]);
const showConfig = ref(false);
const trainingStatus = ref(null); // 用来存储训练状态
const showRewardModal = ref(false);
const selectedNodes = ref([]);
const dropdownVisible = ref(false);

// 从 Vuex 获取表单数据
const form = reactive({ ...store.state.train.form });
// const evaluationMetrics = ref(['精度', '速度', '稳定性', '资源消耗'])
const evaluationMetrics = ref([ { label: '精度', value: 'accuracy' },
  { label: '速度', value: 'speed' },
  { label: '稳定性', value: 'stability' },
  { label: '资源消耗', value: 'resource_consumption' }
]);

const filteredModels = computed(() => {
  return models.value.filter(models => models.situationselect == form.scene);
});
const pytorchVersions = computed(() => {
  const model = filteredModels.value.find(item => item.name === form.model);
  console.log('当前模型:', model);
  if (model && model.environment) {
	const currentVersionS = model.environment.replace('torch', '');
    const currentVersion = parseFloat(model.environment.replace('torch1.', ''));
	console.log('当前版本String:', currentVersionS);
	console.log('当前版本float:', currentVersion);
    const versions = [];
    for (let i = currentVersion - 2; i <= currentVersion + 2; i++) {
		console.log('当前版本:', i);
    //   if (i >= 1.2 && i <= 1.10) { // 只显示在允许的版本范围内
        versions.push(`torch1.${i}`);
    //   }
    }
    return versions;
  }
  return [];
});

const toggleDropdown = () => {
  dropdownVisible.value = !dropdownVisible.value
};
// 从后端获取模型列表
const fetchModels = () => {
  $.ajax({
    url: "http://127.0.0.1:3000/model/getlist/",
    type: "get",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    success(resp) {
      models.value = resp;
      console.log("所有的方法：")
      console.log(models.value)
    }
  });
  $.ajax({
    url: "http://127.0.0.1:3000/remote/getRExamples/",
    type: "post",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    success(resp) {
      situations.value = resp.data;
    }
  });
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

      form.ip = gameNodes.value[0].ip;
      form.port = gameNodes.value[0].port;
      // console.log(resp)
    }
  });
  $.ajax({
    url: "http://127.0.0.1:3000/engine/getAll",
    type: "get",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    success(resp) {
      engineNodes.value = resp.freeList;
    },
    error(err) {
      console.error("获取引擎节点失败:", err);
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
    trainingStatus.value = "已为您分配 CPU 和 GPU, 通过gRPC连接到服务器...";
  }, 1500);

  // 进行后端请求，开始训练

  $.ajax({
    url: "http://127.0.0.1:3000/train/add/",
    type: "post",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    data: {
      trainingName: form.trainingName,
      scene: form.scene,
      model: form.model,
      trainTime: form.trainTime,
      pytorchVersion: form.pytorchVersion,
      trainIters: form.trainIterations, 
      learningRate: form.learningRate,
      batchSize: form.batchSize,
      selectedMetrics: JSON.stringify(form.selectedMetrics),
      trainDevice: form.trainDevice,
      ip: form.ip,
      port: form.port,
      modelParams: JSON.stringify(form.modelParams),
      needEngines: selectedNodes.value.length,
    },
    success(resp) {
      console.log(resp);
      trainingStatus.value = resp.msg;
      fetchModels();
    },
    error(resp) {
      console.log(resp);
    }
  });
};

const entities = ref([]); // 存储当前场景的实体
const selectedRewards = ref([]); // 选中的奖励目标（实体 ID）
const rewardWeights = reactive({}); // 记录每个实体的权重

const fetchEntities = (exampleId) => {
  if (!exampleId) return;

  $.ajax({
    url: `http://127.0.0.1:3000/remote/getEntitys/?exampleId=${exampleId}`,
    type: "post",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    success(resp) {
      if (resp.success) {
        entities.value = resp.data;
        // 初始化权重
        entities.value.forEach(entity => {
          rewardWeights[entity.id] = 1; // 默认权重设为 1
        });
      }
    },
    error(err) {
      console.error("获取实体失败:", err);
    }
  });
};
const closeModal = () => {
  showRewardModal.value = false
} 
const saveRewards = () => {
  showRewardModal.value = false
}

// 监听场景变化，获取实体
watch(() => form.scene, (newScene) => {
  const example = situations.value.find(s => s.projectname === newScene);
  if (example) {
    fetchEntities(example.exampleid); // 通过 exampleId 获取实体信息
  }
});

// // 更新 Vuex 数据
// const saveConfig = () => {
//   form.selectedRewards = selectedRewards.value.map(id => ({
//     id,
//     weight: rewardWeights[id] || 1, // 如果权重未设置，默认使用 1
//   }));

//   // 其他 saveConfig 逻辑...
// };


// 页面加载时获取模型列表
onMounted(fetchModels);
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
  width: 400px;
  max-width: 90%;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: bold;
  margin-bottom: 10px;
}

.modal-body {
  max-height: 300px;
  overflow-y: auto;
}

.reward-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reward-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8f9fa;
  padding: 8px;
  border-radius: 5px;
}

.reward-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1rem;
}

.weight-input {
  width: 70px;
  text-align: center;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
  gap: 10px;
}

.custom-select-container {
  position: relative;
  width: 100%;
}

.form-select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: #fff;
  font-size: 14px;
  appearance: none; /* 移除默认箭头 */
  -webkit-appearance: none;
  -moz-appearance: none;
}

.form-select:focus {
  border-color: #007bff;
  outline: none;
}

/* 处理自定义选择框的下拉 */
.form-select option {
  padding: 10px;
}
/* 可选自定义样式 */
</style>

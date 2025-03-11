<template>
  <div class="container mt-4">
    <h2 class="mb-4">参数配置</h2>

    <!-- 输入训练名称 -->
    <div class="form-group mb-3">
      <label for="trainingName">对战名称:</label>
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
      <label for="pytorchVersion">选择场景</label>
      <select v-model="form.scene" id="scene" class="form-select">
        <option v-for="situation in situations" :key="situation.id" :value="situation.projectname">
          {{ situation.projectname }}
        </option>
      </select>
    </div>

    <!-- 选择模型 -->
    <div class="form-group mb-3">
      <label for="model">选择模型:</label>
      <select v-model="form.model" id="model" class="form-select">
        <option v-for="training in filteredTrainings" :key="training.id" :value="training.trainingname">
          {{ training.trainingname }}
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

    <!-- 提交按钮 -->
    <button @click="saveConfig" class="btn btn-primary me-2">保存配置</button>
    <button @click="startTraining" class="btn btn-primary">开始对战</button>

    <!-- 输出保存的配置信息 -->
    <div v-if="showConfig" class="alert alert-success mt-3">
      <h5>保存的配置信息:</h5>
      <pre>{{ form }}</pre>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed} from 'vue';
import { useStore } from 'vuex';
import $ from 'jquery';

const store = useStore();
const situations = ref([]);
const trainings = ref([]);
const showConfig = ref(false);
const gameNodes = ref([]);

// 从 Vuex 获取表单数据
const form = reactive({ ...store.state.pk.form });

// 从后端获取模型列表
const fetchModels = () => {
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
    url: "http://127.0.0.1:3000/train/getlist/",
    type: "get",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    success(resp) {
      console.log(resp)
      trainings.value = resp;
    }
  });

  $.ajax({
    url: "http://127.0.0.1:3000/games/get/all/",
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
  store.dispatch('setForm', form);
  showConfig.value = true;
};


const filteredTrainings = computed(() => {
  return trainings.value.filter(training => training.scene == form.scene);
});
const startTraining = () => {
    $.ajax({
        url: "http://127.0.0.1:3000/infer/add/",
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            inferName: form.trainingName,
            scene: form.scene,
            model: form.model,
            ip: form.ip,
            port: form.port
        },
        success(resp) {
            console.log(resp)
            fetchModels()
        },
        error(resp) {
            console.log(resp)
        }
    });
};
// 页面加载时获取模型列表
onMounted(fetchModels);
</script>

<style scoped>
/* 可选自定义样式 */
</style>

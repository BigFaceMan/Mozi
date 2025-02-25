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
      <label for="pytorchVersion">选择想定</label>
      <select v-model="form.scene" id="scene" class="form-select">
        <option v-for="situation in situations" :key="situation.id" :value="situation.name">
          {{ situation.name }}
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

// 从 Vuex 获取表单数据
const form = reactive({ ...store.state.pk.form });

// 从后端获取模型列表
const fetchModels = () => {
  $.ajax({
    url: "http://127.0.0.1:3000/situation/getlist/",
    type: "get",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    success(resp) {
      situations.value = resp;
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

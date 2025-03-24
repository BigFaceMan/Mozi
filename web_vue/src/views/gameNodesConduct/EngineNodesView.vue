<template>
  <div class="container py-5">
    <h2 class="text-center mb-4 title">🚀 计算引擎节点</h2>
    
    <div class="row justify-content-center">
      <div class="col-12">
        <h3 class="text-center text-success">🟢 空闲节点</h3>
      </div>
      <div class="col-lg-4 col-md-6 col-sm-12" v-for="(node, index) in engineNodes" :key="'free-' + index">
        <div class="card engine-card shadow-lg">
          <div class="card-body text-center">
            <h5 class="card-title">🖥️ 节点 {{ index + 1 }}</h5>
            <ul class="list-group list-group-flush text-start">
              <li class="list-group-item"><strong>📌 引擎名称:</strong> {{ node.nodeName }}</li>
              <li class="list-group-item"><strong>🌐 IP 地址:</strong> {{ node.ip }}</li>
              <li class="list-group-item"><strong>🔌 端口:</strong> {{ node.port }}</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <div class="row justify-content-center mt-5">
      <div class="col-12">
        <h3 class="text-center text-danger">🔴 占用节点</h3>
      </div>
      <div class="col-lg-4 col-md-6 col-sm-12" v-for="(node, index) in engineUsingNodes" :key="'used-' + index">
        <div class="card engine-card shadow-lg used">
          <div class="card-body text-center">
            <h5 class="card-title">🖥️ 节点 {{ index + 1 }}</h5>
            <ul class="list-group list-group-flush text-start">
              <li class="list-group-item"><strong>📌 引擎名称:</strong> {{ node.nodeName }}</li>
              <li class="list-group-item"><strong>🌐 IP 地址:</strong> {{ node.ip }}</li>
              <li class="list-group-item"><strong>🔌 端口:</strong> {{ node.port }}</li>
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

const store = useStore();
const engineNodes = ref([]);
const engineUsingNodes = ref([]);

const fetchEngineNodes = () => {
  $.ajax({
    url: "http://127.0.0.1:3000/engine/getAll",
    type: "get",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    success(resp) {
      engineNodes.value = resp.freeList;
      console.log("空闲节点 : ", engineNodes.value)
      engineUsingNodes.value = resp.usingList;
    },
    error(err) {
      console.error("获取引擎节点失败:", err);
    }
  });
};

onMounted(fetchEngineNodes);
</script>

<style scoped>
.container {
  max-width: 1200px;
  animation: fadeIn 0.8s ease-in-out;
}

.title {
  font-size: 2rem;
  font-weight: bold;
  color: #007bff;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.engine-card {
  border-radius: 16px;
  background: linear-gradient(135deg, #ffffff, #f8f9fa);
  transition: transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out;
  margin-bottom: 20px;
  overflow: hidden;
}

.engine-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.card-body {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 25px;
}

.card-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #007bff;
  margin-bottom: 15px;
}

.list-group-item {
  font-size: 1.1rem;
  background-color: #ffffff;
  border: none;
  padding: 12px 20px;
  color: #333;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.used {
  background: linear-gradient(135deg, #ffe6e6, #ffcccc);
}
</style>
<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between mb-4">
      <div class="input-group" style="width: 350px;">
        <input type="text" class="form-control" placeholder="查找日志内容..." v-model="searchQuery" @input="filterLogs">
        <button class="btn btn-outline-secondary" type="button" @click="resetSearch">重置</button>
      </div>
    </div>
    <div class="d-flex justify-content-between mb-4">
      <div class="input-group" style="width: 350px;">
        <label for="startDate" class="input-group-text">开始日期</label>
        <input type="date" id="startDate" class="form-control" v-model="startDate" @input="filterLogs" placeholder="开始日期">
      </div>
      <div class="input-group" style="width: 350px;">
        <label for="endDate" class="input-group-text">结束日期</label>
        <input type="date" id="endDate" class="form-control" v-model="endDate" @input="filterLogs" placeholder="结束日期">
      </div>
    </div>

    <div class="btn-group mb-3">
      <button class="btn btn-info" @click="selectLogType('operationLogs')">操作日志</button>
      <button class="btn btn-success" @click="selectLogType('trainingLogs')">训练日志</button>
      <button class="btn btn-warning" @click="selectLogType('battleLogs')">对战日志</button>
      <button class="btn btn-danger" @click="selectLogType('dangerLogs')">异常预警日志</button>
    </div>
    <div class="card shadow-sm">
      <div class="card-body">
        <h5 class="card-title">日志列表</h5>
        <table class="table table-striped table-hover">
          <thead class="table-light">
            <tr>
              <th scope="col">日志日期</th>
              <th scope="col" class="text-center">内容</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in paginatedLogs" :key="log.id">
              <td>{{ log.timestamp }}</td>
              <td class="text-center">{{ log.log }}</td>
            </tr>
          </tbody>
        </table>
        <nav aria-label="Page navigation">
          <ul class="pagination justify-content-center">
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <button class="page-link" @click="goToPage(currentPage - 1)">上一页</button>
            </li>
            <li class="page-item disabled">
              <span class="page-link">当前是第 {{ currentPage }} / {{ totalPages }} 页</span>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <button class="page-link" @click="goToPage(currentPage + 1)">下一页</button>
            </li>
            <li class="page-item">
              <input type="number" v-model="jumpPage" class="form-control" style="width: 70px;" @change="jumpToPage" min="1" :max="totalPages" placeholder="页码">
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import $ from 'jquery';

const store = useStore();
const logs = ref({
  operationLogs: [],
  trainingLogs: [],
  battleLogs: [],
  dangerLogs: []
});
const searchQuery = ref('');
const startDate = ref('');
const endDate = ref('');
const selectedLogType = ref('operationLogs');
const currentPage = ref(1);
const logsPerPage = ref(20);
const jumpPage = ref(currentPage.value);

const fetchLogs = () => {
  $.ajax({
    url: "http://127.0.0.1:3000/logs/user/",
    type: "post",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    data: { username: store.state.user.username },
    success(resp) {
      logs.value.operationLogs = resp;
    },
    error(err) {
      console.log(err);
    }
  });

  $.ajax({
    url: "http://127.0.0.1:3000/logs/train/",
    type: "post",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    data: { username: store.state.user.username },
    success(resp) {
      logs.value.trainingLogs = resp;
    },
    error(err) {
      console.log(err);
    }
  });

  $.ajax({
    url: "http://127.0.0.1:3000/logs/battle/",
    type: "post",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    data: { username: store.state.user.username },
    success(resp) {
      logs.value.battleLogs = resp;
    },
    error(err) {
      console.log(err);
    }
  });

  $.ajax({
    url: "http://127.0.0.1:3000/logs/exception/",
    type: "post",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    data: { username: store.state.user.username },
    success(resp) {
      logs.value.dangerLogs = resp;
    },
    error(err) {
      console.log(err);
    }
  });
};

const groupedLogs = computed(() => {
  return {
    operationLogs: logs.value.operationLogs || [],
    trainingLogs: logs.value.trainingLogs || [],
    battleLogs: logs.value.battleLogs || [],
    dangerLogs: logs.value.dangerLogs || []
  };
});

const filteredLogs = computed(() => {
  let logList = groupedLogs.value[selectedLogType.value] || [];
  
  // 按内容搜索
  if (searchQuery.value) {
    logList = logList.filter(log => 
      log.log.toLowerCase().includes(searchQuery.value.toLowerCase())
    );
  }

  // 按日期范围过滤
  if (startDate.value) {
    logList = logList.filter(log => new Date(log.timestamp) >= new Date(startDate.value));
  }
  if (endDate.value) {
    logList = logList.filter(log => new Date(log.timestamp) <= new Date(endDate.value));
  }

  return logList;
});

const paginatedLogs = computed(() => {
  const startIndex = (currentPage.value - 1) * logsPerPage.value;
  return filteredLogs.value.slice(startIndex, startIndex + logsPerPage.value);
});

const totalPages = computed(() => {
  return Math.ceil(filteredLogs.value.length / logsPerPage.value);
});

onMounted(() => {
  fetchLogs();
});

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  jumpPage.value = currentPage.value; // Update jumpPage when page is changed
};

const selectLogType = (logType) => {
  selectedLogType.value = logType;
  currentPage.value = 1;
};

const filterLogs = () => {
  currentPage.value = 1; // Reset to the first page when filtering
};

const resetSearch = () => {
  searchQuery.value = '';
  startDate.value = '';
  endDate.value = '';
};

const jumpToPage = () => {
  if (jumpPage.value < 1 || jumpPage.value > totalPages.value) {
    jumpPage.value = currentPage.value;
    return;
  }
  currentPage.value = jumpPage.value;
};
</script>

<style scoped>
.container {
  /* max-width: 800px; */
  margin: auto;
}
</style>

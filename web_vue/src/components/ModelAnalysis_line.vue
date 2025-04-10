<template>
  <ContentField>
    <div class="manual-container">
      <h2 class="manual-title">📘 用户手册</h2>
      <button @click="toggleModal">切换图表</button>
      <!-- 模态框 -->
      <div v-if="showModal" class="modal-overlay" @click="closeModal">
        <div class="modal-content" @click.stop>
          <button @click="closeModal" class="close-btn">关闭</button>
          <LineChart :key="chartKey" :training="training" />
        </div>
      </div>
    </div>
  </ContentField>
</template>

<script setup>
import { ref } from 'vue';
import LineChart from './LineChart.vue';

const showModal = ref(false);
const chartKey = ref(0);

// 切换模态框的显示状态
const toggleModal = () => {
  showModal.value = !showModal.value;
  chartKey.value += 1; // 每次切换时，强制重新渲染图表
};

// 关闭模态框
const closeModal = () => {
  showModal.value = false;
};

// 假设图表数据
const training = {
    id: 297,
};
</script>

<style scoped>
.manual-container {
  text-align: center;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* 半透明背景 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.1);
  max-width: 90%; /* 使模态框宽度更大，最大宽度为视口的 90% */
  width: 800px;  /* 设置固定宽度，增加宽度 */
  max-height: 90vh; /* 限制模态框的最大高度为视口高度的 90% */
  overflow-y: auto; /* 使内容区域可滚动 */
}


.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: red;
  color: white;
  border: none;
  padding: 5px 10px;
  cursor: pointer;
}

.close-btn:hover {
  background-color: darkred;
}
</style>

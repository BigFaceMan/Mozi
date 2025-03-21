<template>
  <div class="modal-overlay">
    <div class="card">
      <div class="card-header">
        <h5>场景详情</h5>
        <button class="close-btn-header" @click="closeDetail">×</button> <!-- 右上角关闭按钮 -->
      </div>
      <div class="card-body">
        <div class="details">
          <div class="detail-item" v-for="(value, key) in displayFields" :key="key">
            <div class="label">{{ key }}:</div>
            <div class="value">{{ value }}</div>
          </div>
        </div>

        <h6 class="section-title">场景实体信息</h6>
        <div v-if="SceneEntitys.length">
          <ul class="entity-list">
            <li v-for="(entity, index) in SceneEntitys" :key="index">
              <strong>分组名称:</strong> {{ entity.groupName }} <br />
              <strong>实体名称:</strong> {{ entity.entityName }}
            </li>
          </ul>
        </div>
        <p v-else class="no-data">没有相关的场景实体信息。</p>

        <h6 class="section-title">评估指标体系</h6>
        <div v-if="SceneIndicator.length">
          <ul class="indicator-list">
            <li v-for="(indicator, index) in SceneIndicator" :key="index">
              <strong>{{ indicator.name }}:</strong> {{ indicator.weight }}
            </li>
          </ul>
        </div>
        <p v-else class="no-data">没有相关的评估指标体系。</p>

        <button class="close-btn" @click="closeDetail">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, defineProps, defineEmits, ref, computed } from 'vue';
import $ from "jquery"; // 继续使用 jQuery

// 接收数据
const props = defineProps({
  rexample: { type: Object, required: true }
});
const SceneEntitys = ref([]);
const SceneIndicator = ref([]);

// 映射展示字段
const displayFields = computed(() => ({
  ID: props.rexample.id,
  场景名: props.rexample.projectname,
  想定名: props.rexample.situationname,
  方案名: props.rexample.solutionname,
  实例名: props.rexample.examplename,
  创建时间: props.rexample.createtime
}));

const emit = defineEmits(['close']);
const closeDetail = () => emit('close');

// 获取评估指标数据
const getExampleIndicator = (exampleId) => {
  $.ajax({
    url: "http://127.0.0.1:3000/remote/getExampleIndicator/",
    type: "POST",
    data: { exampleId: exampleId },
    success(resp) {
      SceneIndicator.value = resp.data || [];
      console.log("SceneIndicator : ", SceneIndicator.value);
    },
    error(resp) {
      console.error("提交失败:", resp);
    }
  });
};

// 获取实体数据
const getSceneEntitys = (projectId) => {
  $.getJSON(`http://127.0.0.1:3000/remote/getSceneEntitys/?projectId=${projectId}`)
    .done((resp) => {
      SceneEntitys.value = resp.data || [];
      console.log("example id is : ", props.rexample.exampleid);
      getExampleIndicator(props.rexample.exampleid);
    })
    .fail((err) => {
      console.error("获取 SceneEntitys 失败:", err);
    });
};

onMounted(() => getSceneEntitys(props.rexample.id));
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.4); /* 背景半透明 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
  overflow-y: auto; /* 允许垂直滚动 */
}

.card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  width: 650px; /* 增大宽度 */
  margin: 20px;
  max-height: 80vh; /* 最大高度为视口的80% */
}

.card-header {
  background: linear-gradient(135deg, #007bff, #0056b3);
  color: #fff;
  text-align: center;
  padding: 20px;
  font-size: 1.5rem; /* 增大字体 */
  position: relative; /* 使右上角的按钮能够定位 */
}

.card-body {
  padding: 20px;
  background: #f9f9f9;
  overflow-y: auto; /* 启用内容区域的滚动 */
  max-height: 70vh; /* 限制高度，允许滚动 */
}

.details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px; /* 增加间距 */
  align-items: center;
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.label {
  font-size: 1.1rem; /* 增大字体 */
  font-weight: bold;
  color: #333;
}

.value {
  font-size: 1rem; /* 增大字体 */
  color: #666;
}

.section-title {
  font-size: 1.3rem;
  font-weight: bold;
  color: #0056b3;
  margin-top: 20px;
}

.entity-list {
  list-style: none;
  padding: 0;
  margin-top: 20px;
}

.entity-list li {
  background: #fff;
  padding: 12px; /* 增加内边距 */
  border-radius: 6px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.indicator-list {
  list-style: none;
  padding: 0;
  margin-top: 20px;
}

.indicator-list li {
  background: #fff;
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.no-data {
  text-align: center;
  color: #888;
}

.close-btn {
  width: 100%;
  background: #007bff;
  color: white;
  padding: 12px;
  font-size: 1.2rem; /* 增大字体 */
  border: none;
  border-radius: 8px;
  cursor: pointer;
  margin-top: 20px;
  transition: background 0.3s ease;
}

.close-btn:hover {
  background: #0056b3;
}

/* 右上角关闭按钮的样式 */
.close-btn-header {
  position: absolute;
  top: 10px;
  right: 10px;
  background: transparent;
  color: white;
  font-size: 1.5rem;
  border: none;
  cursor: pointer;
}

.close-btn-header:hover {
  color: #ff0000;
}
</style>

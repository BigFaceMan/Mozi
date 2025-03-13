<template>
  <ContentField>
    <div class="container mt-4">
      <div class="modal-content">
        <div class="row mt-5">
          <div class="col-md-4">
            <div class="card">
              <div class="card-header bg-primary text-white">
                <h5 class="mb-0">选择评估指标</h5>
              </div>
              <div class="table-responsive">
                <table class="table table-hover">
                  <thead>
                    <tr>
                      <th>评估指标名</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="assess in assessTree" :key="assess.id">
                      <td style="cursor: pointer;">{{ assess.name }}</td>
                      <td class="d-flex justify-content-center align-items-center gap-2">
                        <button class="btn stylish-btn btn-success" @click="selectAssess = assess">
                          <i class="fas fa-check-circle"></i> 选择
                        </button>
                        <button class="btn stylish-btn btn-info" @click="chooseAssess(assess)">
                          <i class="fas fa-angle-double-right"></i> 展开
                        </button>
                        <button class="btn stylish-btn btn-warning" @click="goBackAssess(assess)">
                          <i class="fas fa-arrow-left"></i> 返回
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
                <div class="d-flex justify-content-center mt-3" style="margin-bottom: 5%">
                  <button class="btn btn-secondary" :disabled="currentPageCountry === 1" @click="currentPageCountry--">‹ 上一页</button>
                  <span class="mx-3">第 {{ currentPageCountry }} / {{ totalPagesCountry }} 页</span>
                  <button class="btn btn-secondary" :disabled="currentPageCountry === totalPagesCountry" @click="currentPageCountry++">下一页 ›</button>
                </div>
              </div>
            </div>
          </div>

          <div class="text-center mt-4">
            <button class="btn btn-lg btn-success shadow rounded-pill mx-3 px-4 py-2" :disabled="!selectAssess"
              @click="submitSelection()">
              ✅ 确定选择
            </button>
          </div>
        </div>
      </div>
    </div>
  </ContentField>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import ContentField from './ContentField.vue';
// import $ from 'jquery';

const assessTree = ref([]);  // 存储评估指标树
const selectAssess = ref(null);  // 存储选中的评估指标

// 假数据
const fetchAssessRoot = () => {
  assessTree.value = [
    {
      id: 2233,
      name: '2023评测指标',
      pId: 0,
      isLeaf: 0,
      children: [
        {
          id: 2234,
          name: '子指标1',
          pId: 2233,
          isLeaf: 1,
          children: [],
        },
        {
          id: 2235,
          name: '子指标2',
          pId: 2233,
          isLeaf: 1,
          children: [],
        },
      ],
    },
    {
      id: 2236,
      name: '2024评测指标',
      pId: 0,
      isLeaf: 1,
      children: [],
    },
  ];
};

// 展开评估指标子树
const chooseAssess = (assess) => {
  if (!assess.isLeaf) {
    fetchAssessCTree(assess.id);
  }
};

// 返回上一层
const goBackAssess = (assess) => {
  if (assess.pId === 0) {
    fetchAssessRoot();
  } else {
    // 如果有父节点，获取父节点的子树数据
    fetchAssessCTree(assess.pId);
  }
};

// 获取子树数据
const fetchAssessCTree = (parentId) => {
  // 这里模拟数据，可以根据父节点id请求后端获取具体的子树数据
  if (parentId === 2233) {
    assessTree.value = [
      {
        id: 2234,
        name: '子指标1',
        pId: 2233,
        isLeaf: 1,
        children: [],
      },
      {
        id: 2235,
        name: '子指标2',
        pId: 2233,
        isLeaf: 1,
        children: [],
      },
    ];
  }
};

// 提交选择
const submitSelection = () => {
  if (selectAssess.value) {
    console.log('选中的评估指标:', selectAssess.value);
  }
};

onMounted(() => {
  fetchAssessRoot();
});
</script>

<style scoped>
</style>

<template>
    <ContentField>
        <div class="container mt-4">
            <!-- 第一部分：实例管理 -->
            <div class="d-flex justify-content-between align-items-center">
                <h4>场景管理</h4>
                <button class="btn btn-primary" @click="showCreateModal = true">➕ 新增场景</button>
            </div>
            
            <!-- 搜索框 -->
        <!-- 搜索框 -->
            <div class="d-flex mt-3">
                <input type="text" class="form-control w-25" v-model="searchQuery" placeholder="搜索场景..." @input="searchRExamples">
            </div>

            
            <div class="table-responsive mt-3">
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>场景名</th>
                            <th>实例名</th>
                            <th>创建时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="rexample in pagedRExamples" :key="rexample.id">
                            <td>{{ rexample.exampleid }}</td>
                            <td>{{ rexample.projectname }}</td>
                            <td>{{ rexample.examplename }}</td>
                            <td>{{ rexample.createtime }}</td>
                            <td>
                                <button class="btn btn-danger btn-sm" @click="deleteRExample(rexample.id)">🗑 删除</button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <!-- 分页 -->
            <div class="d-flex justify-content-center mt-3">
                <button class="btn btn-secondary" :disabled="currentPage === 1" @click="currentPage--">‹ 上一页</button>
                <span class="mx-3">第 {{ currentPage }} 页</span>
                <button class="btn btn-secondary" :disabled="currentPage === totalPages" @click="currentPage++">下一页 ›</button>
            </div>

        <div v-if="showCreateModal" class="modal-overlay">
            <div class="modal-content">
                <!-- 右上角关闭按钮 -->
                <button class="close-btn" @click="showCreateModal = false">✖</button>

                <!-- 美观的项目名称输入框 -->
                <div class="form-group mt-3 text-center">
                    <label for="projectName" class="font-weight-bold text-primary">场景名称：</label>
                    <input type="text" id="projectName" v-model="projectName" 
                        class="form-control stylish-input mx-auto" 
                        placeholder="请输入场景名称">
                </div>

                <div class="row mt-5">
                    <!-- 第一列：想定列表 -->
                    <div class="col-md-4">
                        <div class="card">
                            <div class="card-header bg-primary text-white">
                                <h5 class="mb-0">选择想定</h5>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>想定名</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr v-for="situation in pagedSituations" :key="situation.id"
                                            :class="{ 'table-active': selectedSituationId === situation.id }"
                                            @click="selectSituation(situation.id)">
                                            <td>{{ situation.id }}</td>
                                            <td>{{ situation.taskName }}</td>
                                        </tr>
                                    </tbody>
                                </table>
                                <div class="d-flex justify-content-center mt-3" style="margin-bottom: 5%">
                                    <button class="btn btn-secondary" :disabled="currentPageSituation === 1" @click="currentPageSituation--">‹ 上一页</button>
                                    <span class="mx-3">第 {{ currentPageSituation }} 页</span>
                                    <button class="btn btn-secondary" :disabled="currentPageSituation === totalPagesSituation" @click="currentPageSituation++">下一页 ›</button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 第二列：方案列表 -->
                    <div class="col-md-4" v-if="selectedSituationId">
                        <div class="card">
                            <div class="card-header bg-success text-white">
                                <h5 class="mb-0">选择方案</h5>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>方案名</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr v-for="solution in pagedSolution" :key="solution.id"
                                            :class="{ 'table-active': selectedSolutionId === solution.id }"
                                            @click="selectSolution(solution.id)">
                                            <td>{{ solution.id }}</td>
                                            <td>{{ solution.missionName }}</td>
                                        </tr>
                                    </tbody>
                                </table>
                                <div class="d-flex justify-content-center mt-3" style="margin-bottom: 5%">
                                    <button class="btn btn-secondary" :disabled="currentPageSolution === 1" @click="currentPageSolution--">‹ 上一页</button>
                                    <span class="mx-3">第 {{ currentPageSolution }} 页</span>
                                    <button class="btn btn-secondary" :disabled="currentPageSolution === totalPagesSolution" @click="currentPageSolution++">下一页 ›</button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 第三列：实例列表 -->
                    <div class="col-md-4" v-if="selectedSolutionId">
                        <div class="card">
                            <div class="card-header bg-warning text-dark">
                                <h5 class="mb-0">选择实例</h5>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>实例名</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr v-for="example in pagedExamples" :key="example.id"
                                            :class="{ 'table-active': selectedExampleId === example.id }"
                                            @click="selectedExample(example)">
                                            <td>{{ example.id }}</td>
                                            <td>{{ example.name }}</td>
                                        </tr>
                                    </tbody>
                                </table>

                                <div class="d-flex justify-content-center mt-3" style="margin-bottom: 5%">
                                    <button class="btn btn-secondary" :disabled="currentPageExample === 1" @click="currentPageExample--">‹ 上一页</button>
                                    <span class="mx-3">第 {{ currentPageExample }} 页</span>
                                    <button class="btn btn-secondary" :disabled="currentPageExample === totalPagesExample" @click="currentPageExample++">下一页 ›</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 确认按钮 -->
                <div class="text-center mt-4" v-if="selectedExampleId">
                    <button class="btn btn-lg btn-primary" @click="submitSelection">✅ 确定选择</button>
                </div>
            </div>
        </div>

        </div>
    </ContentField>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import ContentField from '../../components/ContentField.vue';
import $ from "jquery";
import store from "../../store";

// 数据存储
const examples = ref([]);
const RExamples = ref([]);
const situations = ref([]);
const solutions = ref([]);
const projectName = ref(null);

const selectedSituationId = ref(null);
const selectedSolutionId = ref(null);
const selectedExampleId = ref(null);
const selectedExampleName = ref(null);
const showCreateModal = ref(false);
const searchQuery = ref(""); // 搜索框的输入


//  RExample
// 当前第几页
const currentPage = ref(1);
// 每页多少个数据
const pageSize = 5; // 每页显示10条数据
// 当前页所有的数据
const pagedRExamples = computed(() => {
    const start = (currentPage.value - 1) * pageSize;
    const end = currentPage.value * pageSize;
    return RExamples.value.slice(start, end);
});
// 总共多少页
const totalPages = computed(() => {
    return Math.ceil(RExamples.value.length / pageSize);
});

// Situation
const currentPageSituation = ref(1);
const pageSizeSituation = 5; // 每页显示10条数据
const pagedSituations = computed(() => {
    const start = (currentPageSituation.value - 1) * pageSizeSituation;
    const end = currentPageSituation.value * pageSizeSituation;
    return situations.value.slice(start, end);
});
const totalPagesSituation = computed(() => {
    return Math.ceil(situations.value.length / pageSizeSituation);
});

// Solution
const currentPageSolution = ref(1);
const pageSizeSolution = 5; // 每页显示10条数据
const pagedSolution = computed(() => {
    const start = (currentPageSolution.value - 1) * pageSizeSolution;
    const end = currentPageSolution.value * pageSizeSolution;
    return solutions.value.slice(start, end);
});
const totalPagesSolution = computed(() => {
    return Math.ceil(solutions.value.length / pageSizeSolution);
});


//  Example
// 当前第几页
const currentPageExample = ref(1);
// 每页多少个数据
const pageSizeExample = 5; // 每页显示10条数据
// 当前页所有的数据
const pagedExamples = computed(() => {
    const start = (currentPageExample.value - 1) * pageSizeExample;
    const end = currentPageExample.value * pageSizeExample;
    return examples.value.slice(start, end);
});
// 总共多少页
const totalPagesExample = computed(() => {
    return Math.ceil(examples.value.length / pageSizeExample);
});

// 获取 RExample 列表
const fetchRExamples = () => {
    $.ajax({
        url: "http://localhost:3000/remote/getRExamples/",
        type: "POST",
        success(resp) {
            RExamples.value = resp.data;
            console.log(resp);
        },
        error(resp) {
            console.error("获取 RExamples 失败:", resp);
        }
    });
};

// 删除 RExample
const deleteRExample = (exampleId) => {
    if (!confirm("确定要删除此实例吗？")) return;
    
    $.ajax({
        url: "http://localhost:3000/remote/deleteRExample/",
        type: "POST",
        headers: { Authorization: "Bearer " + store.state.user.token },
        data: { exampleId },
        success(resp) {
            if (resp.code === 200) {
                alert("删除成功！");
                fetchRExamples();
            } else {
                alert("删除失败: " + resp.message);
            }
        },
        error(resp) {
            console.error("删除失败:", resp);
            alert("删除失败 ❌");
        }
    });
};

// 获取想定列表
const fetchSituations = () => {
    $.ajax({
        url: "http://localhost:3000/remote/getSituations/",
        type: "post",
        success(resp) {
            if (resp.code === 200) {
                situations.value = resp.data;
                console.log("get situations : ", situations.value)
            } else {
                console.log("get situations erro : ", resp)
            }
        },
        error(resp) {
            console.error("获取想定失败:", resp);
        }
    });
};

// 选择想定，获取方案
const selectSituation = (situationId) => {
    selectedSituationId.value = situationId;
    selectedSolutionId.value = null; // 清空方案
    selectedExampleId.value = null; // 清空实例
    solutions.value = [];
    examples.value = [];

    $.ajax({
        url: `http://localhost:3000/remote/getSolutions/?id=${situationId}`,
        type: "post",
        success(resp) {
            if (resp.code === 200) {
                solutions.value = resp.data;
            }
        },
        error(resp) {
            console.error("获取方案失败:", resp);
        }
    });
};

// 选择方案，获取实例
const selectSolution = (solutionId) => {
    selectedSolutionId.value = solutionId;
    selectedExampleId.value = null; // 清空实例
    examples.value = [];

    $.ajax({
        url: `http://localhost:3000/remote/getExamples/?id=${solutionId}`,
        type: "post",
        success(resp) {
            if (resp.code === 200) {
                examples.value = resp.data;
            }
        },
        error(resp) {
            console.error("获取实例失败:", resp);
        }
    });
};

const selectedExample = (example) => {
    selectedExampleId.value = example.id;
    selectedExampleName.value = example.name;
};

// 提交最终选择的想定、方案、实例
const submitSelection = () => {
    if (!selectedSituationId.value || !selectedSolutionId.value || !selectedExampleId.value) {
        alert("请完整选择想定、方案和实例");
        showCreateModal.value = false;
        return;
    }

    $.ajax({
        url: "http://localhost:3000/remote/saveRExample/",
        type: "POST",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            situationId: selectedSituationId.value,
            solutionId: selectedSolutionId.value,
            exampleId: selectedExampleId.value,
            exampleName: selectedExampleName.value,
            projectName: projectName.value
        },
        success(resp) {
            console.log("提交成功:", resp);
            alert("提交成功！ 🎉");
            showCreateModal.value = false;
            fetchRExamples()
        },
        error(resp) {
            console.error("提交失败:", resp);
            alert("提交失败 ❌");
            showCreateModal.value = false;
        }
    });
};

// 搜索过滤 RExample
const searchRExamples = () => {
    currentPage.value = 1; // 重置为第一页
    // 根据搜索框输入进行过滤
    const query = searchQuery.value.trim().toLowerCase();
    if (query) {
        RExamples.value = RExamples.value.filter(rexample =>
            rexample.projectname.toLowerCase().includes(query)
        );
    } else {
        fetchRExamples(); // 如果搜索框为空，则重新加载全部实例
    }
};


// 页面加载时获取想定列表和实例列表
onMounted(() => {
    fetchSituations();
    fetchRExamples();
});
</script>

<style scoped>
/* 模态框背景 */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5); /* 半透明背景 */
    display: flex;
    align-items: center;
    justify-content: center;
    overflow-y: auto; /* 允许在模态框内滚动 */
}

/* .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1050;
} */

/* 模态框内容 */
.modal-content {
    background: white;
    border-radius: 10px;
    padding: 20px;
    width: 80%;
    max-width: 1000px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    position: relative;
}

/* 右上角关闭按钮 */
.close-btn {
    position: absolute;
    top: 10px;
    right: 10px;
    background: transparent;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: #333;
    transition: 0.3s;
}

.close-btn:hover {
    color: #ff0000;
}

/* 卡片样式 */
.card {
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    border-radius: 10px;
}

/* 表格行悬停效果 */
.table-hover tbody tr {
    cursor: pointer;
    transition: 0.2s;
}

.table-hover tbody tr:hover {
    background-color: #f8f9fa;
}

/* 选中的表格行 */
.table-active {
    background-color: #007bff !important;
    color: white !important;
    font-weight: bold;
}
.stylish-input {
    width: 60%; /* 让输入框更宽 */
    padding: 10px;
    font-size: 16px;
    border-radius: 8px; /* 圆角 */
    border: 1px solid #ccc;
    box-shadow: 0px 2px 5px rgba(0, 0, 0, 0.1); /* 轻微阴影 */
    transition: all 0.3s ease-in-out;
}

.stylish-input:focus {
    border-color: #007bff; /* 聚焦时边框变蓝 */
    box-shadow: 0px 3px 7px rgba(0, 123, 255, 0.3);
    outline: none;
}

</style>

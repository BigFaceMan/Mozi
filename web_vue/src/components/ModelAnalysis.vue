<template>
    <div class="container mt-4">
        <!-- Search and Filters -->
        <div class="d-flex justify-content-between mb-3">
            <div class="input-group" style="width: 300px;">
                <input type="text" class="form-control" placeholder="查找模型..." v-model="searchQuery" @input="filterTrainings">
                <button class="btn btn-outline-secondary" type="button" @click="resetSearch">重置</button>
            </div>

        </div>
        <!-- Training Records Table -->
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">模型列表</h5>
                <table class="table table-hover">
                    <thead>
                    <tr>
                        <th scope="col">模型名</th>
                        <th scope="col">场景</th>
                        <th scope="col">方法</th>
                        <th scope="col">强化学习环境</th>
                        <th scope="col">训练状态</th>
                        <th scope="col">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="group in groupedTrainings" :key="group.key">
                        <!-- 主行（显示 latestTraining） -->
                        <td>
                            {{ group.latestTraining.trainingname.split("_")[0] }}
                        </td>
                        <td>{{ group.latestTraining.scene }}</td>
                        <td>{{ group.latestTraining.model }}</td>
                        <td>{{ group.latestTraining.pytorchversion }}</td>
                        <td>
                        <span v-if="group.latestTraining.running === 0">训练完成</span>
                        <span v-else-if="group.latestTraining.running === 2">已暂停</span>
                        <span v-else-if="group.latestTraining.running === 3">外部导入模型</span>
                        </td>
                        <td>
                            <button class="btn btn-sm ms-1 mb-1" style="background-color:teal;" v-if="group.latestTraining.running == '3' && group.latestTraining.upload == '2'" @click="addGoodModel(group.latestTraining)">已上传</button>
                            <button class="btn btn-sm ms-1 mb-1" style="background-color: coral;" v-if="group.latestTraining.running == '3'" @click="generateReport(group.latestTraining)">生成报告</button>
                            <button class="btn btn-sm ms-1 mb-1" style="background-color:darkseagreen;" v-if="group.latestTraining.running == '3'" @click="modelTest(group.latestTraining)">模型测试</button>
                            <button class="btn btn-sm ms-1 mb-1" style="background-color: cornflowerblue;" v-if="group.latestTraining.running == '3'" @click="downloadModel(group.latestTraining)">下载</button>
                            <button class="btn btn-sm btn-danger ms-1 mb-1" v-if="group.latestTraining.running == '3'" @click="deleteTraining(group.latestTraining)">删除模型</button>
                            <button class="btn btn-sm btn-info" v-if="group.latestTraining.running == '0'" @click="visualizeReport(group.latestTraining)">训练日志</button>
                            <button class="btn btn-sm btn-warning ms-1 mb-1" v-if="group.latestTraining.running == '0'" @click="openTrainingDetails(group)"> 历史模型 </button>
                            <button class="btn btn-sm btn-secondary ms-1 mb-1" v-if="group.latestTraining.running == '0'" @click="viewResourceUsage(group.latestTraining)">资源使用报告</button>
                            <button class="btn btn-sm btn-success ms-1 mb-1" v-if="group.latestTraining.running == '0'" @click="viewSuggestions(group.latestTraining)">智能建议</button>
                            <button class="btn btn-sm btn-warning ms-1 mb-1" v-if="group.latestTraining.running == '0'" @click="viewTrainingReplay(group.latestTraining)">训练回放</button>
                            <button class="btn btn-sm ms-1 mb-1" style="background-color:teal;" v-if="group.latestTraining.running == '0' && group.latestTraining.upload == '0'" @click="addGoodModel(group.latestTraining)">上传模型</button>
                            <button class="btn btn-sm ms-1 mb-1" style="background-color:teal;" v-if="group.latestTraining.running == '0' && group.latestTraining.upload == '1'">正在上传</button>
                            <button class="btn btn-sm ms-1 mb-1" style="background-color:teal;" v-if="group.latestTraining.running == '0' && group.latestTraining.upload == '2'" @click="addGoodModel(group.latestTraining)">已上传</button>
                            <button class="btn btn-sm ms-1 mb-1" style="background-color:teal;" v-if="group.latestTraining.running == '0' && group.latestTraining.upload == '3' && !group.latestTraining.isValidating" @click="validModel(group.latestTraining)">待验证</button>
                            <button class="btn btn-sm ms-1 mb-1" style="background-color:teal;" v-if="group.latestTraining.isValidating" >正在验证</button>
                            <button class="btn btn-sm ms-1 mb-1" style="background-color: coral;" v-if="group.latestTraining.running == '0'" @click="generateReport(group.latestTraining)">生成报告</button>
                            <button class="btn btn-sm ms-1 mb-1" style="background-color:darkseagreen;" v-if="group.latestTraining.running == '0'" @click="modelTest(group.latestTraining)">模型测试</button>
                            <button class="btn btn-sm ms-1 mb-1" style="background-color: cornflowerblue;" v-if="group.latestTraining.running == '0'" @click="downloadModel(group.latestTraining)">下载</button>
                            <button class="btn btn-sm btn-success ms-1 mb-1" v-if="group.latestTraining.running == '0'" @click="continueTraining(group.latestTraining)">继续训练</button>
                            <button class="btn btn-sm btn-danger ms-1 mb-1" v-if="group.latestTraining.running == '0'" @click="deleteTraining(group.latestTraining)">删除模型</button>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <!-- Pagination Controls -->
                <nav>
                    <ul class="pagination justify-content-center">
                        <li class="page-item" :class="{ disabled: currentPage.value === 1 }">
                            <button class="page-link" @click="goToPage(currentPage - 1)">上一页</button>
                        </li>
                        <span class="page-link">{{ currentPage }} / {{ totalPages }} 页</span>
                        <li class="page-item" :class="{ disabled: currentPage.value === totalPages}">
                            <button class="page-link" @click="goToPage(currentPage + 1)">下一页</button>
                        </li>
                    </ul>
                </nav>
        <!-- Start Comparison Button -->
            </div>
        </div>

      <!-- <button @click="toggleModal">切换图表</button> -->
      <!-- 模态框 -->
        <div v-if="showChart" class="modal-overlay" @click="colseTrainingReplay">
            <div class="modal-content" @click.stop>
            <button @click="colseTrainingReplay" class="close-btn">关闭</button>
            <LineChart :key="chartKey" :training="chartTrain" />
            </div>
        </div>


        <div
        class="modal fade"
        id="groupTrainingsModal"
        tabindex="-1"
        aria-labelledby="groupTrainingsModalLabel"
        aria-hidden="true"
        >
        <div class="modal-dialog modal-xl">
        <div class="modal-content shadow-lg rounded-3">
            <div class="modal-header bg-primary text-white">
            <h5 class="modal-title" id="groupTrainingsModalLabel">模型历史记录</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
            <!-- 顶部操作按钮区 -->
            <div class="d-flex justify-content-end mb-3 gap-2">
                <button
                v-if="!isBatch"
                class="btn btn-outline-primary"
                @click="openBatch"
                >
                <i class="bi bi-bar-chart-steps me-1"></i> 批量操作
                </button>

                <button
                v-if="isBatch"
                class="btn btn-danger"
                @click="deleteSelectedModels"
                >
                <i class="bi bi-play-circle me-1"></i> 批量删除
                </button>

                <button
                v-if="isBatch"
                class="btn btn-outline-secondary"
                @click="toggleComparison"
                >
                <i class="bi bi-x-circle me-1"></i> 取消
                </button>

                <button
                v-if="!isComparing"
                class="btn btn-outline-primary"
                @click="openComparison"
                >
                <i class="bi bi-bar-chart-steps me-1"></i> 模型对比
                </button>

                <button
                v-if="isComparing"
                class="btn btn-success"
                @click="startComparison"
                >
                <i class="bi bi-play-circle me-1"></i> 开始对比
                </button>

                <button
                v-if="isComparing"
                class="btn btn-outline-secondary"
                @click="toggleComparison"
                >
                <i class="bi bi-x-circle me-1"></i> 取消对比
                </button>
            </div>

            <!-- 模型列表表格 -->
            <div class="table-responsive">
                <table class="table table-bordered table-hover align-middle text-center">
                <thead class="table-light">
                    <tr>
                    <th v-if="isComparing">选择</th>
                    <th>训练名称</th>
                    <th>场景</th>
                    <th>模型</th>
                    <th>PyTorch 版本</th>
                    <th>状态</th>
                    <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="training in selectedGroup" :key="training.id">
                    <td v-if="isComparing || isBatch">
                        <input
                        type="checkbox"
                        v-model="selectedModels"
                        :value="training"
                        :disabled="selectedModels.length >= 2 && !selectedModels.includes(training)"
                        />
                    </td>
                    <td>{{ training.trainingname }}</td>
                    <td>{{ training.scene }}</td>
                    <td>{{ training.model }}</td>
                    <td>{{ training.pytorchversion }}</td>
                    <td>
                        <span class="badge bg-success" v-if="training.running === 0">训练完成</span>
                        <span class="badge bg-warning text-dark" v-else-if="training.running === 2">已暂停</span>
                        <span class="badge bg-info text-dark" v-else-if="training.running === 3">外部导入模型</span>
                    </td>
                    <td>
                        <button
                        v-if="training.mversion === 1"
                        class="btn btn-sm btn-outline-secondary"
                        disabled
                        >
                        当前版本
                        </button>
                        <button
                        v-else
                        class="btn btn-sm btn-outline-success"
                        @click="rollBack(training)"
                        >
                        回滚到当前版本
                        </button>
                    </td>
                    </tr>
                </tbody>
                </table>
            </div>
            </div>
        </div>
        </div>

        </div>
        <div class="modal fade" id="speedModal" tabindex="-1" aria-labelledby="speedModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="speedModalLabel">🚀 调整训练速度</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body text-center">
                        <input type="range" class="form-range" min="1" max="150" step="1" v-model="tempSpeedMultiplier">
                        <p class="text-center current-speed">当前加速倍数：{{ tempSpeedMultiplier }}x</p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">❌ 取消</button>
                        <button type="button" class="btn btn-primary" @click="confirmSpeed()">✅ 确定</button>
                    </div>
                </div>
            </div>
        </div>
        <!-- Visualization Modal -->
        <div v-if="isVisualizationVisible" class="modal fade show" tabindex="-1" aria-labelledby="visualizationModalLabel" aria-hidden="true" style="display: block;">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">训练日志</h5>
                        <button type="button" class="btn-close" @click="closeVisualization" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <!-- Training Logs -->
                        <div class="training-logs" style="height: 400px; overflow-y: auto; font-family: monospace; background-color: #f8f9fa; padding: 10px; border: 1px solid #dee2e6;">
                            <p>[INFO] Starting training process...</p>
                            <p>[INFO] Model initialized with 2,301,827 parameters.</p>
                            <p>[INFO] Using Adam optimizer with learning rate = 0.001.</p>
                            <p>[INFO] Loading training dataset: 50,000 samples, batch size = 32.</p>
                            <p>[INFO] Epoch 1/1000: Training started.</p>
                            <p>[INFO] Epoch 1, Batch 50/1562: Loss = 0.876, Accuracy = 72.5%</p>
                            <p>[INFO] Epoch 1, Batch 100/1562: Loss = 0.654, Accuracy = 80.1%</p>
                            <p>[INFO] Epoch 1 complete: Loss = 0.512, Accuracy = 85.3%.</p>
                            <p>[INFO] Validation started: 10,000 samples.</p>
                            <p>[INFO] Validation complete: Loss = 0.432, Accuracy = 87.6%.</p>
                            <p>[INFO] Epoch 2/1000: Training started.</p>
                            <p>[INFO] Epoch 2, Batch 50/1562: Loss = 0.412, Accuracy = 88.2%</p>
                            <p>[WARNING] Learning rate scheduler updated: new learning rate = 0.0008.</p>
                            <p>[INFO] Epoch 2 complete: Loss = 0.378, Accuracy = 89.9%.</p>
                            <p>[INFO] Validation started: 10,000 samples.</p>
                            <p>[INFO] Validation complete: Loss = 0.324, Accuracy = 90.8%.</p>
                            <p>[INFO] Epoch 3/1000: Training started.</p>
                            <p>[INFO] Epoch 3, Batch 50/1562: Loss = 0.328, Accuracy = 91.2%</p>
                            <p>[INFO] Epoch 3 complete: Loss = 0.300, Accuracy = 92.4%.</p>
                            <p>[INFO] Validation complete: Loss = 0.280, Accuracy = 93.1%.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>


        <div v-if="isModelTestVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="modelTestModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">模型测试报告</h5>
                        <button type="button" class="btn-close" @click="closeModelTestVisble" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <!-- Display model test data -->
                        <p><strong>测试准确率:</strong> 92.4%</p>
                        <p><strong>测试损失:</strong> 0.237</p>
                        <p><strong>测试集大小:</strong> 10,000 样本</p>
                        <p><strong>预测速度:</strong> 500 次/秒</p>
                    </div>
                </div>
            </div>
        </div>



        <!-- Resource Usage Modal -->

        <div v-if="isResourceUsageVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="resourceUsageModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">资源使用报告</h5>
                        <button type="button" class="btn-close" @click="closeResourceUsage" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <!-- Display resource usage data -->
                        <p><strong>CPU使用率:</strong> {{ resourceUsageData.cpu }}%</p>
                        <p><strong>GPU使用率:</strong> {{ resourceUsageData.gpu }}%</p>
                        <p><strong>内存使用量:</strong> {{ resourceUsageData.memory }} MB</p>
                        <p><strong>显存使用量:</strong> {{ resourceUsageData.gpuMemory }} MB</p>
                        <p><strong>网络使用率:</strong> {{ resourceUsageData.network }} Mbps</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Suggestions Modal -->
        <div v-if="isSuggestionsVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="suggestionsModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">智能建议</h5>
                        <button type="button" class="btn-close" @click="closeSuggestions" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <!-- Display AI suggestions here -->
                        <p>{{ suggestionsData }}</p>
                    </div>
                </div>
            </div>
        </div>

        <div v-if="isComparisonVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="comparisonModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg modal-dialog-centered"> <!-- 使用modal-dialog-centered使模态框居中 -->
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">模型对比结果</h5>
                        <button type="button" class="btn-close" @click="closeComparison" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <p><strong>对比的模型：</strong></p>
                        <p>模型 1：{{ selectedModels[0].trainingname }}</p>
                        <p>模型 2：{{ selectedModels[1].trainingname }}</p>

                        <!-- 对比结果区域 -->
                        <div v-if="ComparisonResults">
                            <h6>对比结果</h6>
                            <p><strong>精度：</strong></p>
                            <ul>
                                <li>模型 1：{{ ComparisonResults.model1.accuracy }}%</li>
                                <li>模型 2：{{ ComparisonResults.model2.accuracy }}%</li>
                            </ul>
                            <p><strong>损失：</strong></p>
                            <ul>
                                <li>模型 1：{{ ComparisonResults.model1.loss }}</li>
                                <li>模型 2：{{ ComparisonResults.model2.loss }}</li>
                            </ul>
                            <p><strong>训练时长：</strong></p>
                            <ul>
                                <li>模型 1：{{ ComparisonResults.model1.trainingTime }} 小时</li>
                                <li>模型 2：{{ ComparisonResults.model2.trainingTime }} 小时</li>
                            </ul>

                            <div v-if="ComparisonResults.betterModel">
                                <h6>结论：</h6>
                                <p><strong>更好的模型：</strong>{{ ComparisonResults.betterModel }}</p>
                            </div>
                        </div>

                        <button class="btn btn-danger mt-3" @click="closeComparison">完成</button>
                    </div>
                </div>
            </div>
        </div>


        <!-- Training Replay Modal -->
        <!-- <div v-if="isTrainingReplayVisible" class="modal fade show" tabindex="-1" aria-labelledby="trainingReplayModalLabel" aria-hidden="true" style="display: block;">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">训练回放</h5>
                        <button type="button" class="btn-close" @click="closeTrainingReplay" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="card-body">
                            <h5 class="title">训练可视化</h5>
                            <iframe src="http://127.0.0.1:6006" width="100%" height="800px" frameborder="0"></iframe>
                        </div>
                    </div>
                </div>
            </div>
        </div> -->
        <div v-if="isTrainingReplayVisible" class="modal fade show" tabindex="-1" aria-labelledby="trainingReplayModalLabel" aria-hidden="true" style="display: block;">
            <div class="modal-dialog modal-lg" style="max-width: 90%; height: 80vh; margin: auto; margin-top: 5%;">
                <div class="modal-content" style="height: 100%; border: none;">
                    <div class="modal-header">
                        <h5 class="modal-title">训练回放</h5>
                        <button type="button" class="btn-close" @click="closeTrainingReplay" aria-label="Close"></button>
                    </div>
                    <div class="modal-body" style="padding: 0;">
                        <!-- 显示加载中或者iframe -->
                        <div v-if="isLoading" class="loading-container">
                            <div class="spinner"></div>
                            <p class="loading-text">正在加载，请稍后...</p>
                        </div>
                        <iframe v-else :src="tensorboardUrl" style="width: 100%; height: 100%;" frameborder="0"></iframe>
                        <!-- <iframe v-else src="http://127.0.0.1:6001" style="width: 100%; height: 100%;" frameborder="0"></iframe> -->
                    </div>
                </div>
            </div>
        </div>


    </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import LineChart from './LineChart.vue';
import $ from 'jquery';
import { Chart } from 'chart.js';
import { useStore } from 'vuex';
import { Modal } from "bootstrap";

const store = useStore();
const tempSpeedMultiplier = ref(1);
const trainings = ref([]);
const filteredTrainings = ref([]);
const paginatedTrainings = ref([]);
const searchQuery = ref('');
const currentPage = ref(1);
const itemsPerPage = 10;
const tensorboardTraining = ref(null);
const tensorboardPort = ref(6001);
const currentAccTrain = ref([])
const selectedGroup = ref(null);

const totalPages = computed(() => Math.ceil(trainings.value.length / itemsPerPage));
const tensorboardUrl = computed(() => `http://${tensorboardTraining.value.ip}:${tensorboardPort.value}`);


const isComparing = ref(false);
const isBatch = ref(false);
const selectedModels = ref([]);
const isComparisonVisible = ref(false);
const ComparisonResults = ref([]);
const expandedGroups = ref({}); // 控制分组展开/折叠

// 计算按 scene + model 分组的数据
const groupedTrainings = computed(() => {
    // if (!trainings.value || trainings.value.length === 0) {
    //     return []; // 避免 undefined
    // }
    const groups = {};
    trainings.value.forEach(training => {
        const key = `${training.scene}-${training.model}`;
        if (!groups[key]) {
            groups[key] = [];
            expandedGroups.value[key] = false;
        }
        groups[key].push(training);
    });

    return Object.entries(groups).map(([key, trainings]) => {
        const latestTraining = trainings.find(t => t.mversion==1) || trainings[0];
        return { key, trainings, latestTraining };
    });
});


const openTrainingDetails = (group) => {
    selectedGroup.value = group.trainings;
    console.log("selectedGroup : ", selectedGroup.value.trainings)
    const modal = new Modal(document.getElementById('groupTrainingsModal'));
    modal.show();
}
const rollBack = (training) => {
    console.log("rollBack : ", training);
    $.ajax({
        url: "http://127.0.0.1:3000/model/rollback/",
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            trainId: training.id,
            scene: training.scene,
            model: training.model,
        },
        success(resp) {
            if (resp.code == 200) {
                console.log("回滚成功", resp);
                fetchTrainings();
            } else {
                console.error("回滚失败", resp);
            }
        },
        error(err) {
            console.error("Error fetching trainings:", err);
        }
    });

    // 确保正确获取模态框元素
    const modalElement = document.getElementById('groupTrainingsModal');
    if (!modalElement) {
        console.error("模态框元素未找到");
        return;
    }
    alert("回滚成功")

    // 获取 Modal 实例并隐藏模态框
    const modal = Modal.getInstance(modalElement) || Modal(modalElement);
    modal.hide();
};




// 更新最新模型
// const updateLatest = (group, newLatest) => {
//     group.trainings.forEach(training => {
//         training.latest = training.id === newLatest.id;
//     });
//     group.latestTraining = newLatest;
// };


const openBatch = () => {
    isBatch.value = true;
}
const openComparison = () => {
    isComparing.value = true;
}
const toggleComparison = () => {
    isComparing.value = false;
    isBatch.value = false;
    selectedModels.value = [];
};


const closeComparison = () => {
    isComparisonVisible.value = false;
    isComparing.value = false;
    selectedModels.value = [];
    ComparisonResults.value = [];
};


const startComparison = () => {
    if (selectedModels.value.length < 2) {
        alert('请选择两个模型进行对比');
        return;
    }
    isComparisonVisible.value = true;

    ComparisonResults.value = {
        model1: {
            accuracy: 95.3, // 模型1的精度
            loss: 0.12,     // 模型1的损失
            trainingTime: 2.5 // 模型1的训练时长
        },
        model2: {
            accuracy: 93.7, // 模型2的精度
            loss: 0.15,     // 模型2的损失
            trainingTime: 3.0 // 模型2的训练时长
        },
        betterModel: "模型 1" // 更好的模型名称
    }
};

const fetchTrainings = () => {
    $.ajax({
        url: "http://127.0.0.1:3000/train/getlist/",
        type: "get",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        success(resp) {
            trainings.value = resp.filter(training => (training.running === 0 || training.running === 3));
            // 为每个训练项添加 isValidata 属性
            trainings.value.forEach(training => {
                // 默认为 false，可以根据需要修改
                training.isValidating = false;
            });
            console.log(trainings.value)
            filteredTrainings.value = resp.filter(training => (training.running === 0 || training.running === 3));
            filteredTrainings.value.forEach(training => {
                // 默认为 false，可以根据需要修改
                training.isValidating = false;
            });
            console.log("totalpages : ", totalPages.value)
            updatePaginatedTrainings();
        },
        error(err) {
            console.error("Error fetching trainings:", err);
        }
    });
};

const filterTrainings = () => {
    const query = searchQuery.value.trim().toLowerCase();
    filteredTrainings.value = trainings.value.filter(training =>
        training.trainingname.toLowerCase().includes(query)
    );
    currentPage.value = 1;
    updatePaginatedTrainings();
};

const updatePaginatedTrainings = () => {
    const start = (currentPage.value - 1) * itemsPerPage;
    const end = start + itemsPerPage;
    paginatedTrainings.value = filteredTrainings.value.slice(start, end);
};

const resetSearch = () => {
    searchQuery.value = '';
    filteredTrainings.value = trainings.value;
    currentPage.value = 1;
    updatePaginatedTrainings();
};

const goToPage = (page) => {
        console.log("page : ", page.value)
    if (page > 0 && page <= totalPages.value) {
        console.log("page : ", page)
        console.log("totalpage : ", totalPages.value)
        currentPage.value = page;
        updatePaginatedTrainings();
    }
};

const isVisualizationVisible = ref(false);
const isResourceUsageVisible = ref(false);
const isSuggestionsVisible = ref(false);
const isTrainingReplayVisible = ref(false);
const resourceUsageData = ref({});
const suggestionsData = ref('');
const lossChart = ref(null);
const isModelTestVisible = ref(false);

const deleteSelectedModels = () => {
  if (confirm(`确定要删除选中的 ${selectedModels.value.length} 个模型吗？`)) {
    const deletePromises = selectedModels.value.map(training =>
      $.ajax({
        url: "http://127.0.0.1:3000/train/remove/",
        type: "post",
        headers: {
          Authorization: "Bearer " + store.state.user.token,
        },
        data: {
          id: training.id,
        }
      })
    );

    Promise.all(deletePromises)
      .then(() => {
        alert("删除成功！");
        selectedModels.value.length = 0;  // 清空选择
        fetchTrainings();
      })
      .catch(err => {
        console.error("批量删除出错:", err);
        alert("部分或全部模型删除失败，请检查控制台");
      });
  }
};


const visualizeReport = (training) => {
    isVisualizationVisible.value = true;

    nextTick(() => {
        const canvasElement = lossChart.value;
        if (!canvasElement) {
          console.error('Canvas element not found');
          return;
        }

        $.ajax({
            url: "http://127.0.0.1:3000/trainlog/getlist/",
            type: "get",
            headers: {
                Authorization: "Bearer " + store.state.user.token,
            },
            data: {
                trainingname: training.trainingname,
            },
            success(resp) {
                const lossData = resp.map(log => log.loss);
                const timestamps = resp.map(log => new Date(log.timestamp).toLocaleString());

                const ctx = canvasElement.getContext('2d');
                lossChart.value = new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: timestamps,
                        datasets: [{
                            label: '损失值',
                            data: lossData,
                            borderColor: 'rgba(75, 192, 192, 1)',
                            backgroundColor: 'rgba(75, 192, 192, 0.2)',
                            fill: true,
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        scales: {
                            x: { title: { display: true, text: '时间' } },
                            y: { title: { display: true, text: '损失' } }
                        }
                    }
                });
            },
            error(err) {
                console.error("Error fetching training log:", err);
            }
        });
    });
};

const addGoodModel = (training) => {
    if (training.upload != '0') {
        return 
    }
    console.log("upload : ", training)
    $.ajax({
        url: "http://127.0.0.1:3000/train/upload/",  // Use the appropriate endpoint for replay data
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            trainingName: training.trainingname,
            uid: training.uid
        },
        success(resp) {
            // Process the raw training log data for replay visualization
            console.log(resp)
            fetchTrainings();
        },
        error(err) {
            console.error("Error fetching replay data:", err);
        }
    });

};

const modelTest = () => {
    isModelTestVisible.value = true;
    // console.log("modelConst")
}
const deleteTraining = (training) => {
    if (confirm('确定要删除这个模型吗？')) {
        $.ajax({
            url: "http://127.0.0.1:3000/train/remove/",  // Use the appropriate endpoint for replay data
            type: "post",
            headers: {
                Authorization: "Bearer " + store.state.user.token,
            },
            data: {
                id: training.id,
            },
            success(resp) {
                // Process the raw training log data for replay visualization
                console.log(resp)
                fetchTrainings();
            },
            error(err) {
                console.error("Error fetching replay data:", err);
            }
        });
    }
};

const closeVisualization = () => {
    isVisualizationVisible.value = false;
    // if (lossChart.value) {
    //     lossChart.value.destroy();
    //     lossChart.value = null;
    // }
};
const closeModelTestVisble = () => {
    isModelTestVisible.value = false;   
}

// Resource usage handling
const viewResourceUsage = (training) => {

    isResourceUsageVisible.value = true;

    $.ajax({
        url: "http://127.0.0.1:3000/trainlog/getlist/",
        type: "get",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            trainingname: training.trainingname,
        },
        success(resp) {
            console.log(resp)
            const avgCPU = (Math.random() * 100).toFixed(2);  // 随机生成 0-100 之间的数
            const avgGPU = (Math.random() * 100).toFixed(2);
            const avgMemory = (Math.random() * 100).toFixed(2);
            const avgGPUMemory = (Math.random() * 100).toFixed(2);
            const avgNetwork = (Math.random() * 100).toFixed(2);
            
            console.log("Random avg CPU:", avgCPU);
            console.log("Random avg memory:", avgMemory);

            resourceUsageData.value = {
                cpu: avgCPU,
                gpu: avgGPU,
                memory: avgMemory,
                gpuMemory: avgGPUMemory,
                network: avgNetwork
            };
        },
        error(err) {
            console.error("Error fetching resource usage:", err);
        }
    });
};

const closeResourceUsage = () => {
    isResourceUsageVisible.value = false;
};

const isLoading = ref(false);
const validModel = (training) => {
    training.isValidating = true;
    $.ajax({
        url: "http://127.0.0.1:3000/train/validataModel/",  // Use the appropriate endpoint for replay data
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            trainingName: training.trainingname,
            uid: training.uid
        },
        success(resp) {
            console.log(resp)
            training.isValidating = false;
            fetchTrainings();
        },
        error(err) {
            console.error("Error fetching replay data:", err);
        }
    });
}
const showChart = ref(false);
const chartKey = ref(0);
const chartTrain = ref(null)
const viewTrainingReplay = (training) => {
    chartTrain.value = training;
    showChart.value = !showChart.value;
    chartKey.value += 1; // 每次切换时，强制重新渲染图表
};
const colseTrainingReplay = () => {
    showChart.value = false;
};


const continueTraining = (training) => {
    $.ajax({
        url: "http://127.0.0.1:3000/train/continue/",  // Use the appropriate endpoint for replay data
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            trainId: training.id,
            trainingName: training.trainingname,
            ip: training.ip,
            port: training.port,
            processId: training.processid,
            tensorboardpath: training.tensorboardpath
        },
        success(resp) {
            // Process the raw training log data for replay visualization
            console.log(resp)
            fetchTrainings();
            alert("已继续训练")
        },
        error(err) {
            console.error("Error fetching replay data:", err);
        }
    });
};

const closeTrainingReplay = () => {
    isTrainingReplayVisible.value = false;
    isLoading.value = false;
    $.ajax({
        url: "http://127.0.0.1:3000/train/deleteTensorboard/",  // Use the appropriate endpoint for replay data
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            tensorboardpath: tensorboardTraining.value.tensorboardpath,
            ip: tensorboardTraining.value.ip,
            port: tensorboardTraining.value.port,
            tPort: tensorboardPort.value,
        },
        success(resp) {
            // Process the raw training log data for replay visualization
            console.log(resp)
        },
        error(err) {
            console.error("Error fetching replay data:", err);
        }
    });
};

import { LineElement, CategoryScale, LinearScale, Title, Tooltip, Legend } from 'chart.js';

// 注册所需的模块
Chart.register(LineElement, CategoryScale, LinearScale, Title, Tooltip, Legend);

const generateReport = function (training) {
    console.log(training)

    // 假设 HTML 文件已经存在于服务器的某个路径下
    const fileUrl = '/report.html';

    // 创建下载链接并触发下载
    const link = document.createElement('a');
    link.href = fileUrl;         // 设置文件 URL
    link.download = '训练报告.html'; // 设置下载文件名
    link.click();                // 模拟点击下载
};

const downloadModel = (training) => {
    $.ajax({
        url: "http://127.0.0.1:3000/model/trainPth/",  // 确保后端接口正确
        type: "get",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            trainId: training.id,
        },
        xhrFields: {
            responseType: 'blob'  // 告诉 jQuery 以二进制 Blob 的方式接收数据
        },
        success(resp) {
            // 创建 Blob 对象
            const blob = new Blob([resp], { type: 'application/octet-stream' });

            // 创建下载链接
            const link = document.createElement('a');
            const url = URL.createObjectURL(blob);
            link.href = url;
            link.download = `model_${training.trainingname}.pth`;  // 设定下载文件名
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);

            // 释放 URL 资源
            URL.revokeObjectURL(url);
        },
        error(err) {
            console.error("Error fetching model file:", err);
        }
    });
};

const trainAcc = (training, speed) => {
    training.speedMultiplier = speed;
    // console.log("train Acc : ", speed)
    $.ajax({
        url: "http://127.0.0.1:3000/train/acc/",  // 确保后端接口正确
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            speed: speed,
        },
        success(resp) {
            console.log("modify train speed : ", resp.msg)
        },
        error(err) {
            console.error("Error fetching model file:", err);
        }
    });
}


const viewSuggestions = () => {
  isSuggestionsVisible.value = true;
    
    const randomNum = Math.floor(Math.random() * 5) + 1;

    let suggestions = [];

    switch(randomNum) {
        case 1:
            suggestions.push('CPU使用率较高，建议优化模型或增加并行计算资源');
            break;
        case 2:
                suggestions.push('GPU使用率较高，建议优化模型或增加GPU数量');
            break;
        case 3:
            suggestions.push('内存使用率较高，建议优化内存使用或增加内存');
            break;
        case 4:
            suggestions.push('网络带宽使用较高，可能会影响训练速度，建议优化数据传输');
            break;
        case 5:
            suggestions.push('CPU使用率较低，可以考虑减少计算资源分配');
            break;
        default:
            suggestions.push('资源使用情况正常，无需调整');
    }

    // 将随机选择的建议显示出来
    suggestionsData.value = suggestions.join('\n');
};
const confirmSpeed = () => {
    // console.log("confirm Speed : ", tempSpeedMultiplier.value)
    // console.log("confirmSpeed Current train : ", currentAccTrain.value)
    // training.speedMultiplier = tempSpeedMultiplier.value;
    trainAcc(currentAccTrain.value, tempSpeedMultiplier.value);
    const modal = Modal.getInstance(document.getElementById("speedModal"));
    modal.hide();
};

const closeSuggestions = () => {
    isSuggestionsVisible.value = false;
};

// Fetch trainings on component mount
onMounted(fetchTrainings);
</script>


<style>
.btn-start-comparison {
    background-color: #4caf50; /* 温和的草绿色 */
    border-color: #4caf50;
    color: white; /* 白色文字 */
    margin-right: 10px; /* 添加右侧间距 */
    padding: 8px 16px; /* 调整按钮内边距 */
    border-radius: 5px; /* 圆角按钮 */
    transition: background-color 0.3s ease; /* 平滑的过渡效果 */
}

.btn-start-comparison:hover {
    background-color: #388e3c; /* 深绿色，悬停时的颜色 */
}

.btn-cancel-comparison {
    background-color: #f44336; /* 鲜艳的红色 */
    border-color: #f44336;
    color: white; /* 白色文字 */
    margin-right: 10px; /* 添加右侧间距 */
    padding: 8px 16px; /* 调整按钮内边距 */
    border-radius: 5px; /* 圆角按钮 */
    transition: background-color 0.3s ease; /* 平滑的过渡效果 */
}

.btn-cancel-comparison:hover {
    background-color: #d32f2f; /* 深红色，悬停时的颜色 */
}
.loading-container {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 100%;
    background: rgba(255, 255, 255, 0.8);
}

.spinner {
    width: 40px;
    height: 40px;
    border: 4px solid rgba(0, 0, 0, 0.1);
    border-top-color: #3498db;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

.loading-text {
    margin-top: 10px;
    font-size: 16px;
    color: #555;
    font-weight: bold;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }
    100% {
        transform: rotate(360deg);
    }
}

.modal-content {
    background: #f8f9fa; 
    border-radius: 12px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.modal-title {
    font-weight: bold;
    color: #333;
}

.form-range::-webkit-slider-runnable-track {
    background: #ddd;
    height: 6px;
    border-radius: 5px;
}

.form-range::-webkit-slider-thumb {
    appearance: none;
    width: 16px;
    height: 16px;
    background: #007bff;
    border-radius: 50%;
    cursor: pointer;
    transition: transform 0.2s ease-in-out;
}

.form-range::-webkit-slider-thumb:hover {
    transform: scale(1.2);
}

.current-speed {
    font-size: 18px;
    font-weight: bold;
    color: #007bff;
}

.btn-modern {
    background: #007bff;
    color: white;
    border-radius: 8px;
    padding: 8px 16px;
    font-weight: bold;
    transition: all 0.2s ease-in-out;
}

.btn-modern:hover {
    background: #0056b3;
    box-shadow: 0 2px 10px rgba(0, 123, 255, 0.3);
}

.btn-cancel {
    background: #6c757d;
}

.btn-cancel:hover {
    background: #545b62;
}

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
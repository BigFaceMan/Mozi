<template>
    <div class="container mt-4">
        <!-- Search and Filters -->
        <div class="d-flex justify-content-between mb-3">
            <div class="input-group" style="width: 300px;">
                <input type="text" class="form-control" placeholder="查找模型..." v-model="searchQuery" @input="filterTrainings">
                <button class="btn btn-outline-secondary" type="button" @click="resetSearch">重置</button>
            </div>
            <div class="ml-auto">
                <button
                    v-if="!isComparing"
                    class="btn btn-primary"
                    @click="openComparison"
                >
                    模型对比
                </button>

                <button
                    v-if="isComparing"
                    class="btn btn-start-comparison"
                    @click="startComparison"
                >
                    开始对比
                </button>

                <button
                    v-if="isComparing"
                    class="btn btn-cancel-comparison"
                    @click="toggleComparison"
                >
                    取消对比
                </button>
            </div>

        </div>
        <!-- Training Records Table -->
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">模型列表</h5>
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th scope="col" v-if="isComparing">选择</th> <!-- 选择列 -->
                            <th scope="col">模型名</th>
                            <th scope="col">场景</th>
                            <th scope="col">方法</th>
                            <th scope="col">强化学习环境</th>
                            <th scope="col">训练状态</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="training in paginatedTrainings" :key="training.id">
                            <td v-if="isComparing">
                                <input
                                type="checkbox"
                                v-model="selectedModels"
                                :value="training"
                                :disabled="selectedModels.length >= 2 && !selectedModels.includes(training)"
                                />
                            </td>
                            <td>{{ training.trainingname.split("_")[0] }}</td>
                            <td>
                                {{ training.scene }}
                            </td>
                            <td>{{ training.model }}</td>
                            <td>{{ training.pytorchversion }}</td>
                            <td>
                                <span v-if="training.running == '0'">已完成</span>
                                <span v-else-if="training.running == '1'">正在训练</span>
                                <span v-else-if="training.running == '2'">已暂停</span>
                            </td>
                        <td>
                            <button class="btn btn-sm btn-info" v-if="training.running == '0'" @click="visualizeReport(training)">训练日志</button>
                            <button class="btn btn-sm btn-secondary ms-2" v-if="training.running == '0'" @click="viewResourceUsage(training)">资源使用报告</button>
                            <button class="btn btn-sm btn-success ms-2" v-if="training.running == '0'" @click="viewSuggestions(training)">智能建议</button>
                            <button class="btn btn-sm btn-warning ms-2" v-if="training.running == '0'" @click="viewTrainingReplay(training)">训练回放</button>
                            <button class="btn btn-sm ms-2" style="background-color:teal;" v-if="training.running == '0' && training.upload == '0'" @click="addGoodModel(training)">上传模型</button>
                            <button class="btn btn-sm ms-2" style="background-color:teal;" v-if="training.running == '0' && training.upload == '1'">正在上传</button>
                            <button class="btn btn-sm ms-2" style="background-color:teal;" v-if="training.running == '0' && training.upload == '2'" @click="addGoodModel(training)">已上传</button>
                            <button class="btn btn-sm ms-2" style="background-color:teal;" v-if="training.running == '0' && training.upload == '3' && !training.isValidating" @click="validModel(training)">待验证</button>
                            <button class="btn btn-sm ms-2" style="background-color:teal;" v-if="training.isValidating" >正在验证</button>
                            <button class="btn btn-sm ms-2" style="background-color: coral;" v-if="training.running == '0'" @click="generateReport(training)">生成报告</button>
                            <button class="btn btn-sm ms-2" style="background-color:darkseagreen;" v-if="training.running == '0'" @click="modelTest(training)">模型测试</button>
                            <button class="btn btn-sm ms-2" style="background-color: cornflowerblue;" v-if="training.running == '0'" @click="downloadModel(training)">下载</button>
                            <button class="btn btn-sm btn-danger ms-2" v-if="training.running == '0'" @click="deleteTraining(training)">删除模型</button>
                            <button class="btn btn-sm btn-success ms-2" v-if="training.running == '1'" @click="viewTrainingReplay(training)">实时监控</button>
                            <button class="btn btn-sm btn-info ms-2" v-if="training.running == '1'" @click="visualizeReport(training)">训练日志</button>
                            <button class="btn btn-sm btn-warning ms-2" v-if="training.running == '1'" @click="stopTraining(training)">暂停训练</button>
                            <button class="btn btn-sm ms-2" style="background-color: chocolate;" v-if="training.running == '1'" @click="restartTraining()">训练重启</button>
                            <button class="btn btn-sm btn-danger ms-2" v-if="training.running == '1'" @click="killTraining(training)">终止训练</button>
                            <button class="btn btn-sm btn-warning ms-2" v-if="training.running == '2'" @click="continueTraining(training)">继续训练</button>
                            <!-- <button class="btn btn-sm btn-warning ms-2" v-if="training.running == '2'" @click="continueTraining(training)">继续训练</button> -->
                            <button class="btn btn-sm btn-danger ms-2" v-if="training.running == '2'" @click="killTraining(training)">终止训练</button>
                            <div class="btn-group ms-2" v-if="training.running == '1'">
                                <button class="btn btn-sm" :style="{ backgroundColor: '#FFA500', color: 'white' }">
                                    <i class="fas fa-tachometer-alt"></i> {{ training.speedMultiplier || 1 }}x 加速
                                </button>
                                <button class="btn btn-sm dropdown-toggle dropdown-toggle-split" :style="{ backgroundColor: '#FFA500', color: 'white' }" data-bs-toggle="dropdown" aria-expanded="false">
                                </button>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" @click="trainAcc(training, 1)">🚀 1x 加速</a></li>
                                    <li><a class="dropdown-item" @click="trainAcc(training, 1.5)">🚀 1.5x 加速</a></li>
                                    <li><a class="dropdown-item" @click="trainAcc(training, 2)">⚡ 2x 加速</a></li>
                                    <li><a class="dropdown-item" @click="trainAcc(training, 3)">🔥 3x 加速</a></li>
                                    <li><a class="dropdown-item" @click="trainAcc(training, 10)">🔥 10x 加速</a></li>
                                </ul>
                            </div>
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
                        <li class="page-item" :class="{ disabled: currentPage.value === totalPages}">
                            <button class="page-link" @click="goToPage(currentPage + 1)">下一页</button>
                        </li>
                    </ul>
                </nav>
        <!-- Start Comparison Button -->
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
import $ from 'jquery';
import { Chart } from 'chart.js';
import { useStore } from 'vuex';

const store = useStore();

const trainings = ref([]);
const filteredTrainings = ref([]);
const paginatedTrainings = ref([]);
const searchQuery = ref('');
const currentPage = ref(1);
const itemsPerPage = 10;
const tensorboardTraining = ref(null);
const tensorboardPort = ref(6001);

const totalPages = computed(() => Math.ceil(trainings.value.length / itemsPerPage));
const tensorboardUrl = computed(() => `http://${tensorboardTraining.value.ip}:${tensorboardPort.value}`);


const isComparing = ref(false);
const selectedModels = ref([]);
const isComparisonVisible = ref(false);
const ComparisonResults = ref([]);
const openComparison = () => {
    isComparing.value = true;
}
const toggleComparison = () => {
    isComparing.value = false;
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
            trainings.value = resp;
            // 为每个训练项添加 isValidata 属性
            trainings.value.forEach(training => {
                // 默认为 false，可以根据需要修改
                training.isValidating = false;
            });
            console.log(trainings.value)
            filteredTrainings.value = resp;
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

const stopTraining = (training) => {
    console.log("training : ", training)
    console.log("hello world")
    $.ajax({
        url: "http://127.0.0.1:3000/train/stop/",  // Use the appropriate endpoint for replay data
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            trainingName: training.trainingname,
            ip: training.ip, 
            port: training.port,
            processId: training.processid
        },
        success(resp){
            // Process the raw training log data for replay visualization
            console.log(resp)
            fetchTrainings();
        },
        error(err) {
            console.error("Error fetching replay data:", err);
        }
    });

};


const killTraining = (training) => {
    $.ajax({
        url: "http://127.0.0.1:3000/train/kill/",  // Use the appropriate endpoint for replay data
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            trainingName: training.trainingname,
            ip: training.ip, 
            port: training.port,
            processId: training.processid
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
const restartTraining = () => {
    fetchTrainings()
}
const continueTraining = (training) => {
    $.ajax({
        url: "http://127.0.0.1:3000/train/continue/",  // Use the appropriate endpoint for replay data
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
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
        },
        error(err) {
            console.error("Error fetching replay data:", err);
        }
    });
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
const viewTrainingReplay = (training) => {
    tensorboardTraining.value = training;
    isTrainingReplayVisible.value = true;
    isLoading.value = true;

    $.ajax({
        url: "http://127.0.0.1:3000/train/addTensorboard/",  // Use the appropriate endpoint for replay data
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            tensorboardpath: tensorboardTraining.value.tensorboardpath,
            ip: tensorboardTraining.value.ip,
            port: tensorboardTraining.value.port,
        },
        success(resp) {
            // Process the raw training log data for replay visualization
            console.log(resp)
            tensorboardPort.value = resp.tPort;
            if (resp.error_message === 'success') {
                isLoading.value = false;
                console.log("success")
                // console.log(tensorboardTraining.value)
                console.log(tensorboardTraining.value.ip + ':' + tensorboardPort.value)
                console.log(tensorboardUrl.value)
            } else {
                isLoading.value = true;
            }
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
            console.log("modify train speed : ", resp.message)
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



</style>
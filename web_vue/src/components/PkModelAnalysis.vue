<template>
    <div class="container mt-4">
        <!-- Search and Filters -->
        <div class="d-flex justify-content-between mb-3">
            <div class="input-group" style="width: 300px;">
                <input type="text" class="form-control" placeholder="查找对战记录..." v-model="searchQuery" @input="filterTrainings">
                <button class="btn btn-outline-secondary" type="button" @click="resetSearch">重置</button>
            </div>
        </div>

        <!-- Training Records Table -->
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">对战记录列表</h5>
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th scope="col">对战名</th>
                            <th scope="col">想定</th>
                            <th scope="col">模型</th>
                            <th scope="col">对战状态</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="training in paginatedTrainings" :key="training.id">
                            <td>{{ training.infername.split("_")[0] }}</td>
                            <td>
                                {{ training.scene }}
                            </td>
                            <td>{{ training.model.split("_")[0] }}</td>
                            <td>
                                <span v-if="training.running == '0'">已完成</span>
                                <span v-else-if="training.running == '1'">正在对战</span>
                                <span v-else-if="training.running == '2'">已暂停</span>
                            </td>
                            <td>
                                <button class="btn btn-sm btn-info" v-if="training.running == '0'" @click="visualizeReport(training)">对战日志</button>
                                <button class="btn btn-sm btn-secondary ms-2" v-if="training.running == '0'" @click="viewResourceUsage(training)">资源使用报告</button>
                                <button class="btn btn-sm btn-success ms-2" v-if="training.running == '0'" @click="viewSuggestions(training)">智能建议</button>
                                <button class="btn btn-sm btn-warning ms-2" v-if="training.running == '0'" @click="viewTrainingReplay(training)">对战回放</button>
                                <button class="btn btn-sm btn-success ms-2" v-if="training.running == '1'" @click="viewTrainingReplay(training)">实时监控</button>
                                <button class="btn btn-sm btn-info ms-2" v-if="training.running == '1'" @click="visualizeReport(training)">对战日志</button>
                                <button class="btn btn-sm ms-2" style="background-color: darkgray;" @click="visualizaSituationInstrc(training)">态势日志</button>
                                <button class="btn btn-sm ms-2" style="background-color:coral;" @click="visualizaInstrc(training)">指令日志</button>
                                <button class="btn btn-sm btn-danger ms-2" v-if="training.running == '0'" @click="deleteTraining(training)">删除记录</button>
                                <button class="btn btn-sm btn-warning ms-2" v-if="training.running == '1'" @click="stopTraining(training)">暂停对战</button>
                                <button class="btn btn-sm btn-danger ms-2" v-if="training.running == '1'" @click="killTraining(training)">终止对战</button>
                                <button class="btn btn-sm btn-warning ms-2" v-if="training.running == '2'" @click="continueTraining(training)">继续对战</button>
                                <button class="btn btn-sm btn-danger ms-2" v-if="training.running == '2'" @click="killTraining(training)">终止对战</button>
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
            </div>
        </div>

        <div v-if="isInstrcVisible" class="modal fade show" tabindex="-1" aria-labelledby="visualizationModalLabel" aria-hidden="true" style="display: block;">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">指令日志</h5>
                        <button type="button" class="btn-close" @click="closeInstrc" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <!-- Training Logs -->
                    <div class="training-logs" style="height: 400px; overflow-y: auto; font-family: monospace; background-color: #f8f9fa; padding: 10px; border: 1px solid #dee2e6;">
                        <!-- 第一轮的态势与指令转换 -->
                        <p>[INFO] 第1轮训练开始。</p>

                        <!-- 指令转换前 -->
                        <p>[CMD] 模型输出（转换前）: [动作=移动, 方向=右, 速度=7]</p>
                        <p>[CMD] 转换为游戏指令: 向右移动，速度为7</p>

                        <!-- 第二轮的态势与指令转换 -->
                        <p>[INFO] 第2轮训练开始。</p>

                        <!-- 指令转换前 -->
                        <p>[CMD] 模型输出（转换前）: [动作=转向, 方向=左, 速度=3]</p>
                        <p>[CMD] 转换为游戏指令: 向左转，速度为3</p>

                        <!-- 第三轮的态势与指令转换 -->
                        <p>[INFO] 第3轮训练开始。</p>

    
                        <!-- 指令转换前 -->
                        <p>[CMD] 模型输出（转换前）: [动作=射击, 方向=前方, 速度=0]</p>
                        <p>[CMD] 转换为游戏指令: 向前射击</p>
                    </div>

                    </div>
                </div>
            </div>
        </div>


        <div v-if="isSituationInstrcVisible" class="modal fade show" tabindex="-1" aria-labelledby="visualizationModalLabel" aria-hidden="true" style="display: block;">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">态势日志</h5>
                        <button type="button" class="btn-close" @click="closeSituationInstrc" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <!-- Training Logs -->
                    <div class="training-logs" style="height: 400px; overflow-y: auto; font-family: monospace; background-color: #f8f9fa; padding: 10px; border: 1px solid #dee2e6;">
                        <!-- 第一轮的态势与指令转换 -->
                        <p>[INFO] 第1轮训练开始。</p>

                        <!-- 态势转换前 -->
                        <p>[STA] 当前游戏状态 - 位置: (x=10, y=20), 速度: (v=5), 角度: (a=45°), 生命值: 85%</p>
                        <p>[STA] 转换为输入数组: [10, 20, 5, 45, 85]</p>

                        <!-- 第二轮的态势与指令转换 -->
                        <p>[INFO] 第2轮训练开始。</p>

                        <!-- 态势转换前 -->
                        <p>[STA] 当前游戏状态 - 位置: (x=15, y=22), 速度: (v=6), 角度: (a=30°), 生命值: 78%</p>
                        <p>[STA] 转换为输入数组: [15, 22, 6, 30, 78]</p>

                        <!-- 第三轮的态势与指令转换 -->
                        <p>[INFO] 第3轮训练开始。</p>

                        <!-- 态势转换前 -->
                        <p>[STA] 当前游戏状态 - 位置: (x=20, y=25), 速度: (v=4), 角度: (a=60°), 生命值: 72%</p>
                        <p>[STA] 转换为输入数组: [20, 25, 4, 60, 72]</p>

                    </div>
                    </div>
                </div>
            </div>
        </div>


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

        <!-- Training Replay Modal -->
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
import store from '@/store';

const trainings = ref([]);
const filteredTrainings = ref([]);
const paginatedTrainings = ref([]);
const searchQuery = ref('');
const currentPage = ref(1);
const itemsPerPage = 10;
const tensorboardTraining = ref(null);
const tensorboardPort = ref(6001);
const tensorboardUrl = computed(() => `http://${tensorboardTraining.value.ip}:${tensorboardPort.value}`);
const totalPages = computed(() => Math.ceil(trainings.value.length / itemsPerPage));
const isLoading = ref(false);

const fetchTrainings = () => {
    $.ajax({
        url: "http://localhost:3000/infer/getlist/",
        type: "get",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        success(resp) {
            trainings.value = resp;
            filteredTrainings.value = resp;
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
const isSituationInstrcVisible = ref(false);
const isInstrcVisible = ref(false);

const visualizaSituationInstrc = () => {
    isSituationInstrcVisible.value = true;
};

const visualizaInstrc = () => {
    isInstrcVisible.value = true;
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
            url: "http://localhost:3000/trainlog/getlist/",
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

const closeVisualization = () => {
    isVisualizationVisible.value = false;
    // if (lossChart.value) {
    //     lossChart.value.destroy();
    //     lossChart.value = null;
    // }
};

const closeSituationInstrc = () => {
    isSituationInstrcVisible.value = false;
    // if (lossChart.value) {
    //     lossChart.value.destroy();
    //     lossChart.value = null;
    // }
};

const closeInstrc = () => {
    isInstrcVisible.value = false;
    // if (lossChart.value) {
    //     lossChart.value.destroy();
    //     lossChart.value = null;
    // }
};

// Resource usage handling
const viewResourceUsage = (training) => {

    isResourceUsageVisible.value = true;

    $.ajax({
        url: "http://localhost:3000/trainlog/getlist/",
        type: "get",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            trainingname: training.infername,
        },
        success(resp) {
            console.log(resp)
            // 随机生成数据
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

const stopTraining = (training) => {
    $.ajax({
        url: "http://localhost:3000/infer/stop/",  // Use the appropriate endpoint for replay data
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            inferName: training.infername,
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

const viewTrainingReplay = (training) => {
    isTrainingReplayVisible.value = true;
    tensorboardTraining.value = training;
    isLoading.value = true;

    $.ajax({
        url: "http://localhost:3000/infer/addTensorboard/",  // Use the appropriate endpoint for replay data
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            tensorboardpath: training.tensorboardpath,
            ip: tensorboardTraining.value.ip,
            port: tensorboardTraining.value.port,
        },
        success(resp) {
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


const killTraining = (training) => {
    console.log(training)
    $.ajax({
        url: "http://localhost:3000/infer/kill/",  // Use the appropriate endpoint for replay data
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            inferName: training.infername,
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

const continueTraining = (training) => {
    $.ajax({
        url: "http://localhost:3000/infer/continue/",  // Use the appropriate endpoint for replay data
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            inferName: training.infername,
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

const deleteTraining = (training) => {
    if (confirm('确定要删除这个记录吗？')) {
        $.ajax({
            url: "http://localhost:3000/infer/remove/",  // Use the appropriate endpoint for replay data
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

const closeTrainingReplay = () => {
    isTrainingReplayVisible.value = false;
    isLoading.value = false;
    $.ajax({
        url: "http://localhost:3000/train/deleteTensorboard/",  // Use the appropriate endpoint for replay data
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

const viewSuggestions = (training) => {

    isSuggestionsVisible.value = true;
    console.log(training)
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
    // $.ajax({
    //     url: "http://localhost:3000/trainlog/getlist/",  // Assuming the same endpoint is used to get logs for suggestions
    //     type: "get",
    //     headers: {
    //         Authorization: "Bearer " + store.state.user.token,
    //     },
    //     data: {
    //         trainingname: training.trainingname,
    //     },
    //     success(resp) {
    //         console.log(resp);

    //         // Calculate average resource usage
    //         const avgCPU = resp.reduce((sum, data) => sum + data.cpuUsage, 0) / resp.length;
    //         const avgGPU = resp.reduce((sum, data) => sum + data.gpuUsage, 0) / resp.length;
    //         const avgMemory = resp.reduce((sum, data) => sum + data.memoryUsage, 0) / resp.length;
    //         const avgNetwork = resp.reduce((sum, data) => sum + data.networkBandwidth, 0) / resp.length;

    //         // Generate suggestions based on resource usage
    //         let suggestions = [];

    //         if (avgCPU > 80) {
    //             suggestions.push('CPU使用率较高，建议优化模型或增加并行计算资源');
    //         } else if (avgCPU < 30) {
    //             suggestions.push('CPU使用率较低，可以考虑减少计算资源分配');
    //         }

    //         if (avgGPU > 80) {
    //             suggestions.push('GPU使用率较高，建议优化模型或增加GPU数量');
    //         } else if (avgGPU < 30) {
    //             suggestions.push('GPU使用率较低，可以考虑减少GPU分配');
    //         }

    //         if (avgMemory > 80) {
    //             suggestions.push('内存使用率较高，建议优化内存使用或增加内存');
    //         }

    //         if (avgNetwork > 80) {
    //             suggestions.push('网络带宽使用较高，可能会影响训练速度，建议优化数据传输');
    //         }

    //         // Combine suggestions into one string
    //         suggestionsData.value = suggestions.join('\n');
    //     },
    //     error(err) {
    //         console.error("Error fetching resource usage for suggestions:", err);
    //     }
    // });
};

const closeSuggestions = () => {
    isSuggestionsVisible.value = false;
};

// Fetch trainings on component mount
onMounted(fetchTrainings);
</script>

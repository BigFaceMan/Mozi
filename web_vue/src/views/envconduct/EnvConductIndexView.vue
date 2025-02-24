<template>
    <div class="container mt-4">
        <!-- Search and Add Model Button -->
        <div class="d-flex justify-content-between mb-3">
            <div class="input-group" style="width: 300px;">
                <input type="text" class="form-control" placeholder="查找想定..." v-model="searchQuery" @input="filterModels">
                <button class="btn btn-outline-secondary" type="button" @click="resetSearch">重置</button>
            </div>
            <button class="btn btn-primary" @click="openAddModelModal">新增想定</button>
        </div>

        <!-- Model Table -->
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">想定列表</h5>
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th scope="col">想定名</th>
                            <th scope="col">想定简介</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="model in paginatedModels" :key="model.id">
                            <td>{{ model.name }}</td>
                            <td>{{ model.summary }}</td>
                            <td>
                                <button class="btn btn-sm btn-secondary" @click="openEditModelModal(model)">编辑</button>
                                <button class="btn btn-sm btn-danger ms-2" @click="deleteModel(model.id)">删除</button>
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
                        <li class="page-item" :class="{ disabled: currentPage.value === totalPages }">
                            <button class="page-link" @click="goToPage(currentPage + 1)">下一页</button>
                        </li>
                    </ul>
                </nav>
            </div>
        </div>

        <!-- Add/Edit Model Modal -->
        <div v-if="isModalVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="modelModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 v-if="isEditing" class="modal-title" id="modelModalLabel">编辑想定</h5>
                        <h5 v-else class="modal-title" id="modelModalLabel">新增想定</h5>
                        <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form>
                            <div class="mb-3">
                                <label for="name" class="form-label">想定名</label>
                                <input type="text" class="form-control" id="name" v-model="form.name">
                            </div>
                            <!-- <div class="mb-3">
                                <label for="summary" class="form-label">想定简介</label>
                                <input type="text" class="form-control" id="summary" v-model="form.summary">
                            </div> -->
                            <div class="mb-3">
                                <label for="environment" class="form-label">选择想定配置文件</label>
                                <input type="file" class="form-control" id="envSelection" @change="handleFileChange" />
                            </div>
                            <!-- <div class="mb-3">
                                <label for="environment" class="form-label">想定选择</label>
                                <select class="form-control" id="envSelection" v-model="form.envSelection">
                                    <option v-for="env in envOptions" :key="env.value" :value="env.value">
                                        {{ env.label }}
                                    </option>
                                </select>
                            </div> -->
                            <div class="mb-3">
                                <label for="environment" class="form-label">想定描述</label>
                                <textarea class="form-control" id="environment" v-model="form.summary" rows="4"></textarea>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
                        <button v-if="isEditing" type="button" class="btn btn-primary" @click="saveModel">保存更改</button>
                        <button v-else type="button" class="btn btn-primary" @click="saveModel">新增想定</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Visualization Modal -->
        <div v-if="isVisualizationVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="visualizationModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">模型可视化</h5>
                        <button type="button" class="btn-close" @click="closeVisualization" aria-label="Close"></button>
                    </div>
                    <div class="modal-body d-flex justify-content-center">
                        <img :src="visualizationPath" alt="Model Visualization" style="max-width: 100%; max-height: 100%;">
                    </div>
                </div>
            </div>
        </div>


        <!-- 添加MLP层 Modal -->
        <div v-if="isLayerVisualizationVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="layerVisualizationModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">添加MLP层</h5>
                        <button type="button" class="btn-close" @click="closeLayerVisualization" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div v-for="(layer, index) in newLayers" :key="index" class="mb-3 d-flex align-items-center">
                            <!-- 层类型 -->
                            <select class="form-select me-2" v-model="layer.type" style="width: 150px;">
                                <option value="Dense">Dense</option>
                                <option value="Activation">Activation</option>
                                <option value="Dropout">Dropout</option>
                            </select>

                            <!-- Dense层参数 -->
                            <div v-if="layer.type === 'Dense'" class="me-2" style="width: 150px;">
                                <input type="number" class="form-control" v-model="layer.units" placeholder="神经元数量">
                            </div>

                            <!-- Activation层参数 -->
                            <div v-if="layer.type === 'Activation'" class="me-2" style="width: 150px;">
                                <input type="text" class="form-control" v-model="layer.activationFunction" placeholder="激活函数">
                            </div>

                            <!-- Dropout层参数 -->
                            <div v-if="layer.type === 'Dropout'" class="me-2" style="width: 150px;">
                                <input type="number" class="form-control" v-model="layer.dropoutRate" placeholder="Dropout比率">
                            </div>

                            <!-- 删除按钮 -->
                            <button class="btn btn-danger btn-sm" @click="removeLayer(index)">删除</button>
                        </div>

                        <!-- 添加层按钮 -->
                        <button class="btn btn-success mt-3" @click="addLayerRow">添加层</button>
                    </div>
                    <div class="modal-footer">
                        <!-- 保存结构按钮 -->
                        <button class="btn btn-primary" @click="saveLayerStructure">保存结构</button>
                        <button class="btn btn-secondary" @click="closeLayerVisualization">关闭</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue';
import ace from 'ace-builds';
import $ from 'jquery';
import store from '@/store';

export default {
    components: {
    },
    setup() {
        ace.config.set(
            "basePath", 
            "https://cdn.jsdelivr.net/npm/ace-builds@" + require('ace-builds').version + "/src-noconflict/");

        const models = ref([]);
        const filteredModels = ref([]);
        const paginatedModels = ref([]);
        const searchQuery = ref('');
        const form = reactive({
            id: null,
            name: '',
            summary: '',
            envSelection: '',
            params: '',
        });
        // paginatedModels.value = [form];
        const isEditing = ref(false);
        const currentPage = ref(1);
        const itemsPerPage = 10;
        const isModalVisible = ref(false);
        const isVisualizationVisible = ref(false);
        const visualizationPath = ref('');
        const isLayerVisualizationVisible = ref(false);  // 控制层级可视化的显示

        const totalPages = computed(() => Math.ceil(filteredModels.value.length / itemsPerPage));

        const openLayerVisualizationModal = () => {
            isLayerVisualizationVisible.value = true;
        };

        const closeLayerVisualization = () => {
            isLayerVisualizationVisible.value = false;
        };
        const handleFileChange = (event) => {
            const file = event.target.files[0];
            if (file) {
                const reader = new FileReader();
                
                // 当文件读取完成时触发
                reader.onload = () => {
                    // 将文件的内容存储到 form.params 中
                    form.params = reader.result; // reader.result 是文件的内容
                };

                // 读取文件内容
                reader.readAsText(file); // 读取文件为文本，如果是二进制文件，可以使用 readAsDataURL
            }
        };

        const envOptions = ref([
             { value: '1', label: '墨子1' },
            { value: '2', label: '墨子2' },
            { value: '3', label: '墨子3' }
        ]);

        // const newLayer = reactive({
        //     type: 'Dense',  // 默认层类型
        //     units: 0,
        //     activationFunction: '',
        //     dropoutRate: 0
        // });
        const newLayers = ref([]); // 存储多个层信息
        // 添加一个新的层行
        const addLayerRow = () => {
            newLayers.value.push({
                type: "Dense", // 默认类型
                units: 0, // 默认神经元数量
                activationFunction: "", // 默认激活函数
                dropoutRate: 0 // 默认Dropout比率
            });
        };

        // 删除一个层
        const removeLayer = (index) => {
            newLayers.value.splice(index, 1);
        };

        // 保存层结构
        const saveLayerStructure = () => {
            console.log("保存的层结构:", newLayers.value);
            // TODO: 将层结构保存到模型表单或其他地方
            closeLayerVisualization();
        };

        const fetchModels = () => {
            // API call to fetch models
            $.ajax({
                url: "http://10.129.79.55:3000/situation/getlist/",
                type: "get",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    models.value = resp;
                    filteredModels.value = resp;
                    updatePaginatedModels();
                }
            });
        };

        const filterModels = () => {
            const query = searchQuery.value.trim().toLowerCase();
            filteredModels.value = models.value.filter(model =>
                model.name.toLowerCase().includes(query)
            );
            currentPage.value = 1;
            updatePaginatedModels();
        };

        const updatePaginatedModels = () => {
            const start = (currentPage.value - 1) * itemsPerPage;
            const end = start + itemsPerPage;
            paginatedModels.value = filteredModels.value.slice(start, end);
        };

        const resetSearch = () => {
            searchQuery.value = '';
            filteredModels.value = models.value;
            currentPage.value = 1;
            updatePaginatedModels();
        };

        const goToPage = (page) => {
            if (page > 0 && page <= totalPages.value) {
                currentPage.value = page;
                updatePaginatedModels();
            }
        };

        const openAddModelModal = () => {
            isEditing.value = false;
            resetForm();
            isModalVisible.value = true;
        };

        const openEditModelModal = (model) => {
            isEditing.value = true;
            Object.assign(form, model);
            isModalVisible.value = true;
        };

        const resetForm = () => {
            form.id = null;
            form.name = '';
            form.summary = '';
            form.params = '';
        };

        const saveModel = () => {
            if (isEditing.value) {
                updateModel();
            } else {
                addModel();
            }
        };

        const addModel = () => {
            if (form.params === '') {
                alert('请上传配置文件');
                return;
            } 
            // 定义正则表达式，检查格式
            const paramsPattern = /input:\s*\d+\s*output:\s*\d+\s*post:\s*localhost:\d+\/\S+\s*get:\s*localhost:\d+\/\S+/;

            // 使用正则表达式检查 form.params 是否符合要求
            if (!paramsPattern.test(form.params)) {
                alert('指令不合法，请重新配置');
                return;
            }
            // console.log("params is : ")
            // console.log(form.params)
            // Define add model logic here
            $.ajax({
                url: "http://10.129.79.55:3000/situation/add/",
                type: "post",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                data: {
                    "name": form.name,
                    "summary": form.summary,
                    "params": form.params,
                    "situationSelection": form.envSelection
                },
                success(resp) {
                    console.log(resp)
                    alert('指令有效，已发送管理指令');
                    fetchModels()
                },
                error(resp) {
                    console.log(resp)
                }
            });
            isModalVisible.value = false
        };

        const updateModel = () => {
            // Define update model logic here
            $.ajax({
                url: "http://10.129.79.55:3000/situation/update/",
                type: "post",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                data: {
                    "name": form.name,
                    "summary": form.summary,
                    "situation": form.situation,
                },
                success(resp) {
                    console.log(resp)
                    fetchModels()
                },
                error(resp) {
                    console.log(resp)
                }
            });
            isModalVisible.value = false
        };

        const deleteModel = (model_id) => {
            if (confirm('确定要删除这个模型吗？')) {
                // Define delete model logic here
                $.ajax({
                    url: "http://10.129.79.55:3000/situation/remove/",
                    type: "post",
                    headers: {
                        Authorization: "Bearer " + store.state.user.token,
                    },
                    data: {
                        id: model_id,
                    },
                    success(resp) {
                        console.log(resp)
                        fetchModels()
                    },
                    error(resp) {
                        console.log(resp)
                    }
                });
            }
        };

        const visualizeModel = (model) => {
            visualizationPath.value = model.structureimage; // Assuming structureimage is a property in model data
            isVisualizationVisible.value = true;
        };

        const closeModal = () => {
            isModalVisible.value = false;
        };

        const closeVisualization = () => {
            isVisualizationVisible.value = false;
        };

        const editorInit = () => {
            // Optional: Set editor configurations or content here
        };

        // Handle image upload (file selection)
        const handleImageUpload = (event) => {
            const file = event.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = () => {
                    form.structureimage = reader.result; // Store the image as a Base64 string
                };
                reader.readAsDataURL(file); // Convert file to Base64
            }
        };

        onMounted(() => {
            fetchModels();
        });

        return {
            models,
            searchQuery,
            filteredModels,
            paginatedModels,
            form,
            isEditing,
            currentPage,
            totalPages,
            isModalVisible,
            isVisualizationVisible,
            visualizationPath,
            filterModels,
            resetSearch,
            goToPage,
            openAddModelModal,
            openEditModelModal,
            closeModal,
            saveModel,
            deleteModel,
            visualizeModel,
            closeVisualization,
            editorInit,
            handleImageUpload,
            openLayerVisualizationModal,
            closeLayerVisualization,
            isLayerVisualizationVisible,
            newLayers,
            addLayerRow,
            removeLayer,
            saveLayerStructure,
            envOptions,
            handleFileChange
        };
    }
};
</script>

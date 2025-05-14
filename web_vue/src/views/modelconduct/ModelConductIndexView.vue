<!--
 * @Author: ssp
 * @Date: 2025-01-06 21:58:24
 * @LastEditTime: 2025-05-13 13:03:56
-->
<template>
    <div class="container mt-4">
        <!-- Search and Add Model Button -->
        <div class="d-flex justify-content-between mb-3">
            <div class="input-group" style="width: 300px;">
                <input type="text" class="form-control" placeholder="查找算法..." v-model="searchQuery" @input="filterModels">
                <button class="btn btn-outline-secondary" type="button" @click="resetSearch">重置</button>
            </div>

            <div class="btn-group" role="group">
                <button class="btn btn-primary" @click="openAddModelAddModal">创建算法</button>
                <button class="btn btn-success" @click="openAddModelModal">添加算法</button>
            </div>
        </div>

        <!-- Model Table -->
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">算法列表</h5>
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th scope="col">算法名</th>
                            <th scope="col">算法简介</th>
                            <th scope="col">运行场景</th>
                            <th scope="col">深度学习环境</th>
                            <th scope="col">算法能力</th>
                            <th scope="col">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="model in paginatedModels" :key="model.id">
                            <td>{{ model.name }}</td>
                            <td>{{ model.summary }}</td>
                            <td>{{ model.situationselect }}</td>
                            <td>{{ model.environment }}</td>
                            <td>{{ model.ability }}</td>
                            <td>
                                <button class="btn btn-sm btn-secondary" @click="openEditModelModal(model)">编辑</button>
                                <!-- <button class="btn btn-sm btn-secondary" >回滚</button> -->
                                <button class="btn btn-sm btn-danger ms-2" @click="deleteModel(model.id)">删除</button>
                                <button class="btn btn-sm btn-info ms-2" @click="visualizeModel(model)">可视化</button>
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
                        <li class="page-item" :class="{ disabled: currentPage.value === totalPages }">
                            <button class="page-link" @click="goToPage(currentPage + 1)">下一页</button>
                        </li>
                    </ul>
                </nav>
            </div>
        </div>



        <div v-if="isModalEditVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="modelModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg"> <!-- 放大一点 -->
                <div class="modal-content border-0 shadow-lg rounded-4">
                    <div class="modal-header bg-primary text-white rounded-top-4">
                        <h5 class="modal-title" id="modelModalLabel">
                            {{ isEditing ? '编辑算法' : '新增算法' }}
                        </h5>
                        <button type="button" class="btn-close btn-close-white" @click="closeModal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body px-4 py-3">
                        <form>
                            <div class="row g-3">
                                <!-- 算法名 -->
                                <div class="col-md-6">
                                    <label for="name" class="form-label fw-semibold">算法名</label>
                                    <input type="text" class="form-control" id="name" v-model="form.name" placeholder="请输入算法名称">
                                </div>

                                <!-- 深度学习环境 -->
                                <div class="col-md-6">
                                    <label for="environment" class="form-label fw-semibold">深度学习环境</label>
                                    <input type="text" class="form-control" id="environment" v-model="form.environment" placeholder="如 PyTorch 1.12, CUDA 11.3">
                                </div>

                                <!-- 选择算法文件 -->
                                <div class="col-md-6">
                                    <label for="modelSelection" class="form-label fw-semibold">上传模型文件</label>
                                    <input type="file" class="form-control" id="modelSelection" @change="handleFileChange">
                                </div>

                                <!-- 想定实例选择 -->
                                <div class="col-md-6">
                                    <label for="situationSelection" class="form-label fw-semibold">想定实例选择</label>
                                    <select class="form-select" id="situationSelection" v-model="form.situationSelection">
                                        <option disabled value="">请选择想定实例</option>
                                        <option v-for="situation in situations" :key="situation.id" :value="situation.name">
                                            {{ situation.projectname }}
                                        </option>
                                    </select>
                                </div>

                                <!-- 算法能力 -->
                                <div class="col-12">
                                    <label for="ability" class="form-label fw-semibold">算法能力</label>
                                    <input type="text" class="form-control" id="ability" v-model="form.ability" placeholder="例如：异常检测、图像识别等">
                                </div>

                                <!-- 算法简介 -->
                                <div class="col-12">
                                    <label for="summary" class="form-label fw-semibold">算法简介</label>
                                    <textarea class="form-control" id="summary" v-model="form.summary" rows="3" placeholder="请输入算法的功能、用途、实现方式等"></textarea>
                                </div>

                                <!-- 算法结构图片 -->
                                <div class="col-12">
                                    <label for="structureimage" class="form-label fw-semibold">结构图片</label>
                                    <input type="file" class="form-control" id="structureimage" @change="handleImageUpload">
                                    <div v-if="form.structureimage" class="mt-2">
                                        <img :src="form.structureimage" alt="模型结构图" class="img-thumbnail" style="max-height: 120px;">
                                    </div>
                                </div>

                                <!-- 算法代码 -->
                                <div class="col-12">
                                    <label for="code" class="form-label fw-semibold">算法代码</label>
                                    <VAceEditor
                                        v-model:value="form.code"
                                        lang="python"
                                        theme="textmate"
                                        @init="editorInit"
                                        style="height: 300px; width: 100%; border-radius: 8px; border: 1px solid #dee2e6;"
                                    />
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer bg-light border-top-0 px-4 py-3">
                        <button type="button" class="btn btn-outline-secondary" @click="closeModal">取消</button>
                        <button type="button" class="btn btn-primary" @click="saveModel">
                            {{ isEditing ? '保存更改' : '新增算法' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>


        <!-- Add/Edit Model Modal -->
        <div v-if="isModalAddVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="modelModalAddLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="modelModalLabel">创建算法</h5>
                        <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form>
                            <div class="mb-3">
                                <label for="code" class="form-label">算法代码</label>
                                <VAceEditor
                                    v-model:value="form.code"
                                    lang="python"
                                    theme="textmate"
                                    @init="editorInit"
                                    style="height: 300px; width: 100%;"
                                />
                            </div>
                            <button type="button" class="btn btn-info" @click="openLayerVisualizationModal">可视化添加</button>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" @click="closeModalAdd">取消</button>
                        <button v-if="isEditing" type="button" class="btn btn-primary" @click="saveModelAdd">保存更改</button>
                        <button v-else type="button" class="btn btn-primary" @click="saveModelAdd">创建算法</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Add/Edit Model Modal -->
        <div v-if="isModalVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="modelModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-xl modal-dialog-centered">
            <div class="modal-content shadow-lg rounded-4 border-0">
            <div class="modal-header bg-primary text-white rounded-top-4">
                <h5 class="modal-title" id="modelModalLabel">添加算法</h5>
                <button type="button" class="btn-close btn-close-white" @click="closeModal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <form>
                <div class="row">
                    <!-- 左半部分 -->
                    <div class="col-md-6">
                    <div class="mb-3">
                        <label for="name" class="form-label">算法名</label>
                        <input type="text" class="form-control" id="name" v-model="form.name">
                    </div>

                    <div class="mb-3">
                        <label class="form-label">选择算法文件</label>
                        <input type="file" class="form-control" @change="handleFileChange" />
                    </div>

                    <div class="mb-3">
                        <label class="form-label">导入模型</label>
                        <input type="file" class="form-control" @change="handleModelPthFileChange" />
                    </div>

                    <div class="mb-3">
                        <label class="form-label">选择数据文件</label>
                        <input type="file" class="form-control" @change="handleDataFileChange" />
                    </div>

                    <div class="mb-3">
                        <label class="form-label">算法能力</label>
                        <input type="text" class="form-control" v-model="form.ability">
                    </div>

                    <div class="mb-3">
                        <label class="form-label">场景选择</label>
                        <select class="form-control" v-model="form.situationSelection">
                        <option v-for="situation in situations" :key="situation.id" :value="situation.projectname">
                            {{ situation.projectname }}
                        </option>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">深度学习环境</label>
                        <select class="form-control" v-model="form.environment">
                        <option disabled value="">请选择一个基准环境</option>
                        <option v-for="i in 12" :key="i" :value="`torch1.${6 + i}`">
                            {{ `torch1.${6 + i}` }}
                        </option>
                        </select>
                    </div>
                    </div>

                    <!-- 右半部分 -->
                    <div class="col-md-6">
                    <div class="mb-3">
                        <label class="form-label">算法简介</label>
                        <textarea class="form-control" v-model="form.summary" rows="9" placeholder="简要描述该算法的核心思路和应用场景..."></textarea>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">算法结构图片</label>
                        <input type="file" class="form-control" @change="handleImageUpload">
                        <div v-if="form.structureimage" class="mt-2">
                        <img :src="form.structureimage" alt="模型结构图片" style="max-width: 100%; max-height: 200px; border: 1px solid #ddd; border-radius: 6px;">
                        </div>
                    </div>
                    </div>
                </div>
                </form>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" @click="closeModal">取消</button>
                <button v-if="isEditing" type="button" class="btn btn-success" @click="saveModel">保存更改</button>
                <button v-else type="button" class="btn btn-primary" @click="saveModel">新增算法</button>
            </div>
            </div>
        </div>
        </div>


        <!-- Visualization Modal -->
        <div v-if="isVisualizationVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="visualizationModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">算法可视化</h5>
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
import { VAceEditor } from 'vue3-ace-editor';
import ace from 'ace-builds';
import $ from 'jquery';
import store from '@/store';
import { saveAs } from 'file-saver';

export default {
    components: {
        VAceEditor
    },
    setup() {
        ace.config.set(
            "basePath", 
            "https://cdn.jsdelivr.net/npm/ace-builds@" + require('ace-builds').version + "/src-noconflict/");

        const models = ref([]);
        const situations = ref([]);
        const filteredModels = ref([]);
        const paginatedModels = ref([]);
        const searchQuery = ref('');
        const form = reactive({
            id: null,
            name: '',
            summary: '',
            environment: '',
            ability: '',
            situationSelection: '',
            methodSelection: '',
            modelPth: '',
            structureimage: '', // 默认为空，等待用户上传
            code: '',
            inferCode: '',
            data: ''
        });
        const methodOptions = ref([
             { value: '1', label: 'DQN' },
            { value: '2', label: 'DDPG' },
            { value: '3', label: 'CNN' }
        ]);
        // paginatedModels.value = [form];
        const isEditing = ref(false);
        const currentPage = ref(1);
        const itemsPerPage = 10;
        const isModalVisible = ref(false);
        const isVisualizationVisible = ref(false);
        const isModalEditVisible = ref(false);  
        const visualizationPath = ref('');
        const isLayerVisualizationVisible = ref(false);  // 控制层级可视化的显示
        const isModalAddVisible = ref(false);

        const totalPages = computed(() => Math.ceil(filteredModels.value.length / itemsPerPage));
        const updateStructureImage = async () => {
            console.log("修改 imageBase64");
            
            // 定义图片路径对应的 URL
            const imagePaths = {
                '1': require('@/assets/images/DQN.png'), // Vue 的资源引用
                '2': require('@/assets/images/DDPG.png'),
                '3': require('@/assets/images/CNN.jpg'),
            };

            // 根据选中的算法获取对应的图片路径
            const imagePath = imagePaths[form.methodSelection] || null;

            if (imagePath) {
                try {
                    // 将图片路径读取为 Base64
                    const base64String = await fetchImageAsBase64(imagePath);
                    form.structureimage = base64String; // 存储 Base64 字符串
                } catch (error) {
                    console.error("图片读取失败:", error);
                    form.structureimage = null; // 如果出错，清空图片
                }
            } else {
                form.structureimage = null; // 清空图片
            }

            console.log("更新图片 Base64:", form.structureimage);
        };

        // 辅助函数：将图片路径读取为 Base64
        const fetchImageAsBase64 = async (imagePath) => {
            const response = await fetch(imagePath); // 读取图片文件
            if (!response.ok) {
                throw new Error(`无法加载图片: ${response.statusText}`);
            }
            const blob = await response.blob(); // 转换为 Blob 对象

            return new Promise((resolve, reject) => {
                const reader = new FileReader();
                reader.onloadend = () => resolve(reader.result); // 转换为 Base64 字符串
                reader.onerror = reject;
                reader.readAsDataURL(blob);
            });
        };

        // const updateStructureImage = () => {
        //     console.log("修改imagpath");
        //     switch (form.methodSelection) {
        //         case '1':
        //             form.structureimage = '../../assets/images/DQN.png'; // 算法 1 对应的图片
        //             break;
        //         case '2':
        //             form.structureimage = '../../assets/images/DDPG.png'; // 算法 2 对应的图片
        //             break;
        //         case '3':
        //             form.structureimage = '../../assets/images/CNN.jpg'; // 算法 3 对应的图片
        //             break;
        //         default:
        //             form.structureimage = null; // 清空图片
        //     }
        //     console.log("更新图片路径:", form.structureimage);
        // }

        const openLayerVisualizationModal = () => {
            isLayerVisualizationVisible.value = true;
        };

        const closeLayerVisualization = () => {
            isLayerVisualizationVisible.value = false;
        };

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
                url: "http://127.0.0.1:3000/model/getlist/",
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

            $.ajax({
                url: "http://127.0.0.1:3000/remote/getRExamples/",
                type: "post",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    situations.value = resp.data;
                    situations.value.push({ id: -1, projectname: "回归预测任务" });
                    // console.log(resp.data)
                }, error(resp) {
                    console.log(resp)
                    console.log("提取环境错误")
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

        const openAddModelAddModal = () => {
            resetForm();
            isModalAddVisible.value = true;
        };

        const openAddModelModal = () => {
            resetForm();
            isModalVisible.value = true;
        };

        const openEditModelModal = (model) => {
            isEditing.value = true;
            Object.assign(form, model);
            isModalEditVisible.value = true;
        };

        const resetForm = () => {
            form.id = null;
            form.name = '';
            form.summary = '';
            form.environment = '';
            form.ability = '';
            form.structureimage = ''; // 清空图片
            form.code = '';
        };

        const saveModel = () => {
            if (isEditing.value) {
                updateModel();
            } else {
                addModel();
            }
        };

        const saveCodeToFile = () => {
            const pythonCode = form.code; // 假设 `form.code` 包含的是 Python 代码

            const blob = new Blob([pythonCode], { type: 'text/plain;charset=utf-8' });
            // saveAs(blob, '@/assets/code/generated_code.py'); // 生成并下载文件
            saveAs(blob, 'generated_code.py'); // 生成并下载文件
        };

        const saveLayersToFile = () => {
            // 假设 newLayers 是一个数组，需要将其转化为 Python 代码格式
            const pythonLayersCode = `new_layers = ${JSON.stringify(newLayers.value, null, 4)}\n`;  // 格式化为 Python 的列表

            const blob = new Blob([pythonLayersCode], { type: 'text/plain;charset=utf-8' });
            // saveAs(blob, '@/assets/code/generated_code.py'); // 生成并下载文件
            saveAs(blob, 'generated_code.py'); // 生成并下载文件
        };

        const saveModelAdd = () => {
     
            addModelAdd()
        };
        const addModelAdd = () => {
            if (form.code != '') {
                saveCodeToFile();
            } else {
                saveLayersToFile();
            }
            fetchModels()
            isModalAddVisible.value = false
        }

        const addModel = () => {
            // 简单校验逻辑
            if (!form.name) {
                alert("请输入模型名称");
                return;
            }
            if (!form.summary) {
                alert("请输入模型简介");
                return;
            }
            if (!form.environment) {
                alert("请输入运行环境");
                return;
            }
            if (!form.ability) {
                alert("请输入模型能力");
                return;
            }
            if (!form.structureimage) {
                alert("请输入结构图地址");
                return;
            }
            if (!form.code) {
                alert("请输入模型代码");
                return;
            }
            // if (!form.methodSelection) {
            //     alert("请选择模型类别");
            //     return;
            // }
            if (!form.situationSelection) {
                alert("请选择适用场景");
                return;
            }

            // 构建 FormData
            const formData = new FormData();
            formData.append("name", form.name);
            formData.append("summary", form.summary);
            formData.append("environment", form.environment);
            formData.append("ability", form.ability);
            formData.append("structureimage", form.structureimage);
            formData.append("code", form.code);
            if (form.modelPth) {
                formData.append("modelPth", form.modelPth); // 上传文件（可选）
            }
            formData.append("modelstruct", JSON.stringify(newLayers.value));
            formData.append("modelselect", form.methodSelection);
            formData.append("situationselect", form.situationSelection);

            $.ajax({
                url: "http://127.0.0.1:3000/model/add/",
                type: "post",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                processData: false,
                contentType: false,
                data: formData,
                success(resp) {
                    console.log(resp);
                    alert("添加成功!!!")
                    fetchModels();
                },
                error(resp) {
                    console.log(resp);
                }
            });

            form.modelPth = null; // 清空上传文件
            isModalVisible.value = false;
        };


        const updateModel = () => {
            // Define update model logic here
            $.ajax({
                url: "http://127.0.0.1:3000/model/update/",
                type: "post",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                data: {
                    "name": form.name,
                    "summary": form.summary,
                    "environment": form.environment,
                    "ability": form.ability,
                    "structureimage": form.structureimage,
                    "code": form.code
                },
                success(resp) {
                    console.log(resp)
                    alert("更新成功!!!")
                    isModalEditVisible.value = false
                    isEditing.value = false;
                    fetchModels()
                },
                error(resp) {
                    console.log(resp)
                }
            });
        };

        const deleteModel = (model_id) => {
            if (confirm('确定要删除这个模型吗？')) {
                // Define delete model logic here
                $.ajax({
                    url: "http://127.0.0.1:3000/model/remove/",
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
            isEditing.value = false;
            isModalVisible.value = false;
            isModalAddVisible.value = false;    
            isModalEditVisible.value = false;
        };

        const closeModalAdd = () => {
            console.log("关闭添加算法");
            isEditing.value = false;
            isModalAddVisible.value = false;
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

        const handleInferFileChange = (event) => {
            const file = event.target.files[0];
            if (file) {
                const reader = new FileReader();
                
                // 当文件读取完成时触发
                reader.onload = () => {
                    // 将文件的内容存储到 form.params 中
                    form.inferCode = reader.result; // reader.result 是文件的内容
                };

                // 读取文件内容
                reader.readAsText(file); // 读取文件为文本，如果是二进制文件，可以使用 readAsDataURL
            }
        };

        const handleFileChange = (event) => {
            const file = event.target.files[0];
            if (file) {
                const reader = new FileReader();
                
                // 当文件读取完成时触发
                reader.onload = () => {
                    // 将文件的内容存储到 form.params 中
                    form.code = reader.result; // reader.result 是文件的内容
                };

                // 读取文件内容
                reader.readAsText(file); // 读取文件为文本，如果是二进制文件，可以使用 readAsDataURL
            }
        };
        const handleModelPthFileChange = (event) => {
            const file = event.target.files[0];
            if (file) {
                form.modelPth = file; // 直接存储 File 对象
                // const reader = new FileReader();
                
                // // 当文件读取完成时触发
                // reader.onload = () => {
                //     // 将文件的内容存储到 form.params 中
                //     form.modelPth = reader.result; // reader.result 是文件的内容
                // };

                // // 读取文件内容
                // reader.readAsText(file); // 读取文件为文本，如果是二进制文件，可以使用 readAsDataURL
            }
        };
        const handleDataFileChange = (event) => {
            const file = event.target.files[0];
            if (file) {
                const reader = new FileReader();
                
                // 当文件读取完成时触发
                reader.onload = () => {
                    // 将文件的内容存储到 form.params 中
                    form.data = reader.result; // reader.result 是文件的内容
                };

                // 读取文件内容
                reader.readAsText(file); // 读取文件为文本，如果是二进制文件，可以使用 readAsDataURL
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
            methodOptions,
            updateStructureImage,
            situations,
            isModalAddVisible,
            openAddModelAddModal,
            closeModalAdd,
            addModelAdd,
            saveModelAdd,
            handleFileChange,
            isModalEditVisible,
            handleDataFileChange,
            handleInferFileChange,
            handleModelPthFileChange
        };
    }
};
</script>

<style>
    .btn-group .btn {
        margin-right: 10px; /* 设置按钮之间的间距 */
    }
    .btn-group .btn:last-child {
        margin-right: 0; /* 去掉最后一个按钮的右边距 */
    }
</style>
<template>
    <div class="container mt-4">
        <!-- 新增模型按钮 -->
        <div class="d-flex justify-content-between mb-3">
            <div class="input-group" style="width: 300px;">
                <input type="text" class="form-control" placeholder="查找模型..." v-model="searchQuery" @input="filterModels">
                <button class="btn btn-outline-secondary" type="button" @click="resetSearch">重置</button>
            </div>
            <button class="btn btn-primary" @click="openAddModelModal">新增模型</button>
        </div>

        <!-- 模型列表表格 -->
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">模型列表</h5>
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th scope="col">模型名</th>
                            <th scope="col">模型简介</th>
                            <th scope="col">运行环境</th>
                            <th scope="col">模型能力</th>
                            <th scope="col">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="model in paginatedModels" :key="model.id">
                            <td>{{ model.name }}</td>
                            <td>{{ model.summary }}</td>
                            <td>{{ model.environment }}</td>
                            <td>{{ model.ability }}</td>
                            <td>
                                <button class="btn btn-sm btn-secondary" @click="openEditModelModal(model)">编辑</button>
                                <button class="btn btn-sm btn-danger ms-2" @click="deleteModel(model.id)">删除</button>
                                <button class="btn btn-sm btn-info ms-2" @click="visualizeModel(model)">可视化</button>
                            </td>
                        </tr>
                    </tbody>
                </table>

                <!-- 分页控件 -->
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

        <!-- 新增/编辑模型弹窗 -->
        <div v-if="isModalVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="modelModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 v-if="isEditing" class="modal-title" id="modelModalLabel">编辑模型</h5>
                        <h5 v-else class="modal-title" id="modelModalLabel">新增模型</h5>
                        <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form>
                            <div class="mb-3">
                                <label for="name" class="form-label">模型名</label>
                                <input type="text" class="form-control" id="name" v-model="form.name">
                            </div>
                            <div class="mb-3">
                                <label for="summary" class="form-label">模型简介</label>
                                <input type="text" class="form-control" id="summary" v-model="form.summary">
                            </div>
                            <div class="mb-3">
                                <label for="environment" class="form-label">运行环境</label>
                                <input type="text" class="form-control" id="environment" v-model="form.environment">
                            </div>
                            <div class="mb-3">
                                <label for="ability" class="form-label">模型能力</label>
                                <input type="text" class="form-control" id="ability" v-model="form.ability">
                            </div>
                            <div class="mb-3">
                                <label for="structureimage" class="form-label">模型结构图片</label>
                                <input type="file" class="form-control" id="structureimage" @change="handleImageUpload">
                                <div v-if="form.structureimage">
                                    <img :src="form.structureimage" alt="模型结构图片" class="mt-2" style="max-width: 100px; max-height: 100px;">
                                </div>
                            </div>
                            <div class="mb-3">
                                <label for="code" class="form-label">模型代码</label>
                                <VAceEditor
                                    v-model:value="form.code"
                                    lang="python"
                                    theme="textmate"
                                    @init="editorInit"
                                    style="height: 300px; width: 100%;"
                                />
                            </div>

                            <!-- 可视化添加按钮 -->
                            <button type="button" class="btn btn-info" @click="openLayerVisualizationModal">可视化添加</button>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
                        <button v-if="isEditing" type="button" class="btn btn-primary" @click="saveModel">保存更改</button>
                        <button v-else type="button" class="btn btn-primary" @click="saveModel">新增模型</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 层级可视化添加弹窗 -->
        <div v-if="isLayerVisualizationVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="layerVisualizationModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">添加MLP层</h5>
                        <button type="button" class="btn-close" @click="closeLayerVisualization" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form>
                            <div class="mb-3">
                                <label for="layerType" class="form-label">层类型</label>
                                <select class="form-select" id="layerType" v-model="newLayer.type">
                                    <option value="Dense">Dense</option>
                                    <option value="Activation">Activation</option>
                                    <option value="Dropout">Dropout</option>
                                </select>
                            </div>
                            <div class="mb-3" v-if="newLayer.type === 'Dense'">
                                <label for="units" class="form-label">神经元数量</label>
                                <input type="number" class="form-control" id="units" v-model="newLayer.units">
                            </div>
                            <div class="mb-3" v-if="newLayer.type === 'Activation'">
                                <label for="activationFunction" class="form-label">激活函数</label>
                                <input type="text" class="form-control" id="activationFunction" v-model="newLayer.activationFunction">
                            </div>
                            <div class="mb-3" v-if="newLayer.type === 'Dropout'">
                                <label for="dropoutRate" class="form-label">Dropout比率</label>
                                <input type="number" class="form-control" id="dropoutRate" v-model="newLayer.dropoutRate">
                            </div>
                            <button type="button" class="btn btn-success" @click="addLayer">添加层</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
// import store from '@/store';
// import $ from 'jquery';

export default {
    components: {
        VAceEditor
    },
    setup() {
        const form = reactive({
            id: null,
            name: '',
            summary: '',
            environment: '',
            ability: '',
            structureimage: '',
            code: ''
        });

        const isModalVisible = ref(false);
        const isEditing = ref(false);
        const isLayerVisualizationVisible = ref(false);  // 控制层级可视化的显示
        const newLayer = reactive({
            type: 'Dense',  // 默认层类型
            units: 0,
            activationFunction: '',
            dropoutRate: 0
        });

        const closeModal = () => {
            isModalVisible.value = false;
        };

        const openAddModelModal = () => {
            isEditing.value = false;
            isModalVisible.value = true;
        };

        const openLayerVisualizationModal = () => {
            isLayerVisualizationVisible.value = true;
        };

        const closeLayerVisualization = () => {
            isLayerVisualizationVisible.value = false;
        };

        const addLayer = () => {
            console.log('Adding layer:', newLayer);
            // 你可以将层信息存储到模型的属性中（例如：form.layers.push(newLayer)）
            // 例如，保存到模型的代码或其他属性
            // 这里不进行具体保存操作，仅用于演示
            closeLayerVisualization();
        };

        return {
            form,
            isModalVisible,
            isEditing,
            isLayerVisualizationVisible,
            newLayer,
            openAddModelModal,
            closeModal,
            openLayerVisualizationModal,
            closeLayerVisualization,
            addLayer
        };
    }
};
</script>


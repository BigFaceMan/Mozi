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
                            <td>{{ rexample.id }}</td>
                            <td>{{ rexample.projectname }}</td>
                            <td>{{ rexample.examplename }}</td>
                            <td>{{ rexample.createtime }}</td>
                            <td>
                                <div class="btn-group" role="group" aria-label="操作">
                                    <button class="btn btn-outline-info btn-sm" @click="viewRExampleInfo(rexample)">
                                    <i class="bi bi-eye"></i> 查看
                                    </button>
                                    <button v-if="rexample.visible===0" class="btn btn-secondary btn-sm" @click="uploadRExample(rexample)">
                                    <i class="bi bi-eye"></i> 上传
                                    </button>
                                    <button v-if="rexample.visible===2" class="btn btn-outline-warning btn-sm">
                                    <i class="bi bi-eye"></i> 等待审计
                                    </button>
                                    <button v-if="rexample.visible===1" class="btn btn-outline-success btn-sm">
                                    <i class="bi bi-eye"></i> 已上传
                                    </button>
                                    <button class="btn btn-outline-danger btn-sm" @click="deleteRExample(rexample.id)">
                                    <i class="bi bi-trash"></i> 删除
                                    </button>
                                </div>
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
                    <div v-if="step === 1">                 
                        <!-- 美观的项目名称输入框 -->
                        <div class="form-group mt-3 text-center">
                            <label for="projectName" class="font-weight-bold text-primary">名称：</label>
                            <input type="text" id="projectName" v-model="projectName" 
                                class="form-control stylish-input mx-auto" 
                                placeholder="请输入项目名称">
                        </div>

                        <div class="row mt-5">
                            <!-- 第一列：想定列表 -->
                            <div class="col-md-4">
                                <div class="p-3">
                                    <div class="input-group stylish-search">
                                        <!-- <span class="input-group-text"><i class="fas fa-search"></i></span> -->
                                        <input v-model="searchQuerySituation" class="form-control" placeholder="搜索想定..." />
                                    </div>
                                </div>
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
                                                    @click="selectSituation(situation.id, situation.taskName)">
                                                    <td>{{ situation.id }}</td>
                                                    <td>{{ situation.taskName }}</td>
                                                </tr>
                                            </tbody>
                                        </table>
                                        <div class="d-flex justify-content-center mt-3" style="margin-bottom: 5%">
                                            <button class="btn btn-secondary" :disabled="currentPageSituation === 1" @click="currentPageSituation--">‹ 上一页</button>
                                            <span class="mx-3">第 {{ currentPageSituation }} / {{ totalPagesSituation }} 页</span>
                                            <button class="btn btn-secondary" :disabled="currentPageSituation === totalPagesSituation" @click="currentPageSituation++">下一页 ›</button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- 第二列：方案列表 -->
                            <div class="col-md-4" v-if="selectedSituationId">
                                <div class="p-3">
                                    <div class="input-group stylish-search">
                                        <!-- <span class="input-group-text"><i class="fas fa-search"></i></span> -->
                                        <input v-model="searchQuerySolution" class="form-control" placeholder="搜索方案..." />
                                    </div>
                                </div>
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
                                                    @click="selectSolution(solution.id, solution.missionName)">
                                                    <td>{{ solution.id }}</td>
                                                    <td>{{ solution.missionName }}</td>
                                                </tr>
                                            </tbody>
                                        </table>
                                        <div class="d-flex justify-content-center mt-3" style="margin-bottom: 5%">
                                            <button class="btn btn-secondary" :disabled="currentPageSolution === 1" @click="currentPageSolution--">‹ 上一页</button>
                                            <span class="mx-3">第 {{ currentPageSolution }} / {{ totalPagesSolution}}页</span>
                                            <button class="btn btn-secondary" :disabled="currentPageSolution === totalPagesSolution" @click="currentPageSolution++">下一页 ›</button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- 第三列：实例列表 -->
                            <div class="col-md-4" v-if="selectedSolutionId">
                                <div class="p-3">
                                    <div class="input-group stylish-search">
                                        <!-- <span class="input-group-text"><i class="fas fa-search"></i></span> -->
                                        <input v-model="searchQueryExample" class="form-control" placeholder="搜索实例..." />
                                    </div>
                                </div>
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
                                            <span class="mx-3">第 {{ currentPageExample }} / {{ totalPagesExample }}页</span>
                                            <button class="btn btn-secondary" :disabled="currentPageExample === totalPagesExample" @click="currentPageExample++">下一页 ›</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <!-- 下一步 -->
                        <div class="text-center mt-4" v-if="selectedExampleId">
                            <button class="btn btn-lg btn-primary" @click="step = 2">➡ 下一步</button>
                        </div>
                    </div>
                    <div v-if="step===2">
                        <div class="row mt-5">
                            <!--阵营 -->
                            <div class="col-md-4">
                                <div class="card">
                                    <div class="card-header bg-primary text-white">
                                        <h5 class="mb-0">选择阵营</h5>
                                    </div>
                                    <div class="table-responsive">
                                        <table class="table table-hover">
                                            <thead>
                                                <tr>
                                                    <th>阵营名</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <tr v-for="country in pagedCountry" :key="country.country"
                                                    :class="{ 'table-active': selectedCountryName === country.country }"
                                                    @click="selectCountry(country)">
                                                    <td>{{ country.country }}</td>
                                                </tr>
                                            </tbody>
                                        </table>
                                        <div class="d-flex justify-content-center mt-3" style="margin-bottom: 5%">
                                            <button class="btn btn-secondary" :disabled="currentPageCountry === 1" @click="currentPageCountry--">‹ 上一页</button>
                                            <span class="mx-3">第 {{ currentPageCountry }} / {{ totalPagesCountry }} 页</span>
                                            <button class="btn btn-secondary" :disabled="currentPageCountry === totalPagesCountry" @click="currentPageSituation++">下一页 ›</button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- 编组 -->
                        <div class="col-md-4" v-if="selectedCountryName">
                                <div class="card">
                                    <div class="card-header bg-success text-white d-flex justify-content-between align-items-center">
                                        <h5 class="mb-0">选择编组</h5>
                                    </div>
                                    <div class="table-responsive">
                                        <table class="table table-hover">
                                            <thead>
                                                <tr>
                                                    <th>编组名</th>
                                                    <th>操作</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <tr v-for="group in pagedGroup" :key="group.id">
                                                    <td style="cursor: pointer;">{{ group.unitGroupName }}</td>
                                                    <td class="d-flex justify-content-center align-items-center gap-2">
                                                        <button class="btn stylish-btn btn-success" @click="selectGroup(group)">
                                                            <i class="fas fa-check-circle"></i> 选择
                                                        </button>
                                                        <button class="btn stylish-btn btn-info" @click="chooseGroup(group)">
                                                            <i class="fas fa-angle-double-right"></i> 展开
                                                        </button>
                                                        <button class="btn stylish-btn btn-warning" @click="goBackGroup()" :disabled="groupStack.length <= 1">
                                                            <i class="fas fa-arrow-left"></i> 返回
                                                        </button>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                        <div class="d-flex justify-content-center mt-3" style="margin-bottom: 5%">
                                            <button class="btn btn-secondary" :disabled="currentPageGroup === 1" @click="currentPageGroup--">‹ 上一页</button>
                                            <span class="mx-3">第 {{ currentPageGroup }} / {{ totalPagesGroup }} 页</span>
                                            <button class="btn btn-secondary" :disabled="currentPageGroup === totalPagesGroup" @click="currentPageGroup++">下一页 ›</button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- 实体 -->
                            <div class="col-md-4" v-if="selectedSolutionId">
                                <div class="card">
                                    <div class="card-header bg-warning text-dark">
                                        <h5 class="mb-0">选择实体</h5>
                                    </div>
                                    <div class="table-responsive">
                                        <table class="table table-hover">
                                            <thead>
                                                <tr>
                                                    <th>类型</th>
                                                    <th>实体名</th>
                                                    <th>选择</th>
                                                    <th> 全选: 
                                                        <input type="checkbox" @change="toggleAllEntities" :checked="isAllSelected">
                                                    </th>
                                                </tr>
                                            </thead>
                                                <tbody>
                                                <tr v-for="entity in pagedEntity" :key="entity.id">
                                                    <td>{{ entity.type }}</td>
                                                    <td>{{ entity.scenarioModelName }}</td>
                                                    <td class="text-center">
                                                    <input
                                                        type="checkbox"
                                                        :value="{
                                                        groupId: selectedGroupId,
                                                        groupName: selectedGroupName,
                                                        entityId: entity.id,
                                                        entityName: entity.scenarioModelName
                                                        }"
                                                        v-model="selectedEntities"
                                                    />
                                                    </td>
                                                </tr>
                                                </tbody>

                                        </table>

                                        <div class="d-flex justify-content-center mt-3" style="margin-bottom: 5%">
                                            <button class="btn btn-secondary" :disabled="currentPageEntity === 1" @click="currentPageEntity--">‹ 上一页</button>
                                            <span class="mx-3">第 {{ currentPageEntity }} / {{ totalPagesEntity }} 页</span>
                                            <button class="btn btn-secondary" :disabled="currentPageEntity === totalPagesEntity" @click="currentPageEntity++">下一页 ›</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="text-center mt-4">
                            <button class="btn btn-lg btn-outline-secondary shadow rounded-pill mx-3 px-4 py-2" 
                                    @click="step = 1">
                                ⬅ 上一步
                            </button>
                            <button class="btn btn-lg btn-success shadow rounded-pill mx-3 px-4 py-2" :disabled="selectedEntities.length === 0"
                                    @click="submitSelection()">
                                ✅ 确定选择
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            <div v-if="showDetail" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="exampleModalLabel" aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <SceneView :rexample="selectedRexample" @close="showDetail = false"/>
                    </div>
                </div>
            </div>
        </div>
    </ContentField>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import ContentField from '../../components/ContentField.vue';
import SceneView from '../../components/SceneView.vue';
import $ from "jquery";
import store from "../../store";

// 数据存储
const examples = ref([]);
const step = ref(1);
const RExamples = ref([]);
const situations = ref([]);
const solutions = ref([]);
const countrys = ref([]);
const groups = ref([])
const entitys = ref([]);
const projectName = ref(null);
const showDetail = ref(false);
const selectedRexample = ref();

const selectedEntities = ref([]); // 存储勾选的实体
const selectedSituationId = ref(null);
const selectedSituationName = ref(null);
const selectedGroupId = ref(null)
const selectedGroupName = ref(null)
const selectedSolutionId = ref(null);
const selectedSolutionName = ref(null);
const selectedExampleId = ref(null);
const selectedExampleName = ref(null);
const selectedCountryName = ref(null);
const showCreateModal = ref(false);
const searchQuery = ref(""); // 搜索框的输入
const searchQuerySituation = ref(""); // 搜索框的输入
const searchQuerySolution = ref(""); // 搜索框的输入
const searchQueryExample = ref(""); // 搜索框的输入

// 编组栈
const groupStack = ref([0]);


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

const filteredSituations = computed(() => {
    return situations.value.filter(s => 
        s.taskName.includes(searchQuerySituation.value) || 
        s.id.toString().includes(searchQuerySituation.value)
    );
});
const pagedSituations = computed(() => {
    const start = (currentPageSituation.value - 1) * pageSizeSituation;
    const end = currentPageSituation.value * pageSizeSituation;
    return filteredSituations.value.slice(start, end);
});
const totalPagesSituation = computed(() => {
    return Math.ceil(filteredSituations.value.length / pageSizeSituation);
});

// Solution
const currentPageSolution = ref(1);
const pageSizeSolution = 5; // 每页显示10条数据

const filteredSolution = computed(() => {
    return solutions.value.filter(s => 
        s.missionName.includes(searchQuerySolution.value) || 
        s.id.toString().includes(searchQuerySolution.value)
    );
});
const pagedSolution = computed(() => {
    const start = (currentPageSolution.value - 1) * pageSizeSolution;
    const end = currentPageSolution.value * pageSizeSolution;
    return filteredSolution.value.slice(start, end);
});
const totalPagesSolution = computed(() => {
    return Math.ceil(filteredSolution.value.length / pageSizeSolution);
});


//  Example
// 当前第几页
const currentPageExample = ref(1);
// 每页多少个数据
const pageSizeExample = 5; // 每页显示10条数据
const filteredExample = computed(() => {
    return examples.value.filter(s => 
        s.name.includes(searchQueryExample.value) || 
        s.id.toString().includes(searchQueryExample.value)
    );
});
// 当前页所有的数据
const pagedExamples = computed(() => {
    const start = (currentPageExample.value - 1) * pageSizeExample;
    const end = currentPageExample.value * pageSizeExample;
    return filteredExample.value.slice(start, end);
});
// 总共多少页
const totalPagesExample = computed(() => {
    return Math.ceil(filteredExample.value.length / pageSizeExample);
});

// Country
const currentPageCountry = ref(1);
// 每页多少个数据
const pageSizeCountry = 5; // 每页显示10条数据
// 当前页所有的数据
const pagedCountry = computed(() => {
    const start = (currentPageCountry.value - 1) * pageSizeCountry;
    const end = currentPageCountry.value * pageSizeCountry;
    return countrys.value.slice(start, end);
});
// 总共多少页
const totalPagesCountry = computed(() => {
    return Math.ceil(countrys.value.length / pageSizeCountry);
});

// Group
const currentPageGroup = ref(1);
// 每页多少个数据
const pageSizeGroup = 5; // 每页显示10条数据
// 当前页所有的数据
const pagedGroup = computed(() => {
    const start = (currentPageGroup.value - 1) * pageSizeGroup;
    const end = currentPageGroup.value * pageSizeGroup;
    return groups.value.slice(start, end);
});
// 总共多少页
const totalPagesGroup = computed(() => {
    return Math.ceil(groups.value.length / pageSizeGroup);
});

// Group
const currentPageEntity = ref(1);
// 每页多少个数据
const pageSizeEntity = 5; // 每页显示10条数据
// 当前页所有的数据
const pagedEntity = computed(() => {
    const start = (currentPageEntity.value - 1) * pageSizeEntity;
    const end = currentPageEntity.value * pageSizeEntity;
    return entitys.value.slice(start, end);
});
// 总共多少页
const totalPagesEntity = computed(() => {
    return Math.ceil(entitys.value.length / pageSizeEntity);
});

const isAllSelected = computed(() => {
    return entitys.value.length > 0 && 
        entitys.value.every(entity => 
            selectedEntities.value.some(selected => selected.entityId === entity.id)
        );
});
const viewRExampleInfo = (rexample) => {
    selectedRexample.value = rexample;
    // console.log("select rexample : ", selectedRexample.value)
    showDetail.value = true;
}
const toggleAllEntities = () => {
    if (isAllSelected.value) {
        // 取消全选：移除当前页的所有实体
        selectedEntities.value = selectedEntities.value.filter(
            selected => !entitys.value.some(entity => entity.id === selected.entityId)
        );
    } else {
        // 全选：添加当前页所有实体
        const newSelections = entitys.value.map(entity => ({
            groupId: selectedGroupId.value,
            groupName: selectedGroupName.value,
            entityId: entity.id,
            entityName: entity.scenarioModelName
        }));
        selectedEntities.value = [...selectedEntities.value, ...newSelections.filter(newEntity =>
            !selectedEntities.value.some(selected => selected.entityId === newEntity.entityId)
        )];
    }
};
const uploadRExample = (rexample) => {
    console.log(rexample)
    $.ajax({
        url: "http://127.0.0.1:3000/remote/uploadRExample/",
        type: "POST",
        headers: { Authorization: "Bearer " + store.state.user.token },
        data: {
            projectId: rexample.id
        },
        success(resp) {
            console.log("上传成功")
            console.log(resp)
            fetchRExamples();
        },
        error(resp) {
            console.log("上传失败")
            console.log(resp)
        }
    })
}
const fetchCountry = (situationId) => {
    $.ajax({
        url: "http://127.0.0.1:3000/remote/getCountry/",
        type: "POST",
        data: {
            situationId: situationId
        },
        success(resp) {
            countrys.value = resp.data;
            console.log(resp);
        },
        error(resp) {
            console.error("获取 country 失败:", resp);
        }
    });
};

// 获取 RExample 列表
const fetchRExamples = () => {
    $.ajax({
        url: "http://127.0.0.1:3000/remote/getRExamples/",
        type: "POST",
        headers: { Authorization: "Bearer " + store.state.user.token },
        success(resp) {
            if (resp.data) {
                RExamples.value = resp.data;
                console.log(resp);
            } else {
                console.log("no RExamples data!")
            }
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
        url: "http://127.0.0.1:3000/remote/deleteRExample/",
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
        url: "http://127.0.0.1:3000/remote/getSituations/",
        type: "post",
        success(resp) {
            if (resp.code === 200) {
                situations.value = resp.data;
                currentPageSituation.value = 1;
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
const selectGroup = (group) => {
    console.log("select : ", group)
    selectedGroupId.value = group.id
    selectedGroupName.value = group.unitGroupName
    $.ajax({
        url: "http://127.0.0.1:3000/remote/getEntity/",
        type: "post",
        data: {
            situationId: selectedSituationId.value,
            groupId: group.id,
            country: selectedCountryName.value,
        },
        success(resp) {
            if (resp.code === 200) {
                entitys.value = resp.data.scenarioModel;
                currentPageEntity.value = 1;
                console.log(entitys.value);
            }
        },
        error(resp) {
            console.error("获取实体失败:", resp);
        }
    });
}
const chooseGroup = (group) => {
    console.log("child : ", group.childrenEdit)
    if (group.childrenEdit === false) {
        return ;
    }
    console.log(group.id)
    groupStack.value.push(group.id)
    $.ajax({
        url: "http://127.0.0.1:3000/remote/getGroup/",
        type: "post",
        data: {
            situationId: selectedSituationId.value,
            pid: group.id,
            country: selectedCountryName.value,
        },
        success(resp) {
            if (resp.code === 200) {
                if (resp.data.length != 0) {
                    groups.value = resp.data;
                    currentPageGroup.value = 1;
                    console("update group !!!!")
                    console.log(groups.value);
                } else {
                    console.log("return chosse groud len 0 !!")
                }
            }
        },
        error(resp) {
            console.error("获取编组失败:", resp);
        }
    });
}
const goBackGroup = () => {
    groupStack.value.pop()
    const currentGId = groupStack.value[groupStack.value.length - 1]
    $.ajax({
        url: "http://127.0.0.1:3000/remote/getGroup/",
        type: "post",
        data: {
            situationId: selectedSituationId.value,
            pid: currentGId,
            country: selectedCountryName.value,
        },
        success(resp) {
            if (resp.code === 200) {
                groups.value = resp.data;
                currentPageGroup.value = 1;
                console.log(groups.value);
            }
        },
        error(resp) {
            console.error("获取编组失败:", resp);
        }
    });
}

// 选择阵营，提取对应的编组
const selectCountry = (country) => {
    selectedCountryName.value = country.country;
    groups.value = null
    selectedGroupId.value = null; // 清空方案
    groupStack.value = [0]
    console.log("select country : ", country)
    console.log("query group id : ", groupStack.value[0]);
    const lastGroupId = groupStack.value.length > 0 ? groupStack.value[groupStack.value.length - 1] : null;
    // console.log("query group id : ", lastGroupId);

    $.ajax({
        url: "http://127.0.0.1:3000/remote/getGroup/",
        type: "post",
        data: {
            situationId: selectedSituationId.value,
            pid: lastGroupId,
            country: selectedCountryName.value,
        },
        success(resp) {
            if (resp.code === 200) {
                groups.value = resp.data;
                console.log(groups.value);
            }
        },
        error(resp) {
            console.error("获取编组失败:", resp);
        }
    });
};
// 选择想定，获取方案
const selectSituation = (situationId, situationName) => {
    selectedSituationId.value = situationId;
    selectedSituationName.value = situationName
    selectedSolutionId.value = null; // 清空方案
    selectedExampleId.value = null; // 清空实例
    solutions.value = [];
    examples.value = [];

    $.ajax({
        url: `http://127.0.0.1:3000/remote/getSolutions/?id=${situationId}`,
        type: "post",
        success(resp) {
            if (resp.code === 200) {
                solutions.value = resp.data;
                currentPageSolution.value = 1;
            }
        },
        error(resp) {
            console.error("获取方案失败:", resp);
        }
    });
};

// 选择方案，获取实例
const selectSolution = (solutionId, solutionName) => {
    selectedSolutionId.value = solutionId;
    selectedSolutionName.value = solutionName;
    selectedExampleId.value = null; // 清空实例
    examples.value = [];

    $.ajax({
        url: `http://127.0.0.1:3000/remote/getExamples/?id=${solutionId}`,
        type: "post",
        success(resp) {
            if (resp.code === 200) {
                examples.value = resp.data;
                currentPageExample.value = 1;
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
    console.log("select Example : ", selectedExampleId.value)
    console.log("select Example situation id : ", selectedSituationId.value)
    fetchCountry(selectedSituationId.value)
};

// 提交最终选择的想定、方案、实例
const submitSelection = () => {
    if (!selectedSituationId.value || !selectedSolutionId.value || !selectedExampleId.value || !projectName.value) {
        alert("请输入项目名、请完整选择想定、方案和实例");
        showCreateModal.value = false;
        return;
    }
    console.log("select entity : ", selectedEntities.value)

    $.ajax({
        url: "http://127.0.0.1:3000/remote/saveRExample/",
        type: "POST",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            situationId: selectedSituationId.value,
            situationName: selectedSituationName.value,
            solutionId: selectedSolutionId.value,
            solutionName: selectedSolutionName.value,
            exampleId: selectedExampleId.value,
            exampleName: selectedExampleName.value,
            projectName: projectName.value,
            selectCountry: selectedCountryName.value,
            selectedEntities: JSON.stringify(selectedEntities.value)
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
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow-y: auto;
}

.modal-content {
    position: relative;
    width: 90%;
    max-width: 1200px;
    min-width: 800px;
    background: white;
    border-radius: 10px;
    padding: 20px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    overflow-x: auto;
}

.table th, .table td {
    min-width: 120px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    padding: 10px;
}

.table-hover tbody tr:hover td {
    white-space: normal;
    overflow: visible;
}

@media (max-width: 1200px) {
    .modal-content {
        width: 95%;
        max-width: none;
    }
    
    .table th, .table td {
        padding: 8px;
        font-size: 14px;
    }
}

@media (max-width: 768px) {
    .modal-content {
        width: 100%;
    }
    
    .table {
        font-size: 12px;
    }
}
</style>

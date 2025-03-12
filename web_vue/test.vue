<template>

<div v-if="showCreateModal" class="modal-overlay">
    <div class="modal-content">
        <button class="close-btn" @click="showCreateModal = false">✖</button>

        <!-- 步骤 1: 选择 想定 -> 方案 -> 实例 -->
        <div v-if="step === 1">
            <div class="form-group mt-3 text-center">
                <label for="projectName" class="font-weight-bold text-primary">名称：</label>
                <input type="text" id="projectName" v-model="projectName" 
                    class="form-control stylish-input mx-auto" 
                    placeholder="请输入项目名称">
            </div>

            <div class="row mt-5">
                <!-- 想定列表 -->
                <div class="col-md-4">
                    <div class="card">
                        <div class="card-header bg-primary text-white">
                            <h5 class="mb-0">选择想定</h5>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead><tr><th>ID</th><th>想定名</th></tr></thead>
                                <tbody>
                                    <tr v-for="situation in pagedSituations" :key="situation.id"
                                        :class="{ 'table-active': selectedSituationId === situation.id }"
                                        @click="selectSituation(situation.id)">
                                        <td>{{ situation.id }}</td>
                                        <td>{{ situation.taskName }}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- 方案列表 -->
                <div class="col-md-4" v-if="selectedSituationId">
                    <div class="card">
                        <div class="card-header bg-success text-white">
                            <h5 class="mb-0">选择方案</h5>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead><tr><th>ID</th><th>方案名</th></tr></thead>
                                <tbody>
                                    <tr v-for="solution in pagedSolution" :key="solution.id"
                                        :class="{ 'table-active': selectedSolutionId === solution.id }"
                                        @click="selectSolution(solution.id)">
                                        <td>{{ solution.id }}</td>
                                        <td>{{ solution.missionName }}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- 实例列表 -->
                <div class="col-md-4" v-if="selectedSolutionId">
                    <div class="card">
                        <div class="card-header bg-warning text-dark">
                            <h5 class="mb-0">选择实例</h5>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead><tr><th>ID</th><th>实例名</th></tr></thead>
                                <tbody>
                                    <tr v-for="example in pagedExamples" :key="example.id"
                                        :class="{ 'table-active': selectedExampleId === example.id }"
                                        @click="selectExample(example)">
                                        <td>{{ example.id }}</td>
                                        <td>{{ example.name }}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 下一步 -->
            <div class="text-center mt-4" v-if="selectedExampleId">
                <button class="btn btn-lg btn-primary" @click="step = 2">➡ 下一步</button>
            </div>
        </div>

        <!-- 步骤 2: 选择 国家 -> 编组 -> 实体 -->
        <div v-if="step === 2">
            <div class="row mt-5">
                <!-- 国家列表 -->
                <div class="col-md-4">
                    <div class="card">
                        <div class="card-header bg-info text-white">
                            <h5 class="mb-0">选择国家</h5>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead><tr><th>国家</th></tr></thead>
                                <tbody>
                                    <tr v-for="country in countryList" :key="country"
                                        :class="{ 'table-active': selectedCountry === country }"
                                        @click="selectCountry(country)">
                                        <td>{{ country }}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- 编组列表 -->
                <div class="col-md-4" v-if="selectedCountry">
                    <div class="card">
                        <div class="card-header bg-secondary text-white">
                            <h5 class="mb-0">选择编组</h5>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead><tr><th>ID</th><th>编组名</th></tr></thead>
                                <tbody>
                                    <tr v-for="group in groupList" :key="group.id"
                                        :class="{ 'table-active': selectedGroupId === group.id }"
                                        @click="selectGroup(group.id)">
                                        <td>{{ group.id }}</td>
                                        <td>{{ group.name }}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- 实体列表 -->
                <div class="col-md-4" v-if="selectedGroupId">
                    <div class="card">
                        <div class="card-header bg-dark text-white">
                            <h5 class="mb-0">选择实体</h5>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead><tr><th>ID</th><th>实体名</th></tr></thead>
                                <tbody>
                                    <tr v-for="entity in entityList" :key="entity.id"
                                        :class="{ 'table-active': selectedEntityId === entity.id }"
                                        @click="selectEntity(entity.id)">
                                        <td>{{ entity.id }}</td>
                                        <td>{{ entity.name }}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 上一步 & 确认 -->
            <div class="text-center mt-4">
                <button class="btn btn-lg btn-secondary" @click="step = 1">⬅ 上一步</button>
                <button class="btn btn-lg btn-success" v-if="selectedEntityId" @click="submitSelection">✅ 确定选择</button>
            </div>
        </div>
    </div>
</div>
</template>
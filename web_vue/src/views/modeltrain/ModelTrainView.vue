<!--
 * @Author: ssp
 * @Date: 2024-11-12 11:01:55
 * @LastEditTime: 2024-12-13 12:08:20
-->
<template>
        <div class="d-flex">
            <!-- 左侧侧边栏 -->
            <div class="sidebar p-3">
                <div class="menu-background">
                    <button @click="right_states = 'parameters'" class="btn btn-primary custom-btn">创建模型</button>
                    <button @click="right_states = 'analysis'" class="btn btn-primary custom-btn">模型列表</button>
                </div>
            </div>

            <!-- 右侧内容区 -->
            <div class="content flex-grow-1 p-3" style="margin-top: 10px; margin-right: 10px" >
                <!-- 根据 right_states 的值显示不同的组件 -->
                <ModelParameters v-if="right_states === 'parameters'" />
                <ModelAnalysis v-if="right_states === 'analysis'" />
            </div>
        </div>
</template>

<script>
import ModelParameters from '../../components/ModelParameters.vue'
import ModelAnalysis from '../../components/ModelAnalysis.vue'
import { ref } from 'vue';

export default {
    components: {
        ModelParameters,
        ModelAnalysis
    },
    // data() {
    //     return {
    //         right_states: 'parameters' // 默认显示 "参数配置" 页面
    //     }
    // }
    setup() {
        const right_states = ref('parameters') 
        // const form_parameter = reactive({
        //                 trainingName: 'CNN训练',
        //                 model: 'CNN',
        //                 pytorchVersion: '1.8',
        //                 modelParams: '',
        //             });
        return {
            right_states,
            // form_parameter,
        }
    }
}
</script>

<style scoped>
.sidebar {
  width: 180px;
  background-color: transparent;
}

.custom-btn {
  width: 90%;
  margin: 5px 0 5px 5px;
  font-size: 14px;
  padding: 5px 10px;
}

.custom-btn {
  width: 90%;
  margin: 5px 0 5px 5px;
  font-size: 14px;
  padding: 5px 10px;
  background-color: #c0c0c0; /* Silver color */
  border: 1px solid #ccc; /* Light border to match the silver look */
  color: #333; /* Dark text for contrast */
}

.custom-btn:hover {
  background-color: #b3b3b3; /* Slightly darker gray on hover */
}

.content {
  background-color: rgba(248, 249, 250, 0.8);
  border-radius: 8px;
}
</style>

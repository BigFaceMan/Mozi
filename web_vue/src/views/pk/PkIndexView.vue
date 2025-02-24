<!--
 * @Author: ssp
 * @Date: 2024-11-12 11:01:55
 * @LastEditTime: 2024-12-08 16:42:59
-->
<template>
        <div class="d-flex">
            <!-- 左侧侧边栏 -->
            <div class="sidebar p-3">
                <div class="menu-background">
                    <button @click="right_states = 'parameters'" class="btn btn-primary custom-btn">参数配置</button>
                    <button @click="right_states = 'training'" class="btn btn-primary custom-btn">推理结果</button>
                </div>
            </div>

            <!-- 右侧内容区 -->
            <div class="content flex-grow-1 p-3" style="margin-top: 10px; margin-right: 10px" >
                <!-- 根据 right_states 的值显示不同的组件 -->
                <ModelParameters v-if="right_states === 'parameters'" />
                <ModelTraining v-if="right_states === 'training'" />
            </div>
        </div>
</template>

<script>
import ModelParameters from '../../components/PkModelParameters.vue'
import ModelTraining from '../../components/PkModelAnalysis.vue'
import { ref } from 'vue';

export default {
    components: {
        ModelParameters,
        ModelTraining,
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

<template>
  <ContentField>
    <div class="container text-center">
      <h2 class="text-primary mb-4">对战调度</h2>
      <div class="row">
        <!-- Status Info Card -->
        <div class="col-md-4">
          <div class="card mb-4 shadow-sm">
            <div class="card-header bg-dark text-white">
              <h5 class="mb-0">态势信息</h5>
            </div>
            <div class="card-body">
              <p class="card-text">
                支持对接收的合法战场态势数据结构进行特征提取和整合，生成符合要求的态势数据。
                支持将处理后的信息转化为强化学习智能体的输入，确保智能体能够基于实时态势数据进行有效决策。
              </p>
              <div class="mt-4">
                <button class="btn btn-dark" @click="navigateToModelTrain">进入</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Action Command Card -->
        <div class="col-md-4">
          <div class="card mb-4 shadow-sm">
            <div class="card-header bg-dark text-white">
              <h5 class="mb-0">动作指令</h5>
            </div>
            <div class="card-body">
              <p class="card-text">
                支持将强化学习智能体输出的高层次动作指令转换为仿真环境可执行的具体操作函数。
                该功能确保智能体动作的准确性和执行的有效性，实现智能体与仿真环境之间的无缝衔接。
              </p>
              <div class="mt-4">
                <button class="btn btn-dark" @click="navigateToModelTrain">转换</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Interface Call Card -->
        <div class="col-md-4">
          <div class="card mb-4 shadow-sm">
            <div class="card-header bg-dark text-white">
              <h5 class="mb-0">接口调用</h5>
            </div>
            <div class="card-body">
              <p class="card-text">
                提供远程调用接口，支持智能体、环境、仿真平台等之间的跨系统调用。
                接口支持 gRPC 等多种方式，记录所有通信调用的日志信息，支持实时监控和日志回放。
              </p>
              <div class="mt-4">
                <button class="btn btn-dark" @click="navigateToModelTrain">接收</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </ContentField>
</template>

<script>
import ContentField from '../../components/ContentField.vue';
import { useRouter } from 'vue-router';

export default {
  components: {
    ContentField
  },
  setup() {
    const router = useRouter();

    const navigateToModelTrain = () => {
      router.push({ path: "/modeltrain/" });
    };

    return {
      navigateToModelTrain
    };
  }
};
</script>

<style scoped>
.container {
  padding-top: 2rem;
}
.card-header {
  font-weight: bold;
  font-size: 1.2rem;
}
.card-body {
  font-size: 0.9rem;
}
.btn {
  width: 80px;
}
</style>

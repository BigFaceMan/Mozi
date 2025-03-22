<template>
  <div class="container mt-5">
    <h2 class="text-center mb-5 text-primary">数据分析回归任务</h2>

    <div class="card shadow-lg mb-5 border-0">
      <div class="card-body">
        <form @submit.prevent="startPrediction">
          <div class="row">
            <!-- 输入文件 -->
            <div class="col-md-4 mb-4">
              <label for="inputFile" class="form-label">选择输入文件</label>
              <input type="file" class="form-control" id="inputFile" @change="handleFileChange('input', $event)" required>
            </div>

            <!-- 输出文件 -->
            <div class="col-md-4 mb-4">
              <label for="outputFile" class="form-label">选择输出文件</label>
              <input type="file" class="form-control" id="outputFile" @change="handleFileChange('output', $event)" required>
            </div>

            <!-- 预测文件 -->
            <div class="col-md-4 mb-4">
              <label for="predictFile" class="form-label">选择预测文件</label>
              <input type="file" class="form-control" id="predictFile" @change="handleFileChange('predict', $event)" required>
            </div>
          </div>

          <div class="mb-4">
            <label for="modelSelect" class="form-label">选择回归模型</label>
            <select class="form-select form-select-lg" id="modelSelect" v-model="selectedModel" required>
              <option value="linear">线性回归</option>
              <option value="svm">SVM</option>
              <option value="Logistic">Logistic Regression</option>
              <option value="Network">Neural Network</option>
              <option value="CNN">CNN</option>
            </select>
          </div>

          <!-- 开始预测按钮 -->
          <div class="d-flex justify-content-center">
            <button class="btn btn-primary btn-lg px-5 py-3" type="submit">开始预测</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 显示预测结果 -->
    <div v-if="predictionResult" class="alert alert-info mt-5 shadow-sm">
      <h4 class="alert-heading text-center">预测结果</h4>
      <pre class="text-center">{{ predictionResult }}</pre>
    </div>

    <!-- 显示加载状态 -->
    <div v-if="loading" class="text-center mt-4">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">加载中...</span>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import $ from "jquery";

export default {
  setup() {
    const inputFile = ref(null);
    const outputFile = ref(null);
    const predictFile = ref(null);
    const selectedModel = ref('linear');
    const predictionResult = ref(null);
    const loading = ref(false);

    const handleFileChange = (type, event) => {
      if (type === 'input') {
        inputFile.value = event.target.files[0];
      } else if (type === 'output') {
        outputFile.value = event.target.files[0];
      } else if (type === 'predict') {
        predictFile.value = event.target.files[0];
      }
    };
    const startPrediction = () => {
      if (!inputFile.value || !outputFile.value || !predictFile.value) {
        alert("请先选择所有文件！");
        return;
      }

      console.log("here!!!!!!!!!!")
      loading.value = true; // 显示加载状态

      const formData = new FormData();
      formData.append("inputFile", inputFile.value);
      formData.append("outputFile", outputFile.value);
      formData.append("predictFile", predictFile.value);
      formData.append("model", selectedModel.value);

      setTimeout(() => {
        $.ajax({
          url: "http://127.0.0.1:3000/regress/",
          type: "POST", 
          data: formData,
          processData: false, // 不要处理 FormData
          contentType: false, // 让浏览器自动设置 `multipart/form-data`
          success(resp) {
            console.log("上传成功", resp);
            predictionResult.value = resp; // 显示预测结果
            loading.value = false;
          },
          error(err) {
            console.error("上传失败", err);
            alert("预测失败！");
            loading.value = false;
          }
        });
      }, 2000); // 模拟延迟
    };

    // const startPrediction = () => {
    //   loading.value = true; // 显示加载状态


    //   setTimeout(() => {
    //     // 模拟返回假数据
    //     predictionResult.value = {
    //       model: selectedModel.value,
    //       predictions: [1.23, 2.34, 3.45, 4.56, 5.67], // 假数据
    
    //     };
    //     loading.value = false; // 隐藏加载状态
    //   }, 2000); // 模拟延迟
    // };

    return {
      inputFile,
      outputFile,
      predictFile,
      selectedModel,
      predictionResult,
      loading,
      handleFileChange,
      startPrediction,
    };
  },
};
</script>

<style scoped>
.container {
  max-width: 1100px;
  padding: 30px;
  background-color: #f8f9fa;
  border-radius: 15px;
}

h2 {
  font-size: 2rem;
  font-weight: bold;
}

.card {
  border-radius: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-body {
  background-color: #fff;
  padding: 30px;
}

.form-label {
  font-size: 1.1rem;
  font-weight: 500;
}

.form-select-lg {
  font-size: 1rem;
}

input[type="file"] {
  border: 2px solid #ddd;
  border-radius: 5px;
}

button {
  font-size: 1.2rem;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s ease;
}

button:hover {
  background-color: #0056b3;
  transform: scale(1.05);
}

.spinner-border {
  width: 3rem;
  height: 3rem;
}

.alert {
  border-radius: 10px;
  padding: 20px;
  font-size: 1.1rem;
  background-color: #e7f5ff;
}

.alert-heading {
  font-size: 1.5rem;
  font-weight: 600;
  color: #0d6efd;
}

.pre {
  white-space: pre-wrap;
  word-wrap: break-word;
}

@media (max-width: 768px) {
  .container {
    padding: 20px;
  }

  .row {
    margin-bottom: 20px;
  }

  .btn-lg {
    width: 100%;
  }
}
</style>

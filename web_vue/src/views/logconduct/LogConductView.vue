<template>
    <div class="container mt-4">
      <!-- Search and Add Model Button -->
      <div class="d-flex justify-content-between mb-4">
        <div class="input-group" style="width: 350px;">
          <input type="text" class="form-control" placeholder="查找日志..." v-model="searchQuery" @input="filterLogs">
          <button class="btn btn-outline-secondary" type="button" @click="resetSearch">重置</button>
        </div>
        <button class="btn btn-primary" @click="openAddLogModal">
          <i class="bi bi-plus-circle"></i> 新增日志
        </button>
      </div>
  
      <!-- Log Table -->
      <div class="card shadow-sm">
        <div class="card-body">
          <h5 class="card-title">日志列表</h5>
          <table class="table table-striped table-hover">
            <thead class="table-light">
              <tr>
                <th scope="col">日志日期</th>
                
                <th scope="col" class="text-center">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in paginatedLogs" :key="log.date">
                <td>{{ log.date }}</td>
                <td class="text-center">
                  <button class="btn btn-sm btn-info" @click="viewLog(log, 'operation_log')">
                    <i class="bi bi-eye"></i> 查看操作日志
                  </button>
                  <button class="btn btn-sm btn-secondary ms-2" @click="viewLog(log, 'training_log')">
                    <i class="bi bi-eye"></i> 查看训练日志
                  </button>
                  <button class="btn btn-sm btn-success ms-2" @click="viewLog(log, 'battle_log')">
                    <i class="bi bi-eye"></i> 查看对战日志
                  </button>
                  <button class="btn btn-sm btn-danger ms-2" @click="viewLog(log, 'danger_log')">
                    <i class="bi bi-eye"></i> 查看异常预警日志
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
  
          <!-- Pagination Controls -->
          <nav aria-label="Page navigation">
            <ul class="pagination justify-content-center">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <button class="page-link" @click="goToPage(currentPage - 1)">上一页</button>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <button class="page-link" @click="goToPage(currentPage + 1)">下一页</button>
              </li>
            </ul>
          </nav>
        </div>
      </div>
  
      <!-- View Log Modal -->
      <div v-if="isLogModalVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="logModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="logModalLabel">{{ selectedLog.date }} 日志详情</h5>
              <button type="button" class="btn-close" @click="closeLogModal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
              <h6>{{ selectedLog.type }}:</h6>
              <p>{{ selectedLog.logContent }}</p>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeLogModal">关闭</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        logs: [],  // 存储近十天的日志
        searchQuery: '',  // 搜索框查询
        selectedLog: null,  // 当前查看的日志
        currentPage: 1,  // 当前页码
        logsPerPage: 5,  // 每页日志数
        isLogModalVisible: false,  // 是否显示日志详情模态框
      };
    },
    computed: {
      // 根据搜索框内容过滤日志
      filteredLogs() {
        return this.logs.filter(log => 
          log.date.includes(this.searchQuery) || 
          log.operation_log.includes(this.searchQuery) ||
          log.training_log.includes(this.searchQuery) ||
          log.battle_log.includes(this.searchQuery)
        );
      },
      // 分页后的日志列表
      paginatedLogs() {
        const startIndex = (this.currentPage - 1) * this.logsPerPage;
        const endIndex = this.currentPage * this.logsPerPage;
        return this.filteredLogs.slice(startIndex, endIndex);
      },
      // 总页数
      totalPages() {
        return Math.ceil(this.filteredLogs.length / this.logsPerPage);
      },
    },
    mounted() {
      // 页面加载时生成假日志数据
      this.generateFakeLogs();
    },
    methods: {
      // 手动生成假日志数据
      generateFakeLogs() {
        const today = new Date();
        const fakeLogs = [];
  
        for (let i = 0; i < 10; i++) {
          const date = new Date(today);
          date.setDate(today.getDate() - i);  // 日期往回推
          fakeLogs.push({
            date: date.toISOString().split('T')[0],  // 格式化日期
            operation_log: this.generateLongLog(),
            training_log: this.generateLongLog(),
            battle_log: this.generateLongLog(),
            danger_log: "系统错误：无法加载配置文件 \n 内存泄漏：检测到内存使用率异常，超出预定阈值 硬件故障：GPU设备失效，无法分配资源 资源超负荷：网络带宽不足，无法处理高并发请求  数据库崩溃：无法连接到数据库，查询失败  任务执行失败：处理图像数据时发生错误，任务中断  服务崩溃：无法启动训练服务，可能是配置错误  无法连接到服务器：网络请求超时，请检查网络连接  系统崩溃：未捕获异常导致程序崩溃，请查看日志文件  线程死锁：多个线程等待资源，导致死锁状态"
          });
        }
  
        this.logs = fakeLogs;
      },
  
      // 生成一条长日志，包含100多行
      generateLongLog() {
        const logLines = [];
        for (let i = 0; i < 100; i++) {
          logLines.push(this.getRandomSentence());
        }
        return logLines.join("\n");  // 将每行日志拼接成一大段
      },
  
      // 生成一个随机句子
      getRandomSentence() {
        const words = [
          "系统运行正常", "数据更新完毕", "用户操作", "训练完成", "对战结束",
          "日志记录", "操作成功", "资源加载", "性能优化", "策略调整",
          "用户登录", "系统异常", "网络连接", "文件上传", "日志导出", 
          "数据备份", "系统重启", "系统检测", "异常报告", "访问错误", 
          "任务执行", "命令处理", "资源分配", "设备检测", "模型训练",
          "分析结果", "调试信息", "环境检查", "程序优化", "任务完成"
        ];
        return words[Math.floor(Math.random() * words.length)];
      },
  
      // 打开日志详情模态框
      viewLog(log, logType) {
        this.selectedLog = {
          date: log.date,
          type: logType === 'operation_log' ? '操作日志' :
                logType === 'training_log' ? '训练日志' :
                logType === 'danger_log' ? '异常预警日志' : '对战日志',
          logContent: log[logType],
        };
        this.isLogModalVisible = true;
      },
  
      // 关闭日志详情模态框
      closeLogModal() {
        this.isLogModalVisible = false;
        this.selectedLog = null;
      },
  
      // 跳转到指定页码
      goToPage(page) {
        if (page < 1 || page > this.totalPages) return;
        this.currentPage = page;
      },
  
      // 重置搜索框
      resetSearch() {
        this.searchQuery = '';
      },
  
      // 根据搜索框输入过滤日志
      filterLogs() {
        this.currentPage = 1;  // 重置为第一页
      },
    },
  };
  </script>
  
  <style scoped>
  .modal {
    display: block !important;
  }
  
  .list-group-item {
    margin-bottom: 10px;
  }
  
  .btn-group {
    display: flex;
    gap: 10px;
  }
  
  .pagination .page-item .page-link {
    cursor: pointer;
  }
  
  .table th, .table td {
    vertical-align: middle;
  }
  
  .card {
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  }
  
  .btn-sm {
    font-size: 12px;
  }
  
  .modal-header {
    background-color: #f7f7f7;
  }
  
  .modal-footer {
    justify-content: flex-end;
  }
  </style>
  

export default {
  state: {
    socket: null,
    trainingData: true,
    trainingStatus: 'stopped', // 保存训练状态： 'running' 或 'stopped'
    form: {
      trainingName: 'CNNinfer',
      scene: "墨子1",
      model: 'CNN',
      ip: '',
      port: '',
    },
  },
  getters: {
  },
  mutations: {
    updateSocket(state, socket) {
      state.socket = socket;
    },
    updateTrainingData(state, data) {
      state.trainingData = data;
    },
    updateTrainingStatus(state, status) {
      state.trainingStatus = status;
    },
    updateForm(state, formData) {
      console.log("In updateForm change formData : ", formData)
      state.form = { ...state.form, ...formData };
    }
  },
  actions: {
    startTraining({ state, rootState }) {
        const token = rootState.user.token;  // 获取 token
      if (state.socket && state.socket.readyState === WebSocket.OPEN) {
        const payload = {
            event: 'start-training',
            trainingName: state.form.trainingName,
            scene: state.form.scene,
            model: state.form.model,
            pytorchVersion: state.form.pytorchVersion,
            modelParams: state.form.modelParams,
            token: token,
        };
        state.socket.send(JSON.stringify(payload));  // 发送事件和表单数据到后端
      }
    },
    stopTraining({ state }) {
      if (state.socket && state.socket.readyState === WebSocket.OPEN) {
        state.socket.send(JSON.stringify({ event: 'stop-training' }));
      }
    },
    setForm({ commit }, formData) {
      console.log(formData)
      commit('updateForm', formData);
    }
  },
  modules: {
  }
}

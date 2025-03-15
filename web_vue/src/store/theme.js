export default {
  state: {
    useTopNav: true
  },
  getters: {
  },
  mutations: {
    toggleNavLayout(state) {
        state.useTopNav = !state.useTopNav;
    },
  },
  actions: {
  },
  modules: {
  }
}

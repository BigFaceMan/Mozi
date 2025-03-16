export default {
  state: {
    useTopNav: true,
    customBackground: "",
  },
  getters: {
  },
  mutations: {
    toggleNavLayout(state) {
        state.useTopNav = !state.useTopNav;
    },
    setCustomBackground(state, imageUrl) {
        console.log("In setCustomBG change imageUrl : ", imageUrl)
        state.customBackground = imageUrl;
    },
  },
  actions: {
  },
  modules: {
  }
}

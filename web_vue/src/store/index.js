import { createStore } from 'vuex'
import ModuleUser from './user'
import ModulePk from './pk'
import ModuleTrain from './train'
import ThemeSet from './theme'

export default createStore({
  state: {
  },
  getters: {
  },
  mutations: {
  },
  actions: {
  },
  modules: {
    user: ModuleUser,
    pk: ModulePk,
    train: ModuleTrain,
    themeSet: ThemeSet
  }
})

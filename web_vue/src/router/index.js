import { createRouter, createWebHistory } from 'vue-router'
import PkIndexView from '../views/pk/PkIndexView'
import EnvConductIndexView from "../views/envconduct/EnvConductIndexView"
import ModelConductIndexView from "../views/modelconduct/ModelConductIndexView"
import ModelTrainView from "../views/modeltrain/ModelTrainView"
import HelpIndexView from "../views/help/HelpIndexView"
import UserOptIndexView from "../views/user/opt/UserOptIndexView"
import UserInfoindexView from "../views/user/opt/UserInfoindexView"
import NotFound from '../views/error/NotFound'
import UserAccountLoginView from '../views/user/account/UserAccountLoginView'
import UserAccountRegisterView from '../views/user/account/UserAccountRegisterView'
import LogConductView from "../views/logconduct/LogConductView"
import GameNodesView from "../views/gameNodesConduct/GameNodesView"
import DataAnalysisView from "../views/dataAnalysis/DataAnalysisView"
import RmoteLogView from "../views/remoteLog/RmoteLogView"
import store from '../store/index'

const routes = [
  {
    path: "/",
    name: "home",
    // redirect: "/pk/",
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/remoteLog/",
    name: "remoteLog_index",
    component: RmoteLogView,
    meta: {
      requestAuth: false,
    }
  },
  {
    path: "/pk/",
    name: "pk_index",
    component: PkIndexView,
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/dataAnalysis/",
    name: "dataAnalysis_index",
    component: DataAnalysisView,
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/gameNodesView/",
    name: "gameNodes_index",
    component: GameNodesView,
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/logConductView/",
    name: "logConduct_index",
    component: LogConductView,
    meta: {
      requestAuth: true,
    }
  }, 
  {
    path: "/envconduct/",
    name: "envconduct_index",
    component: EnvConductIndexView,
    meta: {
      requestAuth: true,
    }
  }, 
  {
    path: "/modeltrain/",
    name: "modeltrain_index",
    component: ModelTrainView,
    meta: {
      requestAuth: true,
    }
  }, 
  {
    path: "/modelconduct/",
    name: "modelconduct_index",
    component: ModelConductIndexView,
    meta: {
      requestAuth: true,
    }
  }, 
  {
    path: "/help/",
    name: "help_index",
    component: HelpIndexView,
    meta: {
      requestAuth: true,
    }
  }, 
  {
    path: "/user/opt",
    name: "useropt_index",
    component: UserOptIndexView,
    meta: {
      requestAuth: true,
    }
  }, 
  {
    path: "/user/info",
    name: "userinfo_index",
    component: UserInfoindexView,
    meta: {
      requestAuth: true,
    }
  }, 
  {
    path: "/user/account/login/",
    name: "user_account_login",
    component: UserAccountLoginView,
    meta: {
      requestAuth: false,
    }
  },
  {
    path: "/user/account/register/",
    name: "user_account_register",
    component: UserAccountRegisterView,
    meta: {
      requestAuth: false,
    }
  },
  {
    path: "/404/",
    name: "404",
    component: NotFound,
    meta: {
      requestAuth: false,
    }
  },
  {
    path: "/:catchAll(.*)",
    redirect: "/404/"
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.requestAuth && !store.state.user.is_login) {
    // console.log("get here !!!")
    // console.log(store.state.user.is_login)
    // console.log("te route name ", to)
    // console.log("from route name ", from)
    localStorage.setItem("before_route_name", to.name);
    next({name: "user_account_login"});
  } 
  else {
    next()
  }
})
// router.afterEach((to) => {
//   localStorage.setItem('route', to.fullPath)
// })



export default router

import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import axios from 'axios'
import Cookie from '@/utils/cookie'

Vue.config.productionTip = false

Vue.use(ElementUI)

Vue.directive('title', {
  inserted: function (el, binding) {
    document.title = el.dataset.title
  }
})

Vue.prototype.$axios = axios
// defaults 设置全局默认路径
axios.defaults.baseURL = '/'
const Base64 = require('js-base64').Base64

router.beforeEach((to, from, next) => {
  if (to.name === 'Login') {
    next()
  } else {
    const token = Cookie.getCookie('Authorization')
    const userinfo = Cookie.getCookie('userinfo')
    if (!token) {
      router.push({
        name: 'Login'
      })
    } else {
      if (!store.state.Authorization) {
        store.commit('setAuthorization', token)
      }
      if (!store.state.currentuser.username) {
        store.commit('setCurrentuser', JSON.parse(userinfo))
      }
      next()
    }
  }
})
new Vue({
  router,
  store,
  el: '#app',
  Base64,
  render: h => h(App),

  // 安装全局事件总线，$bus就是当前应用的vm
  beforeCreate() {
    Vue.prototype.$bus = this
  }

}).$mount('#app')

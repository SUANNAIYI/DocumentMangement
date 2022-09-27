import Vue from 'vue'
import VueRouter from 'vue-router'
import Content from '../components/Content.vue'
import Preview from '../components/Preview.vue'
import Login from '../views/Login.vue'
import Signup from '../views/Signup.vue'
import Home from '../views/Home.vue'
import OnlyOffice from '../views/OnlyOffice.vue'
import Personal from '../views/Personal.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login
  },
  {
    path: '/home',
    component: Home,
    redirect: '/home/content',
    children: [
      {
        path: 'content',
        name: 'Content',
        component: Content
      },
      {
        path: 'preview',
        name: 'Preview',
        component: Preview
      }
    ]
  },
  {
    path: '/signup',
    component: Signup
  },
  {
    path: '/onlyoffice',
    name: 'OnlyOffice',
    component: OnlyOffice
  },
  {
    path: '/personal',
    name: 'Personal',
    component: Personal
  }

]

const router = new VueRouter({
  routes
})

export default router

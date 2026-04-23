// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/page/HomePage.vue'
import UserLoginPage from '@/page/user/UserLoginPage.vue'
import UserRegisterPage from '@/page/user/UserRegisterPage.vue'
import UserManagePage from '@/page/admin/UserManagePage.vue'
import ACCESS_ENUM from '@/access/accessEnum.ts'
import AppManagePage from '@/page/admin/AppManagePage.vue'
import AppChatPage from '@/page/app/AppChatPage.vue'
import AppEditPage from '@/page/app/AppEditPage.vue'
import ChatManagePage from '@/page/admin/ChatManagePage.vue'

// 单独定义并导出 routes 数组
export const routes = [
  {
    path: '/',
    name: '主页',
    component: HomePage,
  },
  {
    path: '/user/login',
    name: '用户登录',
    component: UserLoginPage,
  },
  {
    path: '/user/register',
    name: '用户注册',
    component: UserRegisterPage,
  },
  {
    path: '/admin/userManage',
    name: '用户管理',
    component: UserManagePage,
    meta:{
      access:ACCESS_ENUM.ADMIN
    }
  },
  {
    path: '/admin/appManage',
    name: '应用管理',
    component: AppManagePage,
    meta:{
      access: ACCESS_ENUM.ADMIN
    }
  },
  {
    path: '/app/chat/:id',
    name: '应用对话',
    component: AppChatPage,
    meta:{
      access:ACCESS_ENUM.USER
    }
  },
  {
    path: '/app/edit/:id',
    name: '编辑应用',
    component: AppEditPage,
    meta: {
      access:ACCESS_ENUM.USER
    }
  },
  {
    path: '/admin/chatManage',
    name: '对话管理',
    component: ChatManagePage,
    meta: {
      access:ACCESS_ENUM.ADMIN
    }
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router

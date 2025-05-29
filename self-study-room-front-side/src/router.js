// 路由配置文件
import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  // 学生端页面路由
  { path: '/login', name: 'StudentLogin', component: () => import('./views/StudentLogin.vue') },
  { path: '/register', name: 'StudentRegister', component: () => import('./views/StudentRegister.vue') },
  { path: '/', name: 'Home', component: () => import('./views/StudentHome.vue') },
  { path: '/room/:roomId', name: 'RoomDetail', component: () => import('./views/RoomDetail.vue') },
  { path: '/bookings', name: 'BookingHistory', component: () => import('./views/BookingHistory.vue') },
  { path: '/recommend', name: 'SeatRecommend', component: () => import('./views/SeatRecommend.vue') },
  // 管理员端页面路由
  { path: '/admin/login', name: 'AdminLogin', component: () => import('./views/AdminLogin.vue') },
  { path: '/admin', name: 'AdminHome', component: () => import('./views/AdminHome.vue') },
  { path: '/admin/rooms/:roomId', name: 'AdminRoomDetail', component: () => import('./views/AdminRoomDetail.vue') },
  { path: '/admin/bookings', name: 'AdminBookingList', component: () => import('./views/AdminBookingList.vue') },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;

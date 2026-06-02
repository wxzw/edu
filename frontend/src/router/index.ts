import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/DashboardView.vue'),
      },
      {
        path: 'campuses',
        name: 'campuses',
        meta: { permission: 'system:campus' },
        component: () => import('@/views/CampusesView.vue'),
      },
      {
        path: 'users',
        name: 'users',
        meta: { permission: 'system:user' },
        component: () => import('@/views/UsersView.vue'),
      },
      {
        path: 'roles',
        name: 'roles',
        meta: { permission: 'system:role' },
        component: () => import('@/views/RolesView.vue'),
      },
      {
        path: 'teachers',
        name: 'teachers',
        meta: { permission: 'edu:teacher' },
        component: () => import('@/views/TeachersView.vue'),
      },
      {
        path: 'courses',
        name: 'courses',
        meta: { permission: 'edu:course' },
        component: () => import('@/views/CoursesView.vue'),
      },
      {
        path: 'classes',
        name: 'classes',
        meta: { permission: 'edu:class' },
        component: () => import('@/views/ClassesView.vue'),
      },
      {
        path: 'students',
        name: 'students',
        meta: { permission: 'edu:student' },
        component: () => import('@/views/StudentsView.vue'),
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  const authStore = useAuthStore();
  if (to.name === 'login') {
    return authStore.isLoggedIn ? { name: 'dashboard' } : true;
  }
  if (!authStore.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } };
  }
  const permission = to.meta.permission as string | undefined;
  if (permission && !authStore.hasPermission(permission)) {
    return { name: 'dashboard' };
  }
  return true;
});

export default router;
